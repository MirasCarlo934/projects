package bm.context.properties;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import javax.xml.ws.http.HTTPException;

import org.apache.log4j.Logger;
import org.json.JSONObject;

import bm.comms.mqtt.MQTTListener;
import bm.comms.mqtt.MQTTPublisher;
import bm.context.OHItemmable;
import bm.context.SymphonyElement;
import bm.context.adaptors.AbstAdaptor;
import bm.context.adaptors.DBAdaptor;
import bm.context.adaptors.OHAdaptor;
import bm.context.adaptors.exceptions.AdaptorException;
import bm.context.devices.Device;
import bm.context.properties.bindings.Binding;
import bm.jeep.device.ReqPOOP;
import bm.jeep.device.ResError;
import bm.jeep.device.ResPOOP;
import bm.main.engines.DBEngine;
import bm.main.engines.exceptions.EngineException;
import bm.main.engines.requests.DBEngine.DeleteDBEReq;
import bm.main.engines.requests.DBEngine.InsertDBEReq;
import bm.main.engines.requests.DBEngine.UpdateDBEReq;
import bm.tools.IDGenerator;

/**
 * A Java-object representation of a real-world device property. This object is instantiated for a device property that
 * does not have any BM-side functionality.
 * 
 * @author carlomiras
 *
 */
public class Property extends AbstProperty {
	
	public Property(PropertyType propType, String SSID, /*String genericName, */String dispname, 
			/*String ohItemType,*/ PropertyMode mode, /*PropertyValueType propValType,*/ DBAdaptor dba, 
			OHAdaptor oha, AbstAdaptor[] additionalAdaptors, Binding binding, String poopRTY, String propIDParam,
			String propValParam, IDGenerator idGenerator) {
		super(propType, SSID, dispname, mode, dba, oha, additionalAdaptors, binding, poopRTY, propIDParam,
				propValParam, idGenerator);
	}
	
	@Override
	public Property clone() {
		return new Property(propType, SSID, displayName, mode, super.mainDBAdaptor, super.mainOHAdaptor, 
				super.additionalAdaptors, binding, poopRTY, propIDParam, propValParam, idg);
	}

	@Override
	public void setValueAction(Object value) {
		// NO OTHER ACTION
	}
}