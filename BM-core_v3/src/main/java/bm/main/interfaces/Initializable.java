package bm.main.interfaces;

/**
 * The interface implemented by all objects that has to undergo additional initialization processes after environment 
 * setup and before VM operation start.
 * @author carlomiras
 *
 */
public interface Initializable {
	
	void initialize() throws Exception;
}
