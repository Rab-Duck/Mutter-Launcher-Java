/**
 * 
 */
package com.rabduck.mutter;

import java.io.*;
import java.util.Properties;

/**
 * @author Rab-Duck
 *
 * reference: https://www.mlab.im.dendai.ac.jp/~yamada/java/properties/
 */
public class EnvManager {
    private Properties conf;
    private String filename = "properties.xml";
    
    public static EnvManager envmngr = new EnvManager();
    public static EnvManager getInstance(){
    	return envmngr;
    }

    private EnvManager(){
        conf = new Properties();
        try {
            conf.loadFromXML(new FileInputStream(filename));
        } catch (IOException e) {
            System.err.println("Cannot open " + filename + ".");
            e.printStackTrace();
        }
    }

    public Integer getIntProperty(String key) {
        if(conf.containsKey(key))
            return Integer.parseInt(conf.getProperty(key));
        else {
            System.err.println("Key not found: " + key);
            return null;
        }
    }
    
    public String getProperty(String key) {
        if(conf.containsKey(key))
            return conf.getProperty(key);
        else {
            System.err.println("Key not found: " + key);
            return "";
        }
    }

    public void addProperty(String key, Integer value) {
    	addProperty(key, value.toString());
    }

    public void addProperty(String key, String value) {
        if(conf.containsKey(key))
            System.err.println("Key already exists: " + key);
        else {
            conf.setProperty(key, value);
        }
    }

    public void storeToXML() {
        try {
            conf.storeToXML(new FileOutputStream(filename), "Mutter Launcher Environment Value");
        } catch (IOException e) {
            System.err.println("Cannot open " + filename + ".");
            e.printStackTrace();
        }
    }
}