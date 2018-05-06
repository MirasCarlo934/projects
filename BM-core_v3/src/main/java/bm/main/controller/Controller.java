package bm.main.controller;

import java.util.LinkedList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONException;
import org.json.JSONObject;

import bm.comms.Sender;
import bm.comms.mqtt.MQTTListener;
import bm.comms.mqtt.MQTTPublisher;
import bm.jeep.JEEPMessageType;
import bm.jeep.JEEPResponse;
import bm.jeep.RawMessage;
import bm.jeep.device.JEEPErrorResponse;
import bm.jeep.device.ReqRequest;
import bm.main.BusinessMachine;
import bm.main.ConfigLoader;
import bm.main.interfaces.Initializable;
import bm.main.modules.MultiModule;
import bm.main.modules.SimpleModule;
import bm.main.repositories.DeviceRepository;

public class Controller implements Runnable {
	private Logger LOG;
	private ConfigLoader cl;
	private LinkedList<RawMessage> rawReqQueue;
	private LinkedList<SimpleModule> moduleQueue;
	private DeviceRepository dr;
	private int rrn = 1;
	
	public Controller(String logDomain, LinkedList<RawMessage> rawReqQueue, LinkedList<SimpleModule> moduleQueue, 
			ConfigLoader configLoader, DeviceRepository deviceRepository/*, MQTTPublisher mqttPublisher*/) {
		LOG = Logger.getLogger(logDomain + ".Controller");
		this.cl = configLoader;
		this.rawReqQueue = rawReqQueue;
		this.moduleQueue = moduleQueue;
		this.dr = deviceRepository;
		LOG.info("Controller started!");
	}
	
	/**
	 * Processes the request intercepted by a <i>Listener</i> object.
	 * 
	 * @param request The request in string format
	 * @param sender The <i>Sender</i> object paired with the <i>Listener</i>
	 */
	@Override
	public void run() {
		while(!Thread.currentThread().isInterrupted()) {
			RawMessage rawMsg = rawReqQueue.poll();
			if(rawMsg != null) {
				LOG.trace("New request found! Checking primary validity...");
				if(checkPrimaryMessageValidity(rawMsg) == JEEPMessageType.REQUEST) {
					ReqRequest r = new ReqRequest(new JSONObject(rawMsg.getMessageStr()), rawMsg.getSender());
					String rty = r.getString("RTY");	
					LOG.trace("Retrieving module for RTY '" + rty + "'");
					SimpleModule m = (SimpleModule) cl.getApplicationContext().getBean(rty);
					m.setRequest(r);
					LOG.trace("Adding module to ModuleQueue (RRN:" + rrn + ")");
					moduleQueue.add(m);
					rrn++;
				} else if(checkPrimaryMessageValidity(rawMsg) == JEEPMessageType.RESPONSE) {
					JEEPResponse r = new JEEPResponse(new JSONObject(rawMsg.getMessageStr()), 
							rawMsg.getSender());
					String rty = r.getJSON().getString("RTY");	
					LOG.trace("Retrieving module for RTY '" + rty + "'");
					MultiModule m = (MultiModule) cl.getApplicationContext().getBean(rty);
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
	 * @param request The Request object
	 * @return <b><i>True</b></i> if the request is valid, <b><i>false</i></b> if: <br>
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
		
		//#1: Checks if the intercepted request is in proper JSON format
		try {
			json = new JSONObject(request);
		} catch(JSONException e) {
			sendError("Improper JSON construction!", rawMsg.getSender());
			return null;
		}
		
		//#2: Checks if there are missing primary request parameters
		if(!json.keySet().contains("RID") || !json.keySet().contains("CID") || !json.keySet().contains("RTY")) {
			sendError("Request does not contain all primary request parameters!", rawMsg.getSender());
			return null;
		}
		
		//#3: Checks if the primary request parameters are null/empty
		if(json.getString("RID").equals("") || json.getString("RID") == null) {
			sendError("Null RID!", rawMsg.getSender());
			return null;
		} else if(json.getString("CID").equals("") || json.getString("CID") == null) {
			sendError("Null DID!", rawMsg.getSender());
			return null;
		} else if(json.getString("RTY").equals("") || json.getString("RTY") == null) {
			sendError("Null RTY!", rawMsg.getSender());
			return null;
		}
		
		//#4: Checks if CID exists
		if(json.getString("RTY").equals("register") || 
				(json.getString("RTY").equals("getRooms") && json.getString("CID").equals("default_topic")));
		else if(!dr.containsComponent(json.getString("CID"))) {
			sendError("CID does not exist!", rawMsg.getSender());
			return null;
		}
		
		//#5 Checks if RTY exists
		boolean b = false;
		if(cl.getApplicationContext().containsBean(json.getString("RTY"))) {
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
			sendError("Invalid RTY!", rawMsg.getSender());
			return null;
		}
	}
	
	private void sendError(String message, Sender sender) {
		LOG.error(message);
		sender.sendErrorResponse(new JEEPErrorResponse(message, sender));
	}
}