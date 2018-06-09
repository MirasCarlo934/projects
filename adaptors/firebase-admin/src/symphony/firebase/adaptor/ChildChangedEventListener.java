package symphony.firebase.adaptor;

import bm.context.devices.Device;
import java.util.Iterator;

import org.apache.log4j.Logger;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;

import symphony.firebase.vo.FirebaseDevice;

public class ChildChangedEventListener implements ChildEventListener {
//	private Logger logger = LoggerFactory.getLogger(ChildChangedEventListener.class);
	private Logger logger = Logger.getLogger(ChildChangedEventListener.class);
	private Device device;
	private FirebaseDevice fbDevice;
	String path;
	String mqttTopic        = "BM";
    int qos             = 0;
	/**
	 * We are setting the path so that we can identify which specific device has triggered the event 
	 * 
	 * @param path
	 */
	public ChildChangedEventListener(String path, FirebaseDevice fbDevice, Device device) {
		this.path = path;
		this.device = device;  
		this.fbDevice = fbDevice; //it seems that fbDevice object is not updated when a new device registers in BM
	}

	public void onChildAdded(DataSnapshot snapshot, String previousChildName) {
		
	}
	/*
	 * deprecated
	 * this was used when onChildChanged was listeneing on devices
	 */
	public void onChildChangedOld(DataSnapshot snapshot, String previousChildName) {
		// trigger the BM for this event
		//this is only triggered when there is a change in the status
		logger.info(path +" property "+snapshot.getKey()+" changed to " + snapshot.getValue());
		Iterator<DataSnapshot> val = snapshot.getChildren().iterator();
		//sample snapshot {name=dining light, id=0001, properties={state={isGoogle=true, name=light state, id=0006, value=[off]}}}
		FirebaseDevice fbDevice = new FirebaseDevice(snapshot);
		logger.info("onChildChanged fbDevice.name="+fbDevice.name);
		logger.info("onChildChanged done  devices MAC="+device.getMAC());
//		logger.info("Publish to MQTT start");
//		publishToMqtt(path, (String)snapshot.getKey(), (String)snapshot.getValue());
//		logger.info("Published to MQTT end");
	}
	/*
	 * This listens to the events generated for a property
	 * 		example /symphony/bahay/kitchen/PIR/properties/Mode 
	 * (non-Javadoc)
	 * @see com.google.firebase.database.ChildEventListener#onChildChanged(com.google.firebase.database.DataSnapshot, java.lang.String)
	 */
	public void onChildChanged(DataSnapshot snapshot, String previousChildName) {
		// trigger the BM for this event
		//this is only triggered when there is a change in the status
		logger.info("onChildChanged snapshot is "+snapshot);
		if (snapshot.hasChild("isGoogle")) {
			String theDevice = snapshot.getRef().getParent().getParent().getKey(); //we are getting the device name to be passed to the DeviceValueEventListener
			logger.info("onChildChanged parent="+theDevice);
			logger.info("onChildChanged has isGoogle property");
			Boolean isGoogle = new Boolean(snapshot.child("isGoogle").getValue(String.class));
			logger.info("onChildChanged has isGoogle property value="+snapshot.child("isGoogle").getValue(String.class));
			if (isGoogle) {
				//The transaction is from Google.  Which means we need to update BM.
//				Device ds[] = devices.getAllDevices();
//				for (int i=0; i < ds.length; i++) {
//					logger.info("devices name="+ds[i].getName()+" id="+ds[i].getSSID()+" room="+ds[i].getRoom().getName());
//				}
				DatabaseReference parentRef = snapshot.getRef().getParent().getParent();
				logger.info("parentRef.getKey()="+parentRef.getKey());
				// Attach a listener to read the data of the device
				String propertyId = snapshot.child("id").getValue(String.class);
				String propertyName = snapshot.child("name").getValue(String.class);
				String properyValue = snapshot.child("value").getValue(String.class);
				logger.info(path +" property "+snapshot.getKey()+" changed to " + properyValue);
				DeviceValueEventListener deviceEvtListener = new DeviceValueEventListener(device, propertyId, propertyName, properyValue);
				parentRef.child("id").addValueEventListener(deviceEvtListener);//we are adding an event listener to the device id, this will query the id of the device.  Then we trigger the BM update on that listener	
			} else {
				//we do nothing here, the transaction is from BM.  Which means we need not update BM.
				logger.info("onChildChanged do nothing, the transaction is from BM.  Which means we need not update BM.");				
			}

		} else {
			//this will only be triggered when a new device registers.
			//we do not need to do anything
			logger.info("onChildChanged isGoogle property not found, this is a Device Registration event.  Do nothing");
		}
		if (snapshot.hasChild("name"))
			logger.info("onChildChanged has name property");
		
//		logger.info("onChildChanged fbDevice.name="+fbDevice.name+" fbDevice.id="+deviceEvtListener.getDeviceID());
//		Device d = devices.getDevice(fbDevice.id);
//		AbstProperty props[] = d.getProperties();
//		for (int i=0; i < props.length; i++) {
//			logger.info("before props "+props[i].getDisplayName()+"="+props[i].getValue());
//		}
//		AbstProperty property = d.getProperty(snapshot.child("id").getValue(String.class)); 
//		logger.info("onChildChanged done  devices MAC="+d.getMAC()+ " property="+property.getDisplayName());
//		AbstProperty p = d.getProperty(snapshot.child("id").getValue(String.class));		
//		p.setValue(snapshot.child("value").getValue(String.class));		
//		for (int i=0; i < props.length; i++) {
//			logger.info("after props "+props[i].getDisplayName()+"="+props[i].getValue());
//		}		
//		//need to test below for the sending to BM		
//		try {
//			logger.info("updating the bm property");
//			p.update("ChildChangedEventListener", false);
//		} catch (AdaptorException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
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
	
//	private void publishToMqtt(String CID, String prop, String command) {
//		String msg = "{RID:'OH-POOP', CID:'$cid',RTY:'poop',property:'$prop',value:'$value'}";
//        msg = msg.replace("$cid", CID.toUpperCase());
//        msg = msg.replace("$prop", prop.toUpperCase());
//        String value = command;
//        if (command.toUpperCase().equals("ON")){
//        	value = "1";
//        } else if (command.toUpperCase().equals("OFF")){
//        	value = "0";
//        }
//        msg = msg.replace("$value", value);
//        logger.info("Will publish " + msg);
//        if (mqttClient != null) {
//        	logger.info("04 Sending data to MQTT Broker");
//        	System.out.println("04 Sending data to MQTT Broker");
//        	try {            		
//        		MqttMessage message = new MqttMessage(msg.getBytes());
//                message.setQos(qos);
//                mqttClient.publish(mqttTopic, message);
//                logger.info("05 Message published");
//                System.out.println("05 Message published");
//            } catch(MqttException me) {
//            	logger.error("reason "+me.getReasonCode());
//            	logger.error("msg "+me.getMessage());
//            	logger.error("loc "+me.getLocalizedMessage());
//            	logger.error("cause "+me.getCause());
//            	logger.error("excep "+me);
//                me.printStackTrace();
////            	if (me.getMessage().equals("Client is not connected")) {
////            		logger.info("reconnecting to mqtt");
////            		connectToMqtt();                		
////            	}                	
//            }
//        } else {
//        	logger.info("03 cannot send data to MQTT Broker");
//        	System.out.println("03 cannot send data to MQTT Broker");
//        }
//        logger.info("06 done");
//        System.out.println("06 done");
//	}

}
