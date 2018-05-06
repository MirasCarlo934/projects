package ui.context.rooms;

import bm.context.adaptors.exceptions.AdaptorException;
import bm.context.rooms.Room;

public class UIRoom extends Room {
	private String color;
	
	public UIRoom(Room room, String color) throws AdaptorException {
		super(room.getSSID(), room.getName(), room.getMainDBAdaptor(), room.getMainOHAdaptor(), 
				room.getAdditionalAdaptors(), room.getIndex());
		this.color = color;
	}
	
	public String getColor() {
		return color;
	}
}
