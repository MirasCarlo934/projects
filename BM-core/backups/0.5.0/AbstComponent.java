package components;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;

import javax.xml.ws.http.HTTPException;

import org.apache.log4j.Logger;
import org.json.JSONObject;

import components.products.AbstProduct;
import components.properties.AbstProperty;
import json.RRP.ResError;
import json.RRP.ResPOOP;
import json.RRP.ResRegister;
import main.adaptors.DBAdaptor;
import main.adaptors.OHAdaptor;
import main.engines.DBEngine;
import main.engines.exceptions.EngineException;
import main.engines.requests.DBEngine.UpdateDBEReq;
import mqtt.MQTTListener;
import tools.IDGenerator;

/**
 * The Component object 
 * 
 * @author Carlo Miras
 *
 */
public abstract class AbstComponent {
	protected DBAdaptor dba;
	protected OHAdaptor oha;
	protected String loggerName;
	protected HashMap<String, AbstProperty> properties = new HashMap<String, AbstProperty>(1);
	protected AbstProduct product;
	protected String SSID;
	protected String MAC;
	protected String name;
	protected String topic;
	protected String room;
	protected boolean active;
	protected IDGenerator idg = new IDGenerator();
	
	public AbstComponent(String SSID, String MAC, String name, String topic, String room, 
			boolean active, AbstProduct product, DBAdaptor dba, OHAdaptor oha) {
		this.dba = dba;
		this.oha = oha;
		this.loggerName = "COM:" + SSID;
		this.setSSID((SSID));
		this.setMAC((MAC));
		this.setName((name));
		this.setTopic((SSID + "_topic"));
		this.setRoom((room));
		this.setProperties(product.getProperties());
		this.setProduct((product));
		setActive(active);
		
		//sets the properties to be owned by this component
		Iterator<AbstProperty> props = properties.values().iterator();
		while(props.hasNext()) {
			AbstProperty prop = props.next();
			prop.setComID(SSID);
		}
	}
	
	public abstract JSONObject[] convertToItemsJSON();
	
	/**
	 * Integrates this component to the BM system. The component is persisted to the DB and is also
	 * given an item representation in OpenHAB
	 * 
	 * @param parentLogDomain The log domain of the Object that called this method
	 * 
	 * @throws EngineException if the DBEngine or OpenHAB HTTPEngine encountered an error in request processing
	 */
	public void integrateComponentToSystem(String parentLogDomain) throws EngineException {
		Logger LOG = getLogger(parentLogDomain);
		LOG.debug("Persisting component to DB...");
		dba.persistComponent(this);
		LOG.debug("Persisting component's properties to DB...");
		Iterator<AbstProperty> props = properties.values().iterator();
		while(props.hasNext()) {
			props.next().persistPropertyToDB();
		}
		LOG.debug("Adding item representation to DB...");
		oha.persistComponent(this);
	}
	
	/**
	 * Deletes this component from the BM system. The component is deleted from the DB and its item
	 * representation in OpenHAB is also deleted.
	 * 
	 * @param parentLogDomain The log domain of the Object that called this method
	 * @throws Exception 
	 * @throws HTTPException 
	 */
	public void deleteComponentFromSystem(String parentLogDomain) throws EngineException {
		Logger LOG = getLogger(parentLogDomain);
		LOG.debug("Deleting component from DB...");
		dba.deleteComponent(this);
		LOG.debug("Deleting item representation from OH...");
		oha.deleteComponent(this);
	}
	
	/**
	 * Persists this Component object to the DB.
	 * 
	 * @param comsTable The table name where this Component will be persisted into
	 * @param propsTable The table name where the Properties of this Component will be persisted
	 * 		into
	 * @param parentLogDomain The log domain of the object that invoked this method
	 * 
	 * @throws EngineException thrown when the persistence fails
	 */
	public void persistComponentToDB() throws EngineException {
		dba.persistComponent(this);
	}
	
	public void persistComponentPropertiesToDB() throws EngineException {
		Iterator<AbstProperty> props = properties.values().iterator();
		while(props.hasNext()) {
			props.next().persistPropertyToDB();
		}
	}
	
	public void registerToOH() throws EngineException {
		oha.persistComponent(this);
	}
	
	public void unregisterFromOH() throws EngineException {
		oha.deleteComponent(this);
	}
	
	/**
	 * Deletes this component from the DB. Also deletes this component's properties from DB.
	 * 
	 * @param dbe The DBEngine that will handle the deletion
	 * @param comsTable The DB table where this component will be deleted from
	 * @param propsTable The DB table where this component's properties will be deleted from
	 * @param parentLogDomain The log domain of the object that invoked this method
	 * 
	 * @throws EngineException thrown when deletion fails
	 */
	public void deleteComponentFromDB() throws EngineException {
		dba.deleteComponent(this);
	}
	
	/**
	 * Publishes the credentials of this Component object to the default topic. Creates a new
	 * ResRegister object that contains the credentials of this Component. This method is invoked <b>
	 * solely</b> by the RegistrationModule.
	 * 
	 * @param mh The MQTTHandler that handles the publishing
	 * @param registerRTY The RTY designation for registration requests
	 */
	public void publishCredentials(MQTTHandler mh, String registerRTY, String parentLogDomain) {
		Logger LOG = getLogger(parentLogDomain);
		LOG.debug("Sending credentials to default_topic...");
		ResRegister response = new ResRegister(getMAC(), getSSID(), registerRTY, getSSID(), getTopic());
		mh.publishToDefaultTopic(response);
	}
	
	public void publishPropertyValues(MQTTHandler mh, String poopRTY, String parentLogDomain) {
		Logger LOG = getLogger(parentLogDomain);
		LOG.debug("Sending property states of " + SSID + " to " + topic + "...");
		Iterator<AbstProperty> props = properties.values().iterator();
		while(props.hasNext()) {
			AbstProperty prop = props.next();
			LOG.trace(prop.getStandardID() + " = " + prop.getValue());
			ResPOOP poop = new ResPOOP("POOP", SSID, poopRTY, prop.getSSID(), prop.getValue());
			mh.publish(poop);
		}
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
	 * @param dbe The DBEngine that will handle the persistence
	 * @param comsTable The DB table name where the persistence will take place
	 * @throws EngineException thrown when persistence fails
	 * @throws InterruptedException thrown when Thread.wait() fails
	 */
	public void setActive(boolean active, String parentLogDomain) throws EngineException {
		Logger LOG = getLogger(parentLogDomain);
		LOG.debug("Setting active state to " + active);
		this.active = active;
		dba.persistComponentState(this);
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

	public String getRoom() {
		return room;
	}

	public void setRoom(String room) {
		this.room = room;
	}

	public String getSSID() {
		return SSID;
	}

	public void setSSID(String sSID) {
		SSID = sSID;
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
