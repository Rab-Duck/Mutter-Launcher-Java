/**
 * 
 */
package com.rabduck.mutter;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
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
	private String historyFilename = "HistoryList.ser";
	private String itemListFilename = "ItemList.ser";
	private String anyFolderListFilename = "FolderList.txt";
    private String envDir;
    
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
    	envDir = userHome + "\\MutterLauncher";
        propsFilename = envDir + "\\" + propsFilename;
        historyFilename = envDir + "\\" + historyFilename;
        itemListFilename = envDir + "\\" + itemListFilename;
        anyFolderListFilename = envDir + "\\" + anyFolderListFilename;

    	Path envPath = Paths.get(envDir);
    	
    	if(Files.isDirectory(envPath)){
            Properties _conf = new Properties();
            try{
	            _conf.loadFromXML(new FileInputStream(propsFilename));
	            for (String key : _conf.stringPropertyNames()) {
					conf.setProperty(key, _conf.getProperty(key));
				}
            }catch(IOException e){
                logger.log(Level.WARNING, "Cannot read personal env file:" + propsFilename, e);
            }
    	}
    	else{
			Files.createDirectory(envPath);
    	}
    	
    	storeToXML();
    }

    public Boolean getBooleanProperty(String key) {
        if(conf.containsKey(key))
            return Boolean.parseBoolean(conf.getProperty(key));
        else {
            logger.log(Level.SEVERE, "Key not found: " + key);
            throw new IllegalArgumentException("Key not found: " + key);
        }
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

    public void setProperty(String key, Boolean value) {
    	setProperty(key, value.toString());
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
        	logger.log(Level.WARNING, "Cannot write " + propsFilename + ".", e);
            // e.printStackTrace();
            ErrorDialog.showErrorDialog("Env file write error:" + propsFilename, e, true);
        }
    }
    
    public void setExecHistory(List<Item> historyList){

    	try (ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(historyFilename)))) {
			oos.writeObject(historyList);
        } catch (IOException e) {
        	logger.log(Level.SEVERE, "Cannot write " + historyFilename + ".", e);
            ErrorDialog.showErrorDialog("History file write error:" + historyFilename, e, true);
        }
		return;
    }

    @SuppressWarnings("unchecked")
	public List<Item> getExecHistory(){
    	List<Item> historyList= new ArrayList<>();
    	if(!Files.exists(Paths.get(historyFilename))){
    		return historyList;
    	}
    	try (ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(new FileInputStream(historyFilename)))) {
    		historyList = (List<Item>)ois.readObject();
        } catch (Exception e) {
        	logger.log(Level.SEVERE, "Cannot read " + envDir + historyFilename + ".", e);
            ErrorDialog.showErrorDialog("History file read error:" + historyFilename, e, true);
        }
		return historyList;
    }
	public void setItemList(List<Item> itemList){
    	try (ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(itemListFilename)))) {
			oos.writeObject(itemList);
        } catch (IOException e) {
        	logger.log(Level.SEVERE, "Cannot write " + itemListFilename + ".", e);
            ErrorDialog.showErrorDialog("History file write error:" + itemListFilename, e, true);
        }
		return;
	}

	@SuppressWarnings("unchecked")
	public List<Item> getItemList(){
    	List<Item> itemList = null;
    	if(!Files.exists(Paths.get(itemListFilename))){
    		return null;
    	}
    	try (ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(new FileInputStream(itemListFilename)))) {
    		itemList = (List<Item>)ois.readObject();
        } catch (Exception e) {
        	logger.log(Level.SEVERE, "Cannot read " + envDir + itemListFilename + ".", e);
            ErrorDialog.showErrorDialog("History file read error:" + itemListFilename, e, true);
        }
		return itemList;
	}
	
	public String [] getAnyFolderList(){
		try {
			List<String> allLines = Files.readAllLines(Paths.get(anyFolderListFilename));
			return allLines.toArray(new String[allLines.size()]);
		} catch (NoSuchFileException nsfe){
			return new String[0];
		} catch (Exception e) {
        	logger.log(Level.SEVERE, "Cannot read " + envDir + anyFolderListFilename + ".", e);
            ErrorDialog.showErrorDialog("FolderList file read error:" + anyFolderListFilename, e, true);
		}
		return new String[0];
	}

}