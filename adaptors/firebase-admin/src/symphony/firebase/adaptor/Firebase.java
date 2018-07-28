package symphony.firebase.adaptor;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;

import org.apache.log4j.Logger;
//import org.eclipse.paho.client.mqttv3.MqttClient;
//import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
//import org.eclipse.paho.client.mqttv3.MqttException;
//import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

<<<<<<< Upstream, based on branch 'master' of https://github.com/MirasCarlo934/projects
import bm.comms.mqtt.MQTTPublisher;
=======
>>>>>>> b0e7896 commit1
import bm.context.adaptors.AbstAdaptor;
import bm.context.adaptors.exceptions.AdaptorException;
import bm.context.devices.Device;
import bm.context.rooms.Room;
import bm.main.interfaces.Initializable;
import symphony.firebase.vo.FirebaseDevice;

/*
 * Notes
 * 	TODO
 * 		1. need to populate the local BM db with the firebase db specific to entity
 * 		2. need the entity to be configurable
 */
public class Firebase extends AbstAdaptor implements Initializable {
//	private Logger logger = LoggerFactory.getLogger(Firebase.class);
	private Logger logger = Logger.getLogger(Firebase.class);
	HashMap symphonyMap = new HashMap();
    String broker       = "tcp://192.168.0.105:1883";
    String clientId     = "SymphonyFirebase";
	String mqttTopic        = "BM";
	DatabaseReference ref;
	String bahayInstance;
//	private DeviceRepository devices;
	private Device device;
	

//	public DeviceRepository getDevices() {
//		return devices;
//	}
//
//	public void setDevices(DeviceRepository devices) {
//		this.devices = devices;
//	}

	/*
	 * Constructor
	 * initiates connection to the firebase database
	 */
	public Firebase(String accountJson, String bahayInstance) {
		super("firebase", "adaptor", "firebase");
		
//		this.devices = devices;
		this.bahayInstance = bahayInstance;
		FileInputStream serviceAccount;
		try {
			logger.info("Constructor start");
			serviceAccount = new FileInputStream(accountJson);
			FirebaseOptions options;
			options = new FirebaseOptions.Builder()
			    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
			    .setDatabaseUrl("https://symphony-dcc4c.firebaseio.com/")
			    .build();
			FirebaseApp.initializeApp(options);
			logger.info("Constructor end");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/*
	 * Connects to the specific database path
	 */
	public void connect() {
		//instantiate value event listener to the database path
		logger.info("Connecting to " + bahayInstance);
		ref = FirebaseDatabase
			    .getInstance()
			    .getReference(bahayInstance);
//		FirebaseValueEventListener myValueEventListener = new FirebaseValueEventListener(ref);
//		ref.addListenerForSingleValueEvent(myValueEventListener);
//		connectToMqtt();
//		ChildAddedListener addChildEventListener = new ChildAddedListener(mqttPublisher, bahayInstance, ref,devices);
		ChildAddedListener addChildEventListener = new ChildAddedListener(bahayInstance, ref,device);
		ref.addChildEventListener(addChildEventListener);
		logger.info("Connection to " + bahayInstance +" successful.  Listener added.");
	}

//	/**
//	 * This is deprecated, we are implementing the AbstAdaptor instead to receive data from BM
//	 */
//	private void connectToMqtt() {
//    	try {
//    		mqttClient = new MqttClient(broker, clientId, persistence);
//    		SymphonyMqttCB callback = new SymphonyMqttCB();
//    		callback.setRef(ref);
//    		mqttClient.setCallback(callback);
//            MqttConnectOptions connOpts = new MqttConnectOptions();
//            connOpts.setCleanSession(true);
//            connOpts.setAutomaticReconnect(true);
//            logger.info("Connecting to broker: "+broker);
//            mqttClient.connect(connOpts);
//            logger.info("Mqtt Connected");
//            mqttClient.subscribe(mqttTopic);
////            logger.info("Publishing message: "+content);
////            MqttMessage message = new MqttMessage(content.getBytes());
////            message.setQos(qos);
////            mqttClient.publish(mqttTopic, message);
////            logger.info("Message published");
//        } catch(MqttException me) {
//        	logger.error("reason "+me.getReasonCode());
//        	logger.error("msg "+me.getMessage());
//        	logger.error("loc "+me.getLocalizedMessage());
//        	logger.error("cause "+me.getCause());
//        	logger.error("excep "+me);
//            me.printStackTrace();
//            logger.info("Mqtt Not Connected");
//        }
//    }

	@Override
	public void initialize() throws Exception {
		connect();
	}

	@Override
	public void deviceDeleted(Device arg0, boolean arg1) throws AdaptorException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deviceRoomUpdated(Device arg0, boolean arg1) throws AdaptorException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deviceStateUpdated(Device bmDevice, boolean arg1) throws AdaptorException {
		logger.info("deviceStateUpdated called.");
		updateFirebaseDB(bmDevice);
		logger.info("deviceStateUpdated end.");
	}


	@Override
	public void propertyCreated(bm.context.properties.Property arg0, boolean arg1) throws AdaptorException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void propertyDeleted(bm.context.properties.Property arg0, boolean arg1) throws AdaptorException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void propertyValueUpdated(bm.context.properties.Property property, boolean arg1) throws AdaptorException {
		logger.info("propertyValueUpdated called.");
		Device bmDevice = property.getDevice();
		logger.info("Room:" +bmDevice.getParentRoom().getName()+ " Device id=" + bmDevice.getSSID() 
			+ " name=" +bmDevice.getName() + " property=" + property.getDisplayName()+ " will be set to " + property.getValue());
		FirebaseDevice firebaseDevice = new FirebaseDevice(bmDevice);
		DatabaseReference propertyRef = ref.child(bmDevice.getParentRoom().getName()).child(bmDevice.getName())
				.child("properties/"+property.getDisplayName());
		HashMap propUpdates = new HashMap();
		propUpdates.put("value", property.getValue());
		propUpdates.put("isGoogle", "false");
		propertyRef.updateChildrenAsync(propUpdates);
		logger.info("done update of property in firebase");	
	}

	@Override
	public void roomCreated(Room arg0, boolean arg1) throws AdaptorException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void roomDeleted(Room arg0, boolean arg1) throws AdaptorException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void roomParentUpdated(Room arg0, boolean arg1) throws AdaptorException {
		// TODO Auto-generated method stub
		
	}
	
	/*
	 * Method to update the firebase db
	 */
	private void updateFirebaseDB(Device bmDevice) {
		logger.info("Room:" +bmDevice.getParentRoom().getName()+ " Device id=" + bmDevice.getSSID() + " name=" +bmDevice.getName());	
		FirebaseDevice firebaseDevice = new FirebaseDevice(bmDevice);
		DatabaseReference deviceRef = ref.child(bmDevice.getParentRoom().getName());
		HashMap newDeviceInRoom = new HashMap();
		newDeviceInRoom.put(bmDevice.getName(), firebaseDevice);
//		deviceRef.setValueAsync(newDeviceInRoom);
		deviceRef.updateChildrenAsync(newDeviceInRoom);
		//need to addChildEventListener to the properties of the device
		logger.info("done create device in firebase");
	}

	@Override
	public void deviceCreated(Device bmDevice, boolean arg1) throws AdaptorException {
		// TODO Auto-generated method stub
		logger.info("deviceCreated called.");
		updateFirebaseDB(bmDevice);
		logger.info("deviceCreated end.");	
	}

	@Override
	public void deviceCredentialsUpdated(Device arg0, boolean arg1) throws AdaptorException {
		logger.info("deviceCredentialsUpdated called.");
		updateFirebaseDB(arg0);
		logger.info("deviceCredentialsUpdated end.");
	}

	@Override
	public void roomCredentialsUpdated(Room arg0, boolean arg1) throws AdaptorException {
		// TODO Auto-generated method stub
		
	}
}
