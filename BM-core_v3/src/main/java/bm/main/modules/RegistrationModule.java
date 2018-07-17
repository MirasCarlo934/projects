package bm.main.modules;

import java.util.HashMap;
import java.util.Iterator;

import bm.comms.Protocol;
import bm.context.adaptors.AdaptorManager;
import bm.context.products.Product;
import bm.context.properties.Property;
import bm.jeep.JEEPManager;
import bm.jeep.exceptions.SecondaryMessageCheckingException;
import bm.jeep.vo.JEEPResponse;
import bm.jeep.vo.device.InboundRegistrationRequest;
import bm.main.modules.exceptions.RequestProcessingException;
import org.json.JSONObject;

import bm.comms.mqtt.MQTTPublisher;
import bm.context.adaptors.exceptions.AdaptorException;
import bm.context.devices.Device;
import bm.context.rooms.Room;
import bm.jeep.vo.JEEPRequest;
import bm.jeep.vo.device.ResError;
import bm.main.repositories.DeviceRepository;
import bm.main.repositories.ProductRepository;
import bm.main.repositories.RoomRepository;
import bm.tools.IDGenerator;

public class RegistrationModule extends Module {
	private String nameParam;
	private String roomIDParam;
	private String propsParam;
	private String poopRTY;
//	private String regIdParam;
//	private String regTopicParam;

	private ProductRepository pr;
	private RoomRepository rr;
	private IDGenerator idg;
	private MQTTPublisher mp;
	private AdaptorManager am;
	private JEEPManager jm;
	private HashMap<String, Protocol> protocols;
	
	public RegistrationModule(String logDomain, String errorLogDomain, String RTY, String poopRTY, String nameParam,
							  String roomIDParam, String propsParam, /*String regIdParam, String regTopicParam,*/
							  MQTTPublisher mqttPublisher, DeviceRepository deviceRepository,
							  ProductRepository productRepository, RoomRepository roomRepository,
							  AdaptorManager adaptorManager, JEEPManager jeepManager,
							  Protocol[] protocols, IDGenerator idg) {
		super(logDomain, errorLogDomain, RegistrationModule.class.getSimpleName(), RTY,
				new String[]{nameParam, roomIDParam}, null, deviceRepository);
		this.pr = productRepository;
		this.rr = roomRepository;
		this.nameParam = nameParam;
		this.roomIDParam = roomIDParam;
		this.propsParam = propsParam;
		this.poopRTY = poopRTY;
//		this.regIdParam = regIdParam;
//		this.regTopicParam = regTopicParam;
		this.idg = idg;
		this.mp = mqttPublisher;
		this.am = adaptorManager;
		this.jm = jeepManager;
		this.protocols = new HashMap<String, Protocol>(protocols.length);
		for(Protocol protocol : protocols) {
		    this.protocols.put(protocol.getProtocolName(), protocol);
        }
	}

	/**
	 * Registers component into system.
	 */
	@Override
	protected void processRequest(JEEPRequest request) throws RequestProcessingException {
		InboundRegistrationRequest reg = new InboundRegistrationRequest(request, nameParam, roomIDParam, propsParam);
		if(request.getJSON().has("exists")) {
			if(checkCredentialChanges(reg)) {
				updateDevice(reg);
			} else {
				returnExistingComponent(reg);
			}
			return;
		}
		
		LOG.info("Registering device " + reg.mac + " to Environment...");
		String ssid = idg.generateCID();
		String topic = ssid + "_topic";
		Product product = pr.getProduct(reg.getCID());
		Room parentRoom = rr.getRoom(reg.room);
		Protocol protocol = reg.getProtocol();
		LOG.debug("Creating Device object...");
		Device d = product.createDevice(ssid, reg.mac, reg.name, topic, protocol, parentRoom, true,
				parentRoom.getHighestIndex() + 1);
		d.setAdaptors(am.getAdaptorsLinkedToProduct(product.getSSID()));
		if(reg.properties != null) {
			JSONObject props = reg.properties;
			Iterator<String> propIDs = props.keys();
			while(propIDs.hasNext()) {
				String propID = propIDs.next();
				Property prop = d.getProperty(propID);
				LOG.debug("Setting property " + propID + " (" + prop.getDisplayName() + ")");
				prop.setValue(props.get(propID));
			}
		}
		try {
			d.create(logDomain, true);
			dr.addDevice(d);
			for(Property p : d.getProperties()) {
				p.update(logDomain, true);
			}
		} catch (AdaptorException e) {
//			LOG.error("Device couldn't be added to Environment!", e);
			throw new RequestProcessingException("Device couldn't be added to Environment!", e);
		}

		LOG.info("Sending device credentials to actual device...");
		jm.sendRegistrationResponse(d, request);
		LOG.info("Registration complete!");
//		return true;
	}

	@Override
	protected void processResponse(JEEPResponse response) {
//		return true;
	}

	@Override
	public void processNonResponse(JEEPRequest request) {
		Device d = dr.getDevice(request.getCID());
		LOG.warn("Actual device with MAC " + d.getMAC() + " failed to respond to its registration message to " +
				"the Environment. The device will be removed from Maestro.");
		try {
			d.delete(logDomain, true);
			jm.sendDetachmentResponse(d, true, request);
			dr.removeDevice(d.getSSID());
			LOG.info("Device " + d.getMAC() + " deleted from Maestro records!");
		} catch (AdaptorException e) {
			LOG.error("Device " + d.getMAC() + " failed to be deleted from the " + e.getAdaptorName()
					+ " adaptor!");
		}
	}

	private void returnExistingComponent(InboundRegistrationRequest request) {
		Device d = dr.getDevice(request.mac);
		LOG.info("Device already exists in system as " + d.getSSID() + "! "
				+ "Returning existing credentials and property states.");
		jm.sendRegistrationResponse(d, request);
		for(Property prop : d.getProperties()) {
			prop.sendValueToDevice(logDomain);
		}

		LOG.info("Activating device " + d.getSSID() + "...");
		try {
			d.setActive(true);
			d.update(logDomain, true);
			LOG.info("Device activated!");
		} catch (AdaptorException e) {
			error("Cannot activate device" + d.getSSID() + "!", e, request.getProtocol());
		}
	}
	
	private void updateDevice(InboundRegistrationRequest request) {
		Device c = dr.getDevice(request.mac);
		LOG.info("Updating device " + c.getSSID() + " credentials...");
		LOG.fatal(c.getProperties()[0].getOH_ID());
		try {
			c.setName(request.name);
			c.setRoom(rr.getRoom(request.room));
			c.update(logDomain, true);
			c.setActive(true);
            c.update(logDomain, true);
			LOG.info("Device updated!");
		} catch (AdaptorException e) {
			error("Cannot updateRules device " + c.getSSID() + " credentials!", e, request.getProtocol());
		}
	}
	
	/**
	 * After confirming that the registering component already exists, another check is made to see if the registration 
	 * request contains different credentials from persisted data. Changes signify component credential updateRules. No
	 * changes signify component activation. <br><br>
	 * 
	 * <b><i>NOTE:</b></i> The request parameter <i>properties</i> will not be checked here.
	 * 
	 * @param request The registration request sent by the registering component
	 * @return <b>true</b> if the request contains changes in credentials, <b>false</b> otherwise
	 */
	private boolean checkCredentialChanges(InboundRegistrationRequest request) {
		Device c = dr.getDevice(request.mac);
		String[] reqCreds = new String[]{request.name, request.room};
		String[] comCreds = new String[] {c.getName(), c.getParentRoom().getSSID()};
		
		for(int i = 0; i < reqCreds.length; i++) {
			if(!reqCreds[i].equals(comCreds[i])) {
				return true;
			}
		}
		
		return false;
	}

	/**
	 * Checks for the following deficiencies in the request (done in a step-by-step manner):
	 * <ol>
	 * 	<li>CID already exists</li>
	 * 	<li>Invalid product ID</li>
	 * 	<li>Invalid room ID</li>
	 * 	<li>Invalid set properties block <i>(<b>Optional:</b> a register request does not have to include a set
	 * 		properties block)</i>
	 * 		<ul>
	 * 			<li>B_Property does not exist in the requested component product</li>
	 * 			<li>Invalid property value</li>
	 * 		</ul>
	 * 	</li>
	 * 
	 * </ol>
	 */
	@Override
	protected boolean additionalRequestChecking(JEEPRequest request) throws SecondaryMessageCheckingException {
		LOG.trace("Additional secondary request parameter checking...");
		InboundRegistrationRequest reg = new InboundRegistrationRequest(request, nameParam, roomIDParam, propsParam);
		
		LOG.trace("Checking productID validity...");
		if(!pr.containsProduct(reg.getCID())) {
			throw new SecondaryMessageCheckingException("Request contains invalid product ID! (" + reg.getCID() + ")");
//			ResError error = new ResError(reg, "Request contains invalid product ID! (" + reg.getCID() + ")");
//			error(error);
//			return false;
		}
		LOG.trace("Checking roomID validity...");
		if(!rr.containsRoom(reg.room)) {
			throw new SecondaryMessageCheckingException("Request contains invalid room ID! (" + reg.room + ")");
//			ResError error = new ResError(reg, "Request contains invalid room ID!");
//			error(error);
//			return false;
		}
		
		LOG.trace("Checking set property block validity...");
		Product prod = pr.getProduct(reg.getCID());
		if(reg.properties != null) { //it's okay if the request does not have a set properties block
			JSONObject props = reg.properties;
			Iterator<String> propIDs = props.keys();
			while(propIDs.hasNext()) {
				String propID = propIDs.next();
				if(prod.containsProperty(propID)) {
					if(!prod.getProperty(propID).checkValueValidity(props.get(propID))) {
						throw new SecondaryMessageCheckingException("Invalid property value for PID ("
								+ propID + ")");
//						ResError error = new ResError(request, "Invalid property value for PID " +
//								propID);
//						error(error);
//						return false;
					}
				} else {
					throw new SecondaryMessageCheckingException("Invalid PID (" + propID + ")");
//					ResError error = new ResError(request, "Invalid PID " + propID);
//					error(error);
//					return false;
				}
			}
		}
		
		LOG.trace("Checking MAC validity...");
		if(dr.containsDevice(reg.mac)) {
			LOG.warn("Request contains a preexisting MAC address!");
			request.getJSON().put("exists", true);
			return true;
		}
		
		return true;
	}

	@Override
	protected boolean additionalResponseChecking(JEEPResponse response) {
		return true;
	}
}
