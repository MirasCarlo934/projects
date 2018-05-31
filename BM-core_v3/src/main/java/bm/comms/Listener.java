package bm.comms;

import java.util.LinkedList;

import org.apache.log4j.Logger;

import bm.jeep.RawMessage;
import bm.main.controller.Controller;

public abstract class Listener {
	protected Logger LOG;
	private Controller controller;
	protected Sender sender;
	
	public Listener(String name, String logDomain, Controller controller, Sender sender) {
		this.controller = controller;
		this.sender = sender;
		LOG = Logger.getLogger(logDomain + "." + name);
	}
	
	/**
	 * Sends a RawMessage to be processed by the <i>Controller</i>.
	 * @param msg The RawMessage to be sent
	 */
	protected void sendRawMessageToContoller(RawMessage msg) {
		controller.addRawMessage(msg);
	}
}
