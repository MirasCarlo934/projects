package ui.main.repositories;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.log4j.Logger;

import bm.context.adaptors.AbstAdaptor;
import bm.context.adaptors.DBAdaptor;
import bm.context.adaptors.OHAdaptor;
import bm.context.adaptors.exceptions.AdaptorException;
import bm.main.engines.DBEngine;
import bm.main.engines.exceptions.EngineException;
import bm.main.engines.requests.DBEngine.SelectDBEReq;
import bm.main.interfaces.Initializable;
import bm.main.repositories.RoomRepository;
import bm.tools.IDGenerator;
import ui.context.rooms.UIRoom;

public class UIRoomRepository extends RoomRepository implements Initializable {
	private Logger LOG;
	private RoomRepository rr; // the RoomRepository of the Symphony instance
	private DBEngine ohDBE;
	
	private String ohRoomsTable;
	private String ssidColname;
	private String colorColname;

	public UIRoomRepository(DBEngine dbe, String getRoomsQuery, String logDomain, IDGenerator idg, DBAdaptor dba,
			OHAdaptor oha, AbstAdaptor[] additionalAdaptors, RoomRepository roomRepository, 
			DBEngine ohDBE, String ohRoomsTable, String ssidColname, String colorColname) {
		super(dbe, getRoomsQuery, logDomain, idg, dba, oha, additionalAdaptors);
		LOG = Logger.getLogger(logDomain + "." + UIRoomRepository.class.getSimpleName());
		this.rr = roomRepository;
		this.ohDBE = ohDBE;
		this.ohRoomsTable = ohRoomsTable;
		this.ssidColname = ssidColname;
		this.colorColname = colorColname;
	}
	
	@Override
	public void initialize() throws Exception {
		super.initialize();
		populate();
	}
	
	public void populate() {
		LOG.info("Populating UIRoomRepository...");
		SelectDBEReq select1 = new SelectDBEReq(idg.generateERQSRequestID(), ohDBE, ohRoomsTable);
		try {
			ResultSet rs1 = (ResultSet) ohDBE.putRequest(select1, Thread.currentThread(), true);
			while(rs1.next()) {
				String ssid = rs1.getString(ssidColname);
				String color = rs1.getString(colorColname);
				LOG.trace("Adding room " + ssid + " to UIRoomRepository");
				try {
					addRoom(new UIRoom(rr.getRoom(ssid), color))	;
				} catch (AdaptorException e) {
					LOG.error("Cannot create UIRoom for room " + ssid + " (" + rr.getRoom(ssid).getName() + ")!");
				}
			}
		} catch (EngineException | SQLException e) {
			LOG.error("Cannot populate UIRoomRepository!", e);
		}
		LOG.debug("UIRoomRepository populated!");
	}
	
	public void addRoom(UIRoom r) {
		super.addRoom(r);
	}
	
	@Override
	public UIRoom getRoom(String roomID) {
		return (UIRoom) super.getRoom(roomID);
	}
	
	@Override
	public UIRoom removeRoom(String roomID) {
		return (UIRoom) super.removeRoom(roomID);
	}
	
	public UIRoom[] getAllUIRooms() {
		UIRoom[] rooms = new UIRoom[super.rooms.size()];
		for(int i = 0; i < super.rooms.size(); i++) {			
			rooms[i] = (UIRoom)getRoom(super.getAllRoomIDs()[i]);
		}
		return rooms;
	}
}
