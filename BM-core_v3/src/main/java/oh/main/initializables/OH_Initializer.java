package oh.main.initializables;

import java.util.HashMap;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.json.JSONObject;

import bm.comms.http.HTTPException;
import bm.comms.http.HTTPSender;
import bm.comms.http.messages.DeleteHTTPReq;
import bm.comms.http.messages.PutHTTPReq;
import bm.context.adaptors.AbstAdaptor;
import bm.context.adaptors.exceptions.AdaptorException;
import bm.context.devices.Device;
import bm.context.properties.AbstProperty;
import bm.main.engines.FileEngine;
import bm.main.engines.exceptions.EngineException;
import bm.main.engines.requests.FileEngine.DeleteStringFEReq;
import bm.main.engines.requests.FileEngine.InsertToFileFEReq;
import bm.main.engines.requests.FileEngine.OverwriteFileFEReq;
import bm.main.interfaces.Initializable;
import bm.main.repositories.DeviceRepository;
import bm.tools.IDGenerator;

public class OH_Initializer implements Initializable {
	private final Logger LOG;
	private final String logDomain;
	private FileEngine sitemapFE;
	private IDGenerator idg;
	private DeviceRepository dr;
	private HTTPSender hs;
	private String sitemapName;
	private String ohIP;

	public OH_Initializer(String logDomain, FileEngine sitemapFE, IDGenerator idg, HTTPSender httpSender, 
			String sitemapName, String ohIP, DeviceRepository deviceRepository) {
		LOG = Logger.getLogger(logDomain + ".Initializer");
		this.logDomain = logDomain;
		this.sitemapFE = sitemapFE;
		this.hs = httpSender;
		this.idg = idg;
		this.ohIP = ohIP;
		this.sitemapName = sitemapName;
		this.dr = deviceRepository;
	}

	@Override
	public void initialize() throws Exception {
		//resets OpenHAB sitemap file
		LOG.debug("Clearing OpenHAB sitemap...");
		String[] lines = new String[4];
		lines[0] = "sitemap home label=\"" + sitemapName + "\"{ ";
		lines[1] = "Frame{";
		lines[2] = "}";
		lines[3] = "}";
		OverwriteFileFEReq offer = new OverwriteFileFEReq(idg.generateERQSRequestID(), sitemapFE, lines);
		try {
			sitemapFE.putRequest(offer, Thread.currentThread(), true);
		} catch (EngineException e) {
			LOG.error("Cannot reset OpenHAB sitemap file! OpenHAB GUI will show erroneous details!", e);
		}
		
		//updates OpenHAB sitemap file
		LOG.info("Updating OpenHAB item registry...");
		Device[] devices = dr.getAllDevices();
		for(int i = 0; i < devices.length; i++) {
			Device d = devices[i];
			try {
				LOG.debug("Updating device " + d.getSSID() + "(" + d.getName() + ")");
				registerDevice(d);
				for(int j = 0; j < d.getProperties().length; j++) {
					AbstProperty p = d.getProperties()[j];
					LOG.debug("Updating device property " + p.getStandardID() + "(" + p.getDisplayName() + ")");
					p.createExcept(new AbstAdaptor[]{p.getMainDBAdaptor()}, logDomain, false);
					p.update(logDomain, false);
//					oha.persistProperty(p, false);
				}
			} catch (AdaptorException e) {
				LOG.error("Device " + d.getSSID() + " cannot be persisted to OpenHAB!", e);
			}
		}
	}
	
	public void registerDevice(Device d) {
		LOG.trace("Adding device " + d.getSSID() + " to OpenHAB item registry...");
		if(d.getProperties().length > 1) { //1-property components are persisted thru their sole property!!!
			JSONObject[] items = d.convertToItemsJSON();
			try {
				addItems(LOG, hs, ohIP, items, true);
			} catch (HTTPException e) {
				LOG.error("Cannot add device to OpenHAB item registry!", e);
			}
			LOG.trace("Device added successfully!");
		}
	}
	
	public static void addItems(Logger LOG, HTTPSender hs, String ohIP, JSONObject[] items, 
			boolean waitUntilAdded) throws HTTPException {
		for(int i = 0; i < items.length; i++) {
			JSONObject json = items[i];
			LOG.trace("Adding item " + json.toString() + " to registry!");
			HashMap<String, String> headers = new HashMap<String, String>(2,1);
			HashMap<String, String> parameters = new HashMap<String, String>(1,1);
			headers.put("Content-Type", "application/json");
			headers.put("Accept", "application/json");
			parameters.put("null", json.toString());
			PutHTTPReq put = new PutHTTPReq(/*idg.generateMixedCharID(10), httpSender, */ohIP + "/rest/items/" + 
					json.getString("name"), headers, parameters, new int[]{200, 201});
			hs.sendHTTPRequest(put, waitUntilAdded);
		}
	}
	
	/**
	 * Deletes an item from the OpenHAB item registry.
	 * 
	 * @param itemName Denoted by the SSID of the device/property/room to be deleted
	 * @param waitUntilDeleted 
	 * @throws HTTPException 
	 */
	public static void deleteItem(Logger LOG, HTTPSender hs, String ohIP, String itemName, 
			boolean waitUntilDeleted) throws HTTPException {
		LOG.trace("Deleting item " + itemName + " from registry...");
		HashMap<String, String> headers = new HashMap<String, String>(2, 1);
		headers.put("Content-Type", "application/json");
		headers.put("Accept", "application/json");
		DeleteHTTPReq delete = new DeleteHTTPReq(/*idg.generateMixedCharID(10), httpSender, */ohIP + 
				"/rest/items/" + itemName, headers, null, new int[]{200, 404});
		hs.sendHTTPRequest(delete, waitUntilDeleted);
	}
	
	public static void addItemToSitemap(FileEngine sitemapFE, IDGenerator idg, String itemStr, 
			boolean waitUntilAdded) throws EngineException {
		InsertToFileFEReq req1 = new InsertToFileFEReq(idg.generateERQSRequestID(), sitemapFE, itemStr,
				"}", false);
		sitemapFE.putRequest(req1, Thread.currentThread(), waitUntilAdded);
	}
	
	/**
	 * Deletes the specified item from the OpenHAB sitemap.
	 * 
	 * @param sitemapFE The FileEngine that handles the OpenHAB sitemap file
	 * @param itemStr
	 * @param waitUntilDeleted
	 * @throws HTTPException
	 */
	public static void deleteItemFromSitemap(FileEngine sitemapFE, IDGenerator idg, String itemStr, 
			boolean waitUntilDeleted) throws EngineException {
		DeleteStringFEReq req1 = new DeleteStringFEReq(idg.generateERQSRequestID(), sitemapFE, itemStr);
		sitemapFE.putRequest(req1, Thread.currentThread(), waitUntilDeleted);
	}
}
