package bm.jeep.admin;

import org.json.JSONObject;

import bm.comms.Sender;

public class ReqCreateRoom extends JEEPAdminRequest {
	private String name;
	private String parentRoom;
	private int index;

	public ReqCreateRoom(JSONObject json, Sender sender) {
		super(json, sender);
//		this.name = name;
//		this.parentRoom = parentRoom;
//		this.index = json.getInt(key);
	}
	
//	public String getName() {
//		return name;
//	}
//	
//	public String getParentRoom() {
//		return parentRoom;
//	}
//	
//	public int getIndex() {
//		return index;
//	}
}
