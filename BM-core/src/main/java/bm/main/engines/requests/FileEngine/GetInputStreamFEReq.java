package bm.main.engines.requests.FileEngine;

/**
 * A FileEngine request that instructs the FileEngine to return an InputStream of the file it is handling.
 * 
 * @author carlomiras
 *
 */
public class GetInputStreamFEReq extends FileEngineRequest {

	/**
	 * Creates a FileEngine request that is used to instruct the FileEngine to return an InputStream of the file it
	 * is handling.
	 * <br><br><br>
	 * <b>Returned response:</b> InputStream object
	 * 
	 * @param id the ID of this engine request
	 */
	public GetInputStreamFEReq(String id) {
		super(id, FileEngineRequestType.getInputStream);
	}
}
