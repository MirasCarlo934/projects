package bm.main.modules;

import bm.context.adaptors.exceptions.AdaptorException;
import bm.context.devices.Device;
import bm.jeep.JEEPRequest;
import bm.main.repositories.DeviceRepository;

/**
 * The DetachmentModule basically unregisters or "detaches" the requesting component from the Symphony system. The 
 * component's records are removed from the database, OpenHAB, and other peripheral systems linked to the component.
 * 
 * @author carlomiras
 *
 */
public class DetachmentModule extends SimpleModule {

	/**
	 * Creates a DetachmentModule
	 * 
	 * @param logDomain the log4j domain that this module will use
	 * @param errorLogDomain the log4j domain where errors will be logged to
	 * @param RTY the JEEP request type that this module handles
	 */
	public DetachmentModule(String logDomain, String errorLogDomain, String RTY, /*MQTTPublisher mp, */DeviceRepository dr) {
		super(logDomain, errorLogDomain, "DetachmentModule", RTY, new String[0], dr);
	}
	
	/**
	 * Creates a DetachmentModule
	 * 
	 * @param logDomain the log4j domain that this module will use
	 * @param errorLogDomain the log4j domain where errors will be logged to
	 * @param RTY the JEEP request type that this module handles
	 */
	public DetachmentModule(String logDomain, String errorLogDomain, String RTY, /*MQTTPublisher mp, */
			DeviceRepository dr, AbstModuleExtension[] extensions) {
		super(logDomain, errorLogDomain, "DetachmentModule", RTY, new String[0], dr, extensions);
	}

	@Override
	protected boolean process(JEEPRequest request) {
		String cid = request.getCID();
		mainLOG.info("Detaching component " + cid + " from system...");
		Device c = dr.removeDevice(cid);
		
		try {
			c.delete(logDomain, true);
		} catch (AdaptorException e) {
			error(e, request.getProtocol());
			return false;
		}
		
		mainLOG.info("Detachment complete!");
		return true;
	}
	
	@Override
	public void run() {
		mainLOG.debug(name + " request processing started!");
		
		if(checkSecondaryRequestParameters(request)) {
			mainLOG.trace("Request valid! Proceeding to request processing...");
			for(int i = 0; i < extensions.length; i++) {
				AbstModuleExtension ext = extensions[i];
				ext.processRequest(request);
			}
			if(process(request)) {
				mainLOG.info("Request processing finished!");
			} else {
				mainLOG.error(name + " did not process the request successfully!");
			}
		} else {
			mainLOG.error("Secondary request params didn't check out. See also the additional request params"
					+ " checking.");
		}
	}

	@Override
	protected boolean additionalRequestChecking(JEEPRequest request) {
		return true;
	}
}
