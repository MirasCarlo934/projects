package bm.context.products;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;

import bm.comms.Protocol;
import bm.comms.Sender;
import bm.context.HTMLTransformable;
import bm.context.properties.Property;
import org.apache.log4j.Logger;

import bm.context.devices.Device;
import bm.context.properties.PropertyMode;
import bm.context.properties.PropertyType;
import bm.context.rooms.Room;
import bm.main.engines.DBEngine;
import bm.tools.IDGenerator;

public class Product implements HTMLTransformable {
	protected String logDomain;
	protected Logger LOG;
	protected String SSID;
	protected String name;
	protected String description;
	protected HashMap<String, Property> properties = new HashMap<String, Property>(10);
	protected DBEngine dbe;
//	protected Vector<AbstAdaptor> adaptors = new Vector<AbstAdaptor>(1, 1);
	protected HashMap<String, PropertyType> propertyTypes;

	private String iconImg;
	
	private String poopRTY;
	private String propIDParam;
	private String propValParam;
	private IDGenerator idg;

	public Product(String mainLogDomain, String SSID, String name, String description,
                   String iconImg, HashMap<String, PropertyType> propertyTypes, String poopRTY,
                   String propIDParam, String propValParam, IDGenerator idGenerator) {
		LOG = Logger.getLogger(mainLogDomain + "." + Product.class.getSimpleName());
		this.logDomain = mainLogDomain;
		this.SSID = SSID;
		this.name = name;
		this.description = description;
		this.iconImg = iconImg;
//		this.OH_icon = OH_icon;
//		this.adaptors = adaptors;
		this.propertyTypes = propertyTypes;
		this.poopRTY = poopRTY;
		this.propIDParam = propIDParam;
		this.propValParam = propValParam;
		this.idg = idGenerator;
//		if(retrieveProperties) {
//			try {
//				productsRS.beforeFirst();
//				retrieveProperties(productsRS);
//			} catch (SQLException e) {
//				LOG.error("Cannot retrieve properties!", e);
//			}
//		}
	}
	
//	public Product(String mainLogDomain, String SSID, String name, String description, /*String OH_icon,*/
//			Property[] properties, /*AbstAdaptor[] adaptors,*/
//			String poopRTY, String propIDParam, String propValParam, IDGenerator idGenerator) {
//		LOG = Logger.getLogger(mainLogDomain + "." + Product.class.getSimpleName());
//		this.logDomain = mainLogDomain;
//		this.SSID = SSID;
//		this.name = name;
//		this.description = description;
////		this.OH_icon = OH_icon;
////		this.adaptors = adaptors;
//		this.poopRTY = poopRTY;
//		this.propIDParam = propIDParam;
//		this.propValParam = propValParam;
//		this.idg = idGenerator;
//		for(int i = 0; i < properties.length; i++) {
//			Property prop = properties[i];
//			this.properties.put(prop.getSSID(), prop);
//		}
//	}
	
	/**
	 * Creates an empty Symphony Product object that has no properties assigned. Properties <b><i>MUST</i></b> be 
	 * assigned to this product using the <i>setProperties()</i> method before this product is assigned to device
	 * objects.
	 * @param mainLogDomain
	 * @param SSID
	 * @param name
	 * @param description
	 */
	public Product(String mainLogDomain, String SSID, String name, String description, String iconImg,
                   String poopRTY, String propIDParam, String propValParam, IDGenerator idGenerator) {
		LOG = Logger.getLogger(mainLogDomain + "." + Product.class.getSimpleName());
		this.logDomain = mainLogDomain;
		this.SSID = SSID;
		this.name = name;
		this.description = description;
		this.iconImg = iconImg;
//		this.OH_icon = OH_icon;
//		this.adaptors = adaptors;
		this.poopRTY = poopRTY;
		this.propIDParam = propIDParam;
		this.propValParam = propValParam;
		this.idg = idGenerator;
	}
	
//	public void initialize(String SSID, String name, String description, String OH_icon, 
//			ResultSet productsRS) {
//		this.SSID = SSID;
//		this.name = name;
//		this.description = description;
//		this.OH_icon = OH_icon;
//		LOG = Logger.getLogger(logDomain + "." + name);
//		try {
//			productsRS.beforeFirst();
//			retrieveProperties(productsRS);
//		} catch (SQLException e) {
//			LOG.error("Cannot retrieve properties!", e);
//		}
//	}
	
//	public void initialize(String SSID, String name, String description, String OH_icon, 
//			Property[] properties) {
//		this.SSID = SSID;
//		this.name = name;
//		this.description = description;
//		this.OH_icon = OH_icon;
//		for(int i = 0; i < properties.length; i++) {
//			Property prop = properties[i];
//			this.properties.put(prop.getSSID(), prop);
//		}
//	}
	
	public Device createDevice(String comID, String MAC, String name, String topic, Protocol protocol,
			Room room, boolean active, int index) throws IllegalArgumentException {
		if(properties.isEmpty())
			throw new IllegalArgumentException("Properties not yet retrieved from DB!");
		else {
			Device d = new Device(comID, MAC, name, topic, protocol, room, active, this, index);
			return d;
		}
	}
	
	/**
	 * Returns a javascript object named "Product" with the format:
	 * 
	 * "new Product('[SSID]', '[name]', '[description]', '[OpenHAB icon]', [[properties array]])
	 */
	@Override
	public String convertToJavascript() {
		Iterator<Property> props = properties.values().iterator();
		String str = "new Product('" + SSID + "', '" + name + "', '" + description + "', '" + iconImg + "', [";
		while(props.hasNext()) {
			Property prop = props.next();
			str += prop.convertToJavascript() + ", ";
		}
		str = str.substring(0, str.length() - 2) + "])";
		return str;
	}
	
	protected void retrieveProperties(ResultSet rs) throws SQLException, IllegalArgumentException {
		while(rs.next()) {
			String prod_ssid =  rs.getString("prod_ssid");
			try{
				if(!prod_ssid.equals(SSID)) {
					continue;
				}
			} catch(NullPointerException e) {
				throw new IllegalArgumentException("Product not yet initialized!", e);
			}
			PropertyType prop_type = propertyTypes.get(rs.getString("prop_type"));
			String prop_dispname = rs.getString("prop_dispname");
			PropertyMode prop_mode = PropertyMode.parseModeFromString(rs.getString("prop_mode"));
			String prop_ssid = rs.getString("prop_index");
			Property prop = new Property(prop_type, prop_ssid, prop_dispname, prop_mode, poopRTY, propIDParam,
                    propValParam, idg);
			properties.put(prop.getSSID(), prop);
		}
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
	 * Checks if the product contains a property with the specified property type
	 * 
	 * @param ptype The property type to be checked
	 * @return <b>true</b> if the property exists, <b>false</b> otherwise
	 */
	public boolean containsPropertyType(PropertyType ptype) {
		Iterator<Property> props = properties.values().iterator();
		while(props.hasNext()) {
			Property prop = props.next();
			if(prop.getPropType().equals(ptype)) return true;
		}
		return false;
	}
	
//	/**
//	 * Assigns the properties for this product. Properties <b><i>MUST</i></b> be assigned right after
//	 * product object instantiation.
//	 * @param properties
//	 */
//	public void setProperties(B_Property[] properties) {
//		for(int i = 0; i < properties.length; i++) {
//			B_Property prop = properties[i];
//			this.properties.put(prop.getSSID(), prop);
//		}
//	}
	
	public void addProperty(Property prop) {
		properties.put(prop.getSSID(), prop);
	}
	
	public Property getProperty(String ssid) {
		return properties.get(ssid);
	}
	
	/**
	 * Returns a copy of the properties that this product contains. This method is called by component objects to retrieve
	 * their properties in instantiation.
	 *
	 * @return a HashMap containing the copies of each property, the key being the property's CPL SSID and the value being
	 * 		the property object itself
	 */
	public HashMap<String, Property> getProperties() {
	    return properties;
//		HashMap<String, Property> props = new HashMap<String, Property>(properties.size());
//		Iterator<Property> ps = properties.values().iterator();
//
//		while(ps.hasNext()) {
//			Property p = ps.next().clone();
//			props.put(p.getSSID(), p);
//		}
//		return props;
	}
	
	/**
	 * @return the sSID
	 */
	public String getSSID() {
		return SSID;
	}
	
	/**
	 * @param sSID the sSID to set
	 */
	public void setSSID(String sSID) {
		SSID = sSID;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

    public String getIconImg() {
        return iconImg;
    }

    public void setIconImg(String iconImg) {
        this.iconImg = iconImg;
    }

//	/**
//	 * @return the oH_icon
//	 */
//	public String getOHIcon() {
//		return OH_icon;
//	}
//
//	/**
//	 * @param oH_icon the oH_icon to set
//	 */
//	public void setOH_icon(String oH_icon) {
//		OH_icon = oH_icon;
//	}
}
