package bm.context.rooms;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.json.JSONObject;

import bm.context.HTMLTransformable;
import bm.context.OHItemmable;
import bm.context.SymphonyElement;
import bm.context.SymphonyObject;
import bm.context.adaptors.AbstAdaptor;
import bm.context.adaptors.DBAdaptor;
import bm.context.adaptors.OHAdaptor;
import bm.context.adaptors.exceptions.AdaptorException;
import bm.context.devices.Device;

public class Room extends SymphonyObject implements OHItemmable, HTMLTransformable {
	private String name;
	private Vector<SymphonyObject> children = new Vector<SymphonyObject>(1,1); //arranged by order added (index)

	//for UI
//	private String color;
	
	/**
	 * Instantiates a new Room value-object.
	 * 
	 * @param SSID the unique 4 alphanumerical character ID of this room
	 * @param parentRoom the SSID of the room where this room belongs to
	 * @param name the name of this room
	 * @throws AdaptorException 
	 */
	public Room(String SSID, Room parentRoom, String name, DBAdaptor dba, OHAdaptor oha, 
			AbstAdaptor[] additionalAdaptors, int index/*, String color*/) throws AdaptorException {
		super(SSID, dba, oha, additionalAdaptors, parentRoom, index);
		this.name = name;
	}
	
	/**
	 * Instantiates a new Room object without a parent room
	 * @param SSID
	 * @param parentRoom
	 * @param name
	 * @param dba
	 * @param oha
	 * @param additionalAdaptors
	 * @throws AdaptorException 
	 */
	public Room(String SSID, String name, DBAdaptor dba, OHAdaptor oha, AbstAdaptor[] additionalAdaptors, 
			int index/*, String color*/) throws AdaptorException {
		super(SSID, dba, oha, additionalAdaptors);
		this.name = name;
		this.roomIndex = index;
	}

	@Override
	public void create(AbstAdaptor adaptor, String parentLogDomain, boolean waitUntilCreated) throws AdaptorException {
		Logger LOG = getLogger(parentLogDomain);
		LOG.debug("Creating room " + SSID + " (" + name + ") in " + adaptor.getServiceName() + "...");
		adaptor.roomCreated(this, waitUntilCreated);
		LOG.debug("Room " + SSID + " (" + name + ") created!");
	}
	
//	public void create(String parentLogDomain, AbstAdaptor[] exceptions, boolean waitUntilPersisted) 
//			throws AdaptorException {
//		Logger LOG = Logger.getLogger(parentLogDomain + "." + "Room:" + SSID);
//		LOG.debug("Persisting room " + SSID + " (" + name + ")");
//		List<AbstAdaptor> excepts = Arrays.asList(exceptions);
//		for(int i = 0; i < adaptors.length; i++) {
//			if(!excepts.contains(adaptors[i]))
//				adaptors[i].roomCreated(this, waitUntilPersisted);
//		}
//	}

	@Override
	public void delete(AbstAdaptor adaptor, String parentLogDomain, boolean waitUntilDeleted) throws AdaptorException {
		Logger LOG = getLogger(parentLogDomain);
		LOG.debug("Deleting room " + SSID + " (" + name + ") from " + adaptor.getServiceName() + "...");
		adaptor.roomDeleted(this, waitUntilDeleted);
		LOG.debug("Room " + SSID + " (" + name + ") deleted!");
	}

	@Override
	public void update(AbstAdaptor adaptor, String parentLogDomain, boolean waitUntilUpdated) 
			throws AdaptorException {
		Logger LOG = getLogger(parentLogDomain);
		LOG.debug("Updating room " + SSID + " (" + name + ") in " + adaptor.getServiceName() + "...");
		adaptor.roomCredentialsUpdated(this, waitUntilUpdated);
		LOG.debug("Room " + SSID + " (" + name + ") updated!");
	}

//	@Override
//	public void update(AbstAdaptor[] exceptions, String parentLogDomain, boolean waitUntilUpdated) 
//			throws AdaptorException {
//		List<AbstAdaptor> excepts = Arrays.asList(exceptions);
//		for(int i = 0; i < adaptors.length; i++) {
//			AbstAdaptor adaptor = adaptors[i];
//			if(!excepts.contains(adaptor)) {
//				adaptor.roomCredentialsUpdated(this, waitUntilUpdated);
//			}
//		}
//	}

//	@Override
//	public void updateExcept(Class[] exceptions, String parentLogDomain, boolean waitUntilUpdated) 
//			throws AdaptorException {
//		Logger LOG = getLogger(parentLogDomain);
//		LOG.debug("Updating room " + SSID + " (" + name + ")");
//		List<Class> excepts = Arrays.asList(exceptions);
//		for(int i = 0; i < adaptors.length; i++) {
//			AbstAdaptor adaptor = adaptors[i];
//			if(!excepts.contains(adaptor.getClass())) {
//				adaptor.roomCredentialsUpdated(this, waitUntilUpdated);
//			}
//		}
//	}
	
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
		if(parentRoom != null)
			json.put("groupNames", new String[]{parentRoom.getSSID()});
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
	
	//FIXME Do something if indices of 2 SmarthomeObjects are the same
//	/**
//	 * Sorts children smarthome elements based on their indices.
//	 */
//	private void sortChildren() {
//		Vector<SmarthomeObject> newChildren = new Vector<SmarthomeObject>(children.size());
//		
//		for(int i = 0; i < children.size(); i++) {
//			SmarthomeObject child = children.get(i);
//			int index = 0;
//			for(; index < newChildren.size(); index++) {
//				if(newChildren.get(index).getIndex() > child.getIndex()) {
//					break;
//				}
//			}
//			newChildren.add(index, child);
//		}
//		children = newChildren;
//	}
	
	/**
	 * Adds a SmarthomeObject to the end of this room (ie. the highest indexed SmarthomeObject).
	 * 
	 * @param obj The SmarthomeObject to be added
	 * @throws AdaptorException 
	 */
	public void addSmarthomeObject(SymphonyObject obj) throws AdaptorException {
		addSmarthomeObject(obj, children.size() + 1);
//		children.add(obj);
//		obj.setRoomIndex(children.size() + 1);
//		sortChildren();
	}
	
	/**
	 * Adds a SmarthomeObject to this room.
	 * 
	 * @param obj The SmarthomeObject to be added
	 * @param index The index of the SmarthomeObject
	 * @throws AdaptorException 
	 */
	public void addSmarthomeObject(SymphonyObject obj, int index) throws AdaptorException {
		if(index <= children.size()) {
			children.add(index, obj);
		} else {
			if(children.isEmpty()) {
				children.add(obj);
			}
			for(int i = 0; i < children.size(); i++) {
				if(children.get(i).getIndex() > index) {
					children.add(obj);
				} else if(children.get(i).getIndex() == index) {
					children.add(obj);
					obj = children.get(i);
					obj.setIndex(obj.getIndex() + 1);
					i++;
				}
			}
		}
//		children.add(index, obj);
		for(int i = 0; i < children.size(); i++) {
			children.get(i).setRoomIndex(i);
		}
//		sortChildren();
	}
	
	@Override
	public void setIndex(int index) throws AdaptorException {
		for(int i = 0; i < adaptors.length; i++) {
			adaptors[i].roomCredentialsUpdated(this, true);
		}
	}
	
	public void removeSmarthomeObject(SymphonyObject obj) {
		children.remove(obj);
	}
	
	/**
	 * Returns the SmarthomeObject in this room with the specified SSID.
	 * 
	 * @param SSID the SSID of the SmarthomeObject
	 * @return the SmarthomeObject, <i>null</i> if there are no SmarthomeObjects with the specified SSID 
	 * 		in this room
	 */
	public SymphonyObject getChild(String SSID) {
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
//		sortChildren();
		Vector<Device> devices = new Vector<Device>(1, 1);
		for(int i = 0; i < children.size(); i++) {
			SymphonyObject child = children.get(i);
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
//		sortChildren();
		Vector<Room> rooms = new Vector<Room>(1, 1);
		for(int i = 0; i < children.size(); i++) {
			SymphonyObject child = children.get(i);
			if(child instanceof Room) {
				rooms.add((Room) child);
			}
		}
		return rooms.toArray(new Room[rooms.size()]);
	}
	
	/**
	 * Returns all the rooms and devices in this room. Rooms and devices are arranged by their room index.
	 * 
	 * @return An array containing all the rooms and devices in this room
	 */
	public SymphonyObject[] getChildren() {
//		sortChildren();
		return children.toArray(new SymphonyObject[children.size()]);
	}
	
	public int getHighestIndex() {
		if(children.isEmpty())
			return 0;
		else
			return children.lastElement().getIndex();
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
//	public void setColor(String color) {
//		this.color = color;
//	}
//	
//	public String getColor() {
//		return color;
//	}

	private Logger getLogger(String parentLogDomain) {
		return Logger.getLogger(parentLogDomain + ".Room:" + SSID);
	}
}
