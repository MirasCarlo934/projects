package bm.main.modules;

import bm.jeep.ReqRequest;
import bm.jeep.ResGetRooms;
import bm.main.repositories.DeviceRepository;
import bm.main.repositories.RoomRepository;
import bm.mqtt.MQTTPublisher;
import bm.smarthome.rooms.Room;

public class GetRoomsModule extends AbstModule {
	private RoomRepository rr;

	public GetRoomsModule(String logDomain, String errorLogDomain, String RTY, MQTTPublisher mp, 
			DeviceRepository dr, RoomRepository rr) {
		super(logDomain, errorLogDomain, GetRoomsModule.class.getSimpleName(), RTY, null, mp, dr);
		this.rr = rr;
	}

	@Override
	protected void process(ReqRequest request) {
		Room[] rooms = rr.getAllRooms();
		for(int i = 0; i < rooms.length; i++) {
			ResGetRooms response = new ResGetRooms(request, true, rooms[i].getSSID(), rooms[i].getName());
			mp.publish(response);
		}
	}

	@Override
	protected boolean additionalRequestChecking(ReqRequest request) {
		return true;
	}

}
