package bm.main.controller;

import java.util.LinkedList;

import bm.comms.Protocol;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.context.ApplicationContext;

import bm.comms.Sender;
import bm.jeep.JEEPMessageType;
import bm.jeep.JEEPResponse;
import bm.jeep.RawMessage;
import bm.jeep.device.JEEPErrorResponse;
import bm.jeep.device.ReqRequest;
import bm.main.Maestro;
import bm.main.modules.MultiModule;
import bm.main.modules.SimpleModule;
import bm.main.repositories.DeviceRepository;

public class Controller implements Runnable {
	private Logger LOG;
	private LinkedList<RawMessage> rawMsgQueue;
	private LinkedList<SimpleModule> moduleQueue;
	private DeviceRepository dr;
	private int rrn = 1;
	
	public Controller(String logDomain, LinkedList<RawMessage> rawReqQueue, LinkedList<SimpleModule> moduleQueue, 
			/*ConfigLoader configLoader, */DeviceRepository deviceRepository/*, MQTTPublisher mqttPublisher*/) {
		LOG = Logger.getLogger(logDomain + ".Controller");
		this.rawMsgQueue = rawReqQueue;
		this.moduleQueue = moduleQueue;
		this.dr = deviceRepository;
		LOG.info("Controller started!");
	}
	
	/**
	 * Adds a RawMessage to the RawMessage queue.
	 * @param msg
	 */
	public void addRawMessage(RawMessage msg) {
		rawMsgQueue.add(msg);
	}
	
	/**
	 * Processes the request intercepted by a <i>Listener</i> object.
	 */
	@Override
	public void run() {
		while(!Thread.currentThread().isInterrupted()) {
			RawMessage rawMsg = rawMsgQueue.poll();
			if(rawMsg != null) {
				LOG.trace("New request found! Checking primary validity...");
				ApplicationContext appContext = Maestro.getApplicationContext();
				if(checkPrimaryMessageValidity(rawMsg) == JEEPMessageType.REQUEST) {
					ReqRequest r = new ReqRequest(new JSONObject(rawMsg.getMessageStr()),
							rawMsg.getProtocol());
					String rty = r.getString("RTY");	
					LOG.trace("Retrieving module for RTY '" + rty + "'");
					SimpleModule m = (SimpleModule) appContext.getBean(rty);
					m.setRequest(r);
					LOG.trace("Adding module to ModuleQueue (RRN:" + rrn + ")");
					moduleQueue.add(m);
					rrn++;
				} else if(checkPrimaryMessageValidity(rawMsg) == JEEPMessageType.RESPONSE) {
					JEEPResponse r = new JEEPResponse(new JSONObject(rawMsg.getMessageStr()), 
							rawMsg.getProtocol());
					String rty = r.getJSON().getString("RTY");	
					LOG.trace("Retrieving module for RTY '" + rty + "'");
					MultiModule m = (MultiModule) appContext.getBean(rty);
					LOG.trace("Returning response for '" + m.getName() + "' module. [RID: " + 
							r.getRID() + "]");
					m.returnResponse(r);
				}
			}
		}
	}
	
	/**
	 * Checks if the raw JEEP message string contains all the required primary parameters
	 * 
	 * @param rawMsg The RawMessage object
	 * @return The JEEP message type of the request if valid (either <b><i>JEEPMessageType.REQUEST</b></i>
	 * 		or <b><i>JEEPMessageType.RESPONSE</i></b>, <b><i>null</i></b> if: <br>
	 * 		<ul>
	 * 			<li>The intercepted request is not in JSON format</li>
	 * 			<li>There are missing primary request parameters</li>
	 * 			<li>There are primary request parameters that are null/empty</li>
	 * 			<li>CID does not exist</li>
	 * 			<li>RTY does not exist</li>
	 * 		</ul>
	 */
	private JEEPMessageType checkPrimaryMessageValidity(RawMessage rawMsg) {
		LOG.trace("Checking primary request parameters...");
		JSONObject json;
		String request = rawMsg.getMessageStr();
        ApplicationContext appContext = Maestro.getApplicationContext();
		
		//#1: Checks if the intercepted request is in proper JSON format
		try {
			json = new JSONObject(request);
		} catch(JSONException e) {
			sendError("Improper JSON construction!", rawMsg.getProtocol());
			return null;
		}
		
		//#2: Checks if there are missing primary request parameters
		if(!json.keySet().contains("RID") || !json.keySet().contains("CID") || !json.keySet().contains("RTY")) {
			sendError("Request does not contain all primary request parameters!",
                    rawMsg.getProtocol());
			return null;
		}
		
		//#3: Checks if the primary request parameters are null/empty
		if(json.getString("RID").equals("") || json.getString("RID") == null) {
			sendError("Null RID!", rawMsg.getProtocol());
			return null;
		} else if(json.getString("CID").equals("") || json.getString("CID") == null) {
			sendError("Null DID!", rawMsg.getProtocol());
			return null;
		} else if(json.getString("RTY").equals("") || json.getString("RTY") == null) {
			sendError("Null RTY!", rawMsg.getProtocol());
			return null;
		}
		
		//#4: Checks if CID exists
		if(json.getString("RTY").equals("register") || 
				(json.getString("RTY").equals("getRooms") && json.getString("CID").equals("default_topic")));
		else if(!dr.containsDevice(json.getString("CID"))) {
			sendError("CID does not exist!", rawMsg.getProtocol());
			return null;
		}
		
		//#5 Checks if RTY exists
		boolean b = false;
		if(appContext.containsBean(json.getString("RTY"))) {
			b = true;
		}
		
		if(b) {
			LOG.trace("Checking if message is a request or response...");
			if(json.has("success")) {
				LOG.trace("Primary message parameters good to go!");
				return JEEPMessageType.RESPONSE;
			} else {
				LOG.trace("Primary message parameters good to go!");
				return JEEPMessageType.REQUEST;
			}
		}
		else {
			sendError("Invalid RTY!", rawMsg.getProtocol());
			return null;
		}
	}
	
	private void sendError(String message, Protocol protocol) {
		LOG.error(message);
		protocol.getSender().sendErrorResponse(new JEEPErrorResponse(message, protocol));
	}
}