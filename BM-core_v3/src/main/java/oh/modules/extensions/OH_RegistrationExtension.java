package oh.modules.extensions;

import java.util.Arrays;
import java.util.Vector;

import org.json.JSONObject;

import bm.comms.http.HTTPException;
import bm.comms.http.HTTPSender;
import bm.context.adaptors.exceptions.AdaptorException;
import bm.context.devices.Device;
import bm.jeep.JEEPRequest;
import bm.jeep.device.ReqRegister;
import bm.main.modules.AbstModuleExtension;
import bm.main.repositories.DeviceRepository;
import oh.main.initializables.OH_Initializer;

public class OH_RegistrationExtension extends AbstModuleExtension {
	private String ohIP;
	private String nameParam;
	private String roomIDParam;
	private String propsParam;
	private DeviceRepository dr;
	private HTTPSender hs;

	public OH_RegistrationExtension(String logDomain, String errorLogDomain, String name, String[] params, 
			DeviceRepository deviceRepository, HTTPSender httpSender, String nameParam, String roomIDParam, 
			String propsParam, String ohIP) {
		super(logDomain, errorLogDomain, name, params);
		this.ohIP = ohIP;
		this.nameParam = nameParam;
		this.roomIDParam = roomIDParam;
		this.propsParam = propsParam;
		this.dr = deviceRepository;
		this.hs = httpSender;
	}

	@Override
	protected void process(JEEPRequest request) {
		ReqRegister reg = new ReqRegister(request, nameParam, roomIDParam, propsParam);
		Device dev = dr.getDevice(reg.mac);
		Vector<JSONObject> items = new Vector<JSONObject>(dev.getProperties().length + 1);
		
		//adds a device to the item registry, updates if it already exists
		mainLOG.debug("Adding device " + dev.getSSID() + " to OpenHAB item registry...");
		if(dev.getProperties().length > 1) { //1-property components are persisted thru their sole property!!!
			items.addAll(Arrays.asList(dev.convertToItemsJSON()));
			mainLOG.debug("Adding properties of " + dev.getSSID() + " to OpenHAB item registry...");
			for(int i = 0; i < dev.getProperties().length; i++) {
				items.addAll(Arrays.asList(dev.getProperties()[i].convertToItemsJSON()));
			}
		} else {
			items.addAll(Arrays.asList(dev.getProperties()[0].convertToItemsJSON()));
		}
		
		try {
			OH_Initializer.addItems(mainLOG, hs, ohIP, items.toArray(new JSONObject[items.size()]), true);
			mainLOG.debug("Device added successfully!");
		} catch (HTTPException e) {
			mainLOG.error("Cannot add device to OpenHAB!", e);
		}
	}

	@Override
	protected boolean additionalRequestChecking(JEEPRequest request) {
		return true;
	}
}
