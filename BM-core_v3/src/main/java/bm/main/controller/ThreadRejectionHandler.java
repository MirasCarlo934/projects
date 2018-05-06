package bm.main.controller;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

import org.apache.log4j.Logger;

import bm.comms.mqtt.MQTTListener;
import bm.comms.mqtt.MQTTPublisher;
import bm.jeep.device.ResError;

public class ThreadRejectionHandler implements RejectedExecutionHandler {
	private static final Logger LOG = Logger.getLogger("controller.ThreadRejectionHandler");
	
	public ThreadRejectionHandler() {
		
	}

	@Override
	public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
		LOG.error("Failed to process the received request due to system overload!");
	}
}
