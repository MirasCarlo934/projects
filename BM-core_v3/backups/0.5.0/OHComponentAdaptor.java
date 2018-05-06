package main.adaptors.OHAdaptor;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.HashMap;
import java.util.Vector;

import javax.xml.ws.http.HTTPException;

import org.json.JSONObject;

import components.AbstComponent;
import components.properties.AbstProperty;
import components.properties.PropertyMode;
import main.engines.AbstEngine;
import main.engines.FileEngine;
import main.engines.HTTPEngine;
import main.engines.exceptions.EngineException;
import main.engines.requests.EngineErrorResponse;
import main.engines.requests.HTTPEngine.PutHEReq;
import tools.IDGenerator;

public class OHComponentAdaptor extends OHAdaptor {
	private IDGenerator idg = new IDGenerator();

	public OHComponentAdaptor(String logDomain, String ohIP, HTTPEngine he, FileEngine rulesFE, 
			FileEngine sitemapFE, HashMap<String, String> itemsList) {
		super(logDomain, OHComponentAdaptor.class.getSimpleName(), ohIP, he, rulesFE, sitemapFE,
				itemsList);
	}

	/**
	 * Registers an item representation of the specified component to the OpenHAB registry 
	 * using REST API.
	 * 
	 * @param c The component to be registered as an item to the OpenHAB registry
	 * @param ohIP The base IP address of the OpenHAB instance
	 * @return <b><i>true</i></b> if registration is successful, a <b>ResError</b> object
	 * 		detailing the error if not
	 * @throws EngineException 
	 */
	public void addItem(AbstComponent c) throws MalformedURLException, IOException, HTTPException, 
			EngineException {
		LOG.debug("Adding component " + c.getSSID() + " to OpenHAB item registry...");
		//URL url;
		//HttpURLConnection conn = null;
		JSONObject[] itemsJSON = convertComponentToItemsJSON(c);
		
		for(int i = 0; i < itemsJSON.length; i++) {
			JSONObject json = itemsJSON[i];
			HashMap<String, String> headers = new HashMap<String, String>(2,1);
			HashMap<String, String> parameters = new HashMap<String, String>(1,1);
			headers.put("Content-Type", "application/json");
			headers.put("Accept", "application/json");
			parameters.put("null", json.toString());
			PutHEReq put = new PutHEReq(idg.generateMixedCharID(10), ohIP + "/rest/items/" + 
					json.getString("name"), headers, parameters);
			Object o = AbstEngine.forwardRequest(put, he, LOG, Thread.currentThread());
			if(o.getClass().equals(EngineException.class)) {
				EngineException e = (EngineException) o;
				throw e;
			} else {
				int responseCode = (Integer) o;
				if(responseCode != 200 && responseCode != 201) {
					throw new HTTPException(responseCode);
				}
			}
		}
		
		LOG.debug("Component registered successfully!");
	}
	
	public void deleteItem(AbstComponent c) {
		
	}
	
	private JSONObject[] convertComponentToItemsJSON(AbstComponent c) {
		LOG.trace("Converting component " + c.getSSID() + " to OpenHAB items JSON...");
		Vector<JSONObject> itemsJSON = new Vector<JSONObject>(c.getProperties().length, 1);
		
		if(c.getProperties().length > 1) {
			LOG.trace("Component has multiple properties! Creating group item first...");
			JSONObject json = new JSONObject();
			json.put("type", "Group");
			json.put("name", c.getSSID());
			json.put("label", c.getName());
			json.put("groupNames", new String[]{c.getRoom()});
			itemsJSON.add(json);
		}
		
		AbstProperty[] props = c.getProperties();
		for(int i = 0; i < props.length; i++) {
			JSONObject json = new JSONObject();
			AbstProperty p = props[i];
			LOG.trace("Converting property " + p.getSSID() + " to JSON...");
			if(p.getMode().equals(PropertyMode.Null)) 
				continue; //terminates here!!!
			String itemName = c.getSSID() + "_" + p.getSSID();
			String label = null;
			String room = null;
			String icon = null;
			String type = null;
			if(p.getMode().equals(PropertyMode.I))
				type = "String";
			else
				type = itemsList.get(p.getPropValType().toString());
			//System.out.println(type);
			if(c.getProperties().length > 1) {
				label = p.getDisplayName();
				room = c.getSSID();
			} else {
				label = c.getName();
				room = c.getRoom();
				icon = c.getProduct().getOHIcon();
			}
			json.put("name", itemName);
			json.put("type", type);
			json.put("label", label);
			json.put("category", icon);
			json.put("groupNames", new String[]{room});
			itemsJSON.add(json);
		}
		
		LOG.trace("Conversion complete!");
		return itemsJSON.toArray(new JSONObject[itemsJSON.size()]);
	}
}
