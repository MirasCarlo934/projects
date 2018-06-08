package bm.comms;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;

import bm.comms.mqtt.MQTTPublisher;
import bm.comms.mqtt.objects.MQTTMessage;
import bm.jeep.JEEPMessage;
import bm.jeep.JEEPRequest;
import bm.jeep.JEEPResponse;
import bm.jeep.device.JEEPErrorResponse;
import bm.main.modules.MultiModule;
import bm.main.modules.SimpleModule;

public abstract class Sender implements Runnable {
	protected Logger LOG;
	private String name;
	protected LinkedList<JEEPMessage> messageQueue = new LinkedList<JEEPMessage>();
	protected HashMap<String, ModuleJEEPRequest> requests = new HashMap<String, ModuleJEEPRequest>(1);
	private Timer timer;
	
	/**
	 * 
	 * @param name
	 * @param logDomain
	 * @param secondsToWaitBeforeResend
	 */
	public Sender(String name, String logDomain, int secondsToWaitBeforeResend, int resendTimeout,
				  boolean isResending) {
		LOG = Logger.getLogger(logDomain + "." + name);
		LOG.info("Starting " + name + " Sender...");
		this.name = name;
		if(isResending) {
            this.timer = new Timer(name + "Timer");
            timer.schedule(new SenderResender(secondsToWaitBeforeResend * 1000,
                            resendTimeout * 1000), 0,
                    secondsToWaitBeforeResend * 1000);
        } else {
		    LOG.warn(name + " Sender is not set to resend outbound requests when a device is nonresponsive!");
        }
        LOG.info(name + " Sender started!");
	}
	
	/**
	 * An abstract method to be initialized by a child class for sending JEEP messages to its corresponding
	 * protocol.
	 * 
	 * @param message The JEEPMessage to send
	 */
	protected abstract void sendJEEPMessage(JEEPMessage message);
	
	/**
	 * Send a JEEPMessage to the protocol of this Sender object.
	 * 
	 * @param message The JEEPMessage to send
	 */
	public void send(JEEPMessage message, SimpleModule module) throws IllegalArgumentException {
		if(message instanceof JEEPResponse) {
			sendJEEPMessage(message);
		} else { //assuming that message is a JEEPRequest
			LOG.trace("JEEPRequest to send! Putting in responses waiting list...");
			ModuleJEEPRequest req;
			try {
				req = new ModuleJEEPRequest((JEEPRequest) message, module); 
				requests.put(req.request.getRID(), req);
				sendJEEPMessage(req.request);
			} catch(ClassCastException e) { //assuming that the specified module is NOT a multimodule
				throw new IllegalArgumentException(module.getName() + " is not a MultiModule object. "
						+ "Only MultiModule objects can send JEEPRequests!");
			}
		}
	}
	
	
	/**
	 * Send a JEEPErrorResponse to the protocol of this Sender object.
	 * 
	 * @param error The JEEPErrorResponse
	 */
	public abstract void sendErrorResponse(JEEPErrorResponse error);
	
	public String getName() {
		return name;
	}
	
	/**
	 * Checks if this Sender is waiting on the response for the specified request.
	 * 
	 * @param rid The RID of the request
	 * @return <b><i>true</i></b> if the request exists, <b><i>false</i></b> otherwise
	 */
	public boolean containsRequest(String rid) {
		return requests.containsKey(rid);
	}
	
	public void removeRequest(String rid) {
		requests.remove(rid);
	}
	
	private class SenderResender extends TimerTask {
		private HashMap<ModuleJEEPRequest, Integer> timeLeft = new HashMap<ModuleJEEPRequest, Integer>(1);
		private int secondsToWaitBeforeResending;
		private int resendTimeout;
		
		private SenderResender(int secondsToWaitBeforeResend, int resendTimeout) {
			this.secondsToWaitBeforeResending = secondsToWaitBeforeResend;
			this.resendTimeout = resendTimeout;
		}

		@Override
		public void run() {
			Iterator<ModuleJEEPRequest> reqs = requests.values().iterator();
			while(reqs.hasNext()) {
				ModuleJEEPRequest req = reqs.next();
				if(!timeLeft.containsKey(req)) {
					timeLeft.put(req, resendTimeout - secondsToWaitBeforeResending);
				}
				if(timeLeft.get(req).intValue() > 0) {
					LOG.debug("No response yet for JEEPRequest " + req.request.getJSON().toString() + 
							". Resending.");
					sendJEEPMessage(req.request);
				} else {
					LOG.warn("Device " + req.request.getCID() + " did not respond to JEEPRequest " + 
							req.request.getJSON().toString() + " within the specified time! " +
							"(Resending will stop)");
					timeLeft.remove(req);
				}
			}
		}
	}
	
	private class ModuleJEEPRequest {
		JEEPRequest request;
		MultiModule module;
		
		private ModuleJEEPRequest(JEEPRequest request, SimpleModule module) throws ClassCastException {
			this.request = request;
			this.module = (MultiModule) module;
		}
	}
}
