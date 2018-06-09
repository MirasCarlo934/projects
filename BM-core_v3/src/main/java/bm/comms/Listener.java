package bm.comms;

import java.util.LinkedList;

import org.apache.log4j.Logger;

import bm.jeep.RawMessage;
import bm.main.controller.Controller;

public abstract class Listener {
	protected Logger LOG;
	protected Protocol protocol;
	private Controller controller;
	
	public Listener(String name, String logDomain, Controller controller) {
		this.controller = controller;
		LOG = Logger.getLogger(logDomain + "." + name);
	}
	
	/**
	 * Sends a RawMessage to be processed by the <i>Controller</i>.
	 * @param msg The RawMessage to be sent
	 */
	protected void sendRawMessageToContoller(RawMessage msg) {
		controller.addRawMessage(msg);
	}

    public void setProtocol(Protocol protocol) {
        this.protocol = protocol;
    }
}
