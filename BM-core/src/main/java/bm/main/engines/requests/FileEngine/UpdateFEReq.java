package bm.main.engines.requests.FileEngine;

/**
 * A FileEngine request that instructs the FileEngine to read again the file it is handling.
 * 
 * @author carlomiras
 *
 */
public class UpdateFEReq extends FileEngineRequest {

	/**
	 * Creates a FileEngine request that instructs the FileEngine to read again the file it is handling. This request is 
	 * often sent when the file was edited outside the BM.
	 * 
	 * @param id the ID of this EngineRequest
	 */
	public UpdateFEReq(String id) {
		super(id, FileEngineRequestType.update);
	}
}