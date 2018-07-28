package bm.main.modules;

import java.sql.SQLException;
import java.util.HashMap;

import bm.jeep.ReqRequest;
import bm.jeep.ResError;
import bm.main.engines.DBEngine;
import bm.main.engines.exceptions.EngineException;
import bm.main.engines.requests.DBEngine.DeleteDBEReq;
import bm.main.repositories.DeviceRepository;
import bm.mqtt.MQTTListener;
import bm.mqtt.MQTTPublisher;
import bm.smarthome.adaptors.exceptions.AdaptorException;
import bm.smarthome.devices.Device;
import bm.tools.IDGenerator;

/**
 * The DetachmentModule basically unregisters or "detaches" the requesting component from the Symphony system. The 
 * component's records are removed from the database, OpenHAB, and other peripheral systems linked to the component.
 * 
 * @author carlomiras
 *
 */
public class DetachmentModule extends AbstModule {

	/**
	 * Creates a DetachmentModule
	 * 
	 * @param logDomain the log4j domain that this module will use
	 * @param errorLogDomain the log4j domain where errors will be logged to
	 * @param RTY the JEEP request type that this module handles
	 * @param mp the MQTTPublisher that will publish the JEEP responses
	 * @param cr the ComponentRepository of this BM
	 */
	public DetachmentModule(String logDomain, String errorLogDomain, String RTY, MQTTPublisher mp, DeviceRepository cr) {
		super(logDomain, errorLogDomain, "DetachmentModule", RTY, new String[0], mp, cr);
	}

	@Override
	protected void process(ReqRequest request) {
		String cid = request.cid;
		mainLOG.info("Detaching component " + cid + " from system...");
		Device c = dr.removeDevice(cid);
		
		try {
			c.delete(logDomain, true);
		} catch (AdaptorException e) {
			error(e);
		}
		
		mainLOG.info("Detachment complete!");
	}

	@Override
	protected boolean additionalRequestChecking(ReqRequest request) {
		return true;
	}
}
