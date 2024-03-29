package bm.main.engines.requests;

import bm.main.engines.AbstEngine;

public abstract class EngineRequest {
	private String id; //the ID of the EngineRequest
	private String engineType; //the Engine class string that can process this EngineRequest
	private Thread requestingThread;
	private boolean waitForResponse;
	protected Object response = null; // the response of the Engine
	
	/**
	 * 
	 * @param id The unique ID of this EngineRequest, given upon instantiation of the EngineRequest
	 * @param engineClass The Class of the Engine in Sring format
	 */
	public EngineRequest(String id, String engineClass) {
		this.id = id;
		this.engineType = engineClass;
	}

	/**
	 * Returns the Engine class in String format that can process this EngineRequest.
	 * @return the Engine class String
	 */
	public String getEngineType() {
		return engineType;
	}

	/**
	 * Returns the response of the Engine after processing
	 * 
	 * @return Engine response in Object form. (Use casting)
	 */
	public Object getResponse() {
		return response;
	}
	
	/**
	 * Sets the response of the Engine after processing. 
	 * <b>Only the appropriate Engine must use this method</b>
	 * 
	 * @param response The response set by the Engine
	 */
	public void setResponse(Object response) {
		this.response = response;
	}

	/**
	 * Returns the ID of this EngineRequest
	 * 
	 * @return EngineRequest ID
	 */
	public String getSSID() {
		return id;
	}

	/**
	 * @return the requestingThread
	 */
	public Thread getRequestingThread() {
		return requestingThread;
	}

	/**
	 * @param requestingThread the requestingThread to set
	 */
	public void setRequestingThread(Thread requestingThread) {
		this.requestingThread = requestingThread;
	}

	/**
	 * @return the waitForResponse
	 */
	public boolean waitForResponse() {
		return waitForResponse;
	}

	/**
	 * @param waitForResponse the waitForResponse to set
	 */
	public void setWaitForResponse(boolean waitForResponse) {
		this.waitForResponse = waitForResponse;
	}
}
