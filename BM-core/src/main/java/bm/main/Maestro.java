package bm.main;

import org.apache.log4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import org.springframework.web.servlet.view.JstlView;

import bm.main.engines.AbstEngine;
import bm.main.engines.FileEngine;
import bm.main.engines.exceptions.EngineException;
import bm.main.engines.requests.FileEngine.ClearFileFEReq;
import bm.main.engines.requests.FileEngine.OverwriteFileFEReq;
import bm.main.repositories.AbstRepository;
import bm.main.repositories.CIRRepository;
import bm.main.repositories.DeviceRepository;
import bm.main.repositories.ProductRepository;
import bm.main.repositories.RoomRepository;
import bm.mqtt.MQTTListener;
import bm.mqtt.MQTTPublisher;
import bm.smarthome.adaptors.OHAdaptor;
import bm.smarthome.devices.Device;
import bm.smarthome.properties.Property;
import bm.tools.*;

@SpringBootApplication
@ComponentScan({"bm.ui"})
@PropertySource({"file:configuration/bm.properties", "file:configuration/user.properties"})
@ImportResource({
	"file:configuration/spring/main-config.xml", "file:configuration/spring/devices-config.xml", 
	"file:configuration/spring/adaptors-config.xml", "file:configuration/spring/engines-config.xml", 
	"file:configuration/spring/mqtt-config.xml", "file:configuration/spring/modules-config.xml", 
	"file:configuration/spring/tools-config.xml", "file:configuration/spring/core-config.xml",
	"file:configuration/spring/product-config.xml"
	/*, "file:configuration/spring/app-config.xml"*/
})
//FIXME Find a way to integrate plex
public class Maestro {
	private static final int build = 1;
	private static final Logger LOG = Logger.getLogger("MAIN.BusinessMachine");
	private Initializables inits;
	private MQTTPublisher mp;
	private DeviceRepository cr;
	private RoomRepository rr;
	private ProductRepository pr;
	private CIRRepository cirr;
	private FileEngine ohSitemapFE;
	private OHAdaptor oha;

	public static void main(String[] args) {
		//initialization phase
		Maestro bm = new Maestro();
		try {
			bm.start(args);
		} catch (Exception e) {
			errorStartup(e, 000);
		}
	}
	
	/**
	 * Can be called in order to signify a fatal error during startup which can lead to runtime errors and 
	 * possible termination of the VM's functionality. This method terminates the VM.
	 * 
	 * @param e The exception that causes the fatal error
	 * @param status The error status of the VM
	 */
	public static void errorStartup(Exception e, int status) {
		LOG.fatal("Fatal error occurred! Maestro shutting down...", e);
		System.exit(status);
	}
	
	 /**
     * This is the method used to setup the Spring environment and object dependencies.
     * It reads the other config files and loads their context.
     * 
     * @throws Exception
     */
    public void start(String[] args) {
    		LOG.info("Starting BusinessMaestro... ");
    		
    		ApplicationContext context = SpringApplication.run(Maestro.class, args);
    		ConfigLoader config = (ConfigLoader) context.getBean("config");
    		config.setApplicationContext(context);
        try {
			mp = (MQTTPublisher) context.getBean("MQTTPublisher");
			cr = (DeviceRepository) context.getBean("Components");
			rr = (RoomRepository) context.getBean("Rooms");
			pr = (ProductRepository) context.getBean("Products");
			cirr = (CIRRepository) context.getBean("CIRs");
			ohSitemapFE = (FileEngine) context.getBean("OH.SitemapFileEngine");
			oha = (OHAdaptor) context.getBean("OHAdaptor");
			inits = (Initializables) context.getBean("Initializables");
			final StartupManager sm = new StartupManager();
			sm.startup();
		} catch (Exception e) {
			Exception ex = new Exception("VM failed to initialize!", e);
			errorStartup(ex, 000);
		}
    }
    
    //FIXME Transfer ApplicationContext to ConfigLoader class
//    public static ApplicationContext getApplicationContext() {
//    		return context;
//    }
    
    private final class StartupManager {
//	    	private AbstRepository[] repositories;
	    	
	    	public StartupManager() {
//	    		repositories = new AbstRepository[]{rr, pr, cr};
	    	}
	    	
	    	public void startup() {
	    		//resets OpenHAB sitemap file
	    		LOG.debug("Clearing OpenHAB sitemap...");
	    		String[] lines = new String[4];
	    		lines[0] = "sitemap home label=\"" + oha.getOHSitemapName() + "\"{ ";
	    		lines[1] = "Frame{";
	    		lines[2] = "}";
	    		lines[3] = "}";
	    		OverwriteFileFEReq offer = new OverwriteFileFEReq("1234567890", lines);
			try {
				ohSitemapFE.forwardRequest(offer, Thread.currentThread(), true);
			} catch (EngineException e) {
				LOG.error("Cannot reset OpenHAB sitemap file! OpenHAB GUI will show erroneous details!", e);
			}
				
			//initializes repositories
    			LOG.debug("Initializing repositories...");
    			try {
    				//repositories must be initialized in THIS order
		    		rr.initialize();
		    		pr.initialize();
		    		cr.initialize();
		    		cirr.initialize();
    			} catch(Exception e) {
    				LOG.fatal("A repository has not initialized! BusinessMachine cannot start!", e);
    				errorStartup(e, 0);
    			}
    			
    			try {
				inits.initializeAll();
			} catch (Exception e) {
				LOG.fatal("A third-party initializable cannot be initialized!", e);
			}
	    		
	    		/*
	    		 * TESTING GROUNDS FOR REPOSITORY TESTING
	    		 */
	    		
	    		/* */	  	
	    		LOG.info("BusinessMachine started!");
	    	}
    }
}