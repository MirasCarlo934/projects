package bm.smarthome.properties.special.timer;

import javax.xml.ws.http.HTTPException;

import org.apache.log4j.Logger;

import bm.smarthome.adaptors.AbstAdaptor;
import bm.smarthome.adaptors.DBAdaptor;
import bm.smarthome.adaptors.OHAdaptor;
import bm.smarthome.adaptors.exceptions.AdaptorException;
import bm.smarthome.devices.TimerDevice;
import bm.smarthome.properties.AbstProperty;
import bm.smarthome.properties.Property;
import bm.smarthome.properties.PropertyMode;
import bm.smarthome.properties.PropertyType;
import bm.smarthome.properties.bindings.Binding;

/**
 * The 'active' property of the Timer device. <b>It is assumed that the property type of this property IS the 'active'
 * property type, as set in COMPROPLIST DB table.</b>
 * @author carlomiras
 *
 */
public class TimerTimeProperty extends AbstProperty {
//	private TimerDevice parentTimerDevice;

	public TimerTimeProperty(PropertyType propType, String SSID, String dispname, PropertyMode mode, DBAdaptor dba,
			OHAdaptor oha, AbstAdaptor[] additionalAdaptors, Binding binding) {
		super(propType, SSID, dispname, mode, dba, oha, additionalAdaptors, binding);
//		this.parentTimerDevice = (TimerDevice) parentDevice;
	}

	@Override
	public TimerTimeProperty clone() {
		return new TimerTimeProperty(propType, SSID, displayName, mode, super.mainDBAdaptor, super.mainOHAdaptor, 
				super.additionalAdaptors, binding);
	}

	@Override
	public void setValueAction(Object value) {
		//NO OTHER ACTION
	}
}
