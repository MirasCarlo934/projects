package bm.main;

import java.util.List;

import org.apache.log4j.Logger;
//import org.springframework.boot.SpringApplication;
//import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.PropertySource;

import bm.comms.Sender;
import bm.comms.mqtt.MQTTPublisher;
import bm.context.adaptors.OHAdaptor;
import bm.main.controller.Controller;
import bm.main.controller.ModuleDispatcher;
import bm.main.engines.AbstEngine;
import bm.main.engines.FileEngine;
import bm.main.repositories.CIRRepository;
import bm.main.repositories.DeviceRepository;
import bm.main.repositories.ProductRepository;
import bm.main.repositories.RoomRepository;
import bm.tools.IDGenerator;

//@SpringBootApplication
//@ComponentScan({"bm.ui"})
//@PropertySource({"file:configuration/bm.properties", "file:configuration/user.properties"})
//@ImportResource({
//	"file:configuration/spring/main-config.xml", "file:configuration/spring/devices-config.xml", 
//	"file:configuration/spring/adaptors-config.xml", "file:configuration/spring/engines-config.xml", 
//	"file:configuration/spring/modules-config.xml", "file:configuration/spring/tools-config.xml", 
//	"file:configuration/spring/core-config.xml", "file:configuration/spring/product-config.xml", 
//	"file:configuration/spring/comms-config.xml", "file:configuration/spring/openhab-config.xml",
//	"file:configuration/spring/repositories-config.xml"
//})
@PropertySource({"file:configuration/bm.properties", "file:configuration/user.properties"})
@ImportResource({
	"file:configuration/spring/main-config.xml", "file:configuration/spring/devices-config.xml", 
	"file:configuration/spring/adaptors-config.xml", "file:configuration/spring/engines-config.xml", 
	"file:configuration/spring/modules-config.xml", "file:configuration/spring/tools-config.xml", 
	"file:configuration/spring/core-config.xml", "file:configuration/spring/product-config.xml", 
	"file:configuration/spring/comms-config.xml", "file:configuration/spring/openhab-config.xml",
	"file:configuration/spring/repositories-config.xml"
})
//FIXME Find a way to integrate plex
public class BusinessMachine {
	private static final int build = 1;
	private static final Logger LOG = Logger.getLogger("MAIN.BusinessMachine");
	private Initializables inits;
	private MQTTPublisher mp;
	private DeviceRepository cr;
	private RoomRepository rr;
	private ProductRepository pr;
	private CIRRepository cirr;
	private FileEngine ohSitemapFE;
	private List<AbstEngine> engines;
	private List<Sender> senders;
	private OHAdaptor oha;
	
	private Controller controller;
	private ModuleDispatcher moduleDispatcher;

	public static void main(String[] args) {
		//initialization phase
		BusinessMachine bm = new BusinessMachine();
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
		LOG.fatal("Fatal error occurred! VM shutting down...", e);
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

//    		ApplicationContext context = SpringApplication.run(BusinessMachine.class, args);
    		ApplicationContext context = new AnnotationConfigApplicationContext(BusinessMachine.class);
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
			controller = (Controller) context.getBean("Controller");
			moduleDispatcher = (ModuleDispatcher) context.getBean("ModuleDispatcher");
			engines = (List<AbstEngine>) context.getBean("Engines");
			senders = (List<Sender>) context.getBean("Senders");
			final StartupManager sm = new StartupManager();
			sm.startup();
		} catch (Exception e) {
			Exception ex = new Exception("VM failed to initialize!", e);
			errorStartup(ex, 000);
		}
    }
    
    private final class StartupManager {
    		private IDGenerator idg = new IDGenerator();
	    	
	    	public void startup() {
	    		//initializes senders
	    		while(!senders.isEmpty()) {
	    			Sender sender = senders.remove(0);
	    			Thread t = new Thread(sender, sender.getName());
	    			t.start();
	    		}
	    		
	    		//initializes engines
	    		while(!engines.isEmpty()) {
	    			AbstEngine engine = engines.remove(0);
	    			Thread t = new Thread(engine, engine.getName());
	    			t.start();
	    		}
	    		
	    		try {
					inits.initializeAll();
				} catch (Exception e) {
					LOG.fatal("A third-party initializable cannot be initialized!", e);
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
    			
    			//run runnables on separate threads
    			Thread t1 = new Thread(controller, controller.getClass().getSimpleName());
    			Thread t2 = new Thread(moduleDispatcher, moduleDispatcher.getClass().getSimpleName());
    			t1.start();
    			t2.start();
	    		
	    		/*
	    		 * TESTING GROUNDS FOR REPOSITORY TESTING
	    		 */
	    		
	    		/* */	  	
	    		LOG.info("BusinessMachine started!");
	    	}
    }
}