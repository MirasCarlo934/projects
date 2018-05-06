package main.adaptors.DBAdaptor;

import java.util.HashMap;

import org.apache.log4j.Logger;

import components.AbstComponent;
import components.CommonComponent;
import components.properties.AbstProperty;
import json.RRP.ResError;
import main.adaptors.AbstAdaptor;
import main.engines.AbstEngine;
import main.engines.DBEngine;
import main.engines.exceptions.EngineException;
import main.engines.requests.EngineErrorResponse;
import main.engines.requests.DBEngine.DBEngineRequest;
import main.engines.requests.DBEngine.DeleteDBEReq;
import main.engines.requests.DBEngine.InsertDBEReq;
import tools.IDGenerator;

public class DBComponentAdaptor extends DBAdaptor {
	private IDGenerator idg = new IDGenerator();
	private String comsTable;
	private String propsTable;

	public DBComponentAdaptor(DBEngine dbe, String comsTable, String propsTable) {
		super(DBComponentAdaptor.class.getSimpleName(), dbe);
		this.comsTable = comsTable;
		this.propsTable = propsTable;
	}
	
	public void persistComponent(AbstComponent c, Thread t) throws EngineException {
		LOG.info("Persisting component to DB...");
		HashMap<String, Object> valuesCom = new HashMap<String, Object>(7,1);
		InsertDBEReq insertCom;
		valuesCom.put("SSID", c.getSSID());
		valuesCom.put("topic", c.getTopic());
		valuesCom.put("mac", c.getMAC());
		valuesCom.put("name", c.getName());
		valuesCom.put("room", c.getRoom());
		valuesCom.put("functn", c.getProduct().getSSID());
		valuesCom.put("active", c.isActive());
		insertCom = new InsertDBEReq(idg.generateMixedCharID(10), comsTable, valuesCom);
		
		//inserts component to DB
		LOG.debug("Inserting component to " + comsTable + " table!");
		Object o = AbstEngine.forwardRequest(insertCom, dbe, LOG, t);
		if(o.getClass().equals(ResError.class)) {
			ResError error = (ResError) o;
			LOG.error("Error inserting component to DB!");
			throw new EngineException("Error inserting component " + c.getSSID() + " to DB! " + 
					error.message, insertCom);
		}
	}
	
	public void deleteComponent(AbstComponent c, Thread t) 
			throws EngineException {
		HashMap<String, Object> args1 = new HashMap<String, Object>(1,1);
		HashMap<String, Object> args2 = new HashMap<String, Object>(1,1);
		args1.put("ssid", c.getSSID());
		args2.put("com_id", c.getSSID());
		DeleteDBEReq delete1 = new DeleteDBEReq(idg.generateMixedCharID(10), comsTable, args1);
		DeleteDBEReq delete2 = new DeleteDBEReq(idg.generateMixedCharID(10), propsTable, args2);
		Object o = AbstEngine.forwardRequest(delete1, dbe, LOG, t);
		if(o.getClass().equals(ResError.class)) {
			LOG.error("Error deleting records from DB!");
			ResError error = (ResError) o;
			throw new EngineException(error.message, delete1);
		}
		
		LOG.debug("Deleting properties from DB...");
		o = AbstEngine.forwardRequest(delete2, dbe, LOG, t);
		if(o.getClass().equals(ResError.class)) {
			LOG.error("Error deleting properties from DB!");
			ResError error = (ResError) o;
			throw new EngineException(error.message, delete2);
		}
	}
}
