package symphony.firebase.vo;

import java.util.HashMap;

import org.apache.log4j.Logger;
import org.json.JSONObject;

import com.google.firebase.database.DataSnapshot;

import bm.context.properties.AbstProperty;
import symphony.firebase.adaptor.Firebase;

public class FirebaseDevice {
	public String room;
	public String id;
	public String name;
	public HashMap<String, Property> properties;
	private Logger logger = Logger.getLogger(FirebaseDevice.class);
	
	/*
	 * Constructor to manually create a device 
	 */
	public FirebaseDevice(String name, String id, HashMap properties) {
		this.id = id;
		this.name = name;
		this.properties = properties;
	}
	
	/*
	 * Constructor to manually create a device, use this to create an initial instance of device.
	 */
	public FirebaseDevice(String room, String name, String id, HashMap properties) {
		this.room = room;
		this.id = id;
		this.name = name;
		this.properties = properties;
		logger.info("room "+room+" device created id="+id+" name="+name);
	}
	
	/*
	 * Constructor to create a device from the BM's device object
	 */
	public FirebaseDevice(bm.context.devices.Device device) {
		this.id = device.getSSID();
		this.name = device.getName();		
		AbstProperty props[] = device.getProperties();
		HashMap propertyMap = new HashMap();
		logger.info("device.name="+device.getName()+" props.length="+props.length);
		for (int i=0; i<props.length; i++) {
			logger.info("device.name="+device.getName()+" props.length="+props.length+" prop="+props[i]);
			Property prop = new Property(props[i]);
			propertyMap.put(props[i].getDisplayName(), prop);
		}
//		properties.put("properties", propertyMap);
		properties = propertyMap;
	}
	
	/*
	 * Constructor to create a device from firebase database snapshot
	 * 
	 * snapshot=DataSnapshot 
	 *   {key = PIR, 
	 *    value = 
	 * 		{name=PIR, 
	 *       id=S2E2, 
	 *       properties=
	 *       	{
	 *       		State={name=State, id=0060, value=0}, 
	 *       		Mode={name=Mode, id=0025, value=0}, 
	 *       		MotionDetected={name=MotionDetected, id=0026, value=1}
	 *       	}
	 *      } 
	 * 	}
	 */
	public FirebaseDevice(DataSnapshot snapshot) {
		logger.info("fireDBDevice Constructor snapshot="+snapshot);
		logger.info("fireDBDevice Constructor key="+snapshot.getKey());
		
		this.name = snapshot.getKey();
		logger.info("firedDBDevice Constructor id="+snapshot.child("id").getValue().toString());
		this.id = snapshot.child("id").getValue().toString();
		DataSnapshot props = snapshot.child("properties");
		properties = new HashMap();
		for(DataSnapshot ds: props.getChildren()){
			logger.info("property start");
			Property p = ds.getValue(Property.class);			
			properties.put(ds.getKey(), p);
			logger.info(" props key="+ds.getKey()+ " value="+ds.getValue()+"  p.id="+p.id+" p.name="+p.name);
		}
	}
	
	public HashMap<String, Property> getProperties() {
		return properties;
	}

	public void setProperties(HashMap<String, Property> properties) {
		this.properties = properties;
	}
}
