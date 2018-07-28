cmake_minimum_required(VERSION 3.12)
project(esp8266)

set(${CMAKE_PROJECT_NAME}_BOARD esp01)
set(CMAKE_CXX_STANDARD 14)

include_directories(SymphonyCore)

add_executable(esp8266
        SymphonyCore/data/admin-v1.1.html
        SymphonyCore/data/admin.html
        SymphonyCore/data/admin.html.bak
        SymphonyCore/data/control.css
        SymphonyCore/data/control.html
        SymphonyCore/data/sample.html
        SymphonyCore/data/symphony-v1.1.js
        SymphonyCore/data/symphony.css
        SymphonyCore/data/symphony.js
        SymphonyCore/readme/load_datafile.txt
        SymphonyCore/readme/Mkspiffs_esptool_settings.docx
        SymphonyCore/Release/arduino.ar
        SymphonyCore/Release/CloudMqttUtil.cpp.d
        SymphonyCore/Release/CloudMqttUtil.cpp.o
        SymphonyCore/Release/MqttUtil.cpp.d
        SymphonyCore/Release/MqttUtil.cpp.o
        SymphonyCore/Release/PushBullet.cpp.d
        SymphonyCore/Release/PushBullet.cpp.o
        SymphonyCore/Release/SpiffsUtil.cpp.d
        SymphonyCore/Release/SpiffsUtil.cpp.o
        SymphonyCore/Release/SymphonyCore.bin
        SymphonyCore/Release/SymphonyCore.cpp.d
        SymphonyCore/Release/SymphonyCore.cpp.o
        SymphonyCore/Release/SymphonyCore.elf
        SymphonyCore/Release/SymphonyProduct.cpp.d
        SymphonyCore/Release/SymphonyProduct.cpp.o
        SymphonyCore/spiffs/data.bin
        SymphonyCore/CloudMqttUtil.cpp
        SymphonyCore/CloudMqttUtil.h
        SymphonyCore/mainhtml.h
        SymphonyCore/mainhtml2.h
        SymphonyCore/mainhtml3.h
        SymphonyCore/MqttUtil.cpp
        SymphonyCore/MqttUtil.h
        SymphonyCore/PushBullet.cpp
        SymphonyCore/PushBullet.h
        SymphonyCore/SpiffsUtil.cpp
        SymphonyCore/SpiffsUtil.h
        SymphonyCore/SymphonyCore.cpp
        SymphonyCore/SymphonyCore.cpp1
        SymphonyCore/SymphonyCore.h
        SymphonyCore/SymphonyCore.h1
        SymphonyCore/SymphonyCore.ino
        SymphonyCore/SymphonyProduct.cpp
        SymphonyCore/SymphonyProduct.h)
