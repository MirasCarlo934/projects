package bm.context.devices;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.xml.ws.http.HTTPException;

import org.apache.log4j.Logger;
import org.json.JSONObject;

import bm.comms.Sender;
import bm.comms.mqtt.MQTTListener;
import bm.comms.mqtt.MQTTPublisher;
import bm.context.HTMLTransformable;
import bm.context.OHItemmable;
import bm.context.SymphonyElement;
import bm.context.SymphonyObject;
import bm.context.adaptors.AbstAdaptor;
import bm.context.adaptors.DBAdaptor;
import bm.context.adaptors.OHAdaptor;
import bm.context.adaptors.exceptions.AdaptorException;
import bm.context.devices.products.AbstProduct;
import bm.context.devices.products.Product;
import bm.context.properties.AbstProperty;
import bm.context.properties.Property;
import bm.context.properties.bindings.Binding;
import bm.context.rooms.Room;
import bm.jeep.device.ResError;
import bm.jeep.device.ResPOOP;
import bm.jeep.device.ResRegister;
import bm.main.engines.DBEngine;
import bm.main.engines.exceptions.EngineException;
import bm.main.engines.requests.DBEngine.UpdateDBEReq;
import bm.tools.IDGenerator;

/**
 * The Device object 
 * 
 * @author Carlo Miras
 *
 */
public class Device extends SymphonyObject implements OHItemmable, HTMLTransformable {
	protected String loggerName;
	protected HashMap<String, AbstProperty> properties = new HashMap<String, AbstProperty>(1);
	protected AbstProduct product;
	protected String MAC;
	protected String name;
	protected String topic;
	protected boolean active;
	protected IDGenerator idg = new IDGenerator();
	
	public Device(String SSID, String MAC, String name, String topic, Room room, 
			boolean active, AbstProduct product, DBAdaptor dba, OHAdaptor oha, AbstAdaptor[] adaptors, 
			int index) throws AdaptorException {
		super(SSID, dba, oha, adaptors, room, index);
		this.loggerName = "DEV:" + SSID;
		this.setMAC((MAC));
		this.setName((name));
		this.setTopic((topic));
		this.setProperties(product.getProperties());
		this.setProduct((product));
		setActive(active);
		
		//sets this device as the owner of the properties given to it
		Iterator<AbstProperty> props = properties.values().iterator();
		while(props.hasNext()) {
			AbstProperty prop = props.next();
			prop.setDevice(this);
		}
		
		//adds this device to its room
//		room.addSmarthomeObject(this);
	}
	
	/**
	 * Persists this component and its properties to the DB, OH, and all the various peripheral systems plugged in
	 * to this specific component. 
	 */
	@Override
	protected void create(AbstAdaptor adaptor, String parentLogDomain, boolean waitUntilCreated) 
			throws AdaptorException {
		final Logger LOG = getLogger(parentLogDomain);
		LOG.debug("Creating device " + SSID + " in " + adaptor.getServiceName() + "...");
		adaptor.deviceCreated(this, waitUntilCreated);
		
		LOG.debug("Creating properties of device " + SSID + " in " + adaptor.getServiceName() + "...");
		Iterator<AbstProperty> props = properties.values().iterator();
		while(props.hasNext()) {
			AbstProperty prop = props.next();
			adaptor.propertyCreated(prop, waitUntilCreated);
			LOG.debug("Property " + prop.getStandardID() + " of device " + SSID + " created!");
		}
		LOG.debug("Device " + SSID + " created!");
	}
	
	/**
	 * Deletes this component and its properties from the DB, OH, and all the various peripheral systems plugged in
	 * to this specific component. 
	 */
	@Override
	protected void delete(AbstAdaptor adaptor, String parentLogDomain, boolean waitUntilDeleted) 
			throws AdaptorException {
		Logger LOG = getLogger(parentLogDomain);
		LOG.debug("Deleting device " + SSID + " from " + adaptor.getServiceName() + "...");
		adaptor.deviceDeleted(this, waitUntilDeleted);
		
		Iterator<AbstProperty> props = properties.values().iterator();
		LOG.debug("Deleting properties of device " + SSID + " from " + adaptor.getServiceName() + "...");
		while(props.hasNext()) {
			AbstProperty prop = props.next();
			adaptor.propertyDeleted(prop, waitUntilDeleted);
			LOG.debug("Property " + prop.getStandardID() + " of device " + SSID + " deleted!");
		}
		parentRoom.removeSmarthomeObject(this);
		LOG.debug("Device " + SSID + " deleted!");
	}
	
	/**
	 * Updates the component in all of the plugged adaptors. <br><br>
	 * 
	 * <b><i>NOTE:</i></b> Set the credentials in this component object <b>FIRST</b> before calling this method.
	 */
	@Override
	protected void update(AbstAdaptor adaptor, String parentLogDomain, boolean waitUntilUpdated) 
			throws AdaptorException {
		Logger LOG = getLogger(parentLogDomain);
		LOG.debug("Updating device " + SSID + "...");
		adaptor.deviceCredentialsUpdated(this, waitUntilUpdated);
		LOG.debug("Device " + SSID + " updated!");
	}
	
//	/**
//	 * Updates the component in all of the plugged adaptors. <br><br>
//	 * 
//	 * <b><i>NOTE:</i></b> Set the credentials in this component object <b>FIRST</b> before calling this method.
//	 */
//	@Override
//	public void update(String parentLogDomain, boolean waitUntilUpdated) throws AdaptorException {
//		Logger LOG = getLogger(parentLogDomain);
//		LOG.debug("Updating component " + SSID + "...");
//		for(int i = 0; i < adaptors.length; i++) {
//			AbstAdaptor a = adaptors[i];
//			a.deviceCredentialsUpdated(this, waitUntilUpdated);
//		}
//		LOG.debug("Component " + SSID + " updated!");
//	}
	
//	@Override
//	public void update(AbstAdaptor[] exceptions, String parentLogDomain, boolean waitUntilUpdated) 
//			throws AdaptorException {
//		//LATER Component: Create update function
//		Logger LOG = getLogger(parentLogDomain);
//		LOG.debug("Updating component " + SSID + "...");
//		for(int i = 0; i < adaptors.length; i++) {
//			AbstAdaptor a = adaptors[i];
//			if(!a.equals(exceptions))
//				a.deviceCredentialsUpdated(this, waitUntilUpdated);
//		}
//		LOG.debug("Component " + SSID + " updated!");
//	}
	
//	@Override
//	public void updateExcept(Class[] exceptions, String parentLogDomain, boolean waitUntilUpdated) 
//			throws AdaptorException {
//		Logger LOG = getLogger(parentLogDomain);
//		LOG.debug("Updating component " + SSID + "...");
//		List<Class> excepts = Arrays.asList(exceptions);
//		for(int i = 0; i < adaptors.length; i++) {
//			AbstAdaptor a = adaptors[i];
//			if(!excepts.contains(a.getClass()))
//				a.deviceCredentialsUpdated(this, waitUntilUpdated);
//		}
//		LOG.debug("Component " + SSID + " updated!");
//	}
	
	/**
	 * Publishes the credentials of this Component object to the default topic. Creates a new
	 * ResRegister object that contains the credentials of this Component.
	 * 
	 * @param mh The MQTTHandler that handles the publishing
	 * @param registerRTY The RTY designation for registration requests
	 * @param parentLogDomain The log domain of the object that called this method
	 */
	public void publishCredentials(Sender sender, String registerRTY, String parentLogDomain) {
		Logger LOG = getLogger(parentLogDomain);
		LOG.debug("Sending credentials to default_topic...");
		ResRegister response = new ResRegister(getMAC(), getSSID(), registerRTY, sender, getSSID(), getTopic());
//		sender.send(response);
//		if(sender.getClass().equals(MQTTPublisher.class)) {
//			MQTTPublisher mp = (MQTTPublisher) sender;
//			mp.publishToDefaultTopic(response);
//		} else {
//			sender.sendJEEPMessage(response);
//		}
	}
	
	/**
	 * Publishes the values of this component's properties to the MQTT server. The values are returned in the form 
	 * of a POOP response JSON.
	 * 
	 * @param mh The MQTTHandler that handles the publishing
	 * @param poopRTY The RTY string for the POOP request
	 * @param parentLogDomain The log domain of the object that called this method
	 */
	public void publishPropertyValues(Sender sender, String poopRTY, String parentLogDomain) {
		Logger LOG = getLogger(parentLogDomain);
		LOG.debug("Sending property states of " + SSID + " to " + topic + "...");
		Iterator<AbstProperty> props = properties.values().iterator();
		while(props.hasNext()) {
			AbstProperty prop = props.next();
			LOG.trace(prop.getStandardID() + " = " + prop.getValue());
			ResPOOP poop = new ResPOOP("POOP", SSID, poopRTY, sender, prop.getSSID(), prop.getValue());
//			sender.send(poop);
		}
	}
	
	/**
	 * Converts a component into JSON format. Also includes the component's properties in the 
	 * conversion
	 * 
	 * @return An array of <i>JSONObjects</i> containing the <i>AbstComponent's</i> JSON 
	 * 		representation
	 */
	@Override
	public JSONObject[] convertToItemsJSON() {
		if(getProperties().length > 1) { //creates a group item if component has >1 properties
			JSONObject json = new JSONObject();
			json.put("type", "Group");
			json.put("name", getSSID());
			json.put("label", getName());
			json.put("category", product.getOHIcon());
			if(parentRoom != null)
				json.put("groupNames", new String[]{getRoom().getSSID()});
			return new JSONObject[]{json};
		} else { //lets the single property define the component in registry
			return new JSONObject[0];
		}
	}
	
	@Override
	public String convertToSitemapString() {
		String itemType;
		if(getProperties().length > 1) {
			itemType = "Group";
		} else {
			AbstProperty p = getProperties()[0];
			itemType = p.getOHItemType();
		}
		return itemType + " item=" + SSID + " [label=\"" + name + "\"] [icon=\"" + product.getOHIcon() + "\"]";
	}
	
	/**
	 * Converts this component and its properties into a Javascript variable.
	 * 
	 * @return the component in Javascript variable form.
	 */
	/*
	 * Ex. var d_SMVX = new Device("SMVX","Motion","CRL0",[{id:"0006",label:"Detected",io:"I"}]);
	 */
	@Override
	public String convertToJavascript() {
		String s = "var d_" + SSID + " = new Device(\"" + SSID + "\", \"" + product.getName() + "\", \"" + name + "\", ";
		if(parentRoom != null)
			s += "\"" + parentRoom.getSSID() + "\", ";
		s += "[";
		Iterator<AbstProperty> props = properties.values().iterator();
		
		while(props.hasNext()) { //convert each property to format ex. {id:"0006",label:"Detected",io:"I"}
			AbstProperty prop = props.next();
			if(!(prop.getPropType().getSSID().equals("INN"))) {
				s += "{id:\"" + prop.getSSID() + "\","
						+ "label:\"" + prop.getDisplayName() + "\","
						+ "io:\"" + prop.getMode().toString() + "\",";
				if(!prop.getPropType().getSSID().equals("STR")) {
					s += "min:" + prop.getPropType().getMin() + ","
							+ "max:" + prop.getPropType().getMax() + ",";
				}
				s = s.substring(0, s.length() - 1) + "},"; //to chomp the last comma and add closing curly bracket
			}
		}
		s = s.substring(0, s.length() - 1) + "]);"; //to chomp the last comma and add closing var characters
		return s;
	}
	
	/**
	 * Checks if the property with the specified PID exists in this component
	 * 
	 * @param pid The property ID
	 * @return <b>true</b> if the property exists, <b>false</b> otherwise
	 */
	public boolean containsProperty(String pid) {
		return properties.containsKey(pid);
	}
	
	/**
	 * Returns the property object with the SSID specified
	 * 
	 * @param ssid The SSID of the property
	 * @return The property, <b><i>null</i></b> if the property does not exist
	 */
	public AbstProperty getProperty(String ssid) {
		return properties.get(ssid);
	}
	
	/**
	 * Returns the properties that have the specified binding
	 * 
	 * @param binding The binding to be checked
	 * @return An array of properties with the specified binding
	 */
	public AbstProperty[] getPropertiesWithBinding(Binding binding) {
		Vector<AbstProperty> props = new Vector<AbstProperty>(1,1);
		Iterator<AbstProperty> allProps = properties.values().iterator();
		while(allProps.hasNext()) {
			AbstProperty prop = allProps.next();
			Binding b = prop.getBinding();
			if(b.getService().equals(binding.getService()) && b.getFunction().equals(
					binding.getFunction())) {
				props.add(prop);
			}
		}
		return props.toArray(new AbstProperty[props.size()]);
	}

	/**
	 * @return the active
	 */
	public boolean isActive() {
		return active;
	}

	/**
	 * Sets the active property of this Component.
	 * 
	 * @param active <b>true</b> if the Component is active, <b>false</b> if not
	 */
	public void setActive(boolean active) {
		this.active = active;
	}
	
	/**
	 * Sets the active state of this Component and persists it to the DB.
	 * 
	 * @param active <b>true</b> if the Component is active, <b>false</b> if not
	 * @param parentLogDomain
	 * @param waitUntilActivated
	 * @throws EngineException thrown when persistence fails
	 * @throws InterruptedException thrown when Thread.wait() fails
	 */
	public void setActive(boolean active, /*String parentLogDomain, */boolean waitUntilActivated) throws AdaptorException {
//		Logger LOG = getLogger(parentLogDomain);
//		LOG.debug("Setting active state to " + active);
		this.active = active;
		for(int i = 0; i < adaptors.length; i++) {
			adaptors[i].deviceStateUpdated(this, waitUntilActivated);
		}
	}
	
	@Override
	public void setIndex(int index) throws AdaptorException {
		for(int i = 0; i < adaptors.length; i++) {
			adaptors[i].deviceCredentialsUpdated(this, true);
		}
	}

	public AbstProduct getProduct() {
		return product;
	}

	public void setProduct(AbstProduct product) {
		this.product = product;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getMAC() {
		return MAC;
	}

	public void setMAC(String mAC) {
		MAC = mAC;
	}

	public String getTopic() {
		return topic;
	}

	public void setTopic(String topic) {
		this.topic = topic;
	}

	public AbstProperty[] getProperties() {
		return properties.values().toArray(new AbstProperty[properties.size()]);
	}

	public void setProperties(HashMap<String, AbstProperty> properties) {
		this.properties = properties;
	}
	
	private Logger getLogger(String parentLogDomain) {
		return Logger.getLogger(parentLogDomain + "." + SSID);
	}
}
