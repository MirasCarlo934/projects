package components.products.factories;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

import org.apache.log4j.Logger;

import components.products.AbstProduct;
import components.properties.AbstProperty;
import main.adaptors.DBAdaptor;
import main.adaptors.OHAdaptor;

public abstract class AbstProductFactory {
	private Logger LOG;
	protected DBAdaptor dba;
	protected OHAdaptor oha;

	public AbstProductFactory(String logDomain, String factoryName, DBAdaptor dba, OHAdaptor oha) {
		LOG = Logger.getLogger(logDomain + "." + factoryName);
		this.dba = dba;
		this.oha = oha;
	}
	
	public AbstProduct createProduct(ResultSet rs) throws SQLException, IllegalArgumentException {
		rs.beforeFirst();
		String SSID = null;
		String name = null;
		String description = null;
		String OH_icon = null;
		while(rs.next()) {
			SSID = rs.getString("prod_ssid");
			name = rs.getString("prod_name");
			description = rs.getString("prod_desc");
			OH_icon = rs.getString("oh_icon");
		}
		rs.beforeFirst();
		return createProductObject(SSID, name, description, OH_icon, retrieveProductProperties(rs));
	}
	
	protected abstract AbstProduct createProductObject(String SSID, String name, String description, 
			String OH_icon, HashMap<String, AbstProperty> properties);
	
	protected abstract HashMap<String, AbstProperty> retrieveProductProperties(ResultSet rs) 
			throws SQLException, IllegalArgumentException;
}
