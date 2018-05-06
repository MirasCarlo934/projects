package bm.context.adaptors.exceptions;

public class AdaptorException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6321948809839903096L;

	public AdaptorException(String message) {
		super(message);
	}

	public AdaptorException(Throwable cause) {
		super(cause);
	}

	public AdaptorException(String message, Throwable cause) {
		super(message, cause);
	}
}
