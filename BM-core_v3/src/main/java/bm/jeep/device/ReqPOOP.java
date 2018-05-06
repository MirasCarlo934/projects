package bm.jeep.device;

import org.json.JSONObject;

import bm.comms.Sender;
import bm.jeep.JEEPRequest;

public class ReqPOOP extends JEEPRequest {
	public String propSSID;
	public Object propValue;

	public ReqPOOP(JSONObject json, Sender sender, String propIDParam, String propValParam) {
		super(json, sender);
		assignVariablesFromJSON(json, propIDParam, propValParam);
	}
	
	public ReqPOOP(JEEPRequest request, String propIDParam, String propValParam) {
		super(request.getJSON(), request.getSender());
		assignVariablesFromJSON(request.getJSON(), propIDParam, propValParam);
	}
	
	public ReqPOOP(String rid, String cid, String rty, Sender sender, String propIDParam, String propValParam, 
			String propID, Object propVal) {
		super(rid, cid, rty, sender);
		json.put(propIDParam, propID);
		json.put(propValParam, propVal);
		assignVariablesFromJSON(json, propIDParam, propValParam);
	}
	
	private void assignVariablesFromJSON(JSONObject json, String propIDParam, String propValParam) {
		propSSID = json.getString(propIDParam);
		propValue = json.get(propValParam);
	}
}