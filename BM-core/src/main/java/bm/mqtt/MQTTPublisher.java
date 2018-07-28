package bm.mqtt;

import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;

import bm.jeep.AbstResponse;
import bm.main.repositories.DeviceRepository;
import bm.mqtt.objects.MQTTMessage;

public class MQTTPublisher extends TimerTask {
	private Logger logger;
	private MQTTClient client;
	private String default_topic;
	private String error_topic;
	private LinkedList<MQTTMessage> queue = new LinkedList<MQTTMessage>();
	private Timer publishTimer = new Timer("MQTTHandler");
	private DeviceRepository cr;
	
	public MQTTPublisher(String logDomain, String default_topic, String error_topic, DeviceRepository cr/*, 
			MQTTClient mqttClient*/) {
		this.logger = Logger.getLogger(logDomain + "." + MQTTPublisher.class.getSimpleName());
		this.default_topic = default_topic;
		this.error_topic = error_topic;
		this.cr = cr;
	}
	
	public void setClient(MQTTClient client) {
		this.client = client;
		publishTimer.schedule(this, 0, 50);
	}
	
	public void run() {
		if(!queue.isEmpty()) {
			MQTTMessage m = queue.removeFirst();
			logger.debug("Publishing message:" + m.message + " to " + m.topic);
			MqttMessage payload = new MqttMessage(m.message.getBytes());
			payload.setQos(0);
			try {
				client.publish(m.topic, payload);
			} catch (MqttPersistenceException e) {
				logger.error("Cannot publish message:" + m.message + " to topic:" + m.topic + "!", e);
			} catch (MqttException e) {
				logger.error("Cannot publish message:" + m.message + " to topic:" + m.topic + "!", e);
			}
		}
	}
	
	/**
	 * Publish to MQTT with String message
	 * @param destination The topic/cid to publish to
	 * @param message The message
	 */
	public void publish(String destination, String message) {
		String topic = destination;
		if(cr.getDevice(destination) != null) { //get Component if destination is a CID
			topic = cr.getDevice(destination).getTopic();
		}
		logger.trace("Adding new MQTTMessage to topic '" + topic + "' to queue...");
		queue.add(new MQTTMessage(topic, message, Thread.currentThread()));
	}
	
	/**
	 * Publish to MQTT with a specific destination
	 * @param destination The topic/cid to publish to
	 * @param message The message
	 */
	public void publish(String destination, AbstResponse response) {
		publish(destination, response.toString());
	}
	
	/**
	 * Publish to MQTT with the CID of the response
	 * @param destination The topic/cid to publish to
	 * @param message The message
	 */
	public void publish(AbstResponse response) {
//		String topic = cr.getComponent(response.cid).getTopic();
		String topic = response.cid;
		publish(topic, response.toString());
	}
	
	public void publishToDefaultTopic(String message) {
		publish(default_topic, message);
	}
	
	public void publishToDefaultTopic(AbstResponse response) {
		publish(default_topic, response.toString());
	}
	
	public void publishToErrorTopic(String message) {
		publish(error_topic, message);
	}
	
	public void publishToErrorTopic(AbstResponse response) {
		publish(error_topic, response.toString());
	}
}
