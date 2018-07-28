package bm.smarthome.rooms;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.json.JSONObject;

import bm.smarthome.adaptors.AbstAdaptor;
import bm.smarthome.adaptors.DBAdaptor;
import bm.smarthome.adaptors.OHAdaptor;
import bm.smarthome.adaptors.exceptions.AdaptorException;
import bm.smarthome.devices.Device;
import bm.smarthome.interfaces.HTMLTransformable;
import bm.smarthome.interfaces.OHItemmable;
import bm.smarthome.interfaces.SmarthomeElement;
import bm.smarthome.interfaces.SmarthomeObject;

public class Room extends SmarthomeObject implements OHItemmable, HTMLTransformable {
	private String name;
//	private Vector<Room> rooms = new Vector<Room>(1,1);
//	private Vector<Device> devices = new Vector<Device>(1,1);
	private Vector<SmarthomeObject> children = new Vector<SmarthomeObject>(1,1); //arranged by order added (index)

	//for UI
	private String color;
	
	/**
	 * Instantiates a new Room value-object.
	 * 
	 * @param SSID the unique 4 alphanumerical character ID of this room
	 * @param parentRoom the SSID of the room where this room belongs to
	 * @param name the name of this room
	 */
	public Room(String SSID, Room parentRoom, String name, DBAdaptor dba, OHAdaptor oha, 
			AbstAdaptor[] additionalAdaptors, String color) {
		super(SSID, dba, oha, additionalAdaptors, parentRoom);
		this.name = name;
//		this.room.addSmarthomeObject(this);
		this.color = color;
	}
	
	/**
	 * Instantiates a new Room object without a parent room
	 * @param SSID
	 * @param parentRoom
	 * @param name
	 * @param dba
	 * @param oha
	 * @param additionalAdaptors
	 */
	public Room(String SSID, String name, DBAdaptor dba, OHAdaptor oha, AbstAdaptor[] additionalAdaptors, 
			String color) {
		super(SSID, dba, oha, additionalAdaptors, null);
		this.name = name;
		this.color = color;
	}

	@Override
	public void persist(String parentLogDomain, boolean waitUntilPersisted) throws AdaptorException {
		Logger LOG = Logger.getLogger(parentLogDomain + "." + "Room:" + SSID);
		LOG.debug("Persisting room " + SSID + " (" + name + ")");
		for(int i = 0; i < adaptors.length; i++) {
			adaptors[i].persistRoom(this, waitUntilPersisted);
		}
	}
	
	public void persist(String parentLogDomain, AbstAdaptor[] exceptions, boolean waitUntilPersisted) 
			throws AdaptorException {
		Logger LOG = Logger.getLogger(parentLogDomain + "." + "Room:" + SSID);
		LOG.debug("Persisting room " + SSID + " (" + name + ")");
		List<AbstAdaptor> excepts = Arrays.asList(exceptions);
		for(int i = 0; i < adaptors.length; i++) {
			if(!excepts.contains(adaptors[i]))
				adaptors[i].persistRoom(this, waitUntilPersisted);
		}
	}

	@Override
	public void delete(String parentLogDomain, boolean waitUntilDeleted) throws AdaptorException {
		Logger LOG = Logger.getLogger(parentLogDomain + "." + "Room:" + SSID);
		LOG.debug("Deleting room " + SSID + " (" + name + ")");
		for(int i = 0; i < adaptors.length; i++) {
			adaptors[i].deleteRoom(this, waitUntilDeleted);
		}
	}

	@Override
	public void update(String parentLogDomain, boolean waitUntilUpdated) throws AdaptorException {
		Logger LOG = getLogger(parentLogDomain);
		LOG.debug("Updating room " + SSID + " (" + name + ")");
		for(int i = 0; i < adaptors.length; i++) {
			AbstAdaptor adaptor = adaptors[i];
			adaptor.updateRoom(this, waitUntilUpdated);
		}
	}

	@Override
	public void update(AbstAdaptor[] exceptions, String parentLogDomain, boolean waitUntilUpdated) 
			throws AdaptorException {
		List<AbstAdaptor> excepts = Arrays.asList(exceptions);
		for(int i = 0; i < adaptors.length; i++) {
			AbstAdaptor adaptor = adaptors[i];
			if(!excepts.contains(adaptor)) {
				adaptor.updateRoom(this, waitUntilUpdated);
			}
		}
	}

	@Override
	public void updateExcept(Class[] exceptions, String parentLogDomain, boolean waitUntilUpdated) 
			throws AdaptorException {
		Logger LOG = getLogger(parentLogDomain);
		LOG.debug("Updating room " + SSID + " (" + name + ")");
		List<Class> excepts = Arrays.asList(exceptions);
		for(int i = 0; i < adaptors.length; i++) {
			AbstAdaptor adaptor = adaptors[i];
			if(!excepts.contains(adaptor.getClass())) {
				adaptor.updateRoom(this, waitUntilUpdated);
			}
		}
	}
	
//	public void persistRoom(String parentLogDomain) throws AdaptorException {
//		Logger LOG = Logger.getLogger(parentLogDomain + "." + "Room:" + SSID);
//		LOG.debug("Persisting room " + SSID + " (" + name + ")");
//		for(int i = 0; i < adaptors.length; i++) {
//			adaptors[i].persistRoom(this);
//		}
//	}
	
	@Override
	public JSONObject[] convertToItemsJSON() {
		JSONObject json = new JSONObject();
		json.put("name", SSID);
		json.put("type", "Group");
		json.put("label", name);
		if(room != null)
			json.put("groupNames", new String[]{room.getSSID()});
		return new JSONObject[]{json};
	}
	
	/**
	 * Converts this Room to a simple sitemap string where it is included ONLY in the main frame of the sitemap
	 */
	@Override
	public String convertToSitemapString() {
		//Group item=J444 label="Kitchen"
		String s = "Group item=" + SSID + " label=\"" + name + "\"";
		return s;
	}
	
	//Ex. var r_CRL0 = new Room("CRL0", "Kuya's Bedroom");
	@Override
	public String convertToJavascript() {
		String s = "var r_" + SSID + " = new Room(\"" + SSID + "\", \"" + name + "\");";
		return s;
	}
	
	/**
	 * Sorts children smarthome elements based on their indices.
	 */
	private void sortChildren() {
		Vector<SmarthomeObject> newChildren = new Vector<SmarthomeObject>(children.size());
		for(int i = 0; i < children.size(); i++) {
			SmarthomeObject child = children.get(i);
			int index = 0;
			for(; index < newChildren.size(); index++) {
				if(newChildren.get(index).getIndex() > child.getIndex()) {
					break;
				}
			}
			newChildren.add(index, child);
		}
		children = newChildren;
	}
	
	public void addSmarthomeObject(SmarthomeObject obj) {
		children.add(obj);
		obj.setRoomIndex(children.size() + 1);
		sortChildren();
	}
	
	public void addSmarthomeObject(SmarthomeObject obj, int index) {
		children.add(index, obj);
		obj.setRoomIndex(index);
		sortChildren();
	}
	
	public void removeSmarthomeObject(SmarthomeObject obj) {
		children.remove(obj);
	}
	
	/**
	 * Returns the SmarthomeObject in this room with the specified SSID.
	 * 
	 * @param SSID the SSID of the SmarthomeObject
	 * @return the SmarthomeObject, <i>null</i> if there are no SmarthomeObjects with the specified SSID 
	 * 		in this room
	 */
	public SmarthomeObject getChild(String SSID) {
		for(int i = 0; i < children.size(); i++) {
			if(children.get(i).getSSID().equals(SSID)) {
				return children.get(i);
			}
		}
		return null;
	}
	
//	/**
//	 * Adds a device to this room
//	 * @param device The device object
//	 */
//	public void addDevice(Device device) {
//		devices.add(device);
//		if(device.getIndex() == -1) { //device initialized from registration, not from DB
//			device.setIndex(children.size() + 1);
//		}
//		children.add(device);
//		sortChildren();
//	}
//	
//	public void removeDevice(Device device) {
//		devices.remove(device);
//		children.remove(device);
//	}
//	
//	/**
//	 * Adds a room to this room
//	 * @param parentDevice The device object
//	 */
//	public void addRoom(Room room) {
//		rooms.add(room);
//		if(room.getIndex() == -1) { //room initialized from registration, not from DB
//			room.setIndex(children.size());
//		} else {
//			
//		}
//		children.add(room);
//		sortChildren();
//	}
//	
//	public void removeRoom(Room room) {
//		rooms.remove(room);
//		children.remove(room);
//	}
	
	/**
	 * Returns all the devices in this room
	 * @return An array containing all the devices
	 */
	public Device[] getDevices() {
		sortChildren();
		Vector<Device> devices = new Vector<Device>(1, 1);
		for(int i = 0; i < children.size(); i++) {
			SmarthomeObject child = children.get(i);
			if(child instanceof Device) {
				devices.add((Device) child);
			}
		}
		return devices.toArray(new Device[devices.size()]);
	}
	
	/**
	 * Returns all the rooms in this room
	 * @return An array containing all the devices
	 */
	public Room[] getRooms() {
		sortChildren();
		Vector<Room> rooms = new Vector<Room>(1, 1);
		for(int i = 0; i < children.size(); i++) {
			SmarthomeObject child = children.get(i);
			if(child instanceof Room) {
				rooms.add((Room) child);
			}
		}
		return rooms.toArray(new Room[rooms.size()]);
	}
	
//	/**
//	 * Sets the parent room of this room. <i><b>NOTE:</b> This method will not update the room in peripheral 
//	 * systems. </i>
//	 * @param room The new parent room of this room
//	 */
//	public void setParentRoom(Room parentRoom) {
//		if(this.parentRoom != null) {
//			this.parentRoom.removeSmarthomeObject(this);
//		}
//		if(parentRoom != null) {
//			this.parentRoom = parentRoom;
//			parentRoom.addSmarthomeObject(this);
//		} else {
//			this.parentRoom = parentRoom;
//		}
//	}
//
//	/**
//	 * Returns the SSID of the room where this room belongs to
//	 * 
//	 * @return the SSID of the parent room, <b><i>null</i></b> if this room does not have a parent
//	 * 		room
//	 */
//	public Room getParentRoom() {
//		return parentRoom;
//	}
	
//	/**
//	 * Returns the order of this device in the room it belongs to
//	 * @return the numerical order of this device in the room
//	 */
//	public int getRoomIndex() {
//		return roomIndex;
//	}
//
//	/**
//	 * Sets the order of this device in the room it belongs to. <b><i>NOTE:</b> this method is only called by the 
//	 * room object when this device is added to it OR when the device is retrieved from the DB.
//	 * 
//	 * @param roomIndex the numerical order of this device in the room
//	 */
//	public void setRoomIndex(int roomIndex) {
//		this.roomIndex = roomIndex;
//	}
	
	/**
	 * Returns all the rooms and devices in this room. Rooms and devices are arranged by their room index.
	 * 
	 * @return An array containing all the rooms and devices in this room
	 */
	public SmarthomeObject[] getChildren() {
		sortChildren();
		return children.toArray(new SmarthomeObject[children.size()]);
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public void setColor(String color) {
		this.color = color;
	}
	
	public String getColor() {
		return color;
	}

	private Logger getLogger(String parentLogDomain) {
		return Logger.getLogger(parentLogDomain + ".Room:" + SSID);
	}
}
