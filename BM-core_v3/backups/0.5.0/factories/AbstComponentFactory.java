package components.factories;

import org.apache.log4j.Logger;

import components.AbstComponent;
import components.products.AbstProduct;
import main.adaptors.DBAdaptor;
import main.adaptors.OHAdaptor;

public abstract class AbstComponentFactory {
	protected Logger LOG;
	protected DBAdaptor dba;
	protected OHAdaptor oha;
	
	public AbstComponentFactory(String logDomain, String factoryName, DBAdaptor dba, 
			OHAdaptor oha) {
		LOG = Logger.getLogger(logDomain + "." + factoryName);
		this.dba = dba;
		this.oha = oha;
	}

	public AbstComponent createComponent(String SSID, String MAC, String name, String topic, 
			String room, boolean active, AbstProduct product) {
		AbstComponent c = createComponentObject(SSID, MAC, name, topic, room, active, product);
		for(int i = 0; i < c.getProperties().length; i++) {
			c.getProperties()[i].setComID(c.getSSID());
		}
		return c;
	}
	
	protected abstract AbstComponent createComponentObject(String SSID, String MAC, String name, String topic, 
			String room, boolean active, AbstProduct product);
}
