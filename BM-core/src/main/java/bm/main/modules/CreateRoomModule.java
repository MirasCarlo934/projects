package bm.main.modules;

import bm.jeep.ReqRequest;
import bm.main.repositories.DeviceRepository;
import bm.main.repositories.RoomRepository;
import bm.mqtt.MQTTPublisher;
import bm.smarthome.adaptors.DBAdaptor;
import bm.smarthome.adaptors.OHAdaptor;
import bm.smarthome.adaptors.exceptions.AdaptorException;
import bm.smarthome.rooms.Room;
import bm.tools.IDGenerator;

public class CreateRoomModule extends AbstModule {
	private RoomRepository rr;
	private IDGenerator idg;
	private DBAdaptor dba;
	private OHAdaptor oha;

	public CreateRoomModule(String logDomain, String errorLogDomain, String RTY, 
			MQTTPublisher mp, DeviceRepository dr, RoomRepository rr, DBAdaptor dba, OHAdaptor oha, 
			IDGenerator idg) {
		super(logDomain, errorLogDomain, CreateRoomModule.class.getSimpleName(), RTY, new String[0], mp, dr);
		this.rr = rr;
		this.idg = idg;
		this.dba = dba;
		this.oha = oha;
	}

	@Override
	protected void process(ReqRequest request) {
		// TODO Create room creation process
	}
	
	/**
	 * Creates a new Room Smarthome Object in the root room of the Symphony Home System.
	 *  
	 * @param name The name of the room
	 * @param color The color of the room
	 * @return The new Room Smarthome Object
	 * @throws AdaptorException if room cannot be persisted to DB
	 */
	public Room createRoom(String name, String color) throws AdaptorException {
		mainLOG.debug("Room creation requested!");
		Room r = new Room(idg.generateCID(rr.getAllRoomIDs()), name, dba, oha, null, color);
		r.setIndex(rr.getLastIndexInRoom(null));
		rr.addRoom(r);
		r.persist(logDomain, true);
		
		mainLOG.info("Room " + r.getSSID() + " (" + r.getName() + ") created in root room!");
		return r;
	}

	@Override
	protected boolean additionalRequestChecking(ReqRequest request) {
		// TODO Create room creation request checking
		return true;
	}

}
