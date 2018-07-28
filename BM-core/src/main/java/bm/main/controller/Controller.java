package bm.main.controller;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import bm.main.Maestro;
import bm.main.repositories.DeviceRepository;
import bm.mqtt.MQTTListener;

public class Controller {
	private Logger LOG;
	public int processCounter = 1;
	private ControllerModuleFactory cmf;
	private ThreadPool threadPool;
	
	public Controller(String logDomain, ControllerModuleFactory controllerModuleFactory, ThreadPool threadPool) {
		LOG = Logger.getLogger(logDomain + ".Controller");
		this.threadPool = threadPool;
		this.cmf = controllerModuleFactory;
		LOG.info("Controller constructed!");
	}

//	public void processMQTTMessage(MqttMessage mqttMessage) {
//		LOG.info("Request received!");
//		ControllerModule module = cmf.createControllerModule(processCounter, mqttMessage);
//		threadPool.execute(module);
//		processCounter++;
//	}
	
	public void processRequest(String request) {
		LOG.info("Request received!");
		ControllerModule module = cmf.createControllerModule(processCounter, request);
		threadPool.execute(module);
		processCounter++;
	}
}