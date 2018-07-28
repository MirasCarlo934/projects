package bm.smarthome.adaptors;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import javax.xml.ws.http.HTTPException;

import org.json.JSONObject;

import bm.main.engines.AbstEngine;
import bm.main.engines.FileEngine;
import bm.main.engines.HTTPEngine;
import bm.main.engines.exceptions.EngineException;
import bm.main.engines.requests.FileEngine.DeleteStringFEReq;
import bm.main.engines.requests.FileEngine.InsertToFileFEReq;
import bm.main.engines.requests.FileEngine.OverwriteFileFEReq;
import bm.main.engines.requests.FileEngine.ReadAllLinesFEReq;
import bm.main.engines.requests.HTTPEngine.DeleteHEReq;
import bm.main.engines.requests.HTTPEngine.PostHEReq;
import bm.main.engines.requests.HTTPEngine.PutHEReq;
import bm.smarthome.adaptors.exceptions.AdaptorException;
import bm.smarthome.devices.Device;
import bm.smarthome.properties.AbstProperty;
import bm.smarthome.properties.AbstProperty;
import bm.smarthome.properties.PropertyMode;
import bm.smarthome.properties.bindings.Binding;
import bm.smarthome.rooms.Room;
import bm.tools.IDGenerator;

public class OHAdaptor extends AbstAdaptor {
	private IDGenerator idg = new IDGenerator();
	private String ohIP;
	private String ohSitemapName;
	private HTTPEngine he;
	private FileEngine sitemapFE;

	public OHAdaptor(String logDomain, String ohIP, String ohSitemapName, HTTPEngine he, FileEngine sitemapFE) {
		super(logDomain, OHAdaptor.class.getSimpleName(), "openhab");
		this.sitemapFE = sitemapFE;
		this.he = he;
		this.ohIP = ohIP;
		this.ohSitemapName = ohSitemapName;
	}
	
	@Override
	public void deleteDevice(Device d, boolean waitUntilDeleted) throws AdaptorException {
		LOG.trace("Deleting component " + d.getSSID() + " from OpenHAB item registry...");	
		if(d.getProperties().length > 1) { //1-property components have no OH items specific to themselves
			try {
				deleteItem(d.getSSID(), waitUntilDeleted);
			} catch (AdaptorException e) {
				AdaptorException a = new AdaptorException("Cannot delete component " + d.getSSID() + " from "
						+ "registry", e);
				throw a;
			}
		}
		
		LOG.trace("Deleting device from sitemap...");
		deleteItemFromSitemap(d.convertToSitemapString(), waitUntilDeleted);
	}
	
	@Override
	public void updateDevice(Device d, boolean waitUntilUpdated) throws AdaptorException {
		LOG.trace("Updating component " + d.getSSID() + " in OpenHAB item registry through standard OHAdaptor "
				+ "persistence process...");
		LOG.trace("Deleting device from sitemap (if it exists in the sitemap...");
		deleteItemFromSitemap(d.convertToSitemapString(), false);
		
		Iterator<AbstProperty> props = Arrays.asList(d.getProperties()).iterator();
		persistDevice(d, waitUntilUpdated);
		while(props.hasNext()) {
			AbstProperty prop = props.next();
			persistProperty(prop, waitUntilUpdated);
		}
		
		if(d.getRoom() == null) {
			LOG.trace("Adding device to sitemap since it has no parent room...");
			addItemToSitemap(d.convertToSitemapString(), waitUntilUpdated);
		}
		LOG.trace("Update successful!");
	}
	
	public void updatePropertyValue(AbstProperty p, boolean waitUntilUpdated) throws AdaptorException {
		LOG.trace("Updating property " + p.getDevice().getSSID() + "_" + p.getSSID() + " state in OpenHAB...");
		HashMap<String, String> parameters = new HashMap<String, String>(1,1);
		HashMap<String, String> headers = new HashMap<String, String>(2, 1);
		headers.put("Content-Type", "text/plain");
		headers.put("Accept", "application/json");
		parameters.put("null", p.transformValueToOHCommand());
		
		PutHEReq request = new PutHEReq(idg.generateMixedCharID(10), ohIP + "/rest/items/" +
				p.getStandardID() + "/state", headers, parameters, new int[]{200, 202});
		try {
			he.forwardRequest(request, Thread.currentThread(), waitUntilUpdated);
		} catch (EngineException e) {
			AdaptorException a = new AdaptorException("Cannot update value of property " + p.getStandardID(), e);
			throw a;
		}
		LOG.trace("Update complete!");
	}

	@Override
	public void persistDevice(Device d, boolean waitUntilPersisted) throws AdaptorException {
		LOG.trace("Persisting component " + d.getSSID() + " to OpenHAB item registry...");
		if(d.getProperties().length > 1) { //1-property components are persisted thru their sole property!!!
			JSONObject[] items = d.convertToItemsJSON();
			try {
				addItems(items, waitUntilPersisted);
			} catch(AdaptorException e) { 
				throw new AdaptorException("Cannot add component " + d.getSSID() + " to OpenHAB item registry", e);
			}
			LOG.trace("Component item representation added successfully!");
		}
	}

	@Override
	public void updateDeviceState(Device c, boolean waitUntilUpdated) throws AdaptorException {
		LOG.trace("Updating state of component " + c.getSSID() + "to " + c.isActive() + "...");
		String itemName;
		if(c.getProperties().length > 1) {
			itemName = c.getSSID();
		} else {
			itemName = c.getProperties()[0].getStandardID();
		}
		try {
			if(c.isActive()) {
				addItems(c.convertToItemsJSON(), waitUntilUpdated);
				if(c.getProperties().length == 1) {
					addItems(c.getProperties()[0].convertToItemsJSON(), waitUntilUpdated);
				}
			} else {
				JSONObject json = new JSONObject();
				json.put("type", "String");
				json.put("name", itemName);
				json.put("label", c.getName() + " [inactive]");
				json.put("groupNames", new String[]{c.getRoom().getSSID()});
				json.put("category", c.getProduct().getOHIcon());
				addItems(new JSONObject[]{json}, waitUntilUpdated);
			}
		} catch(AdaptorException e) {
			throw new AdaptorException("Cannot update state of component " + c.getSSID(), e);
		}
	}

	@Override
	public void persistProperty(AbstProperty p, boolean waitUntilPersisted) throws AdaptorException {
		LOG.trace("Adding property " + p.getStandardID() + " to OpenHAB item registry!");
		JSONObject[] items = p.convertToItemsJSON();
		addItems(items, waitUntilPersisted);
		LOG.trace("Property added successfully!");
	}

	@Override
	public void deleteProperty(AbstProperty p, boolean waitUntilDeleted) throws AdaptorException {
		LOG.trace("Deleting property " + p.getStandardID() + " to OpenHAB item registry!");	
		try {
			deleteItem(p.getStandardID(), waitUntilDeleted);
		} catch (AdaptorException e) {
			throw new AdaptorException("Cannot delete property " + p.getStandardID() + " from registry", e);
		}
	}

	@Override
	public void persistRoom(Room r, boolean waitUntilPersisted) throws AdaptorException {
		//persists the room to the item registry
		LOG.trace("Persisting room " + r.getSSID() + " (" + r.getName() + ") to OpenHAB item registry...");
		try {
			addItems(r.convertToItemsJSON(), waitUntilPersisted);
		} catch(AdaptorException e) {
			throw new AdaptorException("Cannot persist room " + r.getSSID() + "(" + r.getName() + ") to "
					+ "registry", e);
		}
		
		//persists the room to the sitemap file IF it has NO parent room
		LOG.trace("Persisting room " + r.getSSID() + " (" + r.getName() + ") to OpenHAB sitemap file...");
		if(r.getRoom() == null) { 
			addItemToSitemap(r.convertToSitemapString(), waitUntilPersisted);
		}
	}

	@Override
	public void deleteRoom(Room r, boolean waitUntilDeleted) throws AdaptorException {
		// LATER OHAdaptor: Add room deletion
		LOG.trace("Deleting room from sitemap...");
		deleteItem(r.getSSID(), false);
		deleteItemFromSitemap(r.convertToSitemapString(), false);
	}
	
	@Override
	public void updateRoom(Room r, boolean waitUntilUpdated) throws AdaptorException {
		//room update same as room persistence
		LOG.trace("Updating room credentials in OpenHAB thru OpenHAB room persistence...");
		LOG.trace("Deleting room from sitemap (if it exists in the sitemap)...");
		deleteItemFromSitemap(r.convertToSitemapString(), waitUntilUpdated);
		persistRoom(r, waitUntilUpdated);
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
			PutHEReq put = new PutHEReq(idg.generateMixedCharID(10), ohIP + "/rest/items/" + 
					json.getString("name"), headers, parameters, new int[]{200, 201});
			try {
				he.forwardRequest(put, Thread.currentThread(), waitUntilAdded);
			} catch (EngineException e) {
				AdaptorException a = new AdaptorException("Error in registering item. Item JSON: " + 
						json.toString(), e);
				throw a;
			}
		}
	}
	
	private void addItemToSitemap(String itemStr, boolean waitUntilAdded) throws AdaptorException {
		InsertToFileFEReq req1 = new InsertToFileFEReq(idg.generateERQSRequestID(), itemStr,
				"}", false);
		try {
			sitemapFE.forwardRequest(req1, Thread.currentThread(), waitUntilAdded);
		} catch (EngineException e) {
			throw new AdaptorException("Cannot insert item to OH sitemap file!", e);
		}
	}
	
	private void deleteItemFromSitemap(String itemStr, boolean waitUntilDeleted) throws AdaptorException {
		DeleteStringFEReq req1 = new DeleteStringFEReq(idg.generateERQSRequestID(), itemStr);
		try {
			sitemapFE.forwardRequest(req1, Thread.currentThread(), waitUntilDeleted);
		} catch (EngineException e) {
			throw new AdaptorException("Cannot delete item from OH sitemap file!", e);
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
//			ReadAllLinesFEReq ereq1 = new ReadAllLinesFEReq(idg.generateERQSRequestID());
//			Object o = AbstEngine.forwardRequest(ereq1, sitemapFE, LOG, Thread.currentThread());
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
		DeleteHEReq delete = new DeleteHEReq(idg.generateMixedCharID(10), ohIP + 
				"/rest/items/" + itemName, headers, null, new int[]{200, 404});
		try {
			he.forwardRequest(delete, Thread.currentThread(), waitUntilDeleted);
		} catch (EngineException e) {
			AdaptorException a = new AdaptorException("Cannot delete item " + itemName + " from item "
					+ "registry!", e);
			throw a;
		}
	}
	
	/**
	 * Converts a string of characters containing all the items from OpenHAB to JSONObjects. <i>This method only 
	 * accepts the string representation of the response of OpenHAB GET /items function in REST API</i>
	 * 
	 * @param s The string of characters representing the OpenHAB GET /items response
	 * @return An array of JSONObjects that contain all the items retrieved from OpenHAB
	 
	private JSONObject[] parseAllItemsToJSON(String s) {
		LOG.trace("Converting " + s + " to JSONObjects...");
		s = s.substring(1, s.length() - 2);
		LOG.fatal(s);
		return null;
	}*/
	
	public String getOH_IP() {
		return ohIP;
	}
	
	public String getOHSitemapName() {
		return ohSitemapName;
	}

	@Override
	protected boolean checkBinding(Binding b) {
		// OHAdaptor applies to ALL properties
		return true;
	}

	@Override
	public void updateDeviceRoom(Device d, boolean waitUntilUpdated) throws AdaptorException {
		LOG.trace("Updating device parent room thru regular device persistence...");
		persistDevice(d, waitUntilUpdated);
		
		LOG.trace("Deleting device from sitemap...");
		deleteItemFromSitemap(d.convertToSitemapString(), false);
		
		if(d.getRoom() == null) {
			LOG.trace("Adding device to sitemap since it has no parent room...");
			addItemToSitemap(d.convertToSitemapString(), waitUntilUpdated);
		}
	}

	@Override
	public void updateRoomParent(Room r, boolean waitUntilUpdated) throws AdaptorException {
		LOG.trace("Updating room parent thru regular room persistence...");
		persistRoom(r, waitUntilUpdated);
		
		LOG.trace("Deleting room from sitemap...");
		deleteItemFromSitemap(r.convertToSitemapString(), false);
		
		if(r.getRoom() == null) {
			LOG.trace("Adding room to sitemap since it has no parent room...");
			addItemToSitemap(r.convertToSitemapString(), waitUntilUpdated);
		}
	}
}
