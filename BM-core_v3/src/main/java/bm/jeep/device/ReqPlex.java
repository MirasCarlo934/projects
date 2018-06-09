package bm.jeep.device;

import bm.comms.Protocol;
import org.json.JSONObject;

import bm.comms.Sender;
import bm.jeep.JEEPRequest;

public class ReqPlex extends JEEPRequest {
	public String command;

	public ReqPlex(JSONObject json, Protocol protocol, String plexCommandParam) {
		super(json, protocol);
		command = json.getString(plexCommandParam);
	}
}
