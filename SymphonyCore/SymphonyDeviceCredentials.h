/*
 * SymphonyProduct.h
 *
 * This defines the different Symphony products.
 * This is from the COMPONENTS table
 *    COMPONENTS
 *      ssid
 *      topic
 *      mac
 *      name
 *      room
 *      functn
 *      active
 *
 *  Created on: Mar 11, 2017
 *      Author: cels
 */
#ifndef SYMPHONYPRODUCT_H_
#define SYMPHONYPRODUCT_H_

#include <Arduino.h>

#define DEBUG_

//_OUT means pinmode is OUTPUT, _IN means pinmode is INPUT
enum guiType : uint8_t { RADIO_OUT = 1, BUTTON_OUT = 2, SLIDER_OUT = 3 , RADIO_IN = 5, BUTTON_IN = 6, SLIDER_IN = 7, UNDEF = 99};

/*
 * This is for the display of the component in the html
 */
struct guiStruct {
	guiType pinType;
	String label;

	String group;
};

/*
 * This is from the COMPROPLIST table
 *    COMPROPLIST
 *      com_type
 *      prop_type
 *      disp_name
 *      ssid
 *      index
 */
struct propStruct {
	uint8_t index;            //COMPROPLIST.index
	String ssid;              //COMPROPLIST.ssid
	boolean usePin = false;   //used internally.  true: pin is mapped and can be used directly to set the ESP pin. false: pin is not mapped and cannot be used directly
	uint8_t pin = 12;         //ESP pin, used internally.  the ESP pin that can be directly controlled
	int min;
	int max;
	int value;
	guiStruct gui;

	boolean commandDone = false;	//used internally. true:command already done, false:command not yet done, any implementing object should handle command
};

/*
 * This is from the COMPONENTS table
 *    COMPONENTS
 *      ssid
 *      topic
 *      mac
 *      name
 *      room
 *      functn
 *      active
 */
class SymphProduct {
private:
	String productID;
//	int pIndex = 0;
	uint8_t size = 0;
	propStruct* properties = NULL;
public:
//    String deviceID;                      //COMPONENTS.ssid
//    String topic;                         //COMPONENT.topic
//    String mac;                           //COMPONENT.mac
//    String room;                          //COMPONENT.room
//    String name = "";                          //COMPONENTS.name
    String array = "";
//    propertyStruct *properties = NULL;    //reference to properties which is derived from COMPROPLIST
    SymphProduct(String productID, uint8_t size);  //default constructor
//    int addProperty(uint8_t index, uint8_t pin, propertyType pinType, boolean usePin, String ssid, String label, int minValue, int maxValue, int value);  //returns the current index
    void addProperty(uint8_t index, /*String ssid, */boolean usePin, int8_t pin, int min, int max, int value, guiStruct gui);
    void addProperty(uint8_t index, /*String ssid, */boolean usePin, int min, int max, int value, guiStruct gui); //an overloaded function. for logical properties that are not physically connected to a pin
    propStruct* getProperties();
//    propStruct getProperty(String ssid);
    propStruct getKeyVal(int index);
//    void setValue(String ssid, int value);
    void print();
    uint8_t getSize();
    void constructHtml();
    String getHtml();
    static guiStruct createGui(String group, guiType pinType, String label);

    char* getID();
};

class SymphDevice {
private:
	String deviceID;
	String name;
	String topic;
	String mac;
	String room;
	SymphProduct product;
public:
	SymphDevice(String deviceID, String name, String topic, String mac, String room,
			SymphProduct product);
	SymphDevice(String mac, SymphProduct product);

	void setID(char* id);
	char* getID();
	void setName(char* name);
	char* getName();
	void setTopic(char* topic);
	char* getTopic();
	void setRoom(char* roomID);
	char* getRoom();

	SymphProduct getProduct();
	propStruct getProperty(uint8_t index);
	void setPropertyValue(uint8_t index, int value);
	void setDone(uint8_t index);
};

#endif /* SYMPHONYPRODUCT_H_ */
