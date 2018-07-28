package bm.main.modules;

import java.util.HashMap;

import bm.jeep.ReqRequest;
import bm.jeep.ResError;
import bm.main.engines.*;
import bm.main.engines.exceptions.EngineException;
import bm.main.engines.requests.DBEngine.UpdateDBEReq;
import bm.main.repositories.DeviceRepository;
import bm.mqtt.*;
import bm.smarthome.adaptors.exceptions.AdaptorException;
import bm.smarthome.devices.Device;
import bm.tools.IDGenerator;

/**
 * The DeactivationModule handles the JEEP requests sent by a component that is unexpectedly disconnected from the MQTT 
 * server. This module deactivates the component in the database, OpenHAB, and other peripheral systems where the component 
 * is linked to, preventing any interactions with it.
 * 
 * @author carlomiras
 *
 */
public class DeactivationModule extends AbstModule {
	
	/**
	 * Creates a DeactivationModule
	 * 
	 * @param logDomain the log4j domain that this module will use
	 * @param errorLogDomain the log4j domain where errors will be logged to
	 * @param RTY the JEEP request type that this module handles
	 * @param mp the MQTTPublisher that will publish the JEEP responses
	 * @param cr the ComponentRepository of this BM
	 */
	public DeactivationModule(String logDomain, String errorLogDomain, String RTY, MQTTPublisher mp, 
			DeviceRepository cr) {
		super(logDomain, errorLogDomain, "ByeModule", RTY, new String[0], mp, cr);
	}

	@Override
	protected void process(ReqRequest request) {
		Device c = dr.getDevice(request.cid);
		mainLOG.info("Deactivating component " + c.getSSID() + " (MAC:" + c.getMAC() + ")");
		try {
			c.setActive(false, logDomain, true);
		} catch (AdaptorException e) {
			mainLOG.error("Cannot deactivate component!", e);
			return;
		}
		
		mainLOG.info("Component deactivated!");
	}

	@Override
	protected boolean additionalRequestChecking(ReqRequest request) {
		return true;
	}
}
