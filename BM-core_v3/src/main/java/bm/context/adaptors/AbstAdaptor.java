package bm.context.adaptors;

import org.apache.log4j.Logger;

import bm.comms.mqtt.MQTTPublisher;
import bm.context.adaptors.exceptions.AdaptorException;
import bm.context.devices.Device;
import bm.context.properties.AbstProperty;
import bm.context.properties.Property;
import bm.context.properties.bindings.Binding;
import bm.context.rooms.Room;
import bm.main.engines.AbstEngine;
import bm.main.engines.DBEngine;
import bm.main.engines.exceptions.EngineException;
import bm.main.engines.requests.EngineRequest;
import bm.main.engines.requests.DBEngine.DBEngineRequest;
import bm.main.interfaces.Initializable;

public abstract class AbstAdaptor {
	private String serviceName;
	protected final Logger LOG;
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
	public abstract void deviceCreated(Device d, boolean waitUntilCreated) throws AdaptorException;
	public abstract void deviceDeleted(Device d, boolean waitUntilDeleted) throws AdaptorException;
	public abstract void deviceCredentialsUpdated(Device d, boolean waitUntilUpdated) throws AdaptorException;
	public abstract void deviceStateUpdated(Device d, boolean waitUntilUpdated) throws AdaptorException;
	public abstract void deviceRoomUpdated(Device d, boolean waitUntilUpdated) throws AdaptorException;
	
	/*
	 * adaptor methods for property
	 */
	public abstract void propertyCreated(AbstProperty p, boolean waitUntilPersisted) throws AdaptorException;
	public abstract void propertyDeleted(AbstProperty p, boolean waitUntilDeleted) throws AdaptorException;
	public abstract void propertyValueUpdated(AbstProperty p, boolean waitUntilUpdated) throws AdaptorException;
	
	/*
	 * adaptor methods for room
	 */
	public abstract void roomCreated(Room r, boolean waitUntilPersisted) throws AdaptorException;
	public abstract void roomDeleted(Room r, boolean waitUntilDeleted) throws AdaptorException;
	public abstract void roomCredentialsUpdated(Room r, boolean waitUntilUpdated) throws AdaptorException;
	public abstract void roomParentUpdated(Room r, boolean waitUntilUpdated) throws AdaptorException;
	
	public String getServiceName() {
		return serviceName;
	}
}
