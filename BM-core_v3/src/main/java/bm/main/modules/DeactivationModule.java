package bm.main.modules;

import java.util.HashMap;

import bm.comms.mqtt.*;
import bm.context.adaptors.exceptions.AdaptorException;
import bm.context.devices.Device;
import bm.jeep.JEEPRequest;
import bm.jeep.device.ReqRequest;
import bm.jeep.device.ResError;
import bm.main.engines.*;
import bm.main.engines.exceptions.EngineException;
import bm.main.engines.requests.DBEngine.UpdateDBEReq;
import bm.main.repositories.DeviceRepository;
import bm.tools.IDGenerator;

/**
 * The DeactivationModule handles the JEEP requests sent by a component that is unexpectedly disconnected from the MQTT 
 * server. This module deactivates the component in the database, OpenHAB, and other peripheral systems where the component 
 * is linked to, preventing any interactions with it.
 * 
 * @author carlomiras
 *
 */
public class DeactivationModule extends SimpleModule {
	
	/**
	 * Creates a DeactivationModule
	 * 
	 * @param logDomain the log4j domain that this module will use
	 * @param errorLogDomain the log4j domain where errors will be logged to
	 * @param RTY the JEEP request type that this module handles
	 * @param mp the MQTTPublisher that will publish the JEEP responses
	 * @param cr the ComponentRepository of this BM
	 */
	public DeactivationModule(String logDomain, String errorLogDomain, String RTY, /*MQTTPublisher mp,*/ 
			DeviceRepository dr) {
		super(logDomain, errorLogDomain, "ByeModule", RTY, new String[0], dr);
	}
	
	/**
	 * Creates a DeactivationModule
	 * 
	 * @param logDomain the log4j domain that this module will use
	 * @param errorLogDomain the log4j domain where errors will be logged to
	 * @param RTY the JEEP request type that this module handles
	 * @param mp the MQTTPublisher that will publish the JEEP responses
	 * @param cr the ComponentRepository of this BM
	 * @param extensions the module extensions attached to this module
	 */
	public DeactivationModule(String logDomain, String errorLogDomain, String RTY, /*MQTTPublisher mp,*/ 
			DeviceRepository dr, AbstModuleExtension[] extensions) {
		super(logDomain, errorLogDomain, "ByeModule", RTY, new String[0], dr, extensions);
	}

	@Override
	protected boolean process(JEEPRequest request) {
		Device c = dr.getDevice(request.getCID());
		mainLOG.info("Deactivating component " + c.getSSID() + " (MAC:" + c.getMAC() + ")");
		try {
			c.setActive(false, true);
		} catch (AdaptorException e) {
			mainLOG.error("Cannot deactivate component!", e);
			return false;
		}
		
		mainLOG.info("Component deactivated!");
		return true;
	}

	@Override
	protected boolean additionalRequestChecking(JEEPRequest request) {
		return true;
	}
}
