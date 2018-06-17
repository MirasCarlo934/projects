package bm.jeep.vo.device;

import bm.comms.Protocol;
import org.json.JSONObject;

import bm.jeep.vo.JEEPRequest;

public class ReqPOOP extends JEEPRequest {
	public String propSSID;
	public Object propValue;

	public ReqPOOP(JSONObject json, Protocol protocol, String propIDParam, String propValParam) {
		super(json, protocol);
		assignVariablesFromJSON(json, propIDParam, propValParam);
	}
	
	public ReqPOOP(JEEPRequest request, String propIDParam, String propValParam) {
		super(request.getJSON(), request.getProtocol());
		assignVariablesFromJSON(request.getJSON(), propIDParam, propValParam);
	}
	
	public ReqPOOP(String rid, String cid, String rty, Protocol protocol, String propIDParam, String propValParam,
			String propID, Object propVal) {
		super(rid, cid, rty, protocol);
		json.put(propIDParam, propID);
		json.put(propValParam, propVal);
		assignVariablesFromJSON(json, propIDParam, propValParam);
	}
	
	private void assignVariablesFromJSON(JSONObject json, String propIDParam, String propValParam) {
		propSSID = json.getString(propIDParam);
		propValue = json.get(propValParam);
	}
}