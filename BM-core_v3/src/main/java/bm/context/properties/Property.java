package bm.context.properties;

import org.apache.log4j.Logger;
import org.json.JSONObject;

import bm.context.HTMLTransformable;
import bm.context.OHItemmable;
import bm.context.SymphonyElement;
import bm.context.adaptors.AbstAdaptor;
import bm.context.adaptors.exceptions.AdaptorException;
import bm.context.devices.Device;
import bm.tools.IDGenerator;

/**
 * A Java-object representation of a real-world device property. 
 * @author carlomiras
 *
 */
public class Property extends SymphonyElement implements OHItemmable, HTMLTransformable {
	protected Device parentDevice;
	protected String loggerName;
	protected String genericName;
	protected String displayName;
	protected String systemName; //[prop_type]-[prop_mode]-[cpl_SSID]
	protected PropertyMode mode;
	protected PropertyType propType;
	protected Object value = "0";
	
	protected String poopRTY;
	protected String propIDParam;
	protected String propValParam;
	protected IDGenerator idg;

	//TASK add index to constructor params
	public Property(PropertyType propType, String SSID, /*String genericName, */String dispname,
						/*String ohItemType,*/ PropertyMode mode, /*PropertyValueType propValType,*/
						String poopRTY, String propIDParam, String propValParam,
                    IDGenerator idGenerator) {
		super(SSID, 0);
		this.displayName = (dispname);
		this.genericName = propType.getName() + "-" + mode.toString();
		this.setSystemName(genericName, SSID);
		this.mode = (mode);
		this.propType = propType;
		
		this.poopRTY = poopRTY;
		this.propIDParam = propIDParam;
		this.propValParam = propValParam;
		this.idg = idGenerator;
	}

	@Override
	public Property clone() {
		Property p = new Property(propType, SSID, displayName, mode, poopRTY, propIDParam, propValParam,
				idg);
		p.setAdaptors(p.getAdaptors());
		return p;
	}
	
	/**
	 * Checks if the specified value is valid for this property
	 * @param value The value to be checked
	 * @return <b><i>true</i></b> if value is valid, <b><i>false</i></b> otherwise
	 */
//	public abstract boolean checkValueValidity(Object value);
	public boolean checkValueValidity(Object value) {
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
	protected void create(AbstAdaptor adaptor, String parentLogDomain, boolean waitUntilCreated)
			throws AdaptorException {
		final Logger LOG = getLogger(parentLogDomain);
		if(!propType.getOHIcon().equals("none")) {
			LOG.debug("Creating property " + getOH_ID() + " in " + adaptor.getName() + "...");
			adaptor.propertyCreated(this, waitUntilCreated);
			LOG.debug("Property " + getOH_ID() + " created!");
		}
	}
	
//	@Override
//	protected void create(String parentLogDomain, boolean waitUntilCreated) 
//			throws AdaptorException {
//		final Logger LOG = getLogger(parentLogDomain);
//		List<AbstAdaptor> except = Arrays.asList(exceptions);
//		if(!propType.getOHIcon().equals("none")) {
//			LOG.debug("Persisting property " + getStandardID() + "...");
//			for(int i = 0; i < adaptors.length; i++) {
//				if(!except.contains(adaptors[i])) {
//					adaptors[i].propertyCreated(this, waitUntilPersisted);
//				}
//			}
//			LOG.debug("B_Property " + getStandardID() + " persisted!");
//		}
//	}
	
	/**
	 * Deletes this property from the DB, OH, and all the various peripheral systems plugged in to this specific
	 * property.
	 */
	@Override
	protected void delete(AbstAdaptor adaptor, String parentLogDomain, boolean waitUntilDeleted)
			throws AdaptorException {
		Logger LOG = getLogger(parentLogDomain);
		if(!propType.getOHIcon().equals("none")) {
			LOG.debug("Deleting property " + getOH_ID() + " from " + adaptor.getName() + "...");
			adaptor.propertyDeleted(this, waitUntilDeleted);
			LOG.debug("Property " + getOH_ID() + " deleted!");
		}
	}
	
	/**
	 * Updates the value of this property in the DB, OH, and all the various peripheral systems plugged in to this 
	 * specific property.
	 */
	@Override
	protected synchronized void update(AbstAdaptor adaptor, String parentLogDomain, boolean waitUntilUpdated) throws AdaptorException {
		if(!propType.getOHIcon().equals("none")) {
			Logger LOG = getLogger(parentLogDomain);
			LOG.debug("Updating value of property " + getOH_ID() + " in " + adaptor.getName() + 
					"...");
			adaptor.propertyValueUpdated(this, waitUntilUpdated);
			LOG.debug("Property " + getOH_ID() + " updated!");
		}
	}
	
//	@Override
//	public void update(AbstAdaptor[] exceptions, String parentLogDomain, boolean waitUntilUpdated) 
//			throws AdaptorException {
//		Logger LOG = getLogger(parentLogDomain);
//		if(!propType.getOHIcon().equals("none")) {
//			LOG.debug("Updating property value...");
//			List<AbstAdaptor> excepts = Arrays.asList(exceptions);
//			for(int i = 0; i < adaptors.length; i++) {
//				if(!excepts.contains(adaptors[i])) {
//					adaptors[i].propertyValueUpdated(this, waitUntilUpdated);
//				}
//			}
//			LOG.debug("B_Property " + getStandardID() + " updated!");
//		}
//	}
	
//	@Override
//	public void updateExcept(Class[] exceptions, String parentLogDomain, boolean waitUntilUpdated) 
//			throws AdaptorException {
//		Logger LOG = getLogger(parentLogDomain);
//		if(!propType.getOHIcon().equals("none")) {
//			LOG.debug("Updating property value...");
//			List<Class> excepts = Arrays.asList(exceptions);
//			for(int i = 0; i < adaptors.length; i++) {
//				if(!excepts.contains(adaptors[i].getClass())) {
//					adaptors[i].propertyValueUpdated(this, waitUntilUpdated);
//				}
//			}
//			LOG.debug("B_Property " + getStandardID() + " updated!");
//		}
//	}
	
	@Override
	public JSONObject[] convertToItemsJSON() {
		JSONObject json = new JSONObject();
		json.put("name", getOH_ID());
		json.put("type", propType.getOHIcon());
		if(parentDevice.getProperties().length > 1) {
			json.put("groupNames", new String[]{parentDevice.getSSID()});
			json.put("label", displayName);
		} else {
			json.put("groupNames", new String[]{parentDevice.getParentRoom().getSSID()});
			json.put("label", parentDevice.getName());
			json.put("category", parentDevice.getProduct().getIconImg());
		}
		return new JSONObject[]{json};
	}

	@Override
	public String convertToSitemapString() {
		return null;
	}

	/**
	 * Returns a javascript object named "Property" with the format.
	 * "new B_Property('[propType ID]', '[display name]', '[mode]', '[property value]')";
	 */
	@Override
	public String convertToJavascript() {
		String str = "new Property('" + propType.getSSID() + "', '" + displayName + "', '" + mode.toString() + "', '"
				+ value.toString() + "')";
		return str;
	}

	/**
	 * Returns the value of this <i>Property</i>.
	 * @return The value of this <i>Property</i>.
	 */
	public Object getValue() {
		return value;
	}

	/**
	 * Sets the value of this property.
	 * @param value The value of the Property to be set
	 */
	public void setValue(Object value) {
		this.value = value;
	}
	
	/**
	 * Sets the value of this property in this object and calls the external application adaptors to
	 * handle this property value change. This method must only be called when the value change did 
	 * not come from a JEEP request (ie. CIR)
	 * 
	 * @param value The value of the property to be set
	 * @param parentLogDomain The log4j logging domain used by the Object that invokes this method
	 * @param waitUntilUpdated <b><i>true</i></b> if thread must be set to wait until the adaptor/s have
	 * 			completed processing, <b><i>false</i></b> if thread will not be set to wait.
	 * @throws AdaptorException 
	 */
	public void setValue(Object value, String parentLogDomain, boolean waitUntilUpdated) 
			throws AdaptorException {
		setValue(value, null, parentLogDomain, waitUntilUpdated);
		this.value = value;
	}
	
	/**
	 * Sets the value of this property in this object and calls the external application adaptors to
	 * handle this property value change. 
	 * 
	 * @param value The value of the property to be set
	 * @param cid The CID of the component that sent the request to change the property value
	 * @param parentLogDomain The log4j logging domain used by the Object that invokes this method
	 * @param waitUntilUpdated <b><i>true</i></b> if thread must be set to wait until the adaptor/s have
	 * 			completed processing, <b><i>false</i></b> if thread will not be set to wait.
	 * @throws AdaptorException
	 */
	public void setValue(Object value, String cid, String parentLogDomain, boolean waitUntilUpdated) 
			throws AdaptorException {
		Logger LOG = getLogger(parentLogDomain);
		LOG.debug("Setting value of property " + getOH_ID() + " to " + value + "...");
		setValue(value);
		if(cid == null)
			update(parentLogDomain, waitUntilUpdated);
		else
			updateExcept(new String[]{cid}, parentLogDomain, waitUntilUpdated);
	}

//	/**
//	 * Sets the value of this property. Persists this property's value to all adaptors with exceptions.
//	 * 
//	 * @param value The value of the property to be set
//	 * @param exceptions the adaptor classes where this object <b>WILL NOT</b> be persisted to
//	 * @param parentLogDomain The log4j logging domain used by the Object that invokes this method
//	 * @throws Exception 
//	 * @throws HTTPException 
//	 */
//	public void setValue(Object value, Class[] exceptions, String parentLogDomain, boolean waitUntilUpdated) 
//			throws AdaptorException {
//		Logger LOG = getLogger(parentLogDomain);
//		LOG.debug("Setting value of property " + getStandardID() + " to " + value + "...");
//		setValue(value);
//		updateExcept(exceptions, parentLogDomain, waitUntilUpdated);
//	}
	
	/**
	 * Returns the display name of this B_Property. Used primarily in OpenHAB and other UI.
	 * 
	 * @return the display name of this B_Property
	 */
	public String getDisplayName() {
		return displayName;
	}

	/**
	 * Returns the mode of this B_Property. Usually denotes if the property is an input or output.
	 * 
	 * @return the PropertyMode that represents the mode of this property
	 */
	public PropertyMode getMode() {
		return mode;
	}


	/**
	 * Returns the system name of this B_Property. <br><br>
	 * 
	 * <b>Construction:</b><br>
	 * [<i>genericName</i>]-[<i>SSID</i>]
	 * 
	 * @return the system name of this B_Property
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
	 * Returns the data type of the value held by this B_Property.
	 * 
	 * @return the PropertyValueType that represents the data type of the value held by this B_Property
	 */
//	public PropertyValueType getPropValType() {
//		return propValType;
//	}

	/**
	 * Returns the property type of this B_Property denoted in PROPCAT table
	 * 
	 * @return the property type of this B_Property
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
		loggerName = getOH_ID();
	}
	
	/**
	 * Returns the standard ID for this property which is defined as <b>[CID]_[SSID]</b>. This is 
	 * commonly used in OpenHAB.
	 * 
	 * @return the standard ID of this property
	 */
	public String getOH_ID() {
		return parentDevice.getSSID() + "_" + SSID;
	}
	
	public String getOHItemType() {
		return propType.getOHIcon();
	}
	
	protected Logger getLogger(String parentLogDomain) {
		return Logger.getLogger(parentLogDomain + "." + loggerName);
	}
}