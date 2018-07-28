package bm.jeep;

public class ResGetRooms extends AbstResponse {

	public ResGetRooms(AbstRequest request, boolean success, String id, String name) {
		super(request, success);
		addParameter("id", id);
		addParameter("name", name);
	}
}
