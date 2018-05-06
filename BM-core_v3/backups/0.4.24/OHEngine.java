package main.engines;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLConnection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.Executor;

import javax.xml.ws.http.HTTPException;

import org.apache.log4j.Logger;
import org.json.JSONObject;

import components.AbstComponent;
import components.bindings.Binding;
import components.properties.AbstProperty;
import components.properties.PropertyMode;
import components.properties.PropertyValueType;
import json.RRP.ResError;
import main.ComponentRepository;
import main.engines.requests.EngineErrorResponse;
import main.engines.requests.EngineRequest;
import main.engines.requests.DBEngine.SelectDBEReq;
import main.engines.requests.OHEngine.AddItemOHEReq;
import main.engines.requests.OHEngine.OHEngineRequest;
import main.engines.requests.OHEngine.OHRequestType;
import main.engines.requests.OHEngine.RemoveItemOHEReq;
import main.engines.requests.OHEngine.UpdateOHEReq;
import mqtt.MQTTListener;
import tools.IDGenerator;
import tools.JSONReader;
import tools.StringTools;

/**
 * Handles all OpenHAB related interactions for the BM. <br><br>
 * 
 * <b>NOTE:</b> This OHEngine version is only for OpenHab-2.0.0 only! Check backups for 
 * more primitive versions.
 * 
 * @author Carlo
 *
 */
public class OHEngine extends AbstEngine {
	//private static final Logger LOG = Logger.getLogger("BM_LOG.OHEngine");
	private ComponentRepository cr;
	private String ohIP;
	private String os;
	private String OHMqttBroker;
	private String oh_location;
	private String items_filename;
	private String rules_filename;
	private String sitemap_filename;
	private String sitemap_name;
	private FileEngine items;
	private FileEngine sitemap;
	private FileEngine rules;
	private HashMap<String, String> itemsList;
	//private IDGenerator idg = new IDGenerator();
	private OHEngineRequest oher = null;

	public OHEngine(String ohIP, String logDomain, String errorLogDomain, String os, String oh_filepath, String items_filename, 
			String sitemap_filename, String rules_filename, String sitemap_name, 
			ComponentRepository cr, HashMap<String, String> itemsList, String OHMqttBroker) {
		super(logDomain, errorLogDomain, "OHEngine", OHEngine.class.toString());
		this.ohIP = ohIP;
		this.oh_location = oh_filepath;
		this.items_filename = items_filename;
		this.sitemap_filename = sitemap_filename;
		this.rules_filename = rules_filename;
		this.sitemap_name = sitemap_name;
		this.cr = cr;
		this.os = os;
		this.itemsList = itemsList;
		this.OHMqttBroker = OHMqttBroker;
		Object updateResult = updateOH();
		if(updateResult.getClass().equals(EngineErrorResponse.class)) {
			EngineErrorResponse error = (EngineErrorResponse) updateResult;
			LOG.fatal("OHEngine failed to start!");
			if(error.containsException())
				LOG.fatal(error.getMessage(), error.getException());
			else
				LOG.fatal(error.getMessage());
		}
		/*if(connectToFiles()) {
			updateItems();
			updateRules();
			updateSitemap();
			LOG.info("OHEngine started!");
		} else {
			LOG.fatal("OHEngine failed to start!");
		}*/
	}

	@Override
	protected Object processRequest(EngineRequest er) {
		oher = (OHEngineRequest) er;
		
		if(oher.getType() == OHRequestType.start) {
			//startOH();
			connectToFiles();
			updateItems();
			updateRules();
			updateSitemap();
			return oher;
		}
		else if(oher.getType() == OHRequestType.stop) {
			//stopOH();
			oher.setResponse(new EngineErrorResponse("OHEngine", "OpenHAB stop function is defunct! "
					+ "OpenHAB cannot be stopped from within BM!"));
			return oher;
		}
		else if(oher.getType() == OHRequestType.update) {
			updateOH();
			//updateOH()
			/*LOG.debug("Updating OpenHAB files...");
			updateItems();
			updateRules();
			updateSitemap();
			LOG.debug("OpenHAB update complete!");*/
			return oher;
		}
		else if(oher.getType() == OHRequestType.removeItem) {
			RemoveItemOHEReq rioher = (RemoveItemOHEReq) oher;
			rioher.setResponse(removeItem(rioher.getComponent()));
			return rioher.getResponse();
		}
		else if(oher.getType() == OHRequestType.addItem) {
			AddItemOHEReq aioher = (AddItemOHEReq) oher;
			aioher.setResponse(addItem(aioher.getComponent()));
			return aioher.getResponse();
		}
		else {
			return null;
		}
	}
	
	/**
	 * Registers an item representation of the specified component to the OpenHAB registry 
	 * using REST API.
	 * 
	 * @param c The component to be registered as an item to the OpenHAB registry
	 * @return <b><i>true</i></b> if registration is successful, a <b>ResError</b> object
	 * 		detailing the error if not
	 */
	protected Object addItem(AbstComponent c) {
		LOG.debug("Adding component " + c.getSSID() + " to OpenHAB item registry...");
		URL url;
		HttpURLConnection conn = null;
		JSONObject[] itemsJSON = convertComponentToItemsJSON(c);
		
		for(int i = 0; i < itemsJSON.length; i++) {
			JSONObject json = itemsJSON[i];
			try {
				LOG.trace("Establishing connection to OpenHAB...");
				url = new URL("http://" + ohIP + "/rest/items/" + json.getString("name"));
				conn = (HttpURLConnection) url.openConnection();
			} catch (MalformedURLException e) { //for URL
				LOG.error("Error with the URL of the item to be registered!", e);
				EngineErrorResponse error = new EngineErrorResponse("OHEngine", "Error with the "
						+ "URL of the item to be set!", e);
				return error;
			} catch (IOException e) {
				LOG.error("Error establishing connection with OpenHAB!", e);
				EngineErrorResponse error = new EngineErrorResponse("OHEngine", "Error "
						+ "establishing connection with OpenHAB!", e);
				return error;
			} 
			
			try {
				LOG.trace("Registering item " + json.getString("name") + "...");
				LOG.trace("JSON: " + json.toString());
				conn.setRequestMethod("PUT");
				conn.setRequestProperty("Content-Type", "application/json");
				conn.setRequestProperty("Accept", "application/json");
				conn.setDoOutput(true);
				DataOutputStream connOut = new DataOutputStream(conn.getOutputStream());
				try {
					connOut.writeBytes(json.toString());
					connOut.flush();
					connOut.close();
				} catch(IOException e) {
					LOG.error("Error sending JSON to OpenHAB!", e);
					EngineErrorResponse error = new EngineErrorResponse("OHEngine", "Error "
							+ "sending JSON to OpenHAB!", e);
					return error;
				}
				if(conn.getResponseCode() != 200 && conn.getResponseCode() != 201) {
					LOG.error("Error " + conn.getResponseCode() + " in registering item " + 
							json.getString("name") + ". JSON: " + json.toString());
					EngineErrorResponse error = new EngineErrorResponse("OHEngine", "Error "
							+ "sending JSON to OpenHAB!");
					return error;
				}
			} catch (ProtocolException e) {
				LOG.error("Error with protocol!", e);
				EngineErrorResponse error = new EngineErrorResponse("OHEngine", "Error "
						+ "with protocol!", e);
				return error;
			} catch (IOException e1) {
				LOG.error("Error with http connection I/O!", e1);
				EngineErrorResponse error = new EngineErrorResponse("OHEngine", "Error with http "
						+ "connection I/O!", e1);
				return error;
			} 
		}
		
		LOG.debug("Component registered successfully!");
		return true;
	}
	
	/**
	 * Deletes the item representation of the specified component from the OpenHAB registry 
	 * using REST API.
	 * 
	 * @param c
	 * @return <b><i>true</i></b> if removal is successful, a <b>ResError</b> object
	 * 		detailing the error if not
	 */
	protected Object removeItem(AbstComponent c) {
		LOG.debug("Deleting component " + c.getSSID() + " from OpenHAB item registry...");	
		AbstProperty[] props = c.getProperties();
		Vector<String> itemNames = new Vector<String>(c.getProperties().length, 1);
		if(props.length > 1) //for components w/ multiple properties
			itemNames.add(c.getSSID()); //to remove the group component
		for(int i = 0; i < props.length; i++) {
			itemNames.add(c.getSSID() + "_" + props[i].getSSID());
		}
		
		while(!itemNames.isEmpty()) {
			String itemName = itemNames.remove(0);
			removeItem(itemName);
		}
		
		LOG.debug("Component deleted successfully!");
		return true;
	}
	
	/**
	 * Deletes an item from the OpenHAB registry using REST API. This item does not necessarily 
	 * have to be linked with a specific component within the system. 
	 * 
	 * @param SSID The name of the item to be removed
	 * @return <b><i>true</i></b> if removal is successful, a <b>ResError</b> object
	 * 		detailing the error if not
	 */
	protected Object removeItem(String itemName) {
		LOG.trace("Deleting item " + itemName + " from OpenHAB item registry...");	
		URL url;
		HttpURLConnection conn;
		try {
			url = new URL("http://" + ohIP + "/rest/items/" + itemName);
			conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("DELETE");
			if(conn.getResponseCode() == 404) {
				LOG.warn("Item " + itemName + " is already nonexistent in registry!");
			} else if(conn.getResponseCode() != 200) {
				LOG.error("Error " + conn.getResponseCode() + " in deleting item " + 
						itemName);
				EngineErrorResponse error = new EngineErrorResponse("OHEngine", "Error "
						+ "sending JSON to OpenHAB!");
				return error;
			}
		} catch (MalformedURLException e) {
			LOG.error("Error with the URL of the item to be deleted!", e);
			EngineErrorResponse error = new EngineErrorResponse("OHEngine", "Error with the "
					+ "URL of the item to be deleted!", e);
			return error;
		} catch (IOException e) {
			LOG.error("Error establishing connection with OpenHAB!", e);
			EngineErrorResponse error = new EngineErrorResponse("OHEngine", "Error "
					+ "establishing connection with OpenHAB!", e);
			return error;
		}
		LOG.trace("Item deleted successfully!");
		return true;
	}
	
	/**
	 * Updates the OpenHAB system by doing the following:
	 * <ul>
	 * 	<li>Updating the OpenHAB item registry based on the current components</li>
	 * 	<li>Updating the OpenHAB rules file based on the current components</li>
	 * 	<li>Updating the OpenHAB sitemaps file based on the current rooms and components</li>
	 * </ul>
	 * 
	 * @return <b><i>true</i></b> if the update was successful, a <b>ResError</b> object
	 * 		detailing the error if not
	 */
	protected Object updateOH() {
		LOG.debug("Updating OpenHAB...");
		
		LOG.debug("Updating OpenHAB item registry...");
		AbstComponent[] coms = cr.getAllComponents();
		HashMap<String, JSONObject> items = null;
		try {
			JSONObject[] itemsJSON = getAllItemsFromRegistry();
			items = new HashMap<String, JSONObject>(itemsJSON.length);
			for(int i = 0; i < itemsJSON.length; i++) {
				JSONObject itemJSON = itemsJSON[i];
				items.put(itemJSON.getString("name"), itemJSON);
			}
		} catch (HTTPException e) {
			LOG.error("Server responded with an error in getting all items from registry! "
					+ "(Error code: " + e.getStatusCode() + ")");
			EngineErrorResponse error = new EngineErrorResponse("OHEngine", 
					"Server responded with an error in getting all items from registry! "
					+ "(Error code: " + e.getStatusCode() + ")", e);
			return error;
		} catch (MalformedURLException e) {
			LOG.error("Malformed URL in getting all items from registry!");
			EngineErrorResponse error = new EngineErrorResponse("OHEngine", "Malformed URL in "
					+ "getting all items from registry!", e);
			return error;
		} catch (IOException e) {
			LOG.error("Error in connecting with OpenHAB OR with reading from input stream during "
					+ "retrieval of all items!");
			EngineErrorResponse error = new EngineErrorResponse("OHEngine", "Error in connecting "
					+ "with OpenHAB OR with reading from input stream during retrieval of all "
					+ "items!", e);
			return error;
		}
		
		LOG.debug("Deleting undeleted items from registry...");
		for(int i = 0; i < items.size(); i++) { //deletes undeleted items from OpenHAB
			String[] itemNames = items.keySet().toArray(new String[items.size()]);
			String itemName = itemNames[i];
			if(!itemName.contains("_") && !cr.containsComponent(itemName)) {
				removeItem(itemName);
				for(int j = 0; j < itemNames.length; j++) {
					String delName = itemNames[j];
					if(delName.contains(itemName))
						removeItem(delName);
				}
			}
		}
		
		LOG.debug("Adding unadded items to registry...");
		for(int i = 0; i < coms.length; i++) { //adds unadded items from system
			AbstComponent c = coms[i];
			if(!items.containsKey(c.getSSID())) {
				addItem(c);
			}
		}
		
		return true;
	}
	
	/**
	 * Opens a FileHandler connected to the .items, .rules, and .sitemap files in OpenHAB directory	 
	 *
	 * @return <b>True</b> if connection was successful. <b>False</b> if otherwise.
	 */
	private boolean connectToFiles() {
		LOG.info("Connecting to OH files in OpenHAB directory...");
		try {
			//LOG.fatal(items_filepath);
			//LOG.fatal(sitemap_filepath);
			//LOG.fatal(oh_location + "/configurations/rules/" + rules_filename);
			//items = new FileHandler(oh_location + "/conf/items/" + items_filename);
			//rules = new FileHandler(oh_location + "/conf/rules/" + rules_filename);
			//sitemap = new FileHandler(oh_location + "/conf/sitemaps/" + sitemap_filename);
			items = new FileEngine(oh_location + "/" + items_filename, "OH", "error");
			rules = new FileEngine(oh_location + "/" + rules_filename, "OH", "error");
			sitemap = new FileEngine(oh_location + "/" + sitemap_filename, "OH", "error");
			LOG.debug("Connected to OH files!");
			return true;
		} catch (FileNotFoundException e) {
			LOG.error("Cannot open OH files!", e);
			return false;
		}
	}
	
	/**
	 * Updates the contents of the .items file in OpenHAB.
	 */
	private void updateItems() {
		LOG.debug("Updating .items file...");
		//cr.retrieveRooms();
		//cr.retrieveBindings();
		AbstComponent[] coms = cr.getAllComponents();
		Binding[] bindings = cr.getAllBindings();
		HashMap<String, String> rooms = cr.getAllRooms();
		String str = "";
		String groups = "";
		//itemsStr with the mqtt publisher item 
		String itemsStr = "String mqtt_pub \"[%s]\" {mqtt=\">[mqttb:BM:state:*:default]\"}\n\n";		
		
		LOG.debug("Adding rooms to groups declaration...");
		String[] roomIDs = rooms.keySet().toArray(new String[0]);
		for(int i = 0; i < roomIDs.length; i++) {
			String roomID = roomIDs[i];
			String roomName = rooms.get(roomID);
			LOG.trace("Adding room '" + roomName + "' with SSID:" + roomID);
			groups += "Group " + roomID + " \"" + roomName + "\" <attic>\n";
		}
		
		LOG.debug("Adding Components with multiple properties to groups declaration...");
		for(int i = 0; i < coms.length; i++) {
			AbstComponent c = coms[i];
			LOG.trace("Adding component " + c.getSSID() + " with " + c.getProperties().length
					+ " properties to groups");
			if(!c.isActive()) { //if component is not active
				//String COM1 "Component 1 [inactive]" <wallswitch> (ROOM)
				itemsStr += "String " + c.getSSID() + " \"" + c.getName() + " [INACTIVE]\" "
						+ "<" + c.getProduct().getOHIcon() + "> (" + c.getRoom() + ")\n\n";
			}
			else if(c.getProperties().length > 1) {
				//Group COM1 "Component1" <wallswitch> (ROOM)
				groups += "Group " + c.getSSID() + " \"" + c.getName() + "\" <" + 
						c.getProduct().getOHIcon() + "> (" + c.getRoom() + ")\n";
			}
		}
		
		LOG.debug("Adding individual Properties to items declaration...");
		//Template Item:
		//Switch UR6C_0001 "Switch" (U1T4) { mqtt="<[mqttb:openhab/UR6C_topic:command:ON:0001_1],<[mqttb:openhab/UR6C_topic:command:OFF:0001_0],"}
		for(int i = 0; i < coms.length; i++) {
			HashMap<String, String> values = new HashMap<String,String>(1); //for use with BINDINGS
			AbstComponent c = coms[i];
			if(!c.isActive()) continue; //if inactive, do next component
			AbstProperty[] props = c.getProperties();
			for(int j = 0; j < props.length; j++) {
				AbstProperty p = props[j];
				LOG.trace("Adding property " + p.getSSID() + " of component " + c.getSSID());
				String room = null;
				String itemName = null;
				String OH_icon = null;
				if(props.length > 1) { //room = c.SSID
					room = c.getSSID();
					itemName = p.getDisplayName();
				} else { //room = c.room
					room = c.getRoom();
					itemName = c.getName();
					OH_icon = c.getProduct().getOHIcon();
				}
				values.put("mac", c.getMAC());
				values.put("topic", c.getTopic());
				values.put("prop_id", p.getSSID());
				values.put("mqtt_broker", OHMqttBroker);
				
				//Switch COM1_0001 "PropertyName1" <wallswitch> (ROOM) 
				String itemType;
				if(p.getMode().equals(PropertyMode.I)) itemType = "String";
				else itemType = p.getPropValType().toString();
				itemsStr += itemsList.get(itemType) + " " + c.getSSID()
					+ "_" + p.getSSID() + " \"" + itemName;
				if(p.getPropValType().equals(PropertyValueType.string)) {//to add a field variable for String type OH items
					itemsStr += " [%s]";
				}
				itemsStr += "\" ";
				if(OH_icon != null) {
					itemsStr += "<" + OH_icon + "> ";
				}	
				itemsStr += "(" + room + ") { ";
				
				//adding bindings
				for(int k = 0; k < bindings.length; k++) {
					Binding b = bindings[k];
					if((b.getComType().equals("0000") && b.getPropIndex().equals(p.getPropTypeID())) //comtype '0000' means for ALL components
							|| (b.getComType().equals(c.getProduct().getSSID()) && 
							b.getPropIndex().equals(p.getSSID()))) {
						String bind = b.getBinding();
						bind = StringTools.injectStrings(bind, values, new String[]{"{","}"});
						itemsStr += bind + ",";
					}
				}
				itemsStr = itemsStr.substring(0, itemsStr.length() - 1) +  "} \n\n";
			}
		}
		
		str = groups + "\n\n" + itemsStr;
		
		try {
			items.writeToFile(str);
		} catch (IOException e) {
			LOG.error("Cannot write to .items file!", e);
			e.printStackTrace();
			currentRequest.setResponse(new EngineErrorResponse(name, "Cannot write to .items "
					+ "file!", e));
		}
	}
	
	/**
	 * Updates the contents of the .rules file in OpenHAB.
	 */
	private void updateRules() {
		LOG.debug("Updating .rules file...");
		String str = "";
		AbstComponent[] coms = cr.getAllComponents();
		
		for(int i = 0; i < coms.length; i++) {
			AbstComponent c = coms[i];
			AbstProperty[] props = c.getProperties();
			
			HashMap<String, String> values = new HashMap<String, String>(2); //values to be put inside rules files
			values.put("com_ssid", c.getSSID());
			//gets component-specific rules
			try {
				FileEngine specificPropertyRule = new FileEngine
						("resources/openhab/rules/product/" + c.getProduct().getSSID() + ".rules"
								, "OH", "error");
				String[] lines = specificPropertyRule.readAllLines();
				for(int k = 0; k < lines.length; k++) {
					str += StringTools.injectStrings(lines[k], values, 
							new String[]{"[","]"}) + "\n";
				}
			} catch (FileNotFoundException e1) {
				LOG.warn("Rules for component type " + c.getProduct().getSSID() + " not found!");
				//e1.printStackTrace();
			} catch (IOException e) {
				LOG.error("Cannot read lines from " + c.getProduct().getSSID() + ".rules!");
				e.printStackTrace();
			}
			
			//gets general property rules
			for(int j = 0; j < props.length; j++) {
				AbstProperty p = props[j];
				LOG.trace("Updating rules of property " + p.getSSID());
				try { //retrieves rules from general rules files
					values.put("prop_ssid", p.getSSID());
					FileEngine generalPropertyRule = new FileEngine
							("resources/openhab/rules/general/" + p.getPropValType() + ".rules"
									, "OH", "error");
					String[] lines = generalPropertyRule.readAllLines();
					for(int k = 0; k < lines.length; k++) {
						str += StringTools.injectStrings(lines[k], values, 
								new String[]{"[","]"}) + "\n";
					}
				} catch (FileNotFoundException e) {
					LOG.warn("Rules for " + p.getPropValType() + " property value type not "
							+ "found!");
					//e.printStackTrace();
				} catch (IOException e) {
					LOG.error("Cannot read lines from " + p.getPropValType() + ".rules!");
					e.printStackTrace();
				}		        
			}
		}
		
		try {
			rules.writeToFile(str);
		} catch (IOException e) {
			LOG.error("Cannot write to .rules file!", e);
			e.printStackTrace();
			currentRequest.setResponse(new EngineErrorResponse(name, "Cannot write to .rules "
					+ "file!", e));
		}
	}
	
	/**
	 * Updates the contents of the .sitemap file in OpenHAB.
	 */
	private void updateSitemap() {
		LOG.debug("Updating .sitemap file...");
		cr.retrieveRooms();
		HashMap<String, String> rooms = cr.getAllRooms();
		String str = "sitemap home label=\"" + sitemap_name + "\"{ \n"
				+ "Frame {\n";
		
		String[] roomIDs = rooms.keySet().toArray(new String[0]);
		for(int i = 0; i < roomIDs.length; i++) {
			String roomID = roomIDs[i];
			String roomName = rooms.get(roomID);
			str += "Group item=" + roomID + " label=\"" + roomName + "\"\n";
		}
		str += "}\n}";
		
		try {
			sitemap.writeToFile(str);
		} catch (IOException e) {
			LOG.error("Cannot write to .sitemap file!", e);
			e.printStackTrace();
			currentRequest.setResponse(new EngineErrorResponse(name, "Cannot write to .sitemap "
					+ "file!", e));
		}
	}
	
	/**
	 * 
	 * @return A <i>JSONObject</i> array containing all items from the OpenHAB item registry.
	 * @throws MalformedURLException if the URL is malformed
	 * @throws IOException if the connection cannot be established or if the input stream from the
	 * 		connection cannot be read
	 * @throws HTTPException if the server responded with an error
	 */
	private JSONObject[] getAllItemsFromRegistry() throws MalformedURLException, IOException, 
			HTTPException{
		LOG.trace("Retrieving all items from OpenHAB item registry...");
		JSONReader jsonReader = new JSONReader();
		URL url = new URL("http://" + ohIP + "/rest/items?recursive=false");
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		if(conn.getResponseCode() != 200) {
			throw new HTTPException(conn.getResponseCode());
		}
		return jsonReader.readFromInputStream(conn.getInputStream());
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

/*System.out.println(json.toString());
System.out.println(conn.getResponseCode());
BufferedReader connIn = new BufferedReader(new InputStreamReader(
		conn.getInputStream()));
String response = "";
String line = "";
while((line = connIn.readLine()) != null) {
	response += line;
}
connIn.close();
System.out.println(response);*/

/*LOG.debug("Retrieving rooms from DB...");
Object o = dbe.forwardRequest(new SelectDBEReq(idg.generateMixedCharID(10), roomsTable));
if(o.getClass().equals(ResError.class)) {
	ResError e = (ResError) o;
	LOG.error("Cannot retrieve rooms from DB!");
	LOG.error("Error message: " + e.message);
	return false;
} else {
	ResultSet rs1 = (ResultSet) o;
	try {
		while(rs1.next()) {
			rooms.put(rs1.getString("ssid"), rs1.getString("name"));
		}
		rs1.close();
		LOG.debug("Rooms retrieved!");
	} catch (SQLException e) {
		LOG.error("ResultSet error in retrieving rooms!", e);
		e.printStackTrace();
	}
}
*/