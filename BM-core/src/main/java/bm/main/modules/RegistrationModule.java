package bm.main.modules;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import javax.xml.ws.http.HTTPException;

import org.json.JSONObject;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import bm.jeep.ReqRegister;
import bm.jeep.ReqRequest;
import bm.jeep.ResError;
import bm.jeep.ResPOOP;
import bm.jeep.ResRegister;
import bm.main.Maestro;
import bm.main.ConfigLoader;
import bm.main.engines.DBEngine;
import bm.main.engines.exceptions.EngineException;
import bm.main.engines.requests.DBEngine.InsertDBEReq;
import bm.main.engines.requests.DBEngine.RawDBEReq;
import bm.main.engines.requests.DBEngine.SelectDBEReq;
import bm.main.engines.requests.DBEngine.UpdateDBEReq;
import bm.main.repositories.DeviceRepository;
import bm.main.repositories.ProductRepository;
import bm.main.repositories.RoomRepository;
import bm.mqtt.MQTTListener;
import bm.mqtt.MQTTPublisher;
import bm.smarthome.adaptors.exceptions.AdaptorException;
import bm.smarthome.devices.Device;
import bm.smarthome.devices.factories.AbstDeviceFactory;
import bm.smarthome.devices.products.AbstProduct;
import bm.smarthome.devices.products.Product;
import bm.smarthome.properties.AbstProperty;
import bm.smarthome.properties.Property;
import bm.smarthome.rooms.Room;
import bm.tools.IDGenerator;

public class RegistrationModule extends AbstModule {
	private String nameParam;
	private String roomIDParam;
	private String propsParam;
	private String poopRTY;
	private ProductRepository pr;
	private RoomRepository rr;
	private IDGenerator idg;
	
	public RegistrationModule(String logDomain, String errorLogDomain, String RTY, String poopRTY, 
			String nameParam, String roomIDParam, String propsParam, MQTTPublisher mp, DeviceRepository components,
			ProductRepository pr, RoomRepository rr, IDGenerator idg) {
		super(logDomain, errorLogDomain, "RegistrationModule", RTY, new String[]{nameParam, roomIDParam}, mp, components);
		this.pr = pr;
		this.rr = rr;
		this.nameParam = nameParam;
		this.roomIDParam = roomIDParam;
		this.propsParam = propsParam;
		this.poopRTY = poopRTY;
		this.idg = idg;
	}

	/**
	 * Registers component into system.
	 */
	@Override
	protected void process(ReqRequest request) {
		ReqRegister reg = new ReqRegister(request.getJSON(), nameParam, roomIDParam, propsParam);
		if(request.getJSON().has("exists")) {
			if(checkCredentialChanges(reg)) {
				updateComponent(reg);
			} else {
				returnExistingComponent(reg);
			}
			return;
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
		AbstProduct product = pr.getProduct(reg.cid);
		Device c = product.createDevice(ssid, reg.mac, reg.name, topic, rr.getRoom(reg.room), true);
		if(reg.properties != null) {
			JSONObject props = reg.properties;
			Iterator<String> propIDs = props.keys();
			while(propIDs.hasNext()) {
				String propID = propIDs.next();
				AbstProperty prop = c.getProperty(propID);
				mainLOG.debug("Setting property " + propID + " (" + prop.getDisplayName() + ")");
				prop.setValue(props.get(propID));
			}
		} 
		dr.addDevice(c);
		mainLOG.info("Device " + c.getSSID() + " created!");
		
		//persisting device to peripheral systems
		try {
			c.persist(logDomain, true);
		} catch (AdaptorException e) {
			error("Error in persisting device to DB! This device may not exist after "
					+ "the BM restarts!", e);
			return;
		}
		
		//publishing of Component credentials to default topic
		mainLOG.debug("Publishing Component credentials to default topic...");
		c.publishCredentials(mp, requestType, logDomain);
		mainLOG.info("Registration complete!");
	}
	
	private void returnExistingComponent(ReqRegister request) {
//		ReqRegister reg = new ReqRegister(request.getJSON(), nameParam, roomIDParam, propsParam);
		Device c = dr.getDevice(request.mac);
		mainLOG.info("Component already exists in system as " + c.getSSID() + "! "
				+ "Returning existing credentials and property states.");
		c.publishCredentials(mp, requestType, logDomain);
		c.publishPropertyValues(mp, poopRTY, logDomain);
		
		mainLOG.info("Activating component " + c.getSSID() + "...");
		try {
			c.setActive(true, logDomain, true);
			mainLOG.info("Component activated!");
		} catch (AdaptorException e) {
			error("Cannot activate component" + c.getSSID() + "!", e);
		}
	}
	
	private void updateComponent(ReqRegister request) {
		Device c = dr.getDevice(request.mac);
		mainLOG.info("Updating component " + c.getSSID() + " credentials...");
		mainLOG.fatal(c.getProperties()[0].getStandardID());
		try {
			c.setName(request.name);
			c.setRoom(rr.getRoom(request.room));
			c.update(logDomain, true);
			c.setActive(true, logDomain, true);
			mainLOG.info("Component updated!");
		} catch (AdaptorException e) {
			error("Cannot update component " + c.getSSID() + " credentials!", e);
		}
	}
	
	/**
	 * After confirming that the registering component already exists, another check is made to see if the registration 
	 * request contains different credentials from persisted data. Changes signify component credential update. No 
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
		String[] comCreds = new String[] {c.getName(), c.getRoom().getSSID()};
		
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
	 * 			<li>Property does not exist in the requested component product</li>
	 * 			<li>Invalid property value</li>
	 * 		</ul>
	 * 	</li>
	 * 
	 * </ol>
	 */
	@Override
	protected boolean additionalRequestChecking(ReqRequest request) {
		mainLOG.trace("Additional secondary request parameter checking...");
		ReqRegister reg = new ReqRegister(request.getJSON(), nameParam, roomIDParam, propsParam);
		
		mainLOG.trace("Checking productID validity...");
		if(!pr.containsProduct(reg.cid)) {
			ResError error = new ResError(reg, "Request contains invalid product ID! (" + reg.cid + ")");
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
		AbstProduct prod = pr.getProduct(reg.cid);
		if(reg.properties != null) { //it's okay if the request does not have a set properties block
			JSONObject props = reg.properties;
			Iterator<String> propIDs = props.keys();
			while(propIDs.hasNext()) {
				String propID = propIDs.next();
				if(prod.containsProperty(propID)) {
					if(!prod.getProperty(propID).checkValueTypeValidity(props.get(propID))) {
						ResError error = new ResError(request, "Invalid property value for PID " + 
								propID);
						error(error);
						mp.publish(error);
						return false;
					}
				} else {
					ResError error = new ResError(request, "Invalid PID " + propID);
					error(error);
					mp.publish(error);
					return false;
				}
			}
		}
		
		mainLOG.trace("Checking MAC validity...");
		if(dr.containsComponent(reg.mac)) {
			mainLOG.warn("Request contains a preexisting MAC address!");
			request.getJSON().put("exists", true);
			return true;
		}
		
		return true;
	}
}
