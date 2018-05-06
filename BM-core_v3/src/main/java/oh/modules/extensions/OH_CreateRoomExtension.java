package oh.modules.extensions;

import bm.comms.http.HTTPException;
import bm.comms.http.HTTPSender;
import bm.context.adaptors.exceptions.AdaptorException;
import bm.context.rooms.Room;
import bm.jeep.JEEPRequest;
import bm.main.engines.FileEngine;
import bm.main.engines.exceptions.EngineException;
import bm.main.modules.AbstModuleExtension;
import bm.main.repositories.RoomRepository;
import bm.tools.IDGenerator;
import oh.main.initializables.OH_Initializer;

public class OH_CreateRoomExtension extends AbstModuleExtension {
	private RoomRepository rr;
	private HTTPSender hs;
	private FileEngine sitemapFE;
	private String ohIP;
	private IDGenerator idg;

	public OH_CreateRoomExtension(String logDomain, String errorLogDomain, String name, String[] params) {
		super(logDomain, errorLogDomain, name, params);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void process(JEEPRequest request) {
		Room r = rr.getRecentlyAddedRoom();
		
		mainLOG.debug("Adding room " + r.getSSID() + " (" + r.getName() + ") to OpenHAB item registry...");
		try {
			OH_Initializer.addItems(mainLOG, hs, ohIP, r.convertToItemsJSON(), true);
		} catch(HTTPException e) {
			mainLOG.error("Cannot add room " + r.getSSID() + "(" + r.getName() + ") to "
					+ "registry", e);
		}
		
		//persists the room to the sitemap file if it has NO parent room
		if(r.getRoom() == null) { 
			mainLOG.debug("Adding room " + r.getSSID() + " (" + r.getName() + ") to OpenHAB sitemap file...");
			try {
				OH_Initializer.addItemToSitemap(sitemapFE, idg, r.convertToSitemapString(), true);
			} catch (EngineException e) {
				mainLOG.error("Cannot add room to sitemap file!", e);
			}
		}
	}

	@Override
	protected boolean additionalRequestChecking(JEEPRequest request) {
		return true;
	}
}
