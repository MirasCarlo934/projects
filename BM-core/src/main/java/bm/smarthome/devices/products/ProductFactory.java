package bm.smarthome.devices.products;

import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.log4j.Logger;

import bm.smarthome.adaptors.AbstAdaptor;
import bm.smarthome.adaptors.DBAdaptor;
import bm.smarthome.adaptors.OHAdaptor;
import bm.smarthome.properties.Property;
import bm.smarthome.properties.PropertyType;

public class ProductFactory {
	protected Logger LOG;
	protected String logDomain;
	protected DBAdaptor dba;
	protected OHAdaptor oha;
	protected AbstAdaptor[] additionalAdaptors;
	protected static HashMap<String, PropertyType> propertyTypes = new HashMap<String, PropertyType>(6);

	public ProductFactory(String logDomain, DBAdaptor dba, OHAdaptor oha, AbstAdaptor[] additionalAdaptors) {
		this.LOG = Logger.getLogger(logDomain + "." + ProductFactory.class.getSimpleName());
		this.logDomain = logDomain;
		this.dba = dba;
		this.oha = oha;
		this.additionalAdaptors = additionalAdaptors;
	}

	public AbstProduct createProductObject(String SSID, String name, String description, String OH_icon, 
			ResultSet productsRS) {
//		Iterator<PropertyType> test = propertyTypes.values().iterator();
//		while(test.hasNext()) {
//			LOG.fatal(test.next().getSSID());
//		}
		return new Product(logDomain, SSID, name, description, OH_icon, productsRS, dba, oha, additionalAdaptors, 
				propertyTypes, true);
	}
	
	public AbstProduct createProductObject(String SSID, String name, String description, String OH_icon, 
			Property[] properties) {
//		Iterator<PropertyType> test = propertyTypes.values().iterator();
//		while(test.hasNext()) {
//			LOG.fatal(test.next().getSSID());
//		}
		return new Product(logDomain, SSID, name, description, OH_icon, properties, dba, oha, additionalAdaptors);
	}
	
	public AbstProduct createProductObject(String SSID, String name, String description, String OH_icon) {
//		Iterator<PropertyType> test = propertyTypes.values().iterator();
//		while(test.hasNext()) {
//			LOG.fatal(test.next().getSSID());
//		}
		return new Product(logDomain, SSID, name, description, OH_icon, dba, oha, additionalAdaptors);
	}
	
//	public Product createProductObject(String SSID, String name, String description, String OH_icon, 
//			Property[] properties) {
//		return new Product(logDomain, SSID, name, description, OH_icon, properties, dba, oha, additionalAdaptors);
//	}

	public void setPropertyTypes(HashMap<String, PropertyType> propertyTypes) {
		ProductFactory.propertyTypes = propertyTypes;
	}
}
