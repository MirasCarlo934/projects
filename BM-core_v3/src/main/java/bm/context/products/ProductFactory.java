package bm.context.products;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

import bm.context.properties.Property;
import bm.context.properties.PropertyMode;
import bm.jeep.JEEPManager;
import org.apache.log4j.Logger;

import bm.context.properties.PropertyType;
import bm.tools.IDGenerator;

public class ProductFactory {
	protected Logger LOG;
	protected String logDomain;
	protected static HashMap<String, PropertyType> propertyTypes = new HashMap<String, PropertyType>(6);
	private JEEPManager jm;
	
	private String poopRTY;
	protected String propIDParam;
	protected String propValParam;
	private IDGenerator idg;

	public ProductFactory(String logDomain, JEEPManager jeepManager, String poopRTY, String propIDParam,
			String propValParam, IDGenerator idGenerator) {
		this.LOG = Logger.getLogger(logDomain + "." + ProductFactory.class.getSimpleName());
		this.logDomain = logDomain;
		this.jm = jeepManager;
		this.poopRTY = poopRTY;
		this.propIDParam = propIDParam;
		this.propValParam = propValParam;
		this.idg = idGenerator;
	}

    /**
     * Creates a new Product object.
     *
     * @param SSID The product ID
     * @param name The product name
     * @param description The product description
     * @param iconImg The product icon image (openhab icon image name)
     * @param productsRS The
     * @return
     */
	public Product createProductObject(String SSID, String name, String description, String iconImg,
                                       ResultSet productsRS) {
	    Product prod = new Product(logDomain, SSID, name, description, iconImg, propertyTypes,
                poopRTY, propIDParam, propValParam, jm, idg);
        try {
            productsRS.beforeFirst();
            while(productsRS.next()) {
                String prod_ssid =  productsRS.getString("prod_ssid");
                try{
                    if(prod_ssid.equals(SSID)) {
                        PropertyType prop_type = propertyTypes.get(productsRS.getString("prop_type"));
                        String prop_dispname = productsRS.getString("prop_dispname");
                        PropertyMode prop_mode = PropertyMode.parseModeFromString(productsRS.getString("prop_mode"));
                        String prop_ssid = productsRS.getString("prop_index");
                        Property prop = new Property(prop_type, prop_ssid, prop_dispname, prop_mode, poopRTY, propIDParam,
                                propValParam, jm, idg);
                        LOG.debug("Adding property " + prop.getSystemName() + " to product " + prod_ssid + "!");
                        prod.addProperty(prop);
                    }
                } catch(NullPointerException e) {
                    throw new IllegalArgumentException("Product not yet initialized!", e);
                }
            }
        } catch (SQLException e) {
            LOG.error("Could not retrieve properties for product " + prod.getSSID() + "!");
        }

        return prod;
    }

	public void setPropertyTypes(HashMap<String, PropertyType> propertyTypes) {
		ProductFactory.propertyTypes = propertyTypes;
	}
}
