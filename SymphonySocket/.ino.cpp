#ifdef __IN_ECLIPSE__
//This is a automatic generated file
//Please do not modify this file
//If you touch this file your change will be overwritten during the next build
//This file has been generated on 2018-07-22 20:00:06

#include "Arduino.h"
#include <mainhtml.h>
#include <MqttUtil.h>
#include <SpiffsUtil.h>
#include <SymphonyCore.h>
#include <SymphonyProduct.h>
#include "Arduino.h"
#include "SymphonyCore.h"
int WsCallback(uint8_t * payload, size_t length) ;
attribStruct MyMqttCallback(attribStruct property, int scmd) ;
void setTimer(int timeMins) ;
void startTimer() ;
void handleOn();
void handleOff();
void handleToggle();
void setup() ;
void loop() ;

#include "SymphonySocket.ino"


#endif
