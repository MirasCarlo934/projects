package bm.jeep.device;

import bm.comms.Protocol;
import bm.comms.Sender;
import bm.jeep.JEEPRequest;
import bm.jeep.JEEPResponse;

public class ResRegister extends JEEPResponse {
	public String id; //ssid of the newly registered component
	public String topic; //topic of the newly registered component

	public ResRegister(JEEPRequest request, String id, String topic) {
		super(request, true);
		this.id = id;
		this.topic = topic;
		addParameter("id", id);
		addParameter("topic", topic);
	}
	
	public ResRegister(String MAC, String CID, String RTY, Protocol protocol, String id, String topic) {
		super(MAC, CID, RTY, protocol, true);
		this.id = id;
		this.topic = topic;
		addParameter("id", id);
		addParameter("topic", topic);
	}
}
