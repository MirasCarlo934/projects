package bm.main.modules;

import bm.jeep.ReqDeleteRoom;
import bm.jeep.ReqRequest;
import bm.main.engines.DBEngine;
import bm.main.repositories.DeviceRepository;
import bm.mqtt.MQTTPublisher;

public class DeleteRoomModule extends AbstModule {
	private String roomIDParam;
	private DBEngine dbe;

	public DeleteRoomModule(String logDomain, String errorLogDomain, String RTY, String roomIDParam, MQTTPublisher mp, 
			DeviceRepository dr, DBEngine dbe) {
		super(logDomain, errorLogDomain, DeleteRoomModule.class.getSimpleName(), RTY, new String[] {roomIDParam}, 
				mp, dr);
		this.roomIDParam = roomIDParam;
	}

	@Override
	protected void process(ReqRequest request) {
		
	}
	
	public void deleteRoom(String roomID) {
		mainLOG.debug("Deleting room from ");
	}

	@Override
	protected boolean additionalRequestChecking(ReqRequest request) {
		error("Room deletion request is not available for MQTT!");
		return false;
	}
}
