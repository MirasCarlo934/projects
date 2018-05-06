package bm.comms;

import java.util.LinkedList;

import org.apache.log4j.Logger;

import bm.jeep.RawMessage;
import bm.main.controller.Controller;

public abstract class Listener {
	protected Logger LOG;
	protected Controller controller;
	protected Sender sender;
	protected LinkedList<RawMessage> rawMessageQueue;
	
	public Listener(String name, String logDomain, Controller controller, Sender sender, 
			LinkedList<RawMessage> rawMessageQueue) {
		this.controller = controller;
		this.sender = sender;
		this.rawMessageQueue = rawMessageQueue;
		LOG = Logger.getLogger(logDomain + "." + name);
	}
}
