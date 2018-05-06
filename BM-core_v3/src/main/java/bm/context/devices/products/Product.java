package bm.context.devices.products;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import org.apache.log4j.Logger;

import bm.context.adaptors.AbstAdaptor;
import bm.context.adaptors.DBAdaptor;
import bm.context.adaptors.OHAdaptor;
import bm.context.adaptors.exceptions.AdaptorException;
import bm.context.devices.Device;
import bm.context.properties.*;
import bm.context.properties.bindings.Binding;
import bm.context.rooms.Room;
import bm.main.engines.AbstEngine;
import bm.main.engines.DBEngine;
import bm.main.engines.exceptions.EngineException;
import bm.main.engines.requests.DBEngine.RawDBEReq;
import bm.tools.IDGenerator;

public class Product extends AbstProduct {
	
	/**
	 * 
	 * @param mainLogDomain
	 * @param SSID
	 * @param name
	 * @param description
	 * @param OH_icon
	 * @param productsRS
	 * @param dba
	 * @param oha
	 * @param additionalAdaptors
	 * @param propertyTypes
	 * @param retrieveProperties <i><b>True</b></i> if product will retrieve the properties from the <b>productsRS</b>
	 * 			upon instantiation. <i><b>False</b></i> if not. The latter is used by child classes of the Product
	 * 			object.
	 */
	public Product(String mainLogDomain, String SSID, String name, String description, String OH_icon, 
			ResultSet productsRS, DBAdaptor dba, OHAdaptor oha, AbstAdaptor[] additionalAdaptors, 
			HashMap<String, PropertyType> propertyTypes, boolean retrieveProperties, String poopRTY, 
			String propIDParam, String propValParam, IDGenerator idGenerator) {
		super(mainLogDomain, SSID, name, description, OH_icon, productsRS, dba, oha, additionalAdaptors, 
				propertyTypes, retrieveProperties, poopRTY, propIDParam, propValParam, idGenerator);
	}
	
	public Product(String mainLogDomain, String SSID, String name, String description, String OH_icon, 
			Property[] properties, DBAdaptor dba, OHAdaptor oha, AbstAdaptor[] additionalAdaptors, 
			String poopRTY, String propIDParam, String propValParam, IDGenerator idGenerator) {
		super(mainLogDomain, SSID, name, description, OH_icon, dba, oha, additionalAdaptors, poopRTY, 
				propIDParam, propValParam, idGenerator);
	}
	
	/**
	 * Creates an empty Symphony Product object that has no properties assigned. Properties <b><i>MUST</i></b> be 
	 * assigned to this product using the <i>setProperties()</i> method before this product is assigned to device
	 * objects.
	 * @param mainLogDomain
	 * @param SSID
	 * @param name
	 * @param description
	 * @param OH_icon
	 * @param dba
	 * @param oha
	 * @param additionalAdaptors
	 */
	public Product(String mainLogDomain, String SSID, String name, String description, String OH_icon, 
			DBAdaptor dba, OHAdaptor oha, AbstAdaptor[] additionalAdaptors, String poopRTY, String propIDParam,
			String propValParam, IDGenerator idGenerator) {
		super(mainLogDomain, SSID, name, description, OH_icon, dba, oha, additionalAdaptors, poopRTY, 
				propIDParam, propValParam, idGenerator);
	}
	
	

	@Override
	public Device createDevice(String comID, String MAC, String name, String topic, Room room, boolean active, 
			int index)
			throws IllegalArgumentException {
		if(properties.isEmpty())
			throw new IllegalArgumentException("Properties not yet retrieved from DB!");
		else {
			Device d = null;
			try {
				d = new Device(comID, MAC, name, topic, room, active, this, dba, oha, 
						additionalAdaptors, index);
			} catch (AdaptorException e) {
				LOG.error("Device " + comID + " (" + name + ") cannot be created!", e);
			}
			return d;
		}
	}
}
