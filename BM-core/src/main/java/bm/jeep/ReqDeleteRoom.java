package bm.jeep;

import org.json.JSONObject;

public class ReqDeleteRoom extends AbstRequest {
	public String roomID;

	public ReqDeleteRoom(JSONObject json, String roomIDParam) {
		super(json);
		this.roomID = json.getString(roomIDParam);
	}

	public ReqDeleteRoom(String RID, String CID, String RTY, String roomID) {
		super(RID, CID, RTY);
		this.roomID = roomID;
	}
	
	public ReqDeleteRoom(ReqRequest request, String roomIDParam) {
		super(request.getJSON());
		this.roomID = request.getString(roomIDParam);
	}
}
