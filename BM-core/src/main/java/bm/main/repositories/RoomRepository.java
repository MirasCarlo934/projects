package bm.main.repositories;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import bm.jeep.ResError;
import bm.main.engines.AbstEngine;
import bm.main.engines.DBEngine;
import bm.main.engines.exceptions.EngineException;
import bm.main.engines.requests.DBEngine.RawDBEReq;
import bm.main.interfaces.Initializable;
import bm.mqtt.MQTTListener;
import bm.mqtt.MQTTPublisher;
import bm.smarthome.adaptors.AbstAdaptor;
import bm.smarthome.adaptors.DBAdaptor;
import bm.smarthome.adaptors.OHAdaptor;
import bm.smarthome.adaptors.exceptions.AdaptorException;
import bm.smarthome.rooms.Room;
import bm.tools.IDGenerator;

public class RoomRepository /*extends AbstRepository*/ implements Initializable {
	private Logger LOG;
	private String logDomain;
	private DBEngine dbe;
	private HashMap<String, Room> rooms = new HashMap<String, Room>(1);
//	private HashMap<String, Room> parentRooms = new HashMap<String, Room>(1);
	private String getRoomsQuery;
	private IDGenerator idg;
	private DBAdaptor dba;
	private OHAdaptor oha;
	private AbstAdaptor[] additionalAdaptors;

	public RoomRepository(DBEngine dbe, String getRoomsQuery, String logDomain, IDGenerator idg, DBAdaptor dba,
			OHAdaptor oha, AbstAdaptor[] additionalAdaptors/*, MQTTPublisher mp*/) {
//		super(logDomain, RoomRepository.class.getSimpleName());
		this.LOG = Logger.getLogger(logDomain + "." + RoomRepository.class.getSimpleName());
		this.logDomain = logDomain;
		this.dbe = dbe;
		this.getRoomsQuery = getRoomsQuery;
		this.idg = idg;
		this.dba = dba;
		this.oha = oha;
		this.additionalAdaptors = additionalAdaptors;
	}
	
	@Override
	public void initialize() throws Exception {
		retrieveRoomsFromDB();
		updateOH();
	}
	
	public void retrieveRoomsFromDB() {
		LOG.debug("Retrieving rooms from DB...");
		RawDBEReq request = new RawDBEReq(idg.generateERQSRequestID(), getRoomsQuery);
		Object o;
		try {
			o = dbe.forwardRequest(request, Thread.currentThread(), true);
		} catch (EngineException e1) {
			EngineException e = e1;
			LOG.fatal("Cannot retrieve rooms from DB!", e);
			return;
		}
		ResultSet rooms_rs = (ResultSet) o;
		try {
			while(rooms_rs.next()) {
				String id = rooms_rs.getString("ssid");
				String name = rooms_rs.getString("name");
				String parentID = rooms_rs.getString("parent_room");
				String color = rooms_rs.getString("color");
				int index = rooms_rs.getInt("INDEX");
				Room room;
				if(parentID == null) {
					room = new Room(id, name, dba, oha, additionalAdaptors, color);
					room.setIndex(index);
				} else {
					Room parent = new Room(parentID, null, dba, oha, new AbstAdaptor[0], color); //placeholder, signifies that the retrieved room has a parent
					room = new Room(id, parent, name, dba, oha, additionalAdaptors, color);
					room.setIndex(index);
				}
				LOG.debug("Adding room " + id + " (" + name + ") to repository!");
				rooms.put(room.getSSID(), room);
			}
			rooms_rs.close();
		} catch (SQLException e) {
			LOG.fatal("Cannot retrieve rooms from DB!", e);
//			mp.publishToErrorTopic("Cannot retrieve rooms from DB!");
			return;
		}
		
		/*
		 * Sets all parent rooms after all rooms from DB was retrieved 
		 */
		Iterator<Room> roomObjs = rooms.values().iterator();
		while(roomObjs.hasNext()) { 
			Room room = roomObjs.next();
			if(room.getRoom() != null) {
				Room parent = room.getRoom();
				room.setRoom(rooms.get(parent.getSSID()));
			}
		}
		LOG.debug("Room retrieval complete!");
	}
	
	/**
	 * Updates the OpenHAB instance. Adds an item representation for each room in OpenHAB and removes 
	 * the ones that are not included in this repository<br><br>
	 * 
	 * <b>NOTE:</b> Rooms must be retrieved from DB first! Otherwise, OpenHAB will be wiped of group
	 * items! 
	 */
	public void updateOH() {
		LOG.debug("Updating OpenHAB item registry!");
		Iterator<Room> rooms = this.rooms.values().iterator();
		while(rooms.hasNext()) {
			try {
				rooms.next().persist(logDomain, new AbstAdaptor[] {dba}, false);
			} catch (AdaptorException e) {
				LOG.fatal("Cannot update rooms in OpenHAB sitemap! OpenHAB will show erroneous sitemap contents!", 
						e);
//				mp.publishToErrorTopic("Cannot update rooms in OpenHAB sitemap! See log details!");
			}
		}
	}
	
	/**
	 * Checks if the room with the specified room ID exists.
	 * 
	 * @param roomID The room ID to check
	 * @return <b>True</b> if repository contains the room, <b>false</b> otherwise
	 */
	public boolean containsRoom(String roomID) {
		return rooms.containsKey(roomID);
	}
	
	/**
	 * Adds a Room object to this RoomRepository
	 * 
	 * @param r The Room object to be added
	 */
	public void addRoom(Room r) {
		rooms.put(r.getSSID(), r);
	}
	
	/**
	 * Deletes the Room object that represents a room in the Symphony system.
	 * 
	 * @param roomID The room ID of the Room object to delete
	 * @return The Room object with the specified room ID, <i>null</i> if the room ID does not exist in the 
	 * 		repository
	 */
	public Room removeRoom(String roomID) {
		return rooms.remove(roomID);
	}
	
	/**
	 * Returns the Room object that represents a room in the Symphony system.
	 * 
	 * @param roomID The room ID of the Room object to get
	 * @return The Room object with the specified room ID, <i>null</i> if the room ID does not exist in the 
	 * 		repository
	 */
	public Room getRoom(String roomID) {
		return rooms.get(roomID);
	}
	
	/**
	 * Returns all the rooms in this RoomRepository
	 * 
	 * @return An array of Room objects
	 */
	public Room[] getAllRooms() {
		return rooms.values().toArray(new Room[rooms.size()]);
	}
	
	/**
	 * Returns all the room IDs in this RoomRepository
	 * 
	 * @return An array of room ID strings
	 * @return
	 */
	public String[] getAllRoomIDs() {
		String[] ids = new String[rooms.size()];
		Iterator<Room> rs = rooms.values().iterator();
		for(int i = 0; i < ids.length; i++) {
			ids[i] = rs.next().getSSID();
		}
		return ids;
	}
	
	/**
	 * Returns the highest index in the specified room
	 * 
	 * @param roomID The ID of the room. Can be null, which will return the highest index in the root room.
	 * @return the highest index in the room
	 */
	public int getLastIndexInRoom(String roomID) {
		int highest = 0;
		Room[] rs = rooms.values().toArray(new Room[0]);
		
		if(roomID == null ) {
			for(int i = 0; i < rs.length; i++) {
				if(rs[i].getRoom() == null && rs[i].getIndex() > highest) {
					highest = rs[i].getIndex();
				}
			}
		} else if(containsRoom(roomID)) {
			Room parent = rooms.get(roomID);
			for(int i = 0; i < rs.length; i++) {
				if(rs[i].getRoom().equals(parent) && rs[i].getIndex() > highest) {
					highest = rs[i].getIndex();
				}
			}
		}
		
		return highest;
	}
}
