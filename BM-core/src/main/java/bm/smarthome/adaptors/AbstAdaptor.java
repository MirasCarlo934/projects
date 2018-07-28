package bm.smarthome.adaptors;

import org.apache.log4j.Logger;

import bm.main.engines.AbstEngine;
import bm.main.engines.DBEngine;
import bm.main.engines.exceptions.EngineException;
import bm.main.engines.requests.EngineRequest;
import bm.main.engines.requests.DBEngine.DBEngineRequest;
import bm.smarthome.adaptors.exceptions.AdaptorException;
import bm.smarthome.devices.Device;
import bm.smarthome.properties.AbstProperty;
import bm.smarthome.properties.Property;
import bm.smarthome.properties.bindings.Binding;
import bm.smarthome.rooms.Room;

public abstract class AbstAdaptor {
	private String serviceName;
	protected Logger LOG;
	//protected AbstEngine engine;

	/**
	 * 
	 * @param logDomain
	 * @param adaptorName
	 * @param serviceName the external service managed by this Adaptor (eg. 'plex' for PlexAdaptor)
	 */
	public AbstAdaptor(String logDomain, String adaptorName, String serviceName) {
		this.serviceName = serviceName;
		LOG = Logger.getLogger(logDomain + "." + adaptorName);
	}
	
	/*
	 * adaptor methods for device
	 */
	public abstract void persistDevice(Device d, boolean waitUntilPersisted) throws AdaptorException;
	public abstract void deleteDevice(Device d, boolean waitUntilDeleted) throws AdaptorException;
	public abstract void updateDevice(Device d, boolean waitUntilUpdated) throws AdaptorException;
	public abstract void updateDeviceState(Device d, boolean waitUntilUpdated) throws AdaptorException;
	public abstract void updateDeviceRoom(Device d, boolean waitUntilUpdated) throws AdaptorException;
	
	/*
	 * adaptor methods for property
	 */
	public abstract void persistProperty(AbstProperty p, boolean waitUntilPersisted) throws AdaptorException;
	public abstract void deleteProperty(AbstProperty p, boolean waitUntilDeleted) throws AdaptorException;
	public abstract void updatePropertyValue(AbstProperty p, boolean waitUntilUpdated) throws AdaptorException;
	
	/*
	 * adaptor methods for room
	 */
	public abstract void persistRoom(Room r, boolean waitUntilPersisted) throws AdaptorException;
	public abstract void deleteRoom(Room r, boolean waitUntilDeleted) throws AdaptorException;
	public abstract void updateRoom(Room r, boolean waitUntilUpdated) throws AdaptorException;
	public abstract void updateRoomParent(Room r, boolean waitUntilUpdated) throws AdaptorException;
	
	protected boolean checkBinding(Binding b) {
		if(b != null && b.getService().equalsIgnoreCase(serviceName)) {
			return true;
		} else {
			return false;
		}
	}
}
