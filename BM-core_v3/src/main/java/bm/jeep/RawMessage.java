package bm.jeep;

import bm.comms.Sender;

public class RawMessage {
	private String requestStr;
	private Sender sender;

	/**
	 * Creates a <i>RawRequest</i> object which contains the raw request string intercepted by a <i>Listener</i>.
	 * This is considered as "raw" since it is not yet considered as a valid JEEP request.
	 * @param requestStr The raw request string
	 * @param sender The <i>Sender</i> object paired with the <i>Listener</i> that intercepted the raw request
	 */
	public RawMessage(String requestStr, Sender sender) {
		this.requestStr = requestStr;
		this.sender = sender;
	}

	/**
	 * Returns the request string of this <i>RawRequest</i>.
	 * @return The request string
	 */
	public String getMessageStr() {
		return requestStr;
	}
	
	/**
	 * Returns the <i>Sender</i> object paired with the <i>Listener</i> that intercepted this <i>RawRequest</i>.
	 * @return The <i>Sender</i> object
	 */
	public Sender getSender() {
		return sender;
	}
}
