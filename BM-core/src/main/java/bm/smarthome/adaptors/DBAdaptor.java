package bm.smarthome.adaptors;

import java.util.HashMap;

import org.apache.log4j.Logger;

import bm.jeep.ResError;
import bm.main.engines.AbstEngine;
import bm.main.engines.DBEngine;
import bm.main.engines.exceptions.EngineException;
import bm.main.engines.requests.DBEngine.DeleteDBEReq;
import bm.main.engines.requests.DBEngine.InsertDBEReq;
import bm.main.engines.requests.DBEngine.UpdateDBEReq;
import bm.smarthome.adaptors.exceptions.AdaptorException;
import bm.smarthome.devices.Device;
import bm.smarthome.properties.AbstProperty;
import bm.smarthome.properties.Property;
import bm.smarthome.properties.bindings.Binding;
import bm.smarthome.rooms.Room;
import bm.tools.IDGenerator;

public class DBAdaptor extends AbstAdaptor {
	private IDGenerator idg = new IDGenerator();
	private DBEngine dbe;
	private String devsTable;
	private String propsTable;
	private String roomsTable;

	public DBAdaptor(DBEngine dbe, String comsTable, String propsTable, String roomsTable) {
		super(dbe.getLogDomain(), DBAdaptor.class.getSimpleName(), "database");
		this.dbe = dbe;
		this.devsTable = comsTable;
		this.propsTable = propsTable;
		this.roomsTable = roomsTable;
	}
	
	@Override
	public void persistDevice(Device d, boolean waitUntilPersisted) throws AdaptorException {
		LOG.trace("Persisting component to DB...");
		HashMap<String, Object> valuesCom = new HashMap<String, Object>(7,1);
		InsertDBEReq insertCom;
		valuesCom.put("SSID", d.getSSID());
		valuesCom.put("topic", d.getTopic());
		valuesCom.put("mac", d.getMAC());
		valuesCom.put("name", d.getName());
		valuesCom.put("room", d.getRoom().getSSID());
		valuesCom.put("functn", d.getProduct().getSSID());
		valuesCom.put("active", d.isActive());
		valuesCom.put("index", d.getIndex());
		insertCom = new InsertDBEReq(idg.generateMixedCharID(10), devsTable, valuesCom);
		
		//inserts component to DB
		try {
			dbe.forwardRequest(insertCom, Thread.currentThread(), waitUntilPersisted);
		} catch (EngineException e1) {
//			LOG.error("Error inserting component to DB!", e);
			AdaptorException e = new AdaptorException("Error inserting component to DB!", e1);
			throw e;
		}
	}
	
	@Override
	public void updateDevice(Device d, boolean waitUntilUpdated) throws AdaptorException {
		LOG.trace("Updating component in DB...");
		HashMap<String, Object> args = new HashMap<String, Object>(1, 1);
		HashMap<String, Object> values = new HashMap<String, Object>(2, 1);
		args.put("SSID", d.getSSID());
		values.put("name", d.getName());
		values.put("active", d.isActive());
		values.put("index", d.getIndex());
		if(d.getRoom() != null)
			values.put("room", d.getRoom().getSSID());
		else 
			values.put("room", null);
		
		UpdateDBEReq update = new UpdateDBEReq(idg.generateERQSRequestID(), devsTable, values, args);
		try {
			dbe.forwardRequest(update, Thread.currentThread(), waitUntilUpdated);
			LOG.trace("Update successful!");
		} catch (EngineException e) {
			throw new AdaptorException("Error updating component in DB!", e);
		}
	}
	
	public void updateDeviceState(Device d, boolean waitUntilUpdated) throws AdaptorException {
		LOG.trace("Updating device state in DB...");
		Thread t = Thread.currentThread();
		IDGenerator idg = new IDGenerator();
		HashMap<String, Object> vals = new HashMap<String, Object>(1, 1);
		HashMap<String, Object> args = new HashMap<String, Object>(1, 1);
		vals.put("active", d.isActive());
		args.put("SSID", d.getSSID());
		
		//LOG.trace("Updating active state of component to " + comsTable + " table!");
		UpdateDBEReq udber = new UpdateDBEReq(idg.generateMixedCharID(10), devsTable, vals, args);
		try {
			dbe.forwardRequest(udber, t, waitUntilUpdated);
		} catch (EngineException e) {
			AdaptorException a = new AdaptorException("Cannot update device state!", e);
			throw a;
		}
	}
	
	public void updateDeviceRoom(Device d, boolean waitUntilUpdated) throws AdaptorException {
		LOG.trace("Updating device room in DB...");
		Thread t = Thread.currentThread();
		IDGenerator idg = new IDGenerator();
		HashMap<String, Object> vals = new HashMap<String, Object>(1, 1);
		HashMap<String, Object> args = new HashMap<String, Object>(1, 1);
		vals.put("room", d.getRoom().getSSID());
		args.put("SSID", d.getSSID());
		
		UpdateDBEReq udber = new UpdateDBEReq(idg.generateMixedCharID(10), devsTable, vals, args);
		try {
			dbe.forwardRequest(udber, t, waitUntilUpdated);
		} catch (EngineException e) {
			AdaptorException a = new AdaptorException("Cannot update device room!", e);
			throw a;
		}
	}
	
	public void deleteDevice(Device c, boolean waitUntilDeleted) throws AdaptorException {
		LOG.trace("Deleting component from DB...");
		Thread t = Thread.currentThread();
		HashMap<String, Object> args1 = new HashMap<String, Object>(1,1);
		HashMap<String, Object> args2 = new HashMap<String, Object>(1,1);
		args1.put("ssid", c.getSSID());
		args2.put("com_id", c.getSSID());
		DeleteDBEReq delete1 = new DeleteDBEReq(idg.generateMixedCharID(10), devsTable, args1);
		DeleteDBEReq delete2 = new DeleteDBEReq(idg.generateMixedCharID(10), propsTable, args2);
		try {
			dbe.forwardRequest(delete1, t, waitUntilDeleted);
		} catch(EngineException e1) {
			AdaptorException a  = new AdaptorException("Error deleting records from DB!", e1);
			throw a;
		}
		
		LOG.trace("Deleting component properties from DB...");
		//LOG.trace("Deleting properties from DB...");
		try {
			dbe.forwardRequest(delete2, t, waitUntilDeleted);
		} catch (EngineException e) {
			AdaptorException a = new AdaptorException("Error deleting properties from DB!", e);
			throw a;
		}
	}
	
	public void persistProperty(AbstProperty p, boolean waitUntilPersisted) throws AdaptorException {
		//LOG.trace("Persisting property to DB...");
		Thread t = Thread.currentThread();
		HashMap<String, Object> values = new HashMap<String, Object>(4,1);
		values.put("com_id", p.getDevice().getSSID());
		values.put("prop_name", p.getSystemName());
		values.put("prop_value", String.valueOf(p.getValue()));
		values.put("cpl_ssid", p.getSSID());
		
		LOG.trace("Inserting property " + p.getDevice().getSSID() + "_" + p.getSSID() + " to " + propsTable + " table!");
		InsertDBEReq insert = new InsertDBEReq(idg.generateMixedCharID(10), propsTable, values);
		try {
			dbe.forwardRequest(insert, t, waitUntilPersisted);
		} catch (EngineException e) {
			AdaptorException a = new AdaptorException("Error inserting property to DB!", e);
			throw a;
		}
	}
	
	@Override
	public void updatePropertyValue(AbstProperty p, boolean waitUntilUpdated) throws AdaptorException {
		LOG.trace("Updating property value of " + p.getStandardID() + " to " + p.getValue() + " in " + propsTable 
				+ " in DB...");
		Thread t = Thread.currentThread();
		HashMap<String, Object> vals = new HashMap<String, Object>(1, 1);
		HashMap<String, Object> args = new HashMap<String, Object>(2, 1);
		vals.put("prop_value", String.valueOf(p.getValue()));
		args.put("com_id", p.getDevice().getSSID());
		args.put("cpl_ssid", p.getSSID());
		UpdateDBEReq udber = new UpdateDBEReq(idg.generateMixedCharID(10), propsTable, vals, args);
		try {
			dbe.forwardRequest(udber, t, waitUntilUpdated);
		} catch (EngineException e) {
			AdaptorException a = new AdaptorException("Cannot update property value!", e);
			throw a;
		}
	}
	
	/**
	 * <b><i>Defunct.</b><br>
	 * Properties must be deleted from DB alongside their component, deletion of single properties may result to
	 * errors.</i>
	 * <br><br>
	 * Deletes a single property from DB.
	 */
	@Override
	public void deleteProperty(AbstProperty p, boolean waitUntilDeleted) throws AdaptorException {
		
	}

	@Override
	public void persistRoom(Room r, boolean waitUntilPersisted) throws AdaptorException {
		LOG.trace("Persisting room " + r.getSSID() + " to DB...");
		Thread t = Thread.currentThread();
		HashMap<String, Object> vals = new HashMap<String, Object>(1,1);
		vals.put("SSID", r.getSSID());
		vals.put("name", r.getName());
		vals.put("parent_room", r.getRoom());
		vals.put("index", r.getIndex());
		vals.put("color", r.getColor());
		
		InsertDBEReq insert1 = new InsertDBEReq(idg.generateERQSRequestID(), roomsTable, vals);
		try {
			dbe.forwardRequest(insert1, t, waitUntilPersisted);
		} catch (EngineException e) {
			AdaptorException a = new AdaptorException("Cannot insert room to DB! Query : " + 
					insert1.getQuery(), e);
			throw a;
		}
	}

	@Override
	public void deleteRoom(Room r, boolean waitUntilDeleted) throws AdaptorException{
		LOG.trace("Deleting room " + r.getSSID() + " from DB...");
		Thread t = Thread.currentThread();
		HashMap<String, Object> args = new HashMap<String, Object>(1,1);
		args.put("SSID", r.getSSID());
		
		DeleteDBEReq delete1 = new DeleteDBEReq(idg.generateERQSRequestID(), roomsTable, args);
		try {
			dbe.forwardRequest(delete1, t, waitUntilDeleted);
		} catch (EngineException e) {
			AdaptorException a = new AdaptorException("Cannot delete room from DB! Query : " + 
					delete1.getQuery(), e);
			throw a;
		}
	}

	@Override
	public void updateRoom(Room r, boolean waitUntilUpdated) throws AdaptorException {
		LOG.trace("Updating credentials of room " + r.getSSID() + " (" + r.getName() + ") in DB...");
		Thread t = Thread.currentThread();
		HashMap<String, Object> vals = new HashMap<String, Object>(1, 1);
		HashMap<String, Object> args = new HashMap<String, Object>(2, 1);
		vals.put("name", r.getName());
		vals.put("index", r.getIndex());
		vals.put("color", r.getColor());
		if(r.getRoom() == null) {
			vals.put("parent_room", null);
		} else {
			vals.put("parent_room", r.getRoom().getSSID());
		}
		args.put("ssid", r.getSSID());
		UpdateDBEReq udber = new UpdateDBEReq(idg.generateERQSRequestID(), roomsTable, vals, args);
		try {
			dbe.forwardRequest(udber, t, waitUntilUpdated);
		} catch (EngineException e) {
			AdaptorException a = new AdaptorException("Cannot update property value!", e);
			throw a;
		}
	}
	
	public void updateRoomParent(Room r, boolean waitUntilUpdated) throws AdaptorException {
		//room update is done in updateRoom() function
	}

	@Override
	protected boolean checkBinding(Binding b) {
		// DBAdaptor applies to ALL properties
		return true;
	}
}
