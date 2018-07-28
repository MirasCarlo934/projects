package bm.smarthome.devices;

import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;

import bm.smarthome.adaptors.AbstAdaptor;
import bm.smarthome.adaptors.DBAdaptor;
import bm.smarthome.adaptors.OHAdaptor;
import bm.smarthome.adaptors.exceptions.AdaptorException;
import bm.smarthome.devices.products.Product;
import bm.smarthome.devices.products.special.timer.TimerProduct;
import bm.smarthome.properties.Property;
import bm.smarthome.properties.special.timer.TimerActiveProperty;
import bm.smarthome.properties.special.timer.TimerTimeProperty;
import bm.smarthome.rooms.Room;

public class TimerDevice extends Device {
	private Logger LOG;
	private Timer timer;
	private TimerCounter timerCounter;
	private boolean running = false;
	private static String activePropSSID;
	private static String timePropSSID;
	
	public TimerDevice(String SSID, String MAC, String name, String topic, Room room, boolean active, 
			TimerProduct product, DBAdaptor dba, OHAdaptor oha, AbstAdaptor[] adaptors, String timePropSSID, 
			String activePropSSID) {
		super(SSID, MAC, name, topic, room, active, product, dba, oha, adaptors);
		LOG = Logger.getLogger(loggerName + ":Timer");
		timer = new Timer(SSID + "Timer");
		TimerDevice.timePropSSID = timePropSSID;
		TimerDevice.activePropSSID = activePropSSID;
	}
	
	public void startTimer() {
		if(running == false) {
			LOG.debug("Starting timer...");
			timerCounter = new TimerCounter();
			timer.schedule(timerCounter, 0, 1000);
			running = true;
		}
	}
	
	public void stopTimer() {
		if(running == true) {
			LOG.debug("Stopping timer...");
			timerCounter.cancel();
			TimerActiveProperty active = (TimerActiveProperty) getProperty(activePropSSID);
			running = false;
			try {
				active.setValue(0, loggerName, false);
			} catch (AdaptorException e) {
				LOG.error("Cannot set 'active' property to 0!");
			}
		}
	}
	
	private class TimerCounter extends TimerTask {

		@Override
		public void run() {
			TimerTimeProperty timeProp = (TimerTimeProperty) getProperty(timePropSSID);
			float time = Float.parseFloat(timeProp.getValue().toString());
			try {
				if(time > 1) {
					timeProp.setValue(Float.parseFloat(timeProp.getValue().toString()) - 1, loggerName, false);
				} else {
					timeProp.setValue(0, loggerName, false);
					stopTimer();
				}
			} catch (AdaptorException e) {
				LOG.error("Cannot set 'time' property value!");
			}
		}
	}
}
