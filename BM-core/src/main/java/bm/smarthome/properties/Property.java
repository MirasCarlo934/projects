package bm.smarthome.properties;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import javax.xml.ws.http.HTTPException;

import org.apache.log4j.Logger;
import org.json.JSONObject;

import bm.jeep.ReqPOOP;
import bm.jeep.ResError;
import bm.jeep.ResPOOP;
import bm.main.engines.DBEngine;
import bm.main.engines.exceptions.EngineException;
import bm.main.engines.requests.DBEngine.DeleteDBEReq;
import bm.main.engines.requests.DBEngine.InsertDBEReq;
import bm.main.engines.requests.DBEngine.UpdateDBEReq;
import bm.mqtt.MQTTListener;
import bm.mqtt.MQTTPublisher;
import bm.smarthome.adaptors.AbstAdaptor;
import bm.smarthome.adaptors.DBAdaptor;
import bm.smarthome.adaptors.OHAdaptor;
import bm.smarthome.adaptors.exceptions.AdaptorException;
import bm.smarthome.devices.Device;
import bm.smarthome.interfaces.OHItemmable;
import bm.smarthome.interfaces.SmarthomeElement;
import bm.smarthome.properties.bindings.Binding;
import bm.tools.IDGenerator;

/**
 * A Java-object representation of a real-world device property. This object is instantiated for a device property that
 * does not have any BM-side functionality.
 * 
 * @author carlomiras
 *
 */
public class Property extends AbstProperty implements OHItemmable {
	
	public Property(PropertyType propType, String SSID, /*String genericName, */String dispname, 
			/*String ohItemType,*/ PropertyMode mode, /*PropertyValueType propValType,*/ DBAdaptor dba, 
			OHAdaptor oha, AbstAdaptor[] additionalAdaptors, Binding binding) {
		super(propType, SSID, dispname, mode, dba, oha, additionalAdaptors, binding);
	}
	
	@Override
	public Property clone() {
		return new Property(propType, SSID, displayName, mode, super.mainDBAdaptor, super.mainOHAdaptor, 
				super.additionalAdaptors, binding);
	}

	@Override
	public void setValueAction(Object value) {
		// NO OTHER ACTION
	}
}