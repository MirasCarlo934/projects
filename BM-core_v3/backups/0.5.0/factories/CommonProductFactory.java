package components.products.factories;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Vector;

import components.products.AbstProduct;
import components.products.CommonProduct;
import components.properties.AbstProperty;
import components.properties.CommonProperty;
import components.properties.InnateProperty;
import components.properties.PropertyMode;
import components.properties.PropertyValueType;
import components.properties.StringProperty;
import components.properties.commons.AnalogHueProperty;
import components.properties.commons.AnalogProperty;
import components.properties.commons.DigitalProperty;
import components.properties.commons.PercentProperty;
import main.adaptors.DBAdaptor;
import main.adaptors.OHAdaptor;

public class CommonProductFactory extends AbstProductFactory {

	/*public CommonProductFactory(String logDomain, DBAdaptor dba, OHAdaptor oha) {
		super(logDomain, CommonProductFactory.class.getSimpleName(), dba, oha);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected CommonProduct createProductObject(String SSID, String name, String description, String OH_icon,
			HashMap<String, AbstProperty> properties) {
		CommonProduct product = new CommonProduct(SSID, name, description, OH_icon, properties);
		return product;
	}

	@Override
	protected HashMap<String, AbstProperty> retrieveProductProperties(ResultSet rs) throws 
			SQLException, IllegalArgumentException {
		HashMap<String, AbstProperty> properties = new HashMap<String, AbstProperty>(10);
		while(rs.next()) {
			String prop_type = rs.getString("prop_type");
			String prop_dispname = rs.getString("prop_dispname");
			String prop_sysname = rs.getString("prop_sysname");
			String prop_mode = rs.getString("prop_mode");
			String pval_type = rs.getString("prop_val_type");
			//int prop_min = rs.getInt("prop_min");
			//int prop_max = rs.getInt("prop_max");
			String prop_ssid = rs.getString("prop_index");
			//DBComponentAdaptor dba = new DBComponentAdaptor(comID + "_" + prop_index, dbe);
			AbstProperty prop;
			PropertyValueType pvt = PropertyValueType.parsePropValTypeFromString(pval_type);
			if(prop_type.equals("0000")) { //this is an innate property
				prop = new InnateProperty(prop_type, prop_ssid, prop_sysname, prop_dispname, 
						PropertyValueType.parsePropValTypeFromString(pval_type), dba, oha);
			} else if(pvt.equals(PropertyValueType.string)) { //this is a string property
				prop = new StringProperty(prop_type, prop_ssid, prop_sysname, prop_dispname, 
						PropertyMode.parseModeFromString(prop_mode), dba, oha);
			} else if(pvt.equals(PropertyValueType.analog)){ //this is a common property
				prop = new AnalogProperty(prop_type, prop_ssid, prop_sysname, prop_dispname, 
						PropertyMode.parseModeFromString(prop_mode), 
						PropertyValueType.parsePropValTypeFromString(pval_type), dba, oha);
			} else if(pvt.equals(PropertyValueType.analogHue)) {
				prop = new AnalogHueProperty(prop_type, prop_ssid, prop_sysname, prop_dispname, 
						PropertyMode.parseModeFromString(prop_mode), 
						PropertyValueType.parsePropValTypeFromString(pval_type), dba, oha);
			} else if(pvt.equals(PropertyValueType.percent)) {
				prop = new PercentProperty(prop_type, prop_ssid, prop_sysname, prop_dispname, 
						PropertyMode.parseModeFromString(prop_mode), 
						PropertyValueType.parsePropValTypeFromString(pval_type), dba, oha);
			} else if(pvt.equals(PropertyValueType.digital)) {
				prop = new DigitalProperty(prop_type, prop_ssid, prop_sysname, prop_dispname, 
						PropertyMode.parseModeFromString(prop_mode), 
						PropertyValueType.parsePropValTypeFromString(pval_type), dba, oha);
			} else { //PropertyValueType of property is null (invalid)
				throw new IllegalArgumentException("Invalid property value type for property type "
						+ prop_type + "!");
			}
			properties.put(prop.getSSID(), prop);
		}
		return properties;
	}*/

}
