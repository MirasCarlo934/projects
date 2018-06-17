package bm.main.modules;

import bm.context.adaptors.exceptions.AdaptorException;
import bm.context.devices.Device;
import bm.jeep.JEEPManager;
import bm.jeep.vo.JEEPRequest;
import bm.jeep.vo.JEEPResponse;
import bm.main.repositories.DeviceRepository;

/**
 * The DetachmentModule basically unregisters or "detaches" the requesting component from the Symphony system. The 
 * component's records are removed from the database, OpenHAB, and other peripheral systems linked to the component.
 * 
 * @author carlomiras
 *
 */
public class DetachmentModule extends Module {
	private JEEPManager jm;

	/**
	 * Creates a DetachmentModule
	 * 
	 * @param logDomain the log4j domain that this module will use
	 * @param errorLogDomain the log4j domain where errors will be logged to
	 * @param RTY the JEEP request type that this module handles
	 */
	public DetachmentModule(String logDomain, String errorLogDomain, String RTY, JEEPManager jeepManager,
							DeviceRepository dr) {
		super(logDomain, errorLogDomain, "DetachmentModule", RTY, new String[0], null, dr);
		this.jm = jeepManager;
	}
	
//	/**
//	 * Creates a DetachmentModule
//	 *
//	 * @param logDomain the log4j domain that this module will use
//	 * @param errorLogDomain the log4j domain where errors will be logged to
//	 * @param RTY the JEEP request type that this module handles
//	 */
//	public DetachmentModule(String logDomain, String errorLogDomain, String RTY, /*MQTTPublisher mp, */
//			DeviceRepository dr, AbstModuleExtension[] extensions) {
//		super(logDomain, errorLogDomain, "DetachmentModule", RTY, new String[0], null, dr, extensions);
//	}

	@Override
	protected boolean processRequest(JEEPRequest request) {
		String cid = request.getCID();
		Device d = dr.getDevice(cid);
		LOG.info("Detaching device " + cid + " from system...");
		try {
			d.delete(logDomain, true);
			jm.sendDetachmentResponse(d, true, request);
			dr.removeDevice(d.getSSID());
		} catch (AdaptorException e) {
			error(e, request.getProtocol());
			return false;
		}
		
		LOG.info("Detachment complete!");
		return true;
	}

	@Override
	protected boolean processResponse(JEEPResponse response) {
		return true;
	}

	@Override
	public void processNonResponse(JEEPRequest request) {

	}

	@Override
	public void run() {
		LOG.debug(name + " request processing started!");
		
		if(checkSecondaryRequestParameters(request)) {
			LOG.trace("Request valid! Proceeding to request processing...");
			for(int i = 0; i < extensions.length; i++) {
				AbstModuleExtension ext = extensions[i];
				ext.processRequest(request);
			}
			if(processRequest(request)) {
				LOG.info("Request processing finished!");
			} else {
				LOG.error(name + " did not processRequest the request successfully!");
			}
		} else {
			LOG.error("Secondary request params didn't check out. See also the additional request params"
					+ " checking.");
		}
	}

	@Override
	protected boolean additionalRequestChecking(JEEPRequest request) {
		return true;
	}

	@Override
	protected boolean additionalResponseChecking(JEEPResponse response) {
		return true;
	}
}
