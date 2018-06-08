package main.repositories;

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

import components.AbstComponent;
import components.bindings.Binding;
import components.factories.*;
import components.products.AbstProduct;
import components.properties.AbstProperty;
import json.RRP.ResError;
import main.BusinessMachine;
import main.engines.AbstEngine;
import main.engines.DBEngine;
import main.engines.requests.DBEngine.RawDBEReq;
import main.engines.requests.DBEngine.SelectDBEReq;
import main.engines.requests.DBEngine.UpdateDBEReq;
import mqtt.MQTTListener;
import tools.IDGenerator;

public class ComponentRepository extends AbstRepository {
	//private static Logger LOG;
	@Autowired
	private MQTTHandler mh;
	private HashMap<String, String> rooms = new HashMap<String, String>(1,1);
	private HashMap<String, AbstComponent> components = new HashMap<String, AbstComponent>(1);
	private Vector<Binding> bindings = new Vector<Binding>(1,1);
	private HashMap<String, String> registeredMACs = new HashMap<String, String>(1,1); //registered MAC and corresponding SSID
	private IDGenerator idg = new IDGenerator();
	private DBEngine dbe;
	//private Catalog catalog;
	private String deviceQuery;
	private String productQuery;
	private String roomsTable;
	private String bindingsTable;

	public ComponentRepository(String logDomain, DBEngine dbm, String deviceQuery, 
			String productQuery, String roomsTable, String bindingsTable) {
		super(logDomain, ComponentRepository.class.getSimpleName());
		this.deviceQuery = deviceQuery;
		this.productQuery = productQuery;
		this.roomsTable = roomsTable;
		this.bindingsTable = bindingsTable;
		this.dbe = dbm;
	}
	
	@Override
	public void initialize() {
		retrieveComponents();
	}
	
	/**
	 * Retrieves all components from DB.
	 */
	public void retrieveComponents() {
		ApplicationContext appContext = BusinessMachine.getApplicationContext();
		AbstComponentFactory cf;
		AbstProductFactory pf;
		try {
			LOG.info("Populating Components...");
			RawDBEReq dber1 = new RawDBEReq(idg.generateMixedCharID(10), deviceQuery);
			Object o = AbstEngine.forwardRequest(dber1, dbe, LOG, Thread.currentThread());
			if(o.getClass().equals(ResError.class)) {
				ResError error = (ResError) o;
				LOG.error(error.message);
				return;
			}
			ResultSet coms_rs = (ResultSet) o;
			
			while(coms_rs.next()) {
				String SSID = coms_rs.getString("SSID");
				String topic = coms_rs.getString("topic");
				String MAC = coms_rs.getString("MAC");
				String room = coms_rs.getString("room");
				String prod_id = coms_rs.getString("functn");
				String name = coms_rs.getString("name");
				boolean active = coms_rs.getBoolean("ACTIVE");
				
				String prop_id = coms_rs.getString("prop_id");
				Object prop_val = coms_rs.getString("prop_value");
				
				RawDBEReq dber2 = new RawDBEReq(idg.generateMixedCharID(10), productQuery + " and "
						+ "cc.ssid = '" + prod_id + "'");
				Object o2 = AbstEngine.forwardRequest(dber2, dbe, LOG, Thread.currentThread());
				if(o2.getClass().equals(ResError.class)) {
					ResError error = (ResError) o2;
					LOG.error(error.message);
					coms_rs.close();
					return;
				}
				ResultSet prod_rs = (ResultSet) o2;
				if(!components.containsKey(SSID)) { //true if devices does NOT contain this device
					prod_rs.beforeFirst();
					try {
						pf = (AbstProductFactory) appContext.getBean(prod_id);
					} catch(NoSuchBeanDefinitionException e) {
						pf = (AbstProductFactory) appContext.getBean("CommonProductFactory");
					}
					AbstProduct product = pf.createProduct(prod_rs);
					try {
						cf = (AbstComponentFactory) appContext.getBean(product.getSSID());
					} catch(NoSuchBeanDefinitionException e) {
						cf = (AbstComponentFactory) appContext.getBean("CommonComponentFactory");
					}
					LOG.debug("Adding component " + SSID + " into ComponentRepository...");
					AbstComponent com = cf.createComponent(SSID, MAC, name, topic, room, active, 
							product);
					addComponent(com);
					LOG.debug("Component " + SSID + " added!");
				}
				
				//populates properties of component with persisted values
				LOG.debug("Setting property: " + prop_id + " of device: " + SSID + " with value: " + prop_val);
				AbstComponent com = components.get(SSID);
				com.getProperty(prop_id).setValue(prop_val);
				prod_rs.close();
			}
			coms_rs.close();
			LOG.info("Devices population done!");
		} catch (SQLException e) {
			LOG.error("Cannot populate ComponentRepository!", e);
		}
	}
	
	/**
	 * Retrieves all rooms from DB.
	 */
	public void retrieveRooms() {
		LOG.info("Retrieving rooms from DB...");
		SelectDBEReq dber1 = new SelectDBEReq(idg.generateMixedCharID(10), roomsTable);
		dbe.processRequest(dber1, Thread.currentThread());
		try {
			synchronized (Thread.currentThread()){Thread.currentThread().wait();}
		} catch (InterruptedException e) {
			LOG.error("Cannot stop thread!", e);
			e.printStackTrace();
		}
		Object o = dbe.getResponse(dber1.getId());
		if(o.getClass().equals(ResError.class)) {
			ResError e = (ResError) o;
			LOG.error("Cannot retrieve rooms from DB!");
			LOG.error("Error message: " + e.message);
		} else {
			ResultSet rs1 = (ResultSet) o;
			try {
				while(rs1.next()) {
					rooms.put(rs1.getString("ssid"), rs1.getString("name"));
				}
				rs1.close();
				LOG.debug("Rooms retrieved!");
			} catch (SQLException e) {
				LOG.error("ResultSet error in retrieving rooms!", e);
				e.printStackTrace();
			}
		}
	}
	
	public void retrieveBindings() {
		LOG.info("Retrieving OH bindings from DB...");
		SelectDBEReq dber1 = new SelectDBEReq(idg.generateMixedCharID(10), bindingsTable);
		dbe.processRequest(dber1, Thread.currentThread());
		try {
			synchronized (Thread.currentThread()){Thread.currentThread().wait();}
		} catch (InterruptedException e) {
			LOG.error("Cannot stop thread!", e);
			e.printStackTrace();
		}
		Object o = dbe.getResponse(dber1.getId());
		if(o.getClass().equals(ResError.class)) {
			ResError e = (ResError) o;
			LOG.error("Cannot retrieve bindings from DB!");
			LOG.error("Error message: " + e.message);
		} else {
			ResultSet rs1 = (ResultSet) o;
			try {
				while(rs1.next()) {
					bindings.add(new Binding(rs1.getString("SSID"), rs1.getString("com_type"),
							rs1.getString("prop_id"), rs1.getString("binding")));
				}
				rs1.close();
				LOG.debug("Bindings retrieved!");
			} catch (SQLException e) {
				LOG.error("ResultSet error in retrieving bindings!", e);
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Creates a new device entry in BM and DB. Main method called upon by a register request
	 * 
	 * @param register The register request
	 * @return The new Device object, <b>null</b> if:<br>
	 * 		<ul>
	 * 			<li>Error in persistence process</li>
	 * 			<li>Invalid room</li>
	 * 		</ul>
	 */
	/*public Component createNewDevice(Register register) {
		LOG.info("Registering new device...");
		
		if(registeredMACs.containsKey(register.rid)) { //checks if Devices already contain this Device
			LOG.warn("Device: " + register.rid + " already registered!");
			return devices.get(registeredMACs.get(register.rid));
		}
		else {
			LOG.info("Creating new device...");
			String[] existingIDs = new String[0];
			devices.keySet().toArray(existingIDs);
			Component device = new Component(idgen.generateMixedCharID(4, existingIDs), register, register.getProduct(), true);
			LOG.info("Device: " + device.getSSID() + " created! ");
			
			try {
				LOG.info("Persisting new device to DB...");
				dbm.insertQuery(new String[]{device.getSSID(), device.getTopic(), device.getMAC(), device.getName(), 
						device.getRoom(), device.getProduct().getSSID(), String.valueOf(device.isActive())}, 
						"components");
				LOG.info("Device persisted!");
				
				/*=====================================================
				
				LOG.info("Persisting new device properties to DB...");
				B_Property[] properties = device.getProperties().values().toArray(new B_Property[0]);
				
				//gets all existing property SSIDs from DB
				ResultSet rs = dbm.selectQuery("SSID", "comp_properties");
				Vector<String> prop_SSIDs = new Vector<String>(1,1);
				while(rs.next()) {
					prop_SSIDs.add(rs.getString("SSID"));
				}
				
				for(int i = 0; i < properties.length; i++) {
					B_Property property = properties[i];
					String prop_ssid = idgen.generateMixedCharID(4, prop_SSIDs.toArray(new String[0]));
					dbm.insertQuery(new String[]{prop_ssid, device.getSSID(), property.getName(), 
							"0", property.getPropertyID()}, 
							"comp_properties");
					prop_SSIDs.add(prop_ssid);
				}
				LOG.info("New device properties persisted!");
				
				/*=====================================================
				
				LOG.info("Registration process complete! Device: " + device.getSSID() + " registered into BM.");
				addDevice(device);
				return device;
			} catch (SQLException e) {
				LOG.error("Cannot persist new device to DB!", e);
				return null;
			}
		}
	}*/
	
	public void changeDBProperty(String deviceID, String propID, int propValue) {
		AbstComponent device = components.get(deviceID);
		AbstProperty property = device.getProperty(propID);
		property.setValue(propValue);
		
		LOG.info("Updating property:" + propID + " of device:" + deviceID + " in DB...");
		HashMap<String, Object> args = new HashMap<String, Object>(1);
		args.put("CPL_SSID", propID);
		HashMap<String, Object> vals = new HashMap<String, Object>(1);
		vals.put("prop_value", String.valueOf(propValue));
		//dbm.updateQuery("comp_properties", args, vals);
		UpdateDBEReq dber1 = new UpdateDBEReq(idg.generateMixedCharID(10), "comp_properties", 
				args, vals);
		dbe.processRequest(dber1, Thread.currentThread());
		LOG.info("Property updated!");
		/*try {
			LOG.info("Updating property:" + propID + " of device:" + deviceID + " in DB...");
			HashMap<String, Object> args = new HashMap<String, Object>(1);
			args.put("CPL_SSID", propID);
			HashMap<String, Object> vals = new HashMap<String, Object>(1);
			vals.put("prop_value", String.valueOf(propValue));
			//dbm.updateQuery("comp_properties", args, vals);
			dbm.forwardRequest(new UpdateDBEReq(idg.generateMixedCharID(10), "comp_properties", 
					args, vals));
			LOG.info("B_Property updated!");
		} catch (SQLException e) {
			LOG.error("Cannot update property in DB!", e);
		}*/
	}
	
	public void addComponent(AbstComponent device) {
		components.put(device.getSSID(), device);
		registeredMACs.put(device.getMAC(), device.getSSID());
	}
	
	/**
	 * Removes and returns the component from the repository
	 * 
	 * @param SSID The SSID of the component to be removed
	 * @return The component that was removed, <i>null</i> if the SSID specified does not pertain to an 
	 * 		existing component
	 */
	public AbstComponent removeComponent(String SSID) {
		AbstComponent c = components.remove(SSID);
		registeredMACs.remove(c.getMAC());
		return c;
	}
	
	/**
	 * Returns the Component with the specified SSID or MAC
	 * @param s The SSID or MAC to specify
	 * @return the Component with the specified SSID or MAC, <i>null</i> if nonexistent
	 */
	public AbstComponent getComponent(String s) {
		if(components.containsKey(s)) {
			return components.get(s);
		} else if (registeredMACs.containsKey(s)) {
			return components.get(registeredMACs.get(s));
		} else {
			return null;
		}
	}
	
	/**
	 * Returns all the Components registered in this ComponentRepository
	 * @return the array containing all Components
	 */
	public AbstComponent[] getAllComponents() {
		return components.values().toArray(new AbstComponent[components.size()]);
	}
	
	/**
	 * Returns all existing rooms in this ComponentRepository
	 * @return the HashMap containing all room SSID and room names
	 */
	public HashMap<String, String> getAllRooms() {
		return rooms;
	}
	
	/**
	 * Returns all retrieved Bindings from DB
	 * @return the array containing all Bindings
	 */
	public Binding[] getAllBindings() {
		return bindings.toArray(new Binding[bindings.size()]);
	}
	
	/**
	 * Checks if the SSID or the MAC specified already exists in this ComponentRepository
	 * @param str The SSID or MAC to be tested
	 * @return
	 */
	public boolean containsComponent(String str) {
		if(components.containsKey(str) || registeredMACs.containsKey(str)) {
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
		Iterator<AbstComponent> coms = components.values().iterator();
		while(coms.hasNext()) {
			AbstComponent c = coms.next();
			if(c.getName().equals(name)) {
				return true;
			}
		}
		return false;
	}
}