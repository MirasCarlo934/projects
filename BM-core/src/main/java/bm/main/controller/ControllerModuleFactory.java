package bm.main.controller;

import org.eclipse.paho.client.mqttv3.MqttMessage;

import bm.main.ConfigLoader;
import bm.main.repositories.DeviceRepository;
import bm.mqtt.MQTTPublisher;

public class ControllerModuleFactory {
	private MQTTPublisher mp;
	private DeviceRepository cr;
	private ConfigLoader cl;

	public ControllerModuleFactory(MQTTPublisher mp, DeviceRepository cr, ConfigLoader configLoader) {
		this.mp = mp;
		this.cr = cr;
		this.cl = configLoader;
	}

//	public ControllerModule createControllerModule(int processNumber, MqttMessage message) {
//		return new ControllerModule(processNumber, message, mp, cr, cl);
//	}
	
	public ControllerModule createControllerModule(int processNumber, String request) {
		return new ControllerModule(processNumber, request, mp, cr, cl);
	}
}