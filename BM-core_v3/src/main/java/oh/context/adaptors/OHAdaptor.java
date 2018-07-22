package oh.context.adaptors;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;

import bm.context.adaptors.AbstAdaptor;
import bm.context.properties.Property;
import bm.main.repositories.DeviceRepository;
import bm.main.repositories.RoomRepository;
import org.json.JSONObject;

import bm.comms.http.HTTPSender;
import bm.comms.http.messages.DeleteHTTPReq;
import bm.comms.http.messages.PutHTTPReq;
import bm.context.adaptors.exceptions.AdaptorException;
import bm.context.devices.Device;
import bm.context.rooms.Room;
import bm.main.engines.FileEngine;
import bm.main.engines.exceptions.EngineException;
import bm.main.engines.requests.FileEngine.DeleteStringFEReq;
import bm.main.engines.requests.FileEngine.InsertToFileFEReq;
import bm.tools.IDGenerator;

public class OHAdaptor extends AbstAdaptor {
	private IDGenerator idg = new IDGenerator();
	private String ohIP;
	private String ohSitemapName;
	private HTTPSender httpSender;
	private FileEngine sitemapFE;

	public OHAdaptor(String logDomain, String adaptorID, String adaptorName, String ohIP, String ohSitemapName,
					 HTTPSender he, FileEngine sitemapFE) {
		super(logDomain, adaptorID, adaptorName/*, "openhab"*/);
		this.sitemapFE = sitemapFE;
		this.httpSender = he;
		this.ohIP = ohIP;
		this.ohSitemapName = ohSitemapName;
	}
	
	@Override
	public void deviceDeleted(Device d, boolean waitUntilDeleted) throws AdaptorException {
		LOG.trace("Deleting component " + d.getSSID() + " from OpenHAB item registry...");
		if(d.getProperties().length > 1) { //1-property components have no OH items specific to themselves
			try {
				deleteItem(d.getSSID(), waitUntilDeleted);
			} catch (AdaptorException e) {
				AdaptorException a = new AdaptorException("Cannot delete component " + d.getSSID() + " from "
						+ "registry", e, getName());
				throw a;
			}
		}
		
		LOG.trace("Deleting device from sitemap...");
		deleteItemFromSitemap(d.convertToSitemapString(), waitUntilDeleted);
	}
	
	@Override
	public void deviceCredentialsUpdated(Device d, boolean waitUntilUpdated) throws AdaptorException {
		LOG.trace("Updating component " + d.getSSID() + " in OpenHAB item registry through standard OHAdaptor "
				+ "persistence processRequest...");
		LOG.trace("Deleting device from sitemap (if it exists in the sitemap...");
		deleteItemFromSitemap(d.convertToSitemapString(), false);
		
		Iterator<Property> props = Arrays.asList(d.getProperties()).iterator();
		deviceCreated(d, waitUntilUpdated);
		while(props.hasNext()) {
			Property prop = props.next();
			propertyCreated(prop, waitUntilUpdated);
		}
		deviceStateUpdated(d, waitUntilUpdated);
		
		if(d.getParentRoom() == null) {
			LOG.trace("Adding device to sitemap since it has no parent room...");
			addItemToSitemap(d.convertToSitemapString(), waitUntilUpdated);
		}
		LOG.trace("Update successful!");
	}
	
	public void propertyValueUpdated(Property p, boolean waitUntilUpdated) throws AdaptorException {
		LOG.trace("Updating property " + p.getDevice().getSSID() + "_" + p.getSSID() + " state in OpenHAB...");
		HashMap<String, String> parameters = new HashMap<String, String>(1,1);
		HashMap<String, String> headers = new HashMap<String, String>(2, 1);
		headers.put("Content-Type", "text/plain");
		headers.put("Accept", "application/json");
		parameters.put("null", p.transformValueToOHCommand());
		
		PutHTTPReq request = new PutHTTPReq(/*idg.generateMixedCharID(10), he, */ohIP + "/rest/items/" +
				p.getOH_ID() + "/state", headers, parameters, new int[]{200, 202});
		try {
			httpSender.sendHTTPRequest(request, false);
		} catch (bm.comms.http.HTTPException e) {
			AdaptorException a = new AdaptorException("Cannot updateRules value of property " + p.getOH_ID(), e,
					getName());
			throw a;
		}
//		try {
//			he.putRequest(request, Thread.currentThread(), waitUntilUpdated);
//		} catch (EngineException e) {
//			AdaptorException a = new AdaptorException("Cannot updateRules value of property " + p.getStandardID(), e);
//			throw a;
//		}
		LOG.trace("Update complete!");
	}

	@Override
	public void deviceCreated(Device d, boolean waitUntilPersisted) throws AdaptorException {
		LOG.trace("Persisting device " + d.getSSID() + " to OpenHAB item registry...");
		if(d.getProperties().length > 1) { //1-property components are persisted thru their sole property!!!
			JSONObject[] items = d.convertToItemsJSON();
			try {
				addItems(items, waitUntilPersisted);
			} catch(AdaptorException e) { 
				throw new AdaptorException("Cannot add device " + d.getSSID() + " to OpenHAB item registry", e,
						getName());
			}
			LOG.trace("Device item representation added successfully!");
		}
	}

//	@Override
	public void deviceStateUpdated(Device d, boolean waitUntilUpdated) throws AdaptorException {
		LOG.trace("Updating state of device " + d.getSSID() + "to " + d.isActive() + "...");
		String itemName;
		if(d.getProperties().length > 1) {
			itemName = d.getSSID();
		} else {
			itemName = d.getProperties()[0].getOH_ID();
		}
		try {
			if(d.isActive()) {
				addItems(d.convertToItemsJSON(), waitUntilUpdated);
				if(d.getProperties().length == 1) {
					addItems(d.getProperties()[0].convertToItemsJSON(), waitUntilUpdated);
				}
			} else {
				JSONObject json = new JSONObject();
				json.put("type", "String");
				json.put("name", itemName);
				json.put("label", d.getName() + " [inactive]");
				json.put("groupNames", new String[]{d.getParentRoom().getSSID()});
				json.put("category", d.getIcon());
				addItems(new JSONObject[]{json}, waitUntilUpdated);
			}
		} catch(AdaptorException e) {
			throw new AdaptorException("Cannot updateRules state of component " + d.getSSID(), e, getName());
		}
	}

	@Override
	public void propertyCreated(Property p, boolean waitUntilPersisted) throws AdaptorException {
		LOG.trace("Adding property " + p.getOH_ID() + " to OpenHAB item registry!");
		JSONObject[] items = p.convertToItemsJSON();
		addItems(items, waitUntilPersisted);
		LOG.trace("Property added successfully!");
	}

	@Override
	public void propertyDeleted(Property p, boolean waitUntilDeleted) throws AdaptorException {
		LOG.trace("Deleting property " + p.getOH_ID() + " to OpenHAB item registry!");
		try {
			deleteItem(p.getOH_ID(), waitUntilDeleted);
		} catch (AdaptorException e) {
			throw new AdaptorException("Cannot delete property " + p.getOH_ID() + " from registry", e,
					getName());
		}
	}

	@Override
	public void roomCreated(Room r, boolean waitUntilPersisted) throws AdaptorException {
		//persists the room to the item registry
		LOG.trace("Persisting room " + r.getSSID() + " (" + r.getName() + ") to OpenHAB item registry...");
		try {
			addItems(r.convertToItemsJSON(), waitUntilPersisted);
		} catch(AdaptorException e) {
			throw new AdaptorException("Cannot persist room " + r.getSSID() + "(" + r.getName() + ") to "
					+ "registry", e, getName());
		}
		
		//persists the room to the sitemap file IF it has NO parent room
		if(r.getParentRoom() == null) {
            LOG.trace("Persisting room " + r.getSSID() + " (" + r.getName() + ") to OpenHAB sitemap file...");
			addItemToSitemap(r.convertToSitemapString(), waitUntilPersisted);
		}
	}

	@Override
	public void roomDeleted(Room r, boolean waitUntilDeleted) throws AdaptorException {
		LOG.trace("Deleting room " + r.getSSID() + " (" + r.getName() + ") from OpenHAB item registry...");
		deleteItem(r.getSSID(), false);
		LOG.trace("Deleting room " + r.getSSID() + " (" + r.getName() + ") from OpenHAB sitemap file...");
		deleteItemFromSitemap(r.convertToSitemapString(), false);
	}
	
	@Override
	public void roomCredentialsUpdated(Room r, boolean waitUntilUpdated) throws AdaptorException {
		//room updateRules same as room persistence
		LOG.trace("Updating room credentials in OpenHAB thru OpenHAB room persistence...");
		LOG.trace("Deleting room from sitemap (if it exists in the sitemap)...");
		deleteItemFromSitemap(r.convertToSitemapString(), waitUntilUpdated);
		roomCreated(r, waitUntilUpdated);
	}
	
	private void addItems(JSONObject[] items, boolean waitUntilAdded) throws AdaptorException {
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
			try {
				httpSender.sendHTTPRequest(put, false);
			} catch (bm.comms.http.HTTPException e) {
				AdaptorException a = new AdaptorException("Error in registering item. Item JSON: " + 
						json.toString(), e, getName());
				throw a;
			}
		}
	}
	
	private void addItemToSitemap(String itemStr, boolean waitUntilAdded) throws AdaptorException {
		InsertToFileFEReq req1 = new InsertToFileFEReq(idg.generateERQSRequestID(), sitemapFE, itemStr,
				"}", false);
		try {
			sitemapFE.putRequest(req1, Thread.currentThread(), waitUntilAdded);
		} catch (EngineException e) {
			throw new AdaptorException("Cannot insert item to OH sitemap file!", e, getName());
		}
	}
	
	private void deleteItemFromSitemap(String itemStr, boolean waitUntilDeleted) throws AdaptorException {
		DeleteStringFEReq req1 = new DeleteStringFEReq(idg.generateERQSRequestID(), sitemapFE, itemStr);
		try {
			sitemapFE.putRequest(req1, Thread.currentThread(), waitUntilDeleted);
		} catch (EngineException e) {
			throw new AdaptorException("Cannot delete item from OH sitemap file!", e, getName());
		}
	}
	
//	/**
//	 * Adds an array of items in string format to the "home" sitemap of the OpenHAB instance 
//	 * through the .sitemap file.
//	 * 
//	 * @param items The array of items in string format
//	 * @throws EngineException 
//	 */
//	private void addItemsToSitemap(String[] items) throws EngineException {
//		for(int i = 0; i < items.length; i++) {
//			String item = items[i];
//			ReadAllLinesFEReq ereq1 = new ReadAllLinesFEReq(idg.generateERQSRequestID(), sitemapFE);
//			Object o = sitemapFE.putRequest(ereq1, Thread.currentThread(), true);
//			if(o.getClass().equals(EngineException.class)) {
//				LOG.error("Error reading from the OH sitemap file!", (EngineException) o);
//				throw (EngineException) o;
//			}
//			
//		}
//	}
	
	/**
	 * Deletes an item from the OpenHAB item registry.
	 * 
	 * @param itemName Denoted by the SSID of the device/property/room to be deleted
	 * @param waitUntilDeleted 
	 * @throws AdaptorException
	 */
	private void deleteItem(String itemName, boolean waitUntilDeleted) throws AdaptorException {
		LOG.trace("Deleting item " + itemName + " from registry...");
		HashMap<String, String> headers = new HashMap<String, String>(2, 1);
		headers.put("Content-Type", "application/json");
		headers.put("Accept", "application/json");
		DeleteHTTPReq delete = new DeleteHTTPReq(/*idg.generateMixedCharID(10), httpSender, */ohIP + 
				"/rest/items/" + itemName, headers, null, new int[]{200, 404});
		try {
			httpSender.sendHTTPRequest(delete, waitUntilDeleted);
		} catch (bm.comms.http.HTTPException e) {
			AdaptorException a = new AdaptorException("Cannot delete item " + itemName + " from item "
					+ "registry!", e, getName());
			throw a;
		}
//		try {
//			httpSender.putRequest(delete, Thread.currentThread(), waitUntilDeleted);
//		} catch (EngineException e) {
//			AdaptorException a = new AdaptorException("Cannot delete item " + itemName + " from item "
//					+ "registry!", e);
//			throw a;
//		}
	}
	
//	/**
//	 * Converts a string of characters containing all the items from OpenHAB to JSONObjects. <i>This method only
//	 * accepts the string representation of the response of OpenHAB GET /items function in REST API</i>
//	 *
//	 * @param s The string of characters representing the OpenHAB GET /items response
//	 * @return An array of JSONObjects that contain all the items retrieved from OpenHAB
//     */
//	private JSONObject[] parseAllItemsToJSON(String s) {
//		LOG.trace("Converting " + s + " to JSONObjects...");
//		s = s.substring(1, s.length() - 2);
//		LOG.fatal(s);
//		return null;
//	}
	
	public String getOH_IP() {
		return ohIP;
	}
	
	public String getOHSitemapName() {
		return ohSitemapName;
	}

//	@Override
	public void deviceRoomUpdated(Device d, boolean waitUntilUpdated) throws AdaptorException {
		LOG.trace("Updating device parent room thru regular device persistence...");
		deviceCreated(d, waitUntilUpdated);
		
		LOG.trace("Deleting device from sitemap...");
		deleteItemFromSitemap(d.convertToSitemapString(), false);
		
		if(d.getParentRoom() == null) {
			LOG.trace("Adding device to sitemap since it has no parent room...");
			addItemToSitemap(d.convertToSitemapString(), waitUntilUpdated);
		}
	}

	@Override
	public void roomParentUpdated(Room r, boolean waitUntilUpdated) throws AdaptorException {
		LOG.trace("Updating room parent thru regular room persistence...");
		roomCreated(r, waitUntilUpdated);
		
		LOG.trace("Deleting room from sitemap...");
		deleteItemFromSitemap(r.convertToSitemapString(), false);
		
		if(r.getParentRoom() == null) {
			LOG.trace("Adding room to sitemap since it has no parent room...");
			addItemToSitemap(r.convertToSitemapString(), waitUntilUpdated);
		}
	}
}
