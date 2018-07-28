package bm.smarthome.devices.products.special.timer;

import java.sql.ResultSet;
import java.util.Iterator;

import bm.smarthome.adaptors.AbstAdaptor;
import bm.smarthome.adaptors.DBAdaptor;
import bm.smarthome.adaptors.OHAdaptor;
import bm.smarthome.devices.products.AbstProduct;
import bm.smarthome.devices.products.Product;
import bm.smarthome.devices.products.ProductFactory;
import bm.smarthome.properties.Property;
import bm.smarthome.properties.PropertyType;

public class TimerProductFactory extends ProductFactory {
	private static String timerProdSSID;
	private static String activePropSSID;
	private static String timePropSSID;

	public TimerProductFactory(String logDomain, DBAdaptor dba, OHAdaptor oha, AbstAdaptor[] additionalAdaptors, 
			String timerProdSSID, String activePropSSID, String timePropSSID) {
		super(logDomain, dba, oha, additionalAdaptors);
		TimerProductFactory.timerProdSSID = timerProdSSID;
		TimerProductFactory.activePropSSID = activePropSSID;
		TimerProductFactory.timePropSSID = timePropSSID;
	}
	
	public String getTimerProdSSID() {
		return timerProdSSID;
	}
	
	@Override
	public AbstProduct createProductObject(String SSID, String name, String description, String OH_icon, 
			ResultSet productsRS) {
//		Iterator<PropertyType> test = propertyTypes.values().iterator();
//		while(test.hasNext()) {
//			LOG.fatal(test.next().getSSID());
//		}
		return new TimerProduct(logDomain, SSID, name, description, OH_icon, productsRS, dba, oha, additionalAdaptors, 
				propertyTypes, activePropSSID, timePropSSID);
	}
	
	@Override
	public AbstProduct createProductObject(String SSID, String name, String description, String OH_icon, 
			Property[] properties) {
//		Iterator<PropertyType> test = propertyTypes.values().iterator();
//		while(test.hasNext()) {
//			LOG.fatal(test.next().getSSID());
//		}
		return new TimerProduct(logDomain, SSID, name, description, OH_icon, properties, dba, oha, 
				additionalAdaptors, activePropSSID, timePropSSID);
	}
	
	@Override
	public AbstProduct createProductObject(String SSID, String name, String description, String OH_icon) {
//		Iterator<PropertyType> test = propertyTypes.values().iterator();
//		while(test.hasNext()) {
//			LOG.fatal(test.next().getSSID());
//		}
		return new TimerProduct(logDomain, SSID, name, description, OH_icon, dba, oha, 
				additionalAdaptors, activePropSSID, timePropSSID);
	}
}