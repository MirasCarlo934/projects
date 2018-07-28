package bm.smarthome.devices.factories;

import org.apache.log4j.Logger;

import bm.smarthome.adaptors.AbstAdaptor;
import bm.smarthome.adaptors.DBAdaptor;
import bm.smarthome.adaptors.OHAdaptor;
import bm.smarthome.devices.Device;
import bm.smarthome.devices.products.Product;
import bm.smarthome.rooms.Room;

public abstract class AbstDeviceFactory {
	protected Logger LOG;
	protected String prodSSID;

	public AbstDeviceFactory(String logDomain, String factoryName, String prodSSID) {
		LOG = Logger.getLogger(logDomain + "." + factoryName);
		this.prodSSID = prodSSID;
	}
	
	public abstract Device createDevice(String SSID, String MAC, String name, String topic, Room room, boolean active, 
			Product product, DBAdaptor dba, OHAdaptor oha, AbstAdaptor[] adaptors);

	public String getProdSSID() {
		return prodSSID;
	}
}
