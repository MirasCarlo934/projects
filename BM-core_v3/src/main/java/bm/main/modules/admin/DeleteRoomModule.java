package bm.main.modules.admin;

import java.util.HashMap;

import org.json.JSONException;

import bm.comms.mqtt.MQTTPublisher;
import bm.context.adaptors.exceptions.AdaptorException;
import bm.context.rooms.Room;
import bm.jeep.JEEPRequest;
import bm.jeep.device.ReqDeleteRoom;
import bm.jeep.device.ReqRequest;
import bm.main.engines.DBEngine;
import bm.main.engines.exceptions.EngineException;
import bm.main.engines.requests.DBEngine.DeleteDBEReq;
import bm.main.modules.SimpleModule;
import bm.main.repositories.DeviceRepository;
import bm.main.repositories.RoomRepository;
import bm.tools.IDGenerator;

public class DeleteRoomModule extends SimpleModule {
//	private String ssidColname;
	private String roomIDParam;
	private String roomsTable;
	private DBEngine dbe;
	private RoomRepository rr;
	private IDGenerator idg;

	public DeleteRoomModule(String logDomain, String errorLogDomain, String RTY, String roomIDParam, 
			String roomsTable, /*String ssidColname, *//*MQTTPublisher mp, */DeviceRepository dr, 
			RoomRepository rr, DBEngine dbe, IDGenerator idg) {
		super(logDomain, errorLogDomain, DeleteRoomModule.class.getSimpleName(), RTY, new String[] {roomIDParam}, 
				/*mp, */dr);
		this.roomIDParam = roomIDParam;
		this.idg = idg;
		this.roomsTable = roomsTable;
//		this.ssidColname = ssidColname;
		this.rr = rr;
		this.dbe = dbe;
	}

	@Override
	protected boolean process(JEEPRequest request) {
		String roomID = request.getJSON().getString(roomIDParam);
		
		mainLOG.debug("Room " + roomID + " deletion requested!");
		try {
			deleteRoom(roomID);
			mainLOG.info("Room " + roomID + " deleted!");
		} catch (EngineException e) {
			mainLOG.error("Cannot delete room!", e);
			return false;
		}
		return true;
	}
	
	public void deleteRoom(String roomID) throws EngineException{
		Room r = rr.getRoom(roomID);
		
		mainLOG.debug("Deleting room " + r.getSSID() + " (" + r.getName() + ")...");
		try {
			r.delete(logDomain, true);
		} catch (AdaptorException e) {
			mainLOG.error("Cannot delete room!", e);
		}
//		Thread t = Thread.currentThread();
//		HashMap<String, Object> args = new HashMap<String, Object>(1,1);
//		args.put("SSID", r.getSSID());
		
//		DeleteDBEReq delete1 = new DeleteDBEReq(idg.generateERQSRequestID(), dbe, roomsTable, args);
//		dbe.putRequest(delete1, t, true);
	}

	@Override
	protected boolean additionalRequestChecking(JEEPRequest request) {
		String roomID;
		try {
			roomID = request.getJSON().getString(roomIDParam);
		} catch(JSONException e) {
			mainLOG.error("No room ID specified!", e);
			return false;
		}
		
		if(!rr.containsRoom(roomID)) {
			mainLOG.error("Invalid room ID specified!");
			return false;
		}
		
		return true;
	}
}
