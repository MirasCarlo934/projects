package bm.smarthome.properties;

import java.util.Arrays;
import java.util.List;

import javax.xml.ws.http.HTTPException;

import org.apache.log4j.Logger;
import org.json.JSONObject;

import bm.jeep.ResPOOP;
import bm.mqtt.MQTTPublisher;
import bm.smarthome.adaptors.AbstAdaptor;
import bm.smarthome.adaptors.DBAdaptor;
import bm.smarthome.adaptors.OHAdaptor;
import bm.smarthome.adaptors.exceptions.AdaptorException;
import bm.smarthome.devices.Device;
import bm.smarthome.interfaces.HTMLTransformable;
import bm.smarthome.interfaces.OHItemmable;
import bm.smarthome.interfaces.SmarthomeElement;
import bm.smarthome.properties.bindings.Binding;

/**
 * A Java-object representation of a real-world device property. 
 * @author carlomiras
 *
 */
public abstract class AbstProperty extends SmarthomeElement implements OHItemmable, HTMLTransformable {
	protected Device parentDevice;
	protected Binding binding;
	protected String loggerName;
	protected String genericName;
	protected String displayName;
	protected String systemName; //[prop_type]-[prop_mode]-[cpl_SSID]
	protected PropertyMode mode;
	protected PropertyType propType;
	protected Object value = "0";
	
	public AbstProperty(PropertyType propType, String SSID, /*String genericName, */String dispname, 
			/*String ohItemType,*/ PropertyMode mode, /*PropertyValueType propValType,*/ DBAdaptor dba, 
			OHAdaptor oha, AbstAdaptor[] additionalAdaptors, Binding binding) {
		super(SSID, dba, oha, additionalAdaptors);
		this.displayName = (dispname);
		this.genericName = propType.getName() + "-" + mode.toString();
		this.setSystemName(genericName, SSID);
		this.mode = (mode);
		this.propType = propType;
		this.binding = binding;
	}
	
	public abstract AbstProperty clone();
	
	/**
	 * Checks if the specified value is valid for this property
	 * @param value The value to be checked
	 * @return <b>true</b> if value is valid, <b>false</b> otherwise
	 */
//	public abstract boolean checkValueTypeValidity(Object value);
	public boolean checkValueTypeValidity(Object value) {
		return propType.checkValueTypeValidity(value);
	}
	
	/**
	 * Transforms the value of this property into a String which OpenHAB can recognize as a command 
	 * for the item that represents this property. 
	 * 
	 * @return the transformed value
	 */
//	public abstract String transformValueToOHCommand();
	public String transformValueToOHCommand() {
//		System.out.println("COMMAND: " + propType.transformPropValueToOHCommand(value.toString()));
		return propType.transformPropValueToOHCommand(value.toString());
	}
	
//	/**
//	 * Transforms the value of this property into a String which OpenHAB can recognize as a command 
//	 * for the item that represents this property. 
//	 * 
//	 * @param value the property value in string format
//	 * @return the transformed value or the property value if value is not transformable
//	 */
//	public Object transformValueToOHCommand(Object value) {
//		if(valueOHCommandTransform.get(value.toString()) != null) {
//			return valueOHCommandTransform.get(value);
//		} else {
//			return value;
//		}
//	}
	
	/**
	 * Persists this property to the DB, OH, and all the various peripheral systems plugged in to this specific
	 * property.
	 */
	@Override
	public void persist(String parentLogDomain, boolean waitUntilPersisted) throws AdaptorException {
		final Logger LOG = getLogger(parentLogDomain);
		if(!propType.getOHIcon().equals("none")) {
			LOG.debug("Persisting property " + getStandardID() + "...");
			for(int i = 0; i < adaptors.length; i++) {
				adaptors[i].persistProperty(this, waitUntilPersisted);
			}
			LOG.debug("Property " + getStandardID() + " persisted!");
		}
	}
	
	@Override
	public void persist(String parentLogDomain, AbstAdaptor[] exceptions, boolean waitUntilPersisted) 
			throws AdaptorException {
		final Logger LOG = getLogger(parentLogDomain);
		List<AbstAdaptor> except = Arrays.asList(exceptions);
		if(!propType.getOHIcon().equals("none")) {
			LOG.debug("Persisting property " + getStandardID() + "...");
			for(int i = 0; i < adaptors.length; i++) {
				if(!except.contains(adaptors[i])) {
					adaptors[i].persistProperty(this, waitUntilPersisted);
				}
			}
			LOG.debug("Property " + getStandardID() + " persisted!");
		}
	}
	
	/**
	 * Deletes this property from the DB, OH, and all the various peripheral systems plugged in to this specific
	 * property.
	 */
	@Override
	public void delete(String parentLogDomain, boolean waitUntilDeleted) throws AdaptorException {
		Logger LOG = getLogger(parentLogDomain);
		if(!propType.getOHIcon().equals("none")) {
			LOG.debug("Deleting property " + getStandardID() + "...");
			for(int i = 0; i < adaptors.length; i++) {
				adaptors[i].deleteProperty(this, waitUntilDeleted);
			}
			LOG.debug("Property " + getStandardID() + " deleted!");
		}
	}
	
	/**
	 * Updates the value of this property in the DB, OH, and all the various peripheral systems plugged in to this 
	 * specific property.
	 */
	@Override
	public void update(String parentLogDomain, boolean waitUntilUpdated) throws AdaptorException {
		Logger LOG = getLogger(parentLogDomain);
		if(!propType.getOHIcon().equals("none")) {
			LOG.debug("Updating property value...");
			for(int i = 0; i < adaptors.length; i++) {
				adaptors[i].updatePropertyValue(this, waitUntilUpdated);
			}
			LOG.debug("Property " + getStandardID() + " updated!");
		}
	}
	
	@Override
	public void update(AbstAdaptor[] exceptions, String parentLogDomain, boolean waitUntilUpdated) 
			throws AdaptorException {
		Logger LOG = getLogger(parentLogDomain);
		if(!propType.getOHIcon().equals("none")) {
			LOG.debug("Updating property value...");
			List<AbstAdaptor> excepts = Arrays.asList(exceptions);
			for(int i = 0; i < adaptors.length; i++) {
				if(!excepts.contains(adaptors[i])) {
					adaptors[i].updatePropertyValue(this, waitUntilUpdated);
				}
			}
			LOG.debug("Property " + getStandardID() + " updated!");
		}
	}
	
	@Override
	public void updateExcept(Class[] exceptions, String parentLogDomain, boolean waitUntilUpdated) 
			throws AdaptorException {
		Logger LOG = getLogger(parentLogDomain);
		if(!propType.getOHIcon().equals("none")) {
			LOG.debug("Updating property value...");
			List<Class> excepts = Arrays.asList(exceptions);
			for(int i = 0; i < adaptors.length; i++) {
				if(!excepts.contains(adaptors[i].getClass())) {
					adaptors[i].updatePropertyValue(this, waitUntilUpdated);
				}
			}
			LOG.debug("Property " + getStandardID() + " updated!");
		}
	}
	
	/**
	 * Publishes the property value to the specified component (CID). This method is called <b>ONLY</b>
	 * by the POOPModule after a successful property value change.
	 * 
	 * @param mp The MQTTPublisher which will handle the publishing
	 * @param RID The RID of the response that will be published
	 * @param CID The CID of the component where the response will be published
	 * @param poopRTY The RTY designation for POOP requests
	 */
	public void publishPropertyValueToMQTT(MQTTPublisher mp, String RID, String CID, String poopRTY) {
		ResPOOP response = new ResPOOP(RID, CID, poopRTY, getSSID(), value);
		mp.publish(response);
	}
	
	@Override
	public JSONObject[] convertToItemsJSON() {
		JSONObject json = new JSONObject();
		json.put("name", getStandardID());
		json.put("type", propType.getOHIcon());
		if(parentDevice.getProperties().length > 1) {
			json.put("groupNames", new String[]{parentDevice.getSSID()});
			json.put("label", displayName);
		} else {
			json.put("groupNames", new String[]{parentDevice.getRoom().getSSID()});
			json.put("label", parentDevice.getName());
			json.put("category", parentDevice.getProduct().getOHIcon());
		}
		return new JSONObject[]{json};
	}
	
	@Override
	public String convertToSitemapString() {
		return null;
	}
	
	/**
	 * Returns a javascript object named "Property" with the format:
	 * 
	 * "new Property('[propType ID]', '[display name]', '[mode]', '[property value]')";
	 */
	@Override
	public String convertToJavascript() {
		String str = "new Property('" + propType.getSSID() + "', '" + displayName + "', '" + mode.toString() + "', '"
				+ value.toString() + "')";
		return str;
	}

	/**
	 * Returns the value of this Property. Is usually overridden by child classes of the AbstProperty 
	 * to return the value with the adequate data type.
	 * 
	 * @return The value of this Property.
	 */
	public Object getValue() {
		return value;
	}
	
	/**
	 * Additional processes to be done once the value for this property has been set
	 * @param value The new value for the property
	 */
	public abstract void setValueAction(Object value);

	/**
	 * Sets the value of this property.
	 * 
	 * @param value The value of the Property to be set
	 */
	public void setValue(Object value) {
		this.value = value;
		setValueAction(value);
	}
	
	/**
	 * Sets the value of this property. Also persists this new property value to the DB and updates 
	 * the item representation of this property in OpenHAB. Used primarily for POOP requests.
	 * 
	 * @param value The value of the property to be set
	 * @param parentLogDomain The log4j logging domain used by the Object that invokes this method
	 * @throws Exception 
	 * @throws HTTPException 
	 */
	public void setValue(Object value, String parentLogDomain, boolean waitUntilUpdated) 
			throws AdaptorException {
		Logger LOG = getLogger(parentLogDomain);
		LOG.debug("Setting value of property " + getStandardID() + " to " + value + "...");
		setValue(value);
		update(parentLogDomain, waitUntilUpdated);
	}

	/**
	 * Sets the value of this property. Persists this property's value to all adaptors with exceptions.
	 * 
	 * @param value The value of the property to be set
	 * @param exceptions the adaptor classes where this object <b>WILL NOT</b> be persisted to
	 * @param parentLogDomain The log4j logging domain used by the Object that invokes this method
	 * @throws Exception 
	 * @throws HTTPException 
	 */
	public void setValue(Object value, Class[] exceptions, String parentLogDomain, boolean waitUntilUpdated) 
			throws AdaptorException {
		Logger LOG = getLogger(parentLogDomain);
		LOG.debug("Setting value of property " + getStandardID() + " to " + value + "...");
		setValue(value);
		updateExcept(exceptions, parentLogDomain, waitUntilUpdated);
	}
	
	/**
	 * Returns the display name of this Property. Used primarily in OpenHAB and other UI.
	 * 
	 * @return the display name of this Property
	 */
	public String getDisplayName() {
		return displayName;
	}

	/**
	 * Returns the mode of this Property. Usually denotes if the property is an input or output.
	 * 
	 * @return the PropertyMode that represents the mode of this property
	 */
	public PropertyMode getMode() {
		return mode;
	}


	/**
	 * Returns the system name of this Property. <br><br>
	 * 
	 * <b>Construction:</b><br>
	 * [<i>genericName</i>]-[<i>SSID</i>]
	 * 
	 * @return the system name of this Property
	 */
	public String getSystemName() {
		return systemName;
	}

	/**
	 * @param systemName the systemName to set
	 * @param index the index set in table COMPROPLIST
	 */
	private void setSystemName(String systemName, String index) {
		this.systemName = systemName + "-" + index;
	}

	/**
	 * Returns the data type of the value held by this Property.
	 * 
	 * @return the PropertyValueType that represents the data type of the value held by this Property
	 */
//	public PropertyValueType getPropValType() {
//		return propValType;
//	}

	/**
	 * Returns the property type of this Property denoted in PROPCAT table
	 * 
	 * @return the property type of this Property
	 */
	public PropertyType getPropType() {
		return propType;
	}

	/**
	 * Returns the component that owns this property
	 * 
	 * @return the AbstComponent object that owns this property
	 */
	public Device getDevice() {
		return parentDevice;
	}
	
	public void setDevice(Device device) {
		this.parentDevice = device;
		loggerName = getStandardID();
	}
	
	/**
	 * Returns the standard ID for this property which is defined as <b>[CID]_[SSID]</b>. This is 
	 * commonly used in OpenHAB.
	 * 
	 * @return the standard ID of this property
	 */
	public String getStandardID() {
		return parentDevice.getSSID() + "_" + SSID;
	}
	
	public String getOHItemType() {
		return propType.getOHIcon();
	}
	
	/**
	 * Returns the peripheral service binding bound to this property
	 * 
	 * @return the <i>Binding</i> object representing this property's peripheral service binding, 
	 * 		<i>null</i> if this property has no binding
	 */
	public Binding getBinding() {
		return binding;
	}
	
	protected Logger getLogger(String parentLogDomain) {
		return Logger.getLogger(parentLogDomain + "." + loggerName);
	}
}
