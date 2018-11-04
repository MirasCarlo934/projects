#include <Arduino.h>
#include <MqttUtil.h>

void setup() {
    Serial.begin(115200);
    Serial.println("\r\n***********START setup**************");
    Serial.println("\rHello world!");
    Serial.println(MQTT_MAX_PACKET_SIZE);
    Serial.println("\r************END setup***************");
}

void loop() {
    delay(500);
}