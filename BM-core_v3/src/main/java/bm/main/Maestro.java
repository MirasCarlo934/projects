package bm.main;

import bm.comms.Sender;
import bm.context.adaptors.AdaptorManager;
import bm.main.controller.Controller;
import bm.main.controller.ModuleDispatcher;
import bm.main.engines.AbstEngine;
import bm.cir.CIRRepository;
import bm.main.repositories.DeviceRepository;
import bm.main.repositories.ProductRepository;
import bm.main.repositories.RoomRepository;
import org.apache.log4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ImportResource;

import java.util.List;

//import org.springframework.boot.SpringApplication;
//import org.springframework.boot.autoconfigure.SpringBootApplication;

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
@SpringBootApplication
@ComponentScan({"ui"})
//TASK Do not use @ImportResource and @PropertySource
//@PropertySource({"file:configuration/bm.properties", "file:configuration/user.properties", 
//	"file:configuration/ui.properties"})
//@ImportResource({
//	"file:configuration/spring/main-config.xml", "file:configuration/spring/devices-config.xml", 
//	"file:configuration/spring/adaptors-config.xml", "file:configuration/spring/engines-config.xml", 
//	"file:configuration/spring/modules-config.xml", "file:configuration/spring/tools-config.xml", 
//	"file:configuration/spring/core-config.xml", "file:configuration/spring/product-config.xml", 
//	"file:configuration/spring/comms-config.xml", "file:configuration/spring/openhab-config.xml",
//	"file:configuration/spring/repositories-config.xml"
//})
//@ImportResource("${config.locations}")
@ImportResource({"file:configuration/spring/core-config.xml"})
public class Maestro {
	private static ApplicationContext applicationContext;
	private static final int build = 1;
	private static final Logger LOG = Logger.getLogger("MAIN.BusinessMachine");
	private Initializables inits;
	private DeviceRepository dr;
	private RoomRepository rr;
	private ProductRepository pr;
	private CIRRepository cirr;
	private AdaptorManager am;
	private List<AbstEngine> engines;
	private List<Sender> senders;
	
	private Controller controller;
	private ModuleDispatcher moduleDispatcher;

	public static void main(String[] args) {
		Maestro maestro = new Maestro();
		maestro.setup(args);
		maestro.start();
	}
	
	/**
	 * Can be called in order to signify a fatal error during startup which can lead to runtime errors and 
	 * possible termination of the Maestro's functionality. This method terminates the VM.
	 * 
	 * @param e The exception that causes the fatal error
	 * @param status The error status of the VM
	 */
	public static void errorStartup(Exception e, int status) {
		LOG.fatal("Fatal error occurred! VM shutting down...", e);
		System.exit(status);
	}
	
	 /**
     * Retrieves all objects to be initialized for Symphony to run. Objects are retrieved from the 
     * Spring IoC container using the <i>ConfigLoader</i> object.
     */
    public void setup(String[] args) {
    		LOG.info("Starting Maestro... ");
    		ApplicationContext context = SpringApplication.run(Maestro.class, args);
    		Maestro.applicationContext = context;

    		try {
        		//comm layer
                senders = (List<Sender>) context.getBean("Senders");

                //controller layer
                controller = (Controller) context.getBean("Controller");
                moduleDispatcher = (ModuleDispatcher) context.getBean("ModuleDispatcher");
                dr = (DeviceRepository) context.getBean("Devices");
                rr = (RoomRepository) context.getBean("Rooms");
                pr = (ProductRepository) context.getBean("Products");
                cirr = (CIRRepository) context.getBean("CIRs");
                am = (AdaptorManager) context.getBean("AdaptorManager");

                //engine layer
                engines = (List<AbstEngine>) context.getBean("Engines");

                //misc initializables
                inits = (Initializables) context.getBean("Initializables");
            } catch (Exception e) {
                Exception ex = new Exception("VM failed to initialize!", e);
                errorStartup(ex, 000);
            }
    }
    
    /**
     * 
     */
    public void start() {
		//initializes engines
		while(!engines.isEmpty()) {
			AbstEngine engine = engines.remove(0);
			Thread t = new Thread(engine, engine.getName());
			t.start();
		}
		
		//initializes repositories
		LOG.debug("Initializing repositories...");
		try {
			//repositories must be initialized in THIS order
	    		rr.initialize();
	    		pr.initialize();
	    		dr.initialize();
	    		cirr.initialize();
		} catch(Exception e) {
			LOG.fatal("A repository has not initialized! BusinessMachine cannot start!", e);
			errorStartup(e, 0);
		}
		
		//initializes senders
		while(!senders.isEmpty()) {
			Sender sender = senders.remove(0);
			Thread t = new Thread(sender, sender.getName());
			t.start();
		}
		
		//run runnables on separate threads
		Thread t1 = new Thread(controller, controller.getClass().getSimpleName());
		Thread t2 = new Thread(moduleDispatcher, moduleDispatcher.getClass().getSimpleName());
		t1.start();
		t2.start();

		//sets repositories for adaptor manager
		am.setRepositories(pr, dr, rr);

		//initializes other Initializable objects
		try {
			inits.initializeAll();
		} catch (Exception e) {
			LOG.fatal("A third-party initializable cannot be initialized!", e);
		}

		//updates Environment
        rr.updateRoomsInEnvironment();
		dr.updateDevicesInEnvironment();
		
		/*
		 * TESTING GROUNDS FOR REPOSITORY TESTING
		 */
		
		/* */	  	
		LOG.info("BusinessMachine started!");
	}
    
    public static ApplicationContext getApplicationContext() {
    		return applicationContext;
    }
}