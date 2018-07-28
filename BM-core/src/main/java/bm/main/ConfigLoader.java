package bm.main;

import java.util.Iterator;
import java.util.List;

import org.springframework.context.ApplicationContext;

/**
 * The class that loads the filenames of the Spring Configuration files used by the BM.  
 * This is to make the configuration "dynamic".
 */
public class ConfigLoader {
	private ApplicationContext context;
    private List<String> configFiles;

    /**
     * @return Returns the ApplicationContext of the Spring instance.
     */
    public String[] getConfig() {
        String[] s = new String[configFiles.size()];
        int count = 0;
        for (Iterator<String> i = configFiles.iterator(); i.hasNext(); ) {
            Object o = i.next();
            s[count] = (String) o;
            count++;
        }
        return s;
    }
    /**
     * @return Returns the configFiles.
     */
    public List<String> getConfigFiles() {
        return configFiles;
    }
    /**
     * @param configFiles The configFiles to set.
     */
    public void setConfigFiles(List<String> configFiles) {
        this.configFiles = configFiles;
    }
    
    public void addConfigFiles(String[] configFiles) {
	    	for(int i = 0; i < configFiles.length; i++) {
	    		this.configFiles.add(configFiles[i]);
	    	}
    }
    
    public ApplicationContext getApplicationContext() {
    		if(context != null) {
    			return context;
    		} else {
    			throw new NullPointerException("Application context not yet set in ConfigLoader!");
    		}
    }
    
    public void setApplicationContext(ApplicationContext context) {
    		this.context = context;
    }
}
