package bm.main.modules;

import java.util.Iterator;

import bm.context.adaptors.AdaptorManager;
import bm.context.products.Product;
import bm.context.properties.Property;
import org.json.JSONObject;

import bm.comms.mqtt.MQTTPublisher;
import bm.context.adaptors.exceptions.AdaptorException;
import bm.context.devices.Device;
import bm.context.rooms.Room;
import bm.jeep.JEEPRequest;
import bm.jeep.device.ReqRegister;
import bm.jeep.device.ResError;
import bm.jeep.device.ResRegister;
import bm.main.repositories.DeviceRepository;
import bm.main.repositories.ProductRepository;
import bm.main.repositories.RoomRepository;
import bm.tools.IDGenerator;

public class RegistrationModule extends SimpleModule {
	private String nameParam;
	private String roomIDParam;
	private String propsParam;
	private String poopRTY;
	private ProductRepository pr;
	private RoomRepository rr;
	private IDGenerator idg;
	private MQTTPublisher mp;
	private AdaptorManager am;
	
	public RegistrationModule(String logDomain, String errorLogDomain, String RTY, String poopRTY, String nameParam,
							  String roomIDParam, String propsParam, MQTTPublisher mqttPublisher,
							  DeviceRepository deviceRepository, ProductRepository productRepository,
							  RoomRepository roomRepository, AdaptorManager adaptorManager, IDGenerator idg) {
		super(logDomain, errorLogDomain, "RegistrationModule", RTY, new String[]{nameParam, roomIDParam}, /*mp, */deviceRepository);
		this.pr = productRepository;
		this.rr = roomRepository;
		this.nameParam = nameParam;
		this.roomIDParam = roomIDParam;
		this.propsParam = propsParam;
		this.poopRTY = poopRTY;
		this.idg = idg;
		this.mp = mqttPublisher;
		this.am = adaptorManager;
	}
	
	public RegistrationModule(String logDomain, String errorLogDomain, String RTY, 
			AbstModuleExtension[] extensions,String poopRTY, String nameParam, String roomIDParam, 
			String propsParam, MQTTPublisher mqttPublisher, DeviceRepository components, 
			ProductRepository pr, RoomRepository rr, IDGenerator idg) {
		super(logDomain, errorLogDomain, "RegistrationModule", RTY, new String[]{nameParam, roomIDParam}, 
				/*mp, */components, extensions);
		this.pr = pr;
		this.rr = rr;
		this.nameParam = nameParam;
		this.roomIDParam = roomIDParam;
		this.propsParam = propsParam;
		this.poopRTY = poopRTY;
		this.idg = idg;
		this.mp = mqttPublisher;
	}

	/**
	 * Registers component into system.
	 */
	@Override
	protected boolean process(JEEPRequest request) {
		ReqRegister reg = new ReqRegister(request, nameParam, roomIDParam, propsParam);
		if(request.getJSON().has("exists")) {
			if(checkCredentialChanges(reg)) {
				updateDevice(reg);
			} else {
				returnExistingComponent(reg);
			}
			return true;
		}
		
		mainLOG.info("Registering device " + reg.mac + " to system...");
		mainLOG.debug("Creating device ID...");
		String[] existingIDs = new String[dr.getAllDevices().length];
		for(int i = 0; i < existingIDs.length; i++) {
			existingIDs[i] = dr.getAllDevices()[i].getSSID();
		}
		String ssid = idg.generateCID(existingIDs);
		mainLOG.debug("Creating Device object...");
		String topic = ssid + "_topic";
		Product product = pr.getProduct(reg.getCID());
		Room parentRoom = rr.getRoom(reg.room);
		Device d = product.createDevice(ssid, reg.mac, reg.name, topic, parentRoom, true, 
				parentRoom.getHighestIndex() + 1);
		d.setAdaptors(am.getAdaptorsLinkedToProduct(product.getSSID()));
		if(reg.properties != null) {
			JSONObject props = reg.properties;
			Iterator<String> propIDs = props.keys();
			while(propIDs.hasNext()) {
				String propID = propIDs.next();
				Property prop = d.getProperty(propID);
				mainLOG.debug("Setting property " + propID + " (" + prop.getDisplayName() + ")");
				prop.setValue(props.get(propID));
			}
		} 
		dr.addDevice(d);
		mainLOG.info("Device " + d.getSSID() + " created!");
		
		//persisting device to peripheral systems
		try {
			d.create(logDomain, true);
		} catch (AdaptorException e) {
			error("Error in persisting device to DB! This device may not exist after "
					+ "the BM restarts!", e, request.getSender());
			return false;
		}
		
		//publishing of Device credentials to default_topic
		mainLOG.debug("Publishing Component credentials to default topic...");
		ResRegister response = new ResRegister(request, d.getSSID(), d.getTopic());
		mp.publishToDefaultTopic(response);
		d.publishCredentials(request.getSender(), requestType, logDomain);
		mainLOG.info("Registration complete!");
		return true;
	}
	
	private void returnExistingComponent(ReqRegister request) {
//		ReqRegister reg = new ReqRegister(request.getJSON(), nameParam, roomIDParam, propsParam);
		Device c = dr.getDevice(request.mac);
		mainLOG.info("Component already exists in system as " + c.getSSID() + "! "
				+ "Returning existing credentials and property states.");
		ResRegister response = new ResRegister(request, c.getSSID(), c.getTopic());
		mp.publishToDefaultTopic(response);
		c.publishCredentials(request.getSender(), requestType, logDomain);
		c.publishPropertyValues(request.getSender(), poopRTY, logDomain);
		
		mainLOG.info("Activating component " + c.getSSID() + "...");
		try {
			c.setActive(true, true);
			mainLOG.info("Component activated!");
		} catch (AdaptorException e) {
			error("Cannot activate component" + c.getSSID() + "!", e, request.getSender());
		}
	}
	
	private void updateDevice(ReqRegister request) {
		Device c = dr.getDevice(request.mac);
		mainLOG.info("Updating device " + c.getSSID() + " credentials...");
		mainLOG.fatal(c.getProperties()[0].getOH_ID());
		try {
			c.setName(request.name);
			c.setRoom(rr.getRoom(request.room));
			c.update(logDomain, true);
			c.setActive(true, true);
			mainLOG.info("Device updated!");
		} catch (AdaptorException e) {
			error("Cannot updateRules device " + c.getSSID() + " credentials!", e, request.getSender());
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
	private boolean checkCredentialChanges(ReqRegister request) {
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
	protected boolean additionalRequestChecking(JEEPRequest request) {
		mainLOG.trace("Additional secondary request parameter checking...");
		ReqRegister reg = new ReqRegister(request, nameParam, roomIDParam, propsParam);
		
		mainLOG.trace("Checking productID validity...");
		if(!pr.containsProduct(reg.getCID())) {
			ResError error = new ResError(reg, "Request contains invalid product ID! (" + reg.getCID() + ")");
			error(error);
			return false;
		}
		mainLOG.trace("Checking roomID validity...");
		if(!rr.containsRoom(reg.room)) {
			ResError error = new ResError(reg, "Request contains invalid room ID!");
			error(error);
			return false;
		}
		
		mainLOG.trace("Checking set property block validity...");
		Product prod = pr.getProduct(reg.getCID());
		if(reg.properties != null) { //it's okay if the request does not have a set properties block
			JSONObject props = reg.properties;
			Iterator<String> propIDs = props.keys();
			while(propIDs.hasNext()) {
				String propID = propIDs.next();
				if(prod.containsProperty(propID)) {
					if(!prod.getProperty(propID).checkValueValidity(props.get(propID))) {
						ResError error = new ResError(request, "Invalid property value for PID " + 
								propID);
						error(error);
						return false;
					}
				} else {
					ResError error = new ResError(request, "Invalid PID " + propID);
					error(error);
					return false;
				}
			}
		}
		
		mainLOG.trace("Checking MAC validity...");
		if(dr.containsDevice(reg.mac)) {
			mainLOG.warn("Request contains a preexisting MAC address!");
			request.getJSON().put("exists", true);
			return true;
		}
		
		return true;
	}
}
