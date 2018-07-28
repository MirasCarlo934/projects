package main.engines.requests.OHEngine;

import components.AbstComponent;

public class RemoveItemOHEReq extends OHEngineRequest {
	private AbstComponent c;
	
	/**
	 * The EngineRequest used to remove a component from OH using REST API.
	 * 
	 * @param id The generated ID of this EngineRequest
	 * @param c The component to be removed
	 */
	public RemoveItemOHEReq(String id, AbstComponent component) {
		super(id, OHRequestType.removeItem);
		c = component;
	}
	
	/**
	 * Returns the component to be removed
	 * 
	 * @return The component to be removed
	 */
	public AbstComponent getComponent() {
		return c;
	}
}
