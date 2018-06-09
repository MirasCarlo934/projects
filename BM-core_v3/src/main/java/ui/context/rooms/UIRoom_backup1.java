package ui.context.rooms;

import bm.context.adaptors.exceptions.AdaptorException;
import bm.context.rooms.Room;
import ui.context.adaptors.UIAdaptor;

public class UIRoom extends Room {
	private String color;
	
	public UIRoom(Room room, String color) throws AdaptorException {
		super(room.getSSID(), room.getName(), room.getMainDBAdaptor(), room.getMainOHAdaptor(), 
				room.getAdditionalAdaptors(), room.getIndex());
		this.color = color;
	}
	
	/**
	 * Sets the color of the Room as it exists in the WebUI. Acceptable values are all color values 
	 * recognized by CSS.
	 * 
	 * @param color The color of this room, in CSS-acceptable format
	 */
	public void setColor(String color) {
		this.color = color;
	}
	
	/**
	 * Returns the color of the Room as it exists in the WebUI.
	 * 
	 * @return The color of this room
	 */
	public String getColor() {
		return color;
	}
}
