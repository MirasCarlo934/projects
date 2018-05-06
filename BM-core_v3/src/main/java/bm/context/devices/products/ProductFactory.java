package bm.context.devices.products;

import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.log4j.Logger;

import bm.context.adaptors.AbstAdaptor;
import bm.context.adaptors.DBAdaptor;
import bm.context.adaptors.OHAdaptor;
import bm.context.properties.Property;
import bm.context.properties.PropertyType;
import bm.tools.IDGenerator;

public class ProductFactory {
	protected Logger LOG;
	protected String logDomain;
	protected DBAdaptor dba;
	protected OHAdaptor oha;
	protected AbstAdaptor[] additionalAdaptors;
	protected static HashMap<String, PropertyType> propertyTypes = new HashMap<String, PropertyType>(6);
	
	private String poopRTY;
	protected String propIDParam;
	protected String propValParam;
	private IDGenerator idg;

	public ProductFactory(String logDomain, DBAdaptor dba, OHAdaptor oha, AbstAdaptor[] additionalAdaptors, 
			String poopRTY, String propIDParam, String propValParam, IDGenerator idGenerator) {
		this.LOG = Logger.getLogger(logDomain + "." + ProductFactory.class.getSimpleName());
		this.logDomain = logDomain;
		this.dba = dba;
		this.oha = oha;
		this.additionalAdaptors = additionalAdaptors;
		this.poopRTY = poopRTY;
		this.propIDParam = propIDParam;
		this.propValParam = propValParam;
		this.idg = idGenerator;
	}

	public AbstProduct createProductObject(String SSID, String name, String description, String OH_icon, 
			ResultSet productsRS) {
		return new Product(logDomain, SSID, name, description, OH_icon, productsRS, dba, oha, additionalAdaptors, 
				propertyTypes, true, poopRTY, propIDParam, propValParam, idg);
	}
	
	public AbstProduct createProductObject(String SSID, String name, String description, String OH_icon, 
			Property[] properties) {
		return new Product(logDomain, SSID, name, description, OH_icon, properties, dba, oha, additionalAdaptors, 
				poopRTY, propIDParam, propValParam, idg);
	}
	
	public AbstProduct createProductObject(String SSID, String name, String description, String OH_icon) {
		return new Product(logDomain, SSID, name, description, OH_icon, dba, oha, additionalAdaptors, poopRTY, 
				propIDParam, propValParam, idg);
	}

	public void setPropertyTypes(HashMap<String, PropertyType> propertyTypes) {
		ProductFactory.propertyTypes = propertyTypes;
	}
}
