package bm.smarthome.devices;

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

import bm.jeep.ResError;
import bm.jeep.ResPOOP;
import bm.jeep.ResRegister;
import bm.main.engines.DBEngine;
import bm.main.engines.exceptions.EngineException;
import bm.main.engines.requests.DBEngine.UpdateDBEReq;
import bm.mqtt.MQTTListener;
import bm.mqtt.MQTTPublisher;
import bm.smarthome.adaptors.AbstAdaptor;
import bm.smarthome.adaptors.DBAdaptor;
import bm.smarthome.adaptors.OHAdaptor;
import bm.smarthome.adaptors.exceptions.AdaptorException;
import bm.smarthome.devices.products.AbstProduct;
import bm.smarthome.devices.products.Product;
import bm.smarthome.interfaces.HTMLTransformable;
import bm.smarthome.interfaces.OHItemmable;
import bm.smarthome.interfaces.SmarthomeElement;
import bm.smarthome.interfaces.SmarthomeObject;
import bm.smarthome.properties.AbstProperty;
import bm.smarthome.properties.Property;
import bm.smarthome.properties.bindings.Binding;
import bm.smarthome.rooms.Room;
import bm.tools.IDGenerator;

/**
 * The Component object 
 * 
 * @author Carlo Miras
 *
 */
public class Device extends SmarthomeObject implements OHItemmable, HTMLTransformable {
	protected String loggerName;
	protected HashMap<String, AbstProperty> properties = new HashMap<String, AbstProperty>(1);
	protected AbstProduct product;
	protected String MAC;
	protected String name;
	protected String topic;
	protected boolean active;
	protected IDGenerator idg = new IDGenerator();
	
	public Device(String SSID, String MAC, String name, String topic, Room room, 
			boolean active, AbstProduct product, DBAdaptor dba, OHAdaptor oha, AbstAdaptor[] adaptors) {
		super(SSID, dba, oha, adaptors, room);
		this.loggerName = "COM:" + SSID;
		this.setMAC((MAC));
		this.setName((name));
		this.setTopic((SSID + "_topic"));
		this.setProperties(product.getProperties());
		this.setProduct((product));
		setActive(active);
		
		//sets the properties to be owned by this component
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
	public void persist(String parentLogDomain, boolean waitUntilPersisted) throws AdaptorException {
		final Logger LOG = getLogger(parentLogDomain);
		LOG.debug("Persisting component " + SSID + "...");
		for(int i = 0; i < adaptors.length; i++) {
			AbstAdaptor a = adaptors[i];
			a.persistDevice(this, waitUntilPersisted);
		}
		Iterator<AbstProperty> props = properties.values().iterator();
		while(props.hasNext()) {
			props.next().persist(parentLogDomain, waitUntilPersisted);
		}
		LOG.debug("Component " + SSID + " persisted!");
	}
	
	public void persist(String parentLogDomain, AbstAdaptor[] exceptions, boolean waitUntilPersisted) 
			throws AdaptorException {
		//TODO add persist-with-exception method
	}
	
	/**
	 * Deletes this component and its properties from the DB, OH, and all the various peripheral systems plugged in
	 * to this specific component. 
	 */
	@Override
	public void delete(String parentLogDomain, boolean waitUntilDeleted) throws AdaptorException {
		Logger LOG = getLogger(parentLogDomain);
		LOG.debug("Deleting component " + SSID + "...");
		for(int i = 0; i < adaptors.length; i++) {
			AbstAdaptor a = adaptors[i];
			a.deleteDevice(this, waitUntilDeleted);
		}
		Iterator<AbstProperty> props = properties.values().iterator();
		while(props.hasNext()) {
			props.next().delete(parentLogDomain, waitUntilDeleted);
		}
		room.removeSmarthomeObject(this);
		LOG.debug("Component " + SSID + " deleted!");
	}
	
	/**
	 * Updates the component in all of the plugged adaptors. <br><br>
	 * 
	 * <b><i>NOTE:</i></b> Set the credentials in this component object <b>FIRST</b> before calling this method.
	 */
	@Override
	public void update(String parentLogDomain, boolean waitUntilUpdated) throws AdaptorException {
		Logger LOG = getLogger(parentLogDomain);
		LOG.debug("Updating component " + SSID + "...");
		for(int i = 0; i < adaptors.length; i++) {
			AbstAdaptor a = adaptors[i];
			a.updateDevice(this, waitUntilUpdated);
		}
		LOG.debug("Component " + SSID + " updated!");
	}
	
	@Override
	public void update(AbstAdaptor[] exceptions, String parentLogDomain, boolean waitUntilUpdated) 
			throws AdaptorException {
		//LATER Component: Create update function
		Logger LOG = getLogger(parentLogDomain);
		LOG.debug("Updating component " + SSID + "...");
		for(int i = 0; i < adaptors.length; i++) {
			AbstAdaptor a = adaptors[i];
			if(!a.equals(exceptions))
				a.updateDevice(this, waitUntilUpdated);
		}
		LOG.debug("Component " + SSID + " updated!");
	}
	
	@Override
	public void updateExcept(Class[] exceptions, String parentLogDomain, boolean waitUntilUpdated) 
			throws AdaptorException {
		Logger LOG = getLogger(parentLogDomain);
		LOG.debug("Updating component " + SSID + "...");
		List<Class> excepts = Arrays.asList(exceptions);
		for(int i = 0; i < adaptors.length; i++) {
			AbstAdaptor a = adaptors[i];
			if(!excepts.contains(a.getClass()))
				a.updateDevice(this, waitUntilUpdated);
		}
		LOG.debug("Component " + SSID + " updated!");
	}
	
	/**
	 * Publishes the credentials of this Component object to the default topic. Creates a new
	 * ResRegister object that contains the credentials of this Component.
	 * 
	 * @param mh The MQTTHandler that handles the publishing
	 * @param registerRTY The RTY designation for registration requests
	 * @param parentLogDomain The log domain of the object that called this method
	 */
	public void publishCredentials(MQTTPublisher mp, String registerRTY, String parentLogDomain) {
		Logger LOG = getLogger(parentLogDomain);
		LOG.debug("Sending credentials to default_topic...");
		ResRegister response = new ResRegister(getMAC(), getSSID(), registerRTY, getSSID(), getTopic());
		mp.publishToDefaultTopic(response);
	}
	
	/**
	 * Publishes the values of this component's properties to the MQTT server. The values are returned in the form 
	 * of a POOP response JSON.
	 * 
	 * @param mh The MQTTHandler that handles the publishing
	 * @param poopRTY The RTY string for the POOP request
	 * @param parentLogDomain The log domain of the object that called this method
	 */
	public void publishPropertyValues(MQTTPublisher mp, String poopRTY, String parentLogDomain) {
		Logger LOG = getLogger(parentLogDomain);
		LOG.debug("Sending property states of " + SSID + " to " + topic + "...");
		Iterator<AbstProperty> props = properties.values().iterator();
		while(props.hasNext()) {
			AbstProperty prop = props.next();
			LOG.trace(prop.getStandardID() + " = " + prop.getValue());
			ResPOOP poop = new ResPOOP("POOP", SSID, poopRTY, prop.getSSID(), prop.getValue());
			mp.publish(poop);
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
			if(room != null)
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
		if(room != null)
			s += "\"" + room.getSSID() + "\", ";
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
	public void setActive(boolean active, String parentLogDomain, boolean waitUntilActivated) throws AdaptorException {
		Logger LOG = getLogger(parentLogDomain);
		LOG.debug("Setting active state to " + active);
		this.active = active;
		for(int i = 0; i < adaptors.length; i++) {
			adaptors[i].updateDeviceState(this, waitUntilActivated);
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

//	/**
//	 * Returns the order of this device in the room it belongs to
//	 * @return the numerical order of this device in the room
//	 */
//	public int getRoomIndex() {
//		return roomIndex;
//	}
//
//	/**
//	 * Sets the order of this device in the room it belongs to. <b><i>NOTE:</b> this method is only called by the 
//	 * room object when this device is added to it OR when the device is retrieved from the DB.
//	 * 
//	 * @param roomIndex the numerical order of this device in the room
//	 */
//	public void setRoomIndex(int roomIndex) {
//		this.roomIndex = roomIndex;
//	}

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
