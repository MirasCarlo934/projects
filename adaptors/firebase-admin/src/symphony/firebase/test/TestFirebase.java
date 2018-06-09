package symphony.firebase.test;
import bm.context.adaptors.exceptions.AdaptorException;
import bm.context.devices.Device;
import bm.context.properties.Property;
import bm.context.properties.PropertyMode;
import bm.context.properties.PropertyType;
import bm.context.properties.bindings.Binding;
import bm.context.rooms.Room;
import bm.main.interfaces.Initializable;
import bm.main.repositories.DeviceRepository;
import symphony.firebase.adaptor.Firebase;


public class TestFirebase implements Initializable{

	public TestFirebase() {
		// TODO Auto-generated constructor stub
	}
	public static void main(String[] args) {
		DeviceRepository devices = null;
		Firebase firebase = new Firebase("E:/esp8266workspace/firebase-online/symphony-dcc4c.json", "/symphony/bahay");
		//instantiate value event listener to the /symphony database path
		firebase.connect();
		try {
			Thread.sleep(20000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		PropertyType propType = new PropertyType("ssid", "name", "description", "OHItem", 1, 100);
		PropertyMode pmode = PropertyMode.IO;
		bm.context.adaptors.DBAdaptor dba = null;
		bm.context.adaptors.OHAdaptor oha = null;
		bm.context.adaptors.AbstAdaptor[] additionalAdaptors = null;
		Binding b = null;
		Property prop = new Property(propType, "propSSID", "dispname", pmode, dba, oha, additionalAdaptors, b);
		Property props[] = new Property[1];
		props[0] = prop;
		bm.context.devices.products.Product p = new bm.context.devices.products.Product("mainLogDomain", "pSSID", 
				"pname", "pdescription", "OH_icon", props, dba, oha, additionalAdaptors);
		try {
			Room room = new Room("Room1", "dining", dba, oha, additionalAdaptors, 0);
			Device bmDevice = new Device("testSSID", "MAC", "testName", "BM", room, 
				true, p, dba, oha, additionalAdaptors, 1); 
			firebase.deviceStateUpdated(bmDevice, true);
		} catch(AdaptorException ae) {
			
		};
		while (true) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	@Override
	public void initialize() throws Exception {
		// TODO Auto-generated method stub
		DeviceRepository devices = null;
		Firebase firebase = new Firebase("E:/esp8266workspace/firebase-online/symphony-dcc4c.json", "/symphony/bahay");
		firebase.connect();
	}
}
