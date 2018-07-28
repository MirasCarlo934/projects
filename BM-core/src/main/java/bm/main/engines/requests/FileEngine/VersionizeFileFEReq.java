package bm.main.engines.requests.FileEngine;

public class VersionizeFileFEReq extends FileEngineRequest {

	public VersionizeFileFEReq(String id) {
		super(id, FileEngineRequestType.versionize);
	}
}
