package bm.main.controller;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

import org.apache.log4j.Logger;

import bm.jeep.ResError;
import bm.mqtt.MQTTListener;
import bm.mqtt.MQTTPublisher;

public class ThreadRejectionHandler implements RejectedExecutionHandler {
	//LATER  ThreadRejectionHandler: resolve MQTTPublisher dependency
	private static final Logger LOG = Logger.getLogger("controller.ThreadRejectionHandler");
//	private MQTTPublisher mp;
	
	public ThreadRejectionHandler(/*MQTTPublisher mp*/) {
//		this.mp = mp;
	}

	@Override
	public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
		LOG.error("Failed to process the received request due to system overload!");
//		mp.publishToErrorTopic("Controller failed to process the received request due to "
//				+ "system overload!");
	}
}
