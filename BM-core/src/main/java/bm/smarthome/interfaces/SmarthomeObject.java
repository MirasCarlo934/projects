package bm.smarthome.interfaces;

import bm.smarthome.adaptors.AbstAdaptor;
import bm.smarthome.adaptors.DBAdaptor;
import bm.smarthome.adaptors.OHAdaptor;
import bm.smarthome.rooms.Room;

/**
 * A child class of <i>SmarthomeElement</i> class. A SmarthomeObject is a representation of an actual, physical object 
 * that generally belongs to a room.
 * @author carlomiras
 *
 */
public abstract class SmarthomeObject extends SmarthomeElement {
	protected Room room;
	protected int roomIndex = -1;

	public SmarthomeObject(String SSID, DBAdaptor dba, OHAdaptor oha, AbstAdaptor[] additionalAdaptors, Room room) {
		super(SSID, dba, oha, additionalAdaptors);
//		this.room = room;
		setRoom(room);
	}

	public Room getRoom() {
		return room;
	}

	/**
	 * Sets the parent room of this SmarthomeObject. <i><b>NOTE:</b> This method will not update the SmarthomeObject in 
	 * peripheral systems. </i>
	 * @param room The new parent room of this device
	 */
	public void setRoom(Room room) {
		if(this.room != null) {
			this.room.removeSmarthomeObject(this);
		}
		if(room != null) {
			this.room = room;
			room.addSmarthomeObject(this);
		} else {
			this.room = room;
		}
	}
	
	/**
	 * Sets the parent room of this SmarthomeObject. <i><b>NOTE:</b> This method will not update the SmarthomeObject in 
	 * peripheral systems. </i>
	 * @param room The new parent room of this device
	 * @param index	The index of this SmarthomeObject in the room. <i>Index dictates the order in which SmarthomeObjects
	 * 		are ordered in a room.</i>
	 */
	public void setRoom(Room room, int index) {
		if(this.room != null) {
			this.room.removeSmarthomeObject(this);
		}
		if(room != null) {
			this.room = room;
			room.addSmarthomeObject(this, index);
		} else {
			this.room = room;
		}
	}

	public int getRoomIndex() {
		return roomIndex;
	}

	public void setRoomIndex(int roomIndex) {
		this.roomIndex = roomIndex;
	}
}
