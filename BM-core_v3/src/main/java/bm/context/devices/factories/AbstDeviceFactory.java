package bm.context.devices.factories;

import org.apache.log4j.Logger;

import bm.context.adaptors.AbstAdaptor;
import bm.context.adaptors.DBAdaptor;
import bm.context.adaptors.OHAdaptor;
import bm.context.devices.Device;
import bm.context.devices.products.Product;
import bm.context.rooms.Room;

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
