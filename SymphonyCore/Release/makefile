################################################################################
# Automatically-generated file. Do not edit!
################################################################################

-include ..\makefile.init

RM := del 

# All of the sources participating in the build are defined here
-include sources.mk
-include subdir.mk
-include libraries\WiFiManager\subdir.mk
-include libraries\WebSockets\src\libsha1\subdir.mk
-include libraries\WebSockets\src\libb64\subdir.mk
-include libraries\WebSockets\src\subdir.mk
-include libraries\SPI\subdir.mk
-include libraries\PubSubClient\src\subdir.mk
-include libraries\Hash\src\sha1\subdir.mk
-include libraries\Hash\src\subdir.mk
-include libraries\Ethernet\src\utility\subdir.mk
-include libraries\Ethernet\src\subdir.mk
-include libraries\ESP8266mDNS\subdir.mk
-include libraries\ESP8266httpUpdate\src\subdir.mk
-include libraries\ESP8266WiFi\src\subdir.mk
-include libraries\ESP8266WebServer\src\detail\subdir.mk
-include libraries\ESP8266WebServer\src\subdir.mk
-include libraries\ESP8266HTTPClient\src\subdir.mk
-include libraries\ESP8266-RemoteDebug-Telnet\Samples\RemoteDebug_Basic\subdir.mk
-include libraries\ESP8266-RemoteDebug-Telnet\Samples\RemoteDebug_Advanced\subdir.mk
-include libraries\ESP8266-RemoteDebug-Telnet\subdir.mk
-include libraries\DNSServer\src\subdir.mk
-include libraries\ArduinoOTA\subdir.mk
-include core\core\umm_malloc\subdir.mk
-include core\core\spiffs\subdir.mk
-include core\core\libb64\subdir.mk
-include core\core\subdir.mk
-include objects.mk

ifneq ($(MAKECMDGOALS),clean)
ifneq ($(strip $(CC_DEPS)),)
-include $(CC_DEPS)
endif
ifneq ($(strip $(C++_DEPS)),)
-include $(C++_DEPS)
endif
ifneq ($(strip $(C_UPPER_DEPS)),)
-include $(C_UPPER_DEPS)
endif
ifneq ($(strip $(CXX_DEPS)),)
-include $(CXX_DEPS)
endif
ifneq ($(strip $(ASM_DEPS)),)
-include $(ASM_DEPS)
endif
ifneq ($(strip $(INO_DEPS)),)
-include $(INO_DEPS)
endif
ifneq ($(strip $(PDE_DEPS)),)
-include $(PDE_DEPS)
endif
ifneq ($(strip $(S_UPPER_DEPS)),)
-include $(S_UPPER_DEPS)
endif
ifneq ($(strip $(CPP_DEPS)),)
-include $(CPP_DEPS)
endif
ifneq ($(strip $(C_DEPS)),)
-include $(C_DEPS)
endif
endif

-include ..\makefile.defs

# Add inputs and outputs from these tool invocations to the build variables 
AR += \
C:\Users\miras.DESKTOP-JHPGKS4\git\projects\SymphonyCore\Release\arduino.ar \

ELF += \
SymphonyCore.elf \

HEX += \
SymphonyCore.hex \


# All Target
all: SymphonyCore

# Tool invocations
SymphonyCore: $(HEX)
	@echo 'Building target: $@'
	@echo 'Printing size:'
	"E:\sloeberv4\/arduinoPlugin/packages/esp8266/tools/xtensa-lx106-elf-gcc/1.20.0-26-gb404fb9-2/bin/xtensa-lx106-elf-size" -A "C:/Users/miras.DESKTOP-JHPGKS4/git/projects/SymphonyCore/Release/SymphonyCore.elf"
	@echo 'Finished building target: $@'
	@echo ' '

C:\Users\miras.DESKTOP-JHPGKS4\git\projects\SymphonyCore\Release\arduino.ar: $(AR_OBJ)
	@echo 'Starting archiver'
	"E:\sloeberv4\/arduinoPlugin/packages/esp8266/tools/xtensa-lx106-elf-gcc/1.20.0-26-gb404fb9-2/bin/xtensa-lx106-elf-ar" cru "C:\Users\miras.DESKTOP-JHPGKS4\git\projects\SymphonyCore/Release/arduino.ar"    C:/Users/miras.DESKTOP-JHPGKS4/git/projects/SymphonyCore/Release/arduino.ar $(AR_OBJ)
	@echo 'Finished building: $@'
	@echo ' '

SymphonyCore.elf: $(LINK_OBJ) $(AR)
	@echo 'Starting combiner'
	"E:\sloeberv4\/arduinoPlugin/packages/esp8266/tools/xtensa-lx106-elf-gcc/1.20.0-26-gb404fb9-2/bin/xtensa-lx106-elf-gcc" -g -Wall -Wextra -Os -nostdlib -Wl,--no-check-sections -u call_user_start -u _printf_float -u _scanf_float -Wl,-static "-LE:\sloeberv4\/arduinoPlugin/packages/esp8266/hardware/esp8266/2.4.1/tools/sdk/lib" "-LE:\sloeberv4\/arduinoPlugin/packages/esp8266/hardware/esp8266/2.4.1/tools/sdk/ld" "-LE:\sloeberv4\/arduinoPlugin/packages/esp8266/hardware/esp8266/2.4.1/tools/sdk/libc/xtensa-lx106-elf/lib" "-Teagle.flash.4m1m.ld" -Wl,--gc-sections -Wl,-wrap,system_restart_local -Wl,-wrap,spi_flash_read -o "C:\Users\miras.DESKTOP-JHPGKS4\git\projects\SymphonyCore/Release/SymphonyCore.elf" -Wl,--start-group   $(LINK_OBJ) $(AR)  "C:\Users\miras.DESKTOP-JHPGKS4\git\projects\SymphonyCore/Release/arduino.ar" -lhal -lphy -lpp -lnet80211 -llwip_gcc -lwpa -lcrypto -lmain -lwps -laxtls -lespnow -lsmartconfig -lairkiss -lwpa2 -lstdc++ -lm -lc -lgcc -Wl,--end-group "-LC:\Users\miras.DESKTOP-JHPGKS4\git\projects\SymphonyCore/Release" C:/Users/miras.DESKTOP-JHPGKS4/git/projects/SymphonyCore/Release/arduino.ar
	@echo 'Finished building: $@'
	@echo ' '

SymphonyCore.hex: $(ELF)
	@echo 'Create eeprom image'
	"E:\sloeberv4\/arduinoPlugin/packages/esp8266/tools/esptool/0.4.13/esptool.exe" -eo "E:\sloeberv4\/arduinoPlugin/packages/esp8266/hardware/esp8266/2.4.1/bootloaders/eboot/eboot.elf" -bo "C:/Users/miras.DESKTOP-JHPGKS4/git/projects/SymphonyCore/Release/SymphonyCore.bin" -bm dio -bf 80 -bz 4M -bs .text -bp 4096 -ec -eo "C:/Users/miras.DESKTOP-JHPGKS4/git/projects/SymphonyCore/Release/SymphonyCore.elf" -bs .irom0.text -bs .text -bs .data -bs .rodata -bc -ec
	@echo 'Finished building: $@'
	@echo ' '

# Other Targets
clean:
	-$(RM) $(C_UPPER_DEPS)$(PDE_DEPS)$(C_DEPS)$(AR)$(CC_DEPS)$(AR_OBJ)$(C++_DEPS)$(LINK_OBJ)$(CXX_DEPS)$(ASM_DEPS)$(HEX)$(INO_DEPS)$(SIZEDUMMY)$(S_UPPER_DEPS)$(ELF)$(CPP_DEPS)
	-@echo ' '

.PHONY: all clean dependents
.SECONDARY:

-include ..\makefile.targets
