/*
 * MqttUtil.h
 *
 * Utility Helper for connecting to the MQTT Server and parsing of the value objects.
 * We will be using Json objects.
 *
 *  Created on: Mar 11, 2017
 *      Author: cels
 */

#ifndef MQTTUTIL_H_
#define MQTTUTIL_H_

#include <ArduinoJson.h>
#include <PubSubClient.h>
#include <ESP8266WiFi.h>
#include "SymphonyDeviceCredentials.h"

#define DEBUG_

struct prop_declaration {
	String ptype;
	String name;
	String mode;
	int index;
};

class MqttUtil {
	public:
	MqttUtil();
	static PubSubClient *client;
	static const char *clientID;
	static WiFiClient wifiClient;
	static const char* server;      //this is the local instance of MQTT, should be configurable via captive portal
	static int mqttPort;
	static const char* willTopic;
	static boolean isConnectedToBM;
//	static boolean connectToMQTT(const char *id, const char url[], int port, PubSubClient *c, WiFiClient wc, SymphProduct p); //to connect to MQTT server
	static boolean connectToMQTT(const char *id, const char url[], int port, PubSubClient *c, WiFiClient wc, SymphDevice d); //to connect to MQTT server

	static void register_basic(String ngalan, String room, String product);
//	static void register_with_proplist(String ngalan, String room, vector<prop_declaration> proplist);
	static void sendCommand(String ssid, int value);  //should return a response code
	static SymphDevice device;
//	static SymphProduct product;
	static void setMqttCallback(propStruct (* MqttCallback) (propStruct property, int scmd));//todo mar 14 2017, remove this
	static void setCommandCallback(void (* CommandCallback) (String ssid, String cmd));
	static void setBmStatusCB(void (* BmStatusCB) ());
	static void unRegister();

	private:
};


#endif /* MQTTUTIL_H_ */
