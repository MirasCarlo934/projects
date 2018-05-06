package bm.main.modules.admin;

import org.json.JSONException;
import org.json.JSONObject;

import bm.comms.mqtt.MQTTPublisher;
import bm.context.adaptors.DBAdaptor;
import bm.context.adaptors.OHAdaptor;
import bm.context.adaptors.exceptions.AdaptorException;
import bm.context.rooms.Room;
import bm.jeep.JEEPRequest;
import bm.jeep.admin.JEEPAdminRequest;
import bm.jeep.device.ReqRequest;
import bm.main.modules.SimpleModule;
import bm.main.repositories.DeviceRepository;
import bm.main.repositories.RoomRepository;
import bm.tools.BMCipher;
import bm.tools.IDGenerator;

public class CreateRoomModule extends AbstAdminModule {
	private RoomRepository rr;
	private IDGenerator idg;
	private DBAdaptor dba;
	private OHAdaptor oha;
	private String nameParam;
	private String parentParam;
	private String indexParam;

	public CreateRoomModule(String logDomain, String errorLogDomain, String RTY, 
			/*MQTTPublisher mp, */DeviceRepository dr, BMCipher cipher, String pwdParam, String encryptedPwd, 
			RoomRepository rr, DBAdaptor dba, OHAdaptor oha, IDGenerator idg, 
			String nameParam, String parentParam, String indexParam) {
		super(logDomain, errorLogDomain, CreateRoomModule.class.getSimpleName(), RTY, 
				new String[] {pwdParam, nameParam, parentParam, indexParam}, /*mp, */dr, cipher, 
				pwdParam, encryptedPwd);
		this.rr = rr;
		this.idg = idg;
		this.dba = dba;
		this.oha = oha;
		this.nameParam = nameParam;
		this.parentParam = parentParam;
		this.indexParam = indexParam;
	}

	@Override
	protected boolean process(JEEPRequest request) {
		JSONObject json = request.getJSON();
		String name = json.getString(nameParam);
		String parentID = json.getString(parentParam);
		int index = json.getInt(indexParam);
		
		mainLOG.debug("Room creation requested!");
		Room r;
		try {
			r = createRoom(name, parentID, index);
			mainLOG.info("Room " + r.getSSID() + " (" + r.getName() + ") created!");
		} catch (AdaptorException e) {
			mainLOG.error("Cannot create room!", e);
			return false;
		}
		return true;
	}
	
	public Room createRoom(String name, String parentID, int index) throws AdaptorException {
		Room r;
		if(parentID != null) {
			r = new Room(idg.generateCID(rr.getAllRoomIDs()), rr.getRoom(parentID), name, dba, oha, null, 
					index);
		} else {
			r = new Room(idg.generateCID(rr.getAllRoomIDs()), name, dba, oha, null, index);
		}
		rr.addRoom(r);
		r.create(logDomain, true);
		
		return r;
	}
	
	/**
	 * Creates a new Room Smarthome Object in the root room of the Symphony Home System.
	 *  
	 * @param name The name of the room
	 * @return The new Room Smarthome Object
	 * @throws AdaptorException if room cannot be persisted to DB
	 */
	public Room createRoom(String name) throws AdaptorException {
		return createRoom(name, null, rr.getLastIndexInRoom(null));
	}

	@Override
	protected boolean additionalRequestChecking(JEEPRequest request) {
		JSONObject json = request.getJSON();
		String parentID;
		
//		mainLOG.debug("Checking...");
		try {
			json.getString(nameParam);
		} catch(JSONException e) {
			mainLOG.error("No name specified!", e);
			return false;
		}
		try {
			parentID = json.getString(parentParam);
		} catch(JSONException e) {
			mainLOG.error("No parent room ID specified!", e);
			return false;
		}
		try {
			json.getInt(indexParam);
		} catch(JSONException e) {
			mainLOG.error("No room index specified!", e);
			return false;
		}
		
		if(!rr.containsRoom(parentID)) {
			mainLOG.error("Parent room ID '" + parentID + "' does not exist!");
			return false;
		}
		
		return true;
	}

}
