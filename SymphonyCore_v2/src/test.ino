#include <Arduino.h>

void setup() {
    Serial.begin(115200);
    Serial.println("\n***********START setup**************");
    Serial.println("Hello world!");
    Serial.println("************END setup***************");
}

void loop() {
    delay(500);
}