package bm.main.repositories;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import bm.comms.mqtt.MQTTListener;
import bm.context.adaptors.AbstAdaptor;
import bm.context.adaptors.OHAdaptor;
import bm.context.adaptors.exceptions.AdaptorException;
import bm.context.devices.Device;
import bm.context.devices.factories.AbstDeviceFactory;
import bm.context.devices.products.Product;
import bm.context.properties.AbstProperty;
import bm.context.properties.Property;
import bm.jeep.device.ResError;
import bm.main.BusinessMachine;
import bm.main.engines.AbstEngine;
import bm.main.engines.DBEngine;
import bm.main.engines.exceptions.EngineException;
import bm.main.engines.requests.DBEngine.RawDBEReq;
import bm.main.engines.requests.DBEngine.SelectDBEReq;
import bm.main.engines.requests.DBEngine.UpdateDBEReq;
import bm.main.interfaces.Initializable;
import bm.tools.IDGenerator;

public class DeviceRepository /*extends AbstRepository*/ implements Initializable {
	private Logger LOG;
	private HashMap<String, String> rooms = new HashMap<String, String>(1,1);
	private HashMap<String, Device> devices = new HashMap<String, Device>(1);
	private HashMap<String, String> registeredMACs = new HashMap<String, String>(1,1); //registered MAC and corresponding SSID
	private IDGenerator idg = new IDGenerator();
	private DBEngine mainDBE;
	private String deviceQuery;
	private ProductRepository pr;
	private RoomRepository rr;

	public DeviceRepository(String logDomain, DBEngine dbm, String deviceQuery, 
			ProductRepository pr, RoomRepository rr) {
		this.LOG = Logger.getLogger(logDomain + "." + DeviceRepository.class.getSimpleName());
		this.deviceQuery = deviceQuery;
		this.mainDBE = dbm;
		this.pr = pr;
		this.rr = rr;
	}
	
	@Override
	public void initialize() throws Exception {
		retrieveDevices();
	}
	
	/**
	 * Retrieves all components from DB.
	 */
	public void retrieveDevices() {
		try {
			LOG.info("Retrieving components from DB...");
			RawDBEReq dber1 = new RawDBEReq(idg.generateMixedCharID(10), mainDBE, deviceQuery);
			Object o;
			try {
				o = mainDBE.putRequest(dber1, Thread.currentThread(), true);
			} catch (EngineException e) {
				LOG.error("Error in retrieving components from DB!", e);
				return;
			}
			ResultSet devs_rs = (ResultSet) o;
			
			while(devs_rs.next()) {
				String SSID = devs_rs.getString("SSID");
				String topic = devs_rs.getString("topic");
				String MAC = devs_rs.getString("MAC");
				String room = devs_rs.getString("room");
				String prod_id = devs_rs.getString("functn");
				String name = devs_rs.getString("name");
				boolean active = devs_rs.getBoolean("ACTIVE");
				int index = devs_rs.getInt("INDEX");
				
				String prop_id = devs_rs.getString("prop_id");
				Object prop_val = devs_rs.getString("prop_value");
				if(devices.containsKey(SSID)) {
					LOG.debug("Setting property: " + prop_id + " of component: " + SSID + 
							" with value: " + prop_val);
					getDevice(SSID).getProperty(prop_id).setValue(prop_val);
				} else {
					LOG.debug("Adding device " + SSID + " (" + name + ") to repository!");
					Device d = pr.getProduct(prod_id).createDevice(SSID, MAC, name, topic, 
							rr.getRoom(room), active, index);
					LOG.debug("Setting property: " + prop_id + " of device: " + SSID + 
							" with value: " + prop_val);
					d.getProperty(prop_id).setValue(prop_val);
					devices.put(SSID, d);
					registeredMACs.put(MAC, SSID);
				}
			}
			devs_rs.close();
			LOG.info("DeviceRepository population done!");
		} catch (SQLException e) {
			LOG.error("Cannot populate DeviceRepository!", e);
		}
	}
	
	public void addDevice(Device device) {
		devices.put(device.getSSID(), device);
		registeredMACs.put(device.getMAC(), device.getSSID());
	}
	
	/**
	 * Removes and returns the component from the repository
	 * 
	 * @param identifier The SSID or MAC address of the component to be removed
	 * @return The component that was removed, <i>null</i> if the SSID specified does not pertain to an 
	 * 		existing component
	 */
	public Device removeDevice(String identifier) {
		if(registeredMACs.containsKey(identifier))
			identifier = registeredMACs.get(identifier);
		Device c = devices.remove(identifier);
		registeredMACs.remove(c.getMAC());
		return c;
	}
	
	/**
	 * Returns the Component with the specified SSID or MAC
	 * @param s The SSID or MAC to specify
	 * @return the Component with the specified SSID or MAC, <i>null</i> if nonexistent
	 */
	public Device getDevice(String s) {
		if(devices.containsKey(s)) {
			return devices.get(s);
		} else if (registeredMACs.containsKey(s)) {
			return devices.get(registeredMACs.get(s));
		} else {
			return null;
		}
	}
	
	/**
	 * Returns all the devices registered in this ComponentRepository
	 * @return the array containing all Components
	 */
	public Device[] getAllDevices() {
		return devices.values().toArray(new Device[devices.size()]);
	}
	
	/**
	 * Returns all devices that are instances of the specified product SSID
	 * 
	 * @param prodSSID The product SSID to be specified
	 * @return the array containing the devices
	 */
	public Device[] getAllDevicesUnderProductSSID(String prodSSID) {
		Device[] devs = getAllDevices();
		Vector<Device> d = new Vector<Device>(1,1);
		
		for(int i = 0; i < devs.length; i++) {
			Device dev = devs[i];
			if(dev.getProduct().getSSID().equals(prodSSID)) {
				d.add(dev);
			}
		}
		
		return d.toArray(new Device[d.size()]);
	}
	
	/**
	 * Returns all existing rooms in this ComponentRepository
	 * @return the HashMap containing all room SSID and room names
	 */
	public HashMap<String, String> getAllRooms() {
		return rooms;
	}
	
	/**
	 * Checks if the SSID or the MAC specified already exists in this ComponentRepository
	 * @param str The SSID or MAC to be tested
	 * @return
	 */
	public boolean containsComponent(String str) {
		if(devices.containsKey(str) || registeredMACs.containsKey(str)) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Checks if a component with the specified name exists in the repository.
	 * 
	 * @param name The name of the component to be checked
	 * @return
	 */
	public boolean containsComponentWithName(String name) {
		Iterator<Device> coms = devices.values().iterator();
		while(coms.hasNext()) {
			Device c = coms.next();
			if(c.getName().equals(name)) {
				return true;
			}
		}
		return false;
	}
}