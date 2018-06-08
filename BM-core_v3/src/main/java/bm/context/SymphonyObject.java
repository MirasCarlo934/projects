package bm.context;

import bm.context.adaptors.AbstAdaptor;
import bm.context.adaptors.exceptions.AdaptorException;
import bm.context.rooms.Room;

/**
 * A child class of <i>SmarthomeElement</i> class. A SmarthomeObject is a representation of an actual, 
 * physical object that generally belongs to a room.
 * @author carlomiras
 *
 */
//LATER check if roomIndex has to be distinct from index in SymphonyElement
public abstract class SymphonyObject extends SymphonyElement {
	protected Room parentRoom;
//	protected int roomIndex = -1;

	//TASK Remove throws AdaptorException
	public SymphonyObject(String SSID, Room room, int index) {
		super(SSID, index);
//		this.roomIndex = index;
//		super.setIndex(index);
		setRoom(room, index);
	}

	public Room getParentRoom() {
		return parentRoom;
	}

	/**
	 * Sets the parent room of this SmarthomeObject. <i><b>NOTE:</b> This method will not update the SmarthomeObject in 
	 * peripheral systems. </i>
	 * @param room The new parent room of this device
	 * @throws AdaptorException 
	 */
	public void setRoom(Room room) {
		if(this.parentRoom != null) {
			this.parentRoom.removeSmarthomeObject(this);
		}
		if(room != null) {
			this.parentRoom = room;
			room.addSymphonyObject(this);
		} else {
			this.parentRoom = room;
		}
	}
	
	/**
	 * Sets the parent room of this SmarthomeObject. <i><b>NOTE:</b> This method will not update the SmarthomeObject in 
	 * peripheral systems. </i>
	 * @param room The new parent room of this device
	 * @param index	The index of this SmarthomeObject in the room. <i>Index dictates the order in which SmarthomeObjects
	 * 		are ordered in a room.</i>
	 * @throws AdaptorException 
	 */
	public void setRoom(Room room, int index) {
		if(this.parentRoom != null) {
			this.parentRoom.removeSmarthomeObject(this);
		}
		if(room != null) {
			this.parentRoom = room;
			room.addSymphonyObject(this, index);
		} else {
			this.parentRoom = room;
		}
	}

//	public int getRoomIndex() {
//		return roomIndex;
//	}
//
//	public void setRoomIndex(int roomIndex) {
//		this.roomIndex = roomIndex;
//		super.setIndex(roomIndex);
//	}
}
