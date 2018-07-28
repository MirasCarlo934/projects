//package bm.smarthome.devices.factories;
//
//import bm.smarthome.adaptors.AbstAdaptor;
//import bm.smarthome.adaptors.DBAdaptor;
//import bm.smarthome.adaptors.OHAdaptor;
//import bm.smarthome.devices.Device;
//import bm.smarthome.devices.TimerDevice;
//import bm.smarthome.devices.products.Product;
//import bm.smarthome.rooms.Room;
//
//public class TimerDeviceFactory extends AbstDeviceFactory {
//
//	public TimerDeviceFactory(String logDomain, String prodSSID) {
//		super(logDomain, TimerDeviceFactory.class.getSimpleName(), prodSSID);
//	}
//
////	@Override
////	public Device createDevice(String SSID, String MAC, String name, String topic, Room room, boolean active,
////			Product product, DBAdaptor dba, OHAdaptor oha, AbstAdaptor[] adaptors) {
////		LOG.debug("Creating new Timer device [SSID=" + SSID + ", MAC=" + MAC + ", name=" + name + "]...");
////		return new TimerDevice(SSID, MAC, name, topic, room, active, product, dba, oha, adaptors);
////	}
//}
