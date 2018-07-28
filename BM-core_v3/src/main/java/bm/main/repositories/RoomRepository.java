package bm.main.repositories;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.log4j.Logger;

import bm.context.adaptors.exceptions.AdaptorException;
import bm.context.rooms.Room;
import bm.main.engines.DBEngine;
import bm.main.engines.exceptions.EngineException;
import bm.main.engines.requests.DBEngine.RawDBEReq;
import bm.main.interfaces.Initializable;
import bm.tools.IDGenerator;

public class RoomRepository /*extends AbstRepository*/ implements Initializable {
	private Logger LOG;
	private String logDomain;
	protected DBEngine dbe;
	protected HashMap<String, Room> rooms = new HashMap<String, Room>(1);
	private String getRoomsQuery;
	protected IDGenerator idg;
//	private DBAdaptor dba;
	private Room recentlyAddedRoom;

	public RoomRepository(DBEngine dbe, String getRoomsQuery, String logDomain, IDGenerator idg
			/*DBAdaptor dba, */) {
		this.LOG = Logger.getLogger(logDomain + "." + RoomRepository.class.getSimpleName());
		this.logDomain = logDomain;
		this.dbe = dbe;
		this.getRoomsQuery = getRoomsQuery;
		this.idg = idg;
//		this.dba = dba;
	}
	
	@Override
	public void initialize() throws Exception {
		retrieveRoomsFromDB();
//		updateRoomsInEnvironment();
	}
	
	public void retrieveRoomsFromDB() {
		LOG.info("Retrieving rooms from DB...");
		RawDBEReq request = new RawDBEReq(idg.generateERQSRequestID(), dbe, getRoomsQuery);
		Object o;
		try {
			o = dbe.putRequest(request, Thread.currentThread(), true);
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
				int index = rooms_rs.getInt("index");
				Room room;
				LOG.debug("Adding room " + id + " (" + name + ") to repository!");
				if(parentID == null) {
					room = new Room(id, name, color, index);
				} else {
					Room parent = new Room(parentID, null, null, index); //placeholder, signifies that the retrieved room has a parent
					room = new Room(id, parent, name, color, index);
				}
				rooms.put(room.getSSID(), room);
			}
			LOG.debug(rooms.size() + " rooms added!");
			rooms_rs.close();
		} catch (SQLException e) {
			LOG.fatal("Cannot retrieve rooms from DB!", e);
//			mp.publishToErrorTopic("Cannot retrieve rooms from DB!");
			return;
		}
		
		/*
		 * Sets all parent rooms after all rooms from DB was retrieved 
		 */
//		while(v_rooms.isEmpty()) {
//			for(int i = 0; i < v_rooms.size(); i++) { 
//				Room room = v_rooms.get(i);
//				if(room.getParentRoom() != null) {
//					Room parent = room.getParentRoom();
//					if(room.getIndex() <= parent.getRooms().length) {
//						room.setRoom(rooms.get(parent.getSSID()));
//						v_rooms.remove(i);
//					}
//				} else {
//					v_rooms.remove(i);
//				}
//			}
//		}
		Iterator<Room> roomObjs = rooms.values().iterator();
		while(roomObjs.hasNext()) { 
			Room room = roomObjs.next();
			if(room.getParentRoom() != null) {
				Room parent = room.getParentRoom();
				room.setRoom(rooms.get(parent.getSSID()));
//				try {
//					room.setRoom(rooms.get(parent.getSSID()));
//				} catch (AdaptorException e) {
//					LOG.error("Room " + room.getSSID() + " (" + room.getName() + ") cannot be put into "
//							+ "its parent room (ID:" + parent.getSSID() + ", name:" + parent.getName() +")!", 
//							e);
//				}
			}
		}
		LOG.debug("Room retrieval complete!");
	}

	public void updateRoomsInEnvironment() {
	    LOG.debug("Updating rooms in Symphony Environment...");
        Iterator<Room> rooms = this.rooms.values().iterator();
        while(rooms.hasNext()) {
            Room room = rooms.next();
            try {
                room.update(logDomain, false);
            } catch (AdaptorException e) {
                LOG.error("Cannot updateRules room " + room.getSSID() + " in environment!", e);
            }
        }
        LOG.debug("Rooms updated in Symphony Environment!");
    }
	
	/**
	 * Updates the OpenHAB instance. Adds an item representation for each room in OpenHAB and removes 
	 * the ones that are not included in this repository<br><br>
	 * 
	 * <b>NOTE:</b> Rooms must be retrieved from DB first! Otherwise, OpenHAB will be wiped of group
	 * items! 
	 */
	//TASK Remove this. This function must exist outside of core Maestro functionality.
//	public void updateOH() {
//		LOG.debug("Updating OpenHAB item registry!");
//		Iterator<Room> rooms = this.rooms.values().iterator();
//		while(rooms.hasNext()) {
//			try {
//				rooms.next().createExcept(new AbstAdaptor[]{dba}, logDomain, false);
//			} catch (AdaptorException e) {
//				LOG.fatal("Cannot updateRules rooms in OpenHAB sitemap! OpenHAB will show erroneous sitemap contents!",
//						e);
////				mp.publishToErrorTopic("Cannot updateRules rooms in OpenHAB sitemap! See log details!");
//			}
//		}
//	}
	
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
		recentlyAddedRoom = r;
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
	 * Returns the most recently added room in this RoomRepository
	 * 
	 * @return The room object
	 */
	public Room getRecentlyAddedRoom() {
		return recentlyAddedRoom;
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
				if(rs[i].getParentRoom() == null && rs[i].getIndex() > highest) {
					highest = rs[i].getIndex();
				}
			}
		} else if(containsRoom(roomID)) {
			Room parent = rooms.get(roomID);
			for(int i = 0; i < rs.length; i++) {
				if(rs[i].getParentRoom().equals(parent) && rs[i].getIndex() > highest) {
					highest = rs[i].getIndex();
				}
			}
		}
		
		return highest;
	}
}
