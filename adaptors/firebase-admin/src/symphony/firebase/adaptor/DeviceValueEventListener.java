package symphony.firebase.adaptor;

import org.apache.log4j.Logger;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import bm.context.adaptors.exceptions.AdaptorException;
import bm.context.devices.Device;
import bm.context.properties.Property;
import bm.main.repositories.DeviceRepository;

public class DeviceValueEventListener implements ValueEventListener {
	private Logger logger = Logger.getLogger(DeviceValueEventListener.class);
//	private DeviceRepository devices;
	private Device device;
	private String propertyId;
	private String propertyName;
	private String propertyValue;

	/**
	 * We are passing the ref parameter to be able to create an addChildEventListener for each device
	 * @param ref
	 */
	public DeviceValueEventListener(DeviceRepository devices, String propertyId, String propertyName, String propertyValue) {
//		this.devices = devices ;
		this.propertyId = propertyId;
		this.propertyName = propertyName;
		this.propertyValue = propertyValue;
	}
	
	public DeviceValueEventListener(Device device, String propertyId, String propertyName, String propertyValue) {
		this.device = device ;
		this.propertyId = propertyId;
		this.propertyName = propertyName;
		this.propertyValue = propertyValue;
	}
/*
 * (non-Javadoc)
 * @see com.google.firebase.database.ValueEventListener#onDataChange(com.google.firebase.database.DataSnapshot)
 * 
 * firebase sent a datachange event, need to propagate to BM
 */
	public void onDataChange(DataSnapshot snapshot) {
		if (snapshot.getValue() != null) {
            //id exists
			String deviceID = snapshot.getValue(String.class);
			logger.info("onDataChange device id exists, "+deviceID);
			logger.info("onDataChange fbDevice.name="+" fbDevice.id="+deviceID);
//			Device d = devices.getDevice(deviceID);
			Property props[] = device.getProperties();
			for (int i=0; i < props.length; i++) {
				logger.info("before props "+props[i].getDisplayName()+"="+props[i].getValue());
			}
			Property property = device.getProperty(propertyId); 
			logger.info("onDataChange done  devices MAC="+device.getMAC()+ " property="+property.getDisplayName()
				+" value=" +propertyId);
			Property p = device.getProperty(propertyId);		
			p.setValue(propertyValue);
			for (int i=0; i < props.length; i++) {
				logger.info("after props "+props[i].getDisplayName()+"="+props[i].getValue());
			}		
			//need to test below for the sending to BM		
			try {
				logger.info("updating the bm property");
				p.update("firebase", false);
			} catch (AdaptorException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				logger.error("Error in updating the property");
			}
        } else {
            //does not exist, do something else
        	logger.info("does not exist, do something else");
        }
	}

	public void onCancelled(DatabaseError error) {

	}
}
