package bm.smarthome.properties.special.timer;

import bm.smarthome.adaptors.AbstAdaptor;
import bm.smarthome.adaptors.DBAdaptor;
import bm.smarthome.adaptors.OHAdaptor;
import bm.smarthome.devices.Device;
import bm.smarthome.devices.TimerDevice;
import bm.smarthome.properties.AbstProperty;
import bm.smarthome.properties.PropertyMode;
import bm.smarthome.properties.PropertyType;
import bm.smarthome.properties.bindings.Binding;

/**
 * The 'active' property of the Timer device. <b>It is assumed that the property type of this property IS the 'active'
 * property type, as set in COMPROPLIST DB table.</b>
 * @author carlomiras
 *
 */
public class TimerActiveProperty extends AbstProperty {

	public TimerActiveProperty(PropertyType propType, String SSID, String dispname, PropertyMode mode, DBAdaptor dba,
			OHAdaptor oha, AbstAdaptor[] additionalAdaptors, Binding binding) {
		super(propType, SSID, dispname, mode, dba, oha, additionalAdaptors, binding);
	}

	@Override
	public TimerActiveProperty clone() {
		return new TimerActiveProperty(propType, SSID, displayName, mode, super.mainDBAdaptor, super.mainOHAdaptor, 
				super.additionalAdaptors, binding);
	}

	@Override
	public void setValueAction(Object value) {
		TimerDevice parentTimerDevice = (TimerDevice) parentDevice;
		if(parentTimerDevice == null)
			System.out.println("test");
		if(Float.valueOf(value.toString()) == 1)
			parentTimerDevice.startTimer();
		else
			parentTimerDevice.stopTimer();
	}
}
