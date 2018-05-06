package components.factories;

import components.CommonComponent;
import components.products.AbstProduct;
import components.products.CommonProduct;
import main.adaptors.DBAdaptor;
import main.adaptors.OHAdaptor;

public class CommonComponentFactory extends AbstComponentFactory {

	public CommonComponentFactory(String logDomain, DBAdaptor dba, OHAdaptor  oha) {
		super(logDomain, CommonComponentFactory.class.getSimpleName(), dba, oha);
	}

	@Override
	public CommonComponent createComponent(String SSID, String MAC, String name, String topic, 
			String room, boolean active, AbstProduct product) {
		return (CommonComponent) super.createComponent(SSID, MAC, name, topic, room, active, product);
	}
	
	@Override
	public CommonComponent createComponentObject(String SSID, String MAC, String name, String topic, 
			String room, boolean active, AbstProduct product) {
		CommonComponent c = new CommonComponent(SSID, MAC, name, topic, room, active, 
				(CommonProduct)product, dba, oha);
		return c;
	}
}
