package bm.comms.mqtt;

import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;

import bm.comms.Sender;
import bm.comms.mqtt.objects.MQTTMessage;
import bm.jeep.JEEPMessage;
import bm.jeep.JEEPResponse;
import bm.jeep.device.JEEPErrorResponse;
import bm.main.repositories.DeviceRepository;

public class MQTTPublisher extends Sender {
	private MQTTClient client;
	private String default_topic;
	private String error_topic;
	private DeviceRepository dr;
	protected LinkedList<MQTTMessage> queue = new LinkedList<MQTTMessage>();
	
	public MQTTPublisher(String name, String logDomain, String default_topic, String error_topic, 
			DeviceRepository dr, int secondsToWaitBeforeResend, int resendTimeout) {
		super(logDomain, name, secondsToWaitBeforeResend, resendTimeout);
		this.default_topic = default_topic;
		this.error_topic = error_topic;
		this.dr = dr;
		LOG.info(name + " started!");
	}
	
	public void setClient(MQTTClient client) {
		this.client = client;
	}
	
	@Override
	public void run() {
		while(!Thread.currentThread().isInterrupted()) {
			if(!queue.isEmpty()) {
				MQTTMessage m = queue.removeFirst();
				LOG.debug("Publishing message:" + m.message + " to " + m.topic);
				MqttMessage payload = new MqttMessage(m.message.getBytes());
				payload.setQos(2);
				try {
					client.publish(m.topic, payload);
				} catch (MqttPersistenceException e) {
					LOG.error("Cannot publish message " + m.message + " to topic \"" + m.topic + "\"!", e);
				} catch (MqttException e) {
					LOG.error("Cannot publish message " + m.message + " to topic \"" + m.topic + "\"!", e);
				}
			}
		}
	}
	
	@Override
	public void sendJEEPMessage(JEEPMessage message) {
		publish(message);
	}
	
	@Override
	public void sendErrorResponse(JEEPErrorResponse error) {
		if(error.isComplete()) {
			publish(error);
		} else {
			publishToErrorTopic(error);
		}
	}
	
	/**
	 * Publish to MQTT with String message
	 * @param destination The topic/cid to publish to
	 * @param message The message
	 */
	public void publish(String destination, String message) {
		String topic = destination;
		if(dr.getDevice(destination) != null) { //get Component if destination is a CID
			topic = dr.getDevice(destination).getTopic();
		}
		LOG.trace("Adding new MQTTMessage to topic '" + topic + "' to queue...");
		queue.add(new MQTTMessage(topic, message, Thread.currentThread()));
	}
	
	/**
	 * Publish to MQTT with a specific destination
	 * 
	 * @param destination The topic/CID to publish to
	 * @param message The message
	 */
	public void publish(String destination, JEEPResponse response) {
		publish(destination, response.toString());
	}
	
	/**
	 * Publish a JEEPMessage to MQTT
	 * 
	 * @param message The JEEPMessage
	 */
	public void publish(JEEPMessage message) {
		String topic = message.getCID();
		publish(topic, message.toString());
	}
	
	public void publishToDefaultTopic(String message) {
		publish(default_topic, message);
	}
	
	public void publishToDefaultTopic(JEEPResponse response) {
		publish(default_topic, response.toString());
	}
	
	public void publishToErrorTopic(String message) {
		publish(error_topic, message);
	}
	
	public void publishToErrorTopic(JEEPResponse response) {
		publish(error_topic, response.toString());
	}
}
