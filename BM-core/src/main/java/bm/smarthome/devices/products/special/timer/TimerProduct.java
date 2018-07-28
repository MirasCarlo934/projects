package bm.smarthome.devices.products.special.timer;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

import bm.smarthome.adaptors.AbstAdaptor;
import bm.smarthome.adaptors.DBAdaptor;
import bm.smarthome.adaptors.OHAdaptor;
import bm.smarthome.devices.Device;
import bm.smarthome.devices.TimerDevice;
import bm.smarthome.devices.products.AbstProduct;
import bm.smarthome.devices.products.Product;
import bm.smarthome.properties.AbstProperty;
import bm.smarthome.properties.Property;
import bm.smarthome.properties.PropertyMode;
import bm.smarthome.properties.PropertyType;
import bm.smarthome.properties.bindings.Binding;
import bm.smarthome.properties.special.timer.TimerActiveProperty;
import bm.smarthome.properties.special.timer.TimerTimeProperty;
import bm.smarthome.rooms.Room;

public class TimerProduct extends AbstProduct {
	private static String activePropSSID;
	private static String timePropSSID;

	public TimerProduct(String mainLogDomain, String SSID, String name, String description, String OH_icon,
			ResultSet productsRS, DBAdaptor dba, OHAdaptor oha, AbstAdaptor[] additionalAdaptors,
			HashMap<String, PropertyType> propertyTypes, String activePropSSID, String timePropSSID) {
		super(mainLogDomain, SSID, name, description, OH_icon, productsRS, dba, oha, additionalAdaptors, 
				propertyTypes, false);
		TimerProduct.activePropSSID = activePropSSID;
		TimerProduct.timePropSSID = timePropSSID;
		try {
			productsRS.beforeFirst();
			retrieveProperties(productsRS);
		} catch (SQLException e) {
			LOG.error("Cannot retrieve properties!", e);
		}
	}

	public TimerProduct(String mainLogDomain, String SSID, String name, String description, String OH_icon,
			Property[] properties, DBAdaptor dba, OHAdaptor oha, AbstAdaptor[] additionalAdaptors, 
			String activePropSSID, String timePropSSID) {
		super(mainLogDomain, SSID, name, description, OH_icon, properties, dba, oha, additionalAdaptors);
		TimerProduct.activePropSSID = activePropSSID;
		TimerProduct.timePropSSID = timePropSSID;
	}

	public TimerProduct(String mainLogDomain, String SSID, String name, String description, String OH_icon,
			DBAdaptor dba, OHAdaptor oha, AbstAdaptor[] additionalAdaptors, 
			String activePropSSID, String timePropSSID) {
		super(mainLogDomain, SSID, name, description, OH_icon, dba, oha, additionalAdaptors);
		TimerProduct.activePropSSID = activePropSSID;
		TimerProduct.timePropSSID = timePropSSID;
	}
	
	@Override
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
			String prop_binding = rs.getString("prop_binding");
			Binding binding = Binding.parseBinding(prop_binding);
			AbstProperty prop;
			if(prop_ssid.equals(activePropSSID)) {
				prop = new TimerActiveProperty(prop_type, prop_ssid, prop_dispname, prop_mode, dba, oha, 
						additionalAdaptors, binding);
			} else if(prop_ssid.equals(timePropSSID)) {
				prop = new TimerTimeProperty(prop_type, prop_ssid, prop_dispname, prop_mode, dba, oha, 
						additionalAdaptors, binding);
			} else {
				prop = new Property(prop_type, prop_ssid, prop_dispname, prop_mode, dba, oha, 
						additionalAdaptors, binding);
			}
			properties.put(prop.getSSID(), prop);
		}
	}
	
	@Override
	public Device createDevice(String comID, String MAC, String name, String topic, 
			Room room, boolean active) throws IllegalArgumentException {
		if(properties.isEmpty())
			throw new IllegalArgumentException("Properties not yet retrieved from DB!");
		else {
			TimerDevice t = new TimerDevice(comID, MAC, name, topic, room, active, this, dba, oha, 
					additionalAdaptors, timePropSSID, activePropSSID);
			return t;
		}
	}
}
