/*
 * SymphonyProduct.cpp
 *
 *  Created on: Mar 11, 2017
 *      Author: cels
 */


#include "SymphonyDeviceCredentials.h"

/****************************************************************************************
 * SymphProduct Class
 public:
    String deviceID;                      //COMPONENTS.ssid
    String topic;                         //COMPONENT.topic
    String mac;                           //COMPONENT.mac
    String room;                          //COMPONENT.room
    String productID;                     //COMPONENTS.product
    propertyStruct *properties;           //reference to properties which is derived from COMPROPLIST
 *
 *   methods:
 ****************************************************************************************/
SymphProduct::SymphProduct(String productID, uint8_t size) { //default constructor
//	deviceID = "0000";
	this->productID = productID;
	this->size = size;
	properties = new propStruct[size];
}

SymphDevice::SymphDevice(String deviceID, String name, String topic, String mac, String room,
		SymphProduct product) {
	this->deviceID = deviceID;
	this->name = name;
	this->topic = topic;
	this->mac = mac;
	this->room = room;
	this->product = product;
}

void SymphDevice::setID(char* id) {
	this->deviceID = id;
}

char* SymphDevice::getID() {
	return deviceID;
}

void SymphDevice::setName(char* name) {
	this->name = name;
}

char* SymphDevice::getName() {
	return name;
}

void SymphDevice::setTopic(char* topic) {
	this->topic = topic;
}

char* SymphDevice::getTopic() {
	return topic;
}

void SymphDevice::setRoom(char* roomID) {
	this->room = roomID;
}

char* SymphDevice::getRoom() {
	return room;
}

SymphProduct SymphDevice::getProduct() {
	return product;
}

propStruct SymphDevice::getProperty(uint8_t index) {
	propStruct prop = product.getProperties()[index];
//    	Serial.printf("i=%i size=%i ID=%s index=%i pin=%u type=%u usepin=%o ssid=%s value=%i\n", i, size, deviceID.c_str(), properties[i].index, properties[i].pin, properties[i].pinType, properties[i].ssid.c_str(), properties[i].value);
	if(prop != NULL) {
#ifdef DEBUG_
		Serial.print("SymphProduct::getProperty index=");Serial.print(index);
		Serial.print("\tsize=");Serial.print(product.size);
		Serial.print("\tID=");Serial.print(deviceID);
		Serial.print("\tpin=");Serial.print(prop.pin);
		Serial.print("\tp.pinType=");Serial.print(prop.gui.pinType);
		Serial.print("\tp.usepin=");Serial.print(prop.usePin);
		Serial.print("\tp.ssid=");Serial.print(prop.ssid);
		Serial.print("\tprop.value=");Serial.println(prop.value);
#endif
		return prop;
	}

	//we did not find any match
	propStruct ps;
	ps.index = -1;
	ps.pin = -1;
	ps.gui.pinType = BUTTON_OUT;
	ps.usePin = false;
	return ps;
}

/*
 * Sets the product property with the given index to commandDone=true
 */
void SymphDevice::setDone(uint8_t index) {
	propStruct prop = getProperty(index);
#ifdef DEBUG_
	Serial.print("\t index=");Serial.print(prop.index);
	Serial.print("\t size=");Serial.print(product.getSize());
	Serial.print("\t ID=");Serial.print(deviceID);
	Serial.print("\t pin=");Serial.print(prop.pin);
	Serial.print("\t p.pinType=");Serial.print(prop.gui.pinType);
	Serial.print("\t p.usepin=");Serial.print(prop.usePin);
	Serial.print("\t p.ssid=");Serial.print(prop.ssid);
	Serial.print("\t prop.value=");Serial.println(prop.value);
#endif
	prop.commandDone = true;
}

/*
 * Sets the value of the product property with the given ssid
 */
void SymphDevice::setPropertyValue(uint8_t index, int value) {
	propStruct prop = getProperty(index);
#ifdef DEBUG_
	Serial.print("\t index=");Serial.print(prop.index);
	Serial.print("\t size=");Serial.print(product.getSize());
	Serial.print("\t ID=");Serial.print(deviceID);
	Serial.print("\t pin=");Serial.print(prop.pin);
	Serial.print("\t p.pinType=");Serial.print(prop.gui.pinType);
	Serial.print("\t p.usepin=");Serial.print(prop.usePin);
	Serial.print("\t p.ssid=");Serial.print(prop.ssid);
	Serial.print("\t prop.value=");Serial.println(prop.value);
#endif
	prop.value = value;
	Serial.print("\t value is set to ");Serial.println(prop.value);
}

/**
 *
 * returns the current index of the properties array
 *
 * index	= the Index for this property
 * ssid		= the SSID of this property (from the COMPROPLIST table)
 * usePin	= if true, pin will be set to the value sent in MQTT and WebSocket.  if false, pin will be set in the callback function.
 * pin		= the corresponding ESP8266 pin where this property is attached.  if < 0, this component is virtual and not attached to a pin
 * gui		= the gui attributes
 * 	pinType		= the type of pin {RADIO_OUT = 1, BUTTON_OUT = 2, SLIDER_OUT = 3 , RADIO_IN = 5, BUTTON_IN = 6, SLIDER_IN = 7, UNDEF = 99}
 * 	label		= the label to be displayed in the WebSocket page
 * 	min			= the minimum value
 * 	max			= the maximum value
 * 	value		= the actual value
 *
 * TODO Apr 16 2017: Need to improve this, this should have the following capabilities
 * 		1. pin should be optional parameter, if it is not needed, then there is no need to set the pinMode
 * 		2. if pin is set, it can either be used or not used.  If not used, pin will not be automatically set, need callback to set is manually.
 */
void SymphProduct::addProperty(uint8_t index, /*String ssid, */boolean usePin, int8_t pin, int min, int max, int value,
		guiStruct gui) {
	propStruct as;
	as.index = index;
	as.pin = pin;
	as.gui = gui;
#ifdef DEBUG_
	Serial.print("SymphProduct::addProperty data index:");Serial.print(as.index);Serial.print(" pin:");Serial.print(as.pin);Serial.print(" type:");Serial.println(as.gui.pinType);
#endif
	if (pin < 0) {
	  //this property is not physical (it is virtual), no need to set the pinmode
#ifdef DEBUG_
		Serial.println("SymphProduct::addProperty this is a virtual pin.");
#endif
	} else {
		if (as.gui.pinType == BUTTON_OUT || as.gui.pinType == RADIO_OUT || as.gui.pinType == SLIDER_OUT) {
			pinMode(as.pin, OUTPUT);
#ifdef DEBUG_
			Serial.printf("SymphProduct::addProperty setting pin%d as OUTPUT.\n", as.pin);
#endif
		} else {
			pinMode(as.pin, INPUT);
#ifdef DEBUG_
			Serial.printf("SymphProduct::addProperty setting pin%d as INPUT.", as.pin);
#endif
		}
	}
	as.usePin = usePin;
#ifdef DEBUG_
	Serial.printf("SymphProduct::addProperty size=%d, sizeof=%d\n", size, sizeof(propStruct));
#endif
	properties[index] = as;
//	pIndex++;
#ifdef DEBUG_
	Serial.print("Done SymphProduct::addProperty data index:");Serial.print(properties[size-1].index);Serial.print("  type:");Serial.println(properties[size-1].gui.pinType);
#endif
}

/**
 * The overloaded addProperty method
 * this is for the virtual property
 */
void SymphProduct::addProperty(uint8_t index, boolean usePin, int min, int max, int value, guiStruct gui) {
	addProperty(index, usePin, -1, min, max, value, gui);
}

propStruct* SymphProduct::getProperties() {
	return properties;
}

/*
 * returns the property specified by the property SSID
 * retuens null if not found
 */
//propStruct SymphProduct::getProperty(String ssid) {
//  for (int i=0; i<size; i++) {
//    if (strcmp(ssid.c_str(), attributes[i].ssid.c_str())==0) {
////    	Serial.printf("i=%i size=%i ID=%s index=%i pin=%u type=%u usepin=%o ssid=%s value=%i\n", i, size, deviceID.c_str(), properties[i].index, properties[i].pin, properties[i].pinType, properties[i].ssid.c_str(), properties[i].value);
//#ifdef DEBUG_
//      Serial.print("SymphProduct::getProperty i=");Serial.print(i);
//	  Serial.print("\tindex=");Serial.print(attributes[i].index);
//	  Serial.print("\tsize=");Serial.print(size);
//	  Serial.print("\tID=");Serial.print(deviceID);
//	  Serial.print("\tpin=");Serial.print(attributes[i].pin);
//	  Serial.print("\tp.pinType=");Serial.print(attributes[i].gui.pinType);
//	  Serial.print("\tp.usepin=");Serial.print(attributes[i].usePin);
//	  Serial.print("\tp.ssid=");Serial.print(attributes[i].ssid);
//	  Serial.print("\tprop.value=");Serial.println(attributes[i].gui.value);
//#endif
//      return attributes[i];
//    }
//  }
//  //we did not find any match
//  propStruct ps;
//  ps.index = -1;
//  ps.pin = -1;
//  ps.gui.pinType = BUTTON_OUT;
//  ps.usePin = false;
//  ps.ssid = "NULL";
//  return ps;
//}

/*
 * Returns the attribStruct data that contains SSID and gui.value of the index in attributes array
 */
propStruct SymphProduct::getKeyVal(int index) {
	return properties[index];
}

/*
 * Utility function to print the product
 */
void SymphProduct::print() {
  for (int i=0; i<size; i++) {
#ifdef DEBUG_
	  Serial.print("SymphProduct::print i=");Serial.print(i);
	  Serial.print("\tindex=");Serial.print(properties[i].index);
	  Serial.print("\tsize=");Serial.print(size);
//      Serial.print("\tID=");Serial.print(deviceID);
      Serial.print("\tp.pin=");Serial.print(properties[i].pin);
      Serial.print("\tp.pinType=");Serial.print(properties[i].gui.pinType);
      Serial.print("\tp.usepin=");Serial.print(properties[i].usePin);
      Serial.print("\tp.ssid=");Serial.print(properties[i].ssid);
      Serial.print("\tprop.value=");Serial.println(properties[i].value);
#endif
  }
}

/*
 * returns the size of the properties array
 */
uint8_t SymphProduct::getSize() {
  return size;
}

/*
 * Returns a string that has html items of the products
 *  <input type='checkbox'> for digital pins
 *   <input type='range'> for analog pins
 */
void SymphProduct::constructHtml() {
  for (int i=0; i <size;i++) {
	  if (i>0)
		  array += ",";
	  if (properties[i].gui.pinType==RADIO_OUT) {
		  //{typ:'Rad',lbl:'RED',val:'0007'}
		  array += "{typ:'Rad',lbl:'";
		  array += properties[i].gui.label;
		  array += "',val:'";
		  array += properties[i].ssid;
		  array += "',grp:'";
		  array += properties[i].gui.group;
		  array += "'}";
	  }
	  if (properties[i].gui.pinType==BUTTON_OUT) {
		  // {typ:'Btn',lbl:'STOP',val:'0009'}
		  array += "{typ:'Btn',lbl:'";
		  array += properties[i].gui.label;
		  array += "',val:'";
		  array += properties[i].ssid;
		  array += "',grp:'";
		  array += properties[i].gui.group;
		  array += "'}";
	  }
	  if (properties[i].gui.pinType==SLIDER_OUT) {
		  //<label class='rng'><div class='lbl'>the range</div><div class='slider'><input type='range' id='Light1:0005' min='0' max='360' onchange='sendRangeWs()'></div></label><br>
		  //{typ:'Rng',lbl:'Hue',val:'0011',min:'0',max:'360'},
		  array += "{typ:'Rng',lbl:'";
		  array += properties[i].gui.label;
		  array += "',val:'";
		  array += properties[i].ssid;
		  array += "',min:'";
		  array += properties[i].min;
		  array += "',max:'";
		  array += properties[i].max;
		  array += "',grp:'";
		  array += properties[i].gui.group;
		  array += "'}";
	  }
	  if (properties[i].gui.pinType==BUTTON_IN) {
		  //<label class='bar'><div class='lbl'>testing lang</div><input type='checkbox' id='Light1:0019'><div class='btn'></div></label><br>
//		  html +="<label class='bar'><div class='lbl'>";
//		  html +=attributes[i].gui.label;
//		  html +="</div><input type='checkbox' disabled id='";
//		  html += name;
//		  html += ":";
//		  html += attributes[i].ssid;
//		  html += "'><div class='btn'></div></label><br>\n";
		  array += "{typ:'Btn',lbl:'";
		  array += properties[i].gui.label;
		  array += "',val:'";
		  array += properties[i].ssid;
		  array += "',grp:'";
		  array += properties[i].gui.group;
		  array += "'}";
	  }
  }
}

/*
 * Returns a string that has html items of the products
 *  <input type='checkbox'> for digital pins
 *   <input type='range'> for analog pins
 */
String SymphProduct::getHtml() {
	return array;
}

guiStruct SymphProduct::createGui(String group, guiType pinType, String label){
	guiStruct gui;
	gui.pinType = pinType;
	gui.label = label;
	gui.group = group;
	return gui;
}

char* SymphProduct::getID() {
	return productID;
}

