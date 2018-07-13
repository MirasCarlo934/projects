package symphony.firebase.adaptor;

import java.util.HashMap;

import org.apache.log4j.Logger;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONObject;

import com.google.firebase.database.DatabaseReference;

import symphony.firebase.vo.FirebaseDevice;
import symphony.firebase.vo.FirebaseProperty;


/**
 * This is deprecated, we are implementing the AbstAdaptor instead to receive data from BM
 * @author miras
 *
 */
public class SymphonyMqttCB implements MqttCallback {
	//private Logger logger = LoggerFactory.getLogger(SymphonyMqttCB.class);
	private Logger logger = Logger.getLogger(SymphonyMqttCB.class);
	private DatabaseReference ref;
	
	public void setRef(DatabaseReference ref) {
		this.ref = ref;
	}

	@Override
	public void connectionLost(Throwable throwable) {
		logger.info("Connection Lost!");
	}

	@Override
	public void deliveryComplete(IMqttDeliveryToken token) {
		// TODO Auto-generated method stub
		logger.info("delivery complete." );
	}

	@Override
	public void messageArrived(String topic, MqttMessage msg) throws Exception {
		// TODO Auto-generated method stub
//		logger.info("message arrived topic=" + topic + " Message=" + msg.getPayload());
		String data = new String(msg.getPayload());
		System.out.println("message arrived topic=" + topic + " Message=" + data);
		logger.info("message arrived topic=" + topic + " Message=" + data);
		JSONObject json = new JSONObject(msg.toString());
		System.out.println("RTY="+json.getString("RTY"));
		logger.info("RTY="+json.getString("RTY"));
		if (json.getString("RTY").equals("register")) {
			registerToFirebaseDB(json);
		} else if (json.getString("RTY").equals("poop")) {
			updateFirebaseDB(json);
		}
	}
	
	/**
	 * Method to register the device to firebase DB
	 * If device already exists, just update 
	 */
	private void registerToFirebaseDB(JSONObject json) {
		FirebaseDevice device = new FirebaseDevice(json.getString("CID"), json.getString("name"), null);
		DatabaseReference usersRef = ref.child(device.id);
		//let us compare the
		System.out.println("start register to db key="+usersRef.getKey());
		logger.info("start register to db key="+usersRef.getKey());
		usersRef.setValueAsync(device);
		System.out.println("done register to db");
		logger.info("done register to db");
	}
	
	private void updateFirebaseDB(JSONObject json) {
		DatabaseReference usersRef = ref.child(json.getString("CID")+"/properties");
		HashMap props = new HashMap();
		props.put("1234", new FirebaseProperty("id1", 7));
		props.put("5467", new FirebaseProperty("id2", 8));
		FirebaseDevice device = new FirebaseDevice("PIR", "PIRName", props);
		usersRef.updateChildrenAsync(props);
		System.out.println("done update db");
		logger.info("done update db");
	}
}
