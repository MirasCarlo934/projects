package bm.main.engines.requests.FileEngine;

/**
 * A FileEngine request that instructs the FileEngine to clear the contents of the file it is handling.
 * 
 * @author carlomiras
 *
 */
public class ClearFileFEReq extends FileEngineRequest {

	/**
	 * Creates a FileEngine request that instructs the FileEngine to clear the contents of the file it is handling.
	 * 
	 * @param id the ID of this EngineRequest
	 */
	public ClearFileFEReq(String id) {
		super(id, FileEngineRequestType.clear);
	}
}
