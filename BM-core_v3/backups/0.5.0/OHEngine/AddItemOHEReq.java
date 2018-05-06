package main.engines.requests.OHEngine;

import components.AbstComponent;

public class AddItemOHEReq extends OHEngineRequest {
	AbstComponent c;

	public AddItemOHEReq(String id, AbstComponent component) {
		super(id, OHRequestType.addItem);
		c = component;
	}
	
	public AbstComponent getComponent() {
		return c;
	}
}
