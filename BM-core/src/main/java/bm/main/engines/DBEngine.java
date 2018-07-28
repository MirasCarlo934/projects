package bm.main.engines;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLNonTransientConnectionException;
import java.sql.SQLTimeoutException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;

import bm.jeep.ResError;
import bm.main.engines.exceptions.EngineException;
import bm.main.engines.requests.EngineRequest;
import bm.main.engines.requests.DBEngine.DBEngineRequest;
import bm.main.engines.requests.DBEngine.QueryType;
import bm.main.engines.requests.DBEngine.RawDBEReq;
import bm.tools.SystemTimer;

public class DBEngine extends AbstEngine {
	private String dbURL;
    private Connection conn;
    private String dbusr;
    private String dbpwd;
    private DBEngineConnectionReconnector reconnector = new DBEngineConnectionReconnector();
    private int reconnectPeriod;
    private boolean connected = false;
    
    /**
     * Primitive instantiation of DBEngine. Automatically connects itself to DB.
     * @param name
     * @param dbURL
     * @param dbusr
     * @param dbpwd
     */
    public DBEngine(String name, String logDomain, String errorLogDomain, String dbURL, String dbusr, String dbpwd, 
    		SystemTimer sysTimer, int reconnectPeriod) {
    	super(logDomain, errorLogDomain, name, DBEngine.class.toString());
    	this.dbURL = dbURL;
    	this.dbusr = dbusr;
    	this.dbpwd = dbpwd;
    	LOG.info("DBEngine started on url: " + dbURL);
		createConnection(dbURL, dbusr, dbpwd);
		sysTimer.schedule(reconnector, 0, reconnectPeriod);
    }
    
    public void createConnection(String dbURL, String dbusr, String dbpwd) {
    	this.dbURL = dbURL;
    	this.dbusr = dbusr;
    	this.dbpwd = dbpwd;
    	try {
			conn = (DriverManager.getConnection(dbURL, dbusr, dbpwd));
			LOG.info("Connected to Derby DB!");
			connected = true;
		} catch (SQLException e) {
			LOG.fatal("Cannot connect to DB! Wait for reconnection...");
			connected = false;
		}
    }
    
    public void closeConnection() throws SQLException {
    	conn.close();
    	LOG.info("Disconnected from Deby DB!");
    }
    

	@Override
	protected Object processRequest(EngineRequest er) {
		DBEngineRequest dber = (DBEngineRequest) er;
		try {
			Object o = executeQuery(dber.getQuery());
			return o;
		} catch(SQLNonTransientConnectionException e) {
			connected = false;
			return (new EngineException(this, "Connection lost with DB! Cannot execute query! Wait for "
					+ "reconnection..."));
		} catch (SQLException e) {
			return (new EngineException(this, "Cannot execute query [" + dber.getQuery() + "]! Check causes for "
					+ "more details.", e));
		} catch(NullPointerException e) {
    			return new EngineException(this, "Connection not yet established!");
    		}
	}
    
    private Object executeQuery(String query) throws SQLException, NullPointerException {
	    	Statement stmt = null;
	    	stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, 
					ResultSet.CONCUR_READ_ONLY);
	    	LOG.trace("Executing " + query + " ...");
	    	stmt.execute(query);
	    	LOG.trace("Query executed successfully!");
	    	if(stmt.getResultSet() == null) { //usually the case for non-select queries
	    		return true;
	    	} else {
	    		return stmt.getResultSet();
	    	}
    }

	public String getDbURL() {
		return dbURL;
	}
	
	private class DBEngineConnectionReconnector extends TimerTask {
		@Override
		public void run() {
			if(!connected) {
				LOG.info("Reconnecting to DB...");
				createConnection(dbURL, dbusr, dbpwd);
			}
		}
	}
}
