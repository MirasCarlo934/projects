//package oh.modules.extensions;
//
//import bm.comms.http.HTTPException;
//import bm.comms.http.HTTPSender;
//import bm.context.adaptors.exceptions.AdaptorException;
//import bm.context.devices.Device;
//import bm.context.properties.Property;
//import bm.jeep.JEEPRequest;
//import bm.main.engines.FileEngine;
//import bm.main.engines.exceptions.EngineException;
//import bm.main.modules.AbstModuleExtension;
//import bm.main.repositories.DeviceRepository;
//import bm.tools.IDGenerator;
//import oh.main.initializables.OH_Initializer;
//
//public class OH_DetachmentExtension extends AbstModuleExtension {
//	private DeviceRepository dr;
//	private HTTPSender hs;
//	private String ohIP;
//	private FileEngine sitemapFE;
//	private IDGenerator idg;
//
//	public OH_DetachmentExtension(String logDomain, String errorLogDomain, String name, String[] params,
//			DeviceRepository deviceRepository, HTTPSender httpSender, FileEngine sitemapFE,
//			IDGenerator idg, String ohIP) {
//		super(logDomain, errorLogDomain, name, params);
//		this.dr = deviceRepository;
//		this.hs = httpSender;
//		this.ohIP = ohIP;
//		this.sitemapFE = sitemapFE;
//		this.idg = idg;
//	}
//
//	@Override
//	protected void process(JEEPRequest request) {
//		Device d = dr.getDevice(request.getCID());
//
//		mainLOG.debug("Deleting device " + d.getSSID() + " from OpenHAB item registry...");
//		if(d.getProperties().length > 1) { //deletes device AND properties
//			try {
//				OH_Initializer.deleteItem(mainLOG, hs, ohIP, d.getSSID(), true);
//				for(int i = 0; i < d.getProperties().length; i++) {
//					Property p = d.getProperties()[i];
//					mainLOG.trace("Deleting property " + p.getOH_ID() + " from OpenHAB item registry...");
//					try {
//						OH_Initializer.deleteItem(mainLOG, hs, ohIP, p.getOH_ID(), true);
//						mainLOG.trace("B_Property deleted!");
//					} catch (HTTPException e) {
//						mainLOG.error("Cannot delete property " + p.getOH_ID() + " from registry", e);
//					}
//				}
//			} catch (HTTPException e) {
//				mainLOG.error("Cannot delete device " + d.getSSID() + " from "
//						+ "registry", e);
//			}
//		} else { //deletes sole property of device in OH
//			Property p = d.getProperties()[0];
//			try {
//				OH_Initializer.deleteItem(mainLOG, hs, ohIP, p.getOH_ID(), true);
//			} catch (HTTPException e) {
//				mainLOG.error("Cannot delete device " + d.getSSID() + " from registry", e);
//			}
//		}
//
//		mainLOG.trace("Deleting device from sitemap...");
//		try {
//			OH_Initializer.deleteItemFromSitemap(sitemapFE, idg, d.convertToSitemapString(), true);
//			mainLOG.debug("Device deleted from OpenHAB!");
//		} catch (EngineException e) {
//			mainLOG.error("Cannot delete device from OpenHAB sitemap file!", e);
//		}
//	}
//
//	@Override
//	protected boolean additionalRequestChecking(JEEPRequest request) {
//		return true;
//	}
//
//}
