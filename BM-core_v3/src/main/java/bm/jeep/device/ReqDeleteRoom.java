package bm.jeep.device;

import org.json.JSONObject;

import bm.comms.Sender;
import bm.jeep.JEEPRequest;

public class ReqDeleteRoom extends JEEPRequest {
	public String roomID;

	public ReqDeleteRoom(JSONObject json, Sender sender, String roomIDParam) {
		super(json, sender);
		this.roomID = json.getString(roomIDParam);
	}

//	public ReqDeleteRoom(String RID, String CID, String RTY, String roomID) {
//		super(RID, CID, RTY);
//		this.roomID = roomID;
//	}
	
	public ReqDeleteRoom(JEEPRequest request, String roomIDParam) {
		super(request.getJSON(), request.getSender());
		this.roomID = request.getString(roomIDParam);
	}
}
