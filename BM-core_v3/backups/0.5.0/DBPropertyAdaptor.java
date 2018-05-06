package main.adaptors.DBAdaptor;

import java.util.HashMap;

import org.apache.log4j.Logger;

import components.properties.AbstProperty;
import json.RRP.ResError;
import main.engines.AbstEngine;
import main.engines.DBEngine;
import main.engines.exceptions.EngineException;
import main.engines.requests.DBEngine.DeleteDBEReq;
import main.engines.requests.DBEngine.InsertDBEReq;
import main.engines.requests.DBEngine.UpdateDBEReq;
import tools.IDGenerator;

public class DBPropertyAdaptor extends DBAdaptor {
	private String propsTable;
	private IDGenerator idg = new IDGenerator();

	public DBPropertyAdaptor(DBEngine dbe, String propsTable) {
		super(DBPropertyAdaptor.class.getSimpleName(), dbe);
		this.propsTable = propsTable;
	}

	public void persistProperty(AbstProperty p, Thread t) 
			throws EngineException {
		HashMap<String, Object> values = new HashMap<String, Object>(4,1);
		values.put("com_id", p.getComID());
		values.put("prop_name", p.getSystemName());
		values.put("prop_value", String.valueOf(p.getValue()));
		values.put("cpl_ssid", p.getSSID());
		
		LOG.debug("Inserting property " + p.getComID() + "_" + p.getSSID() + 
				" to " + propsTable + " table!");
		InsertDBEReq insert = new InsertDBEReq(idg.generateMixedCharID(10), propsTable, values);
		Object o = AbstEngine.forwardRequest(insert, dbe, LOG, t);
		if(o.getClass().equals(ResError.class)) {
			ResError error = (ResError) o;
			LOG.error("Error inserting property to DB!");
			throw new EngineException("Error inserting property " + p.getComID() + "_" + 
					p.getSSID() + " to DB! " + error.message, insert);
		}
	}
	
	public void persistPropertyValue(AbstProperty p, String propsTable, Thread t) 
			throws EngineException {
		HashMap<String, Object> vals = new HashMap<String, Object>(1, 1);
		HashMap<String, Object> args = new HashMap<String, Object>(2, 1);
		vals.put("prop_value", String.valueOf(p.getValue()));
		args.put("com_id", p.getComID());
		args.put("cpl_ssid", p.getSSID());
		UpdateDBEReq udber = new UpdateDBEReq(idg.generateMixedCharID(10), propsTable, vals, args);
		Object o = AbstEngine.forwardRequest(udber, dbe, LOG, t);
		if(o.getClass().equals(ResError.class)) {
			ResError error = (ResError) o;
			LOG.error("Cannot persist property value!");
			throw new EngineException(error.message, udber);
		}
	}
	
	public void deleteProperty(AbstProperty p, String propsTable, Thread t) throws EngineException {
		HashMap<String, Object> args = new HashMap<String, Object>(2, 1);
		args.put("com_id", p.getComID());
		args.put("cpl_ssid", p.getSSID());
		DeleteDBEReq delete = new DeleteDBEReq(idg.generateMixedCharID(10), propsTable, args);
		Object o = AbstEngine.forwardRequest(delete, dbe, LOG, t);
		if(o.getClass().equals(ResError.class)) {
			ResError error = (ResError) o;
			LOG.error("Error deleting from DB!");
			throw new EngineException(error.message, delete);
		}
	}
}
