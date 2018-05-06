package bm.jeep.device;

import org.json.JSONObject;

import bm.comms.Sender;
import bm.jeep.JEEPRequest;

public class ReqPlex extends JEEPRequest {
	public String command;

	public ReqPlex(JSONObject json, Sender sender, String plexCommandParam) {
		super(json, sender);
		command = json.getString(plexCommandParam);
	}
}
