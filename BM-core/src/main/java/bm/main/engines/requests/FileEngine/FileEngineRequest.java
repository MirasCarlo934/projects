package bm.main.engines.requests.FileEngine;

import bm.main.engines.FileEngine;
import bm.main.engines.requests.EngineRequest;

public abstract class FileEngineRequest extends EngineRequest {
	private FileEngineRequestType type;
	
	/**
	 * Creates an engine request for the FileEngine
	 * 
	 * @param id the ID of this EngineRequest
	 * @param requestType this engine request's type
	 */
	public FileEngineRequest(String id, FileEngineRequestType requestType) {
		super(id, FileEngine.class.toString());
		type = requestType;
	}

	public FileEngineRequestType getType() {
		return type;
	}
}
