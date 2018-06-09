package symphony.firebase.adaptor;

import org.apache.log4j.Logger;
//import org.eclipse.paho.client.mqttv3.MqttClient;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;

import bm.comms.mqtt.MQTTListener;
import bm.main.repositories.DeviceRepository;
import symphony.firebase.vo.FirebaseDevice;
import bm.context.devices.Device;

public class ChildAddedListener implements ChildEventListener{
	String path;
	DatabaseReference ref;
	private Logger logger = Logger.getLogger(ChildAddedListener.class);
	private Device device;
	
	public ChildAddedListener(String path, DatabaseReference ref, Device device) {
		this.path =path;
		this.ref=ref;
		this.device = device;
	}
	/*
	 * deprecated
	 * This sets a ChildChangedEventListener to a device
	 * 
	 * @see com.google.firebase.database.ChildEventListener#onChildAdded(com.google.firebase.database.DataSnapshot, java.lang.String)
	 */
	public void onChildAddedOld(DataSnapshot snapshot, String previousChildName) {
		String room = snapshot.getKey();
		logger.info("\n\n*************** start *********************");
		logger.info(path +"/"+room+" added device" + snapshot.getValue());
		ChildChangedEventListener childChangedEventListener = new ChildChangedEventListener(ref.getPath().toString()+"/"+room, null, device);
		ref.child(room).addChildEventListener(childChangedEventListener);
        logger.info("childChangedEventListener to " + ref.getPath().toString()+"/"+room);        
        for(DataSnapshot ds: snapshot.getChildren()){
        	logger.info("device key="+ds.getKey());
        	for(DataSnapshot dsProp: ds.child("properties").getChildren()){
        		logger.info("    properties key="+dsProp.getKey());
        	}
        }
        logger.info("\n*************** end *********************\n\n");
	}
	/*
	 * This sets a ChildChangedEventListener to each of the device's properties
	 */
	public void onChildAdded(DataSnapshot snapshot, String previousChildName) {
		String room = snapshot.getKey();
		logger.info("\n\n*************** start *********************");
        for(DataSnapshot ds: snapshot.getChildren()){
        	logger.info("device key="+ds.getKey());
        	//we set the childChangedEventListener to the device's properties
        	FirebaseDevice fbDevice = new FirebaseDevice(room, ds.getKey(), ds.child("id").getValue(String.class), null);
    		ChildChangedEventListener childChangedEventListener = new ChildChangedEventListener(
    				ref.getPath().toString()+"/"+room+"/"+ds.getKey()+"/properties", fbDevice, device);
    		ref.child(room+"/"+ds.getKey()+"/properties").addChildEventListener(childChangedEventListener);
            logger.info("    added childChangedEventListener to " + ref.getPath().toString()+"/"+room+"/"+ds.getKey()+"/properties");
        }
        logger.info("\n*************** end *********************\n\n");
	}

	public void onChildChanged(DataSnapshot snapshot, String previousChildName) {
		// TODO Auto-generated method stub
		
	}

	public void onChildRemoved(DataSnapshot snapshot) {
		// TODO Auto-generated method stub
		
	}

	public void onChildMoved(DataSnapshot snapshot, String previousChildName) {
		// TODO Auto-generated method stub
		
	}

	public void onCancelled(DatabaseError error) {
		// TODO Auto-generated method stub
		
	}

}
