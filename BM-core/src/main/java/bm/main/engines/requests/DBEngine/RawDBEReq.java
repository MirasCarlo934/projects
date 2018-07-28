package bm.main.engines.requests.DBEngine;

public class RawDBEReq extends DBEngineRequest {

	/**
	 * Creates a DBEngineRequest which is used by the DBEngine to create an SQL statement to the DB.
	 * 
	 * @param id the ID of this EngineRequest
	 * @param query the SQL statement that will be forwarded to the DB
	 */
	public RawDBEReq(String id, String query) {
		super(id, QueryType.RAW, null);
		setQuery(query);
	}
}
