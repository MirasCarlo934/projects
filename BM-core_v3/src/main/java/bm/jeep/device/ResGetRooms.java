package bm.jeep.device;

import bm.jeep.JEEPRequest;
import bm.jeep.JEEPResponse;

public class ResGetRooms extends JEEPResponse {

	public ResGetRooms(JEEPRequest request, boolean success, String id, String name) {
		super(request, success);
		addParameter("id", id);
		addParameter("name", name);
	}
}
