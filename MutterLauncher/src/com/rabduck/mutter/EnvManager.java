/**
 * 
 */
package com.rabduck.mutter;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.InvalidPropertiesFormatException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Rab-Duck
 *
 * reference: https://www.mlab.im.dendai.ac.jp/~yamada/java/properties/
 */
public class EnvManager {
	private static Logger logger = Logger.getLogger(com.rabduck.mutter.EnvManager.class.getName());

	private Properties conf;
    private String propsFilename = "properties.xml";
    
    public static EnvManager envmngr;
    public static EnvManager getInstance() throws IOException{
    	if(envmngr != null){
        	return envmngr;    		
    	}
    	envmngr = new EnvManager();
    	return envmngr;
    }

    private EnvManager() throws IOException{
    	LoadDefaultProperties();
    	LoadPersonalProperties();
    }
    private void LoadDefaultProperties() throws IOException{
        conf = new Properties();
        conf.loadFromXML(new FileInputStream(propsFilename));
    }
    private void LoadPersonalProperties() throws IOException{
    	String userHome = System.getProperty("user.home");
    	String envDir = userHome + "\\MutterLauncher";
        propsFilename = envDir + "\\" + propsFilename;
    	Path envPath = Paths.get(envDir);
    	
    	if(Files.isDirectory(envPath)){
            Properties _conf = new Properties();
            try{
	            _conf.loadFromXML(new FileInputStream(propsFilename));
	            conf = _conf;
            }catch(IOException e){
                logger.log(Level.WARNING, "Can't read personal env file:" + propsFilename, e);
            }
    	}
    	else{
			Files.createDirectory(envPath);
    	}
    	
    	storeToXML();
    }

    public Integer getIntProperty(String key) {
        if(conf.containsKey(key))
            return Integer.parseInt(conf.getProperty(key));
        else {
            logger.log(Level.SEVERE, "Key not found: " + key);
            throw new IllegalArgumentException("Key not found: " + key);
        }
    }
    
    public String getProperty(String key) {
        if(conf.containsKey(key))
            return conf.getProperty(key);
        else {
        	logger.log(Level.SEVERE, "Key not found: " + key);
            throw new IllegalArgumentException("Key not found: " + key);
        }
    }

    public void setProperty(String key, Integer value) {
    	setProperty(key, value.toString());
    }

    public void setProperty(String key, String value) {
        conf.setProperty(key, value);
        storeToXML();
    }

    public void storeToXML() {
        try {
            conf.storeToXML(new FileOutputStream(propsFilename), "Mutter Launcher Environment Value");
        } catch (IOException e) {
        	logger.log(Level.SEVERE, "Cannot open " + propsFilename + ".");
            e.printStackTrace();
            ErrorDialog.showErrorDialog("Env file write error:", "Cannot open " + propsFilename + ".", true);
        }
    }
}