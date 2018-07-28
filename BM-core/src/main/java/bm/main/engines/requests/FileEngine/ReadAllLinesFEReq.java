package bm.main.engines.requests.FileEngine;

/**
 * A FileEngine request that reads all the lines in the file that the FileEngine handles.
 * <br><br>
 * <b>Returns a String[] array</b>
 * @param SSID The ID for this engine request
 */
public class ReadAllLinesFEReq extends FileEngineRequest {

	/**
	 * Creates a request for the FileEngine that reads all the lines in the file that the FileEngine handles.
	 * <br><br>
	 * <b>Returns a String[] array</b>
	 * @param id The ID for this engine request
	 */
	public ReadAllLinesFEReq(String id) {
		super(id, FileEngineRequestType.readAllLines);
	}

}
