/**
 * This is the firmware for a socket controller.
 * Relay should be connected to the pin defined in switchPin.
 * Relay should be wired as Normally Closed.
 */
#include <mainhtml.h>
#include <MqttUtil.h>
#include <SpiffsUtil.h>
#include <SymphonyCore.h>
#include <SymphonyProduct.h>

#include "Arduino.h"
#include "SymphonyCore.h"


Symphony s = Symphony();
SymphProduct product = SymphProduct();
int buffTimerMins = 1800000; //default timer is 30mins
boolean timerStarted = false;
int duration = 1800000;  //default timer is 30mins
//int switchPin = 16;  //new socket
int switchPin = 12;		//old socket
/*
1	Switch1
2	Timer
3	StartTimer
*/
enum propertyIndex : uint8_t {
	Switch1 = 0,	//digital
	Timer = 1,		//analog
	StartTimer = 2,	//digital
};

/*
 * Callback for Websocket events
 */
int WsCallback(uint8_t * payload, size_t length) {
	WsData wsdata = WsData(payload, length);
	Serial.printf("WsCallback payload=%s ssid=%s value=%s\n", payload, wsdata.getSSID().c_str(), wsdata.getValue().c_str());
	product.setValue(wsdata.getSSID(), 1-atoi(wsdata.getValue().c_str()));
	return 0;
}
/*
 * Callback for MQTT events
 */
attribStruct MyMqttCallback(attribStruct property, int scmd) {
//
//	MqttUtil::product.setValue(property.ssid, scmd);
//	attribStruct returnPs;
//	returnPs.pin = 15;
//	returnPs.gui.value = scmd;
//	returnPs.gui.pinType = SLIDER_OUT;
//	return returnPs;

	MqttUtil::product.setValue(property.ssid, scmd);
	MqttUtil::product.setDone(property.ssid);
	switch (MqttUtil::product.getProperty(property.ssid).index) {
		case StartTimer:
			startTimer();
			break;
	    case Timer:
	    	setTimer(scmd);
	    	break;
	    case Switch1:
	    	Serial.printf("Will set pin%d=%d\n", switchPin, 1-scmd);
	    	digitalWrite(switchPin, 1-scmd);
	    	break;
	}
	return (MqttUtil::product.getProperty(property.ssid));
}
void setTimer(int timeMins) {
	duration = timeMins * 60000;
	buffTimerMins = timeMins * 60000;
	Serial.printf("Timer will be set to %dmins.\n", timeMins);
}

void startTimer() {
	timerStarted = true;
	Serial.printf("Timer started.\n");
}
void handleOn(){
	digitalWrite(switchPin, 0);
	Symphony::setProperty("0006", "0");
	s.sendResponse("Successfully turned on.");
}
void handleOff(){
	digitalWrite(switchPin, 1);
	Symphony::setProperty("0006", "1");
	s.sendResponse("Successfully turned off.");
}
void handleToggle(){
	int state = !digitalRead(switchPin);
//	digitalWrite(12, !digitalRead(12));
	char buffer [3];
	itoa (state,buffer,10);
	MqttUtil::sendCommand("0006", state);
	Symphony::setProperty("0006", buffer);
	s.sendResponse("Success");
}
void setup()
{
	Serial.begin(115200);
	Serial.println("\n************START Symphony***************1");
	product.productType = "0003";
	product.room = "U7YY";  //salas
	product.name = "WallSocket";
//	int pIndex;
	product.addProperty(Switch1, "0006", false, switchPin, SymphProduct::createGui("Socket", BUTTON_OUT, "Switch1", 0, 1, 1));
//	product.addProperty(Timer, "0007", false, SymphProduct::createGui("Timer", BUTTON_OUT,"Start",0, 1, 0));
//	product.addProperty(StartTimer, "0008", false, SymphProduct::createGui("Timer", SLIDER_OUT,"Mins",0, 60, 0));
	digitalWrite(switchPin, 0);	//our socket is Normally Closed, to ensure state, we have to manually set it to 0=ON
//	product.print();
	s.setProduct(product);  //always set the product first before running the setup
	s.setWsCallback(&WsCallback);
	s.setMqttCallback(&MyMqttCallback);
	s.on("/on", handleOn);
	s.on("/off", handleOff);
	s.on("/toggle", handleToggle);
	s.setup();
	Serial.println("************END***************");
}

void loop()
{
	s.loop();
	delay(200);
//	if (timerStarted) {
//		duration = duration - 200;
////		Serial.printf("Duration is %d\n", duration);
//		if ( duration < 0 ) {
//			digitalWrite(switchPin, !digitalRead(switchPin));
//			Serial.println("Resetting the timer.");
//			Symphony::setProperty("0008", "0");
//			char buffer [3];
//			itoa (digitalRead(switchPin),buffer,10);
//			Symphony::setProperty("0006", buffer);
//			MqttUtil::sendCommand("0008", 0);
//			timerStarted = false;
//			duration = buffTimerMins;
//		}
//	}
}
