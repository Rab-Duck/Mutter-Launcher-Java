package com.rabduck.mutter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author rab-duck
 *
 */
public class SHFolderCollector implements AppCollector  {

	private static Logger logger = Logger.getLogger(com.rabduck.mutter.MainController.class.getName());
	
	private String [] shFolders = null;
	
	public SHFolderCollector(){
		String [] folderlist = {
				"%APPDATA%\\Microsoft\\Internet Explorer\\Quick Launch",
				"%ProgramData%\\Microsoft\\Windows\\Start Menu",
				"%USERPROFILE%\\AppData\\Roaming\\Microsoft\\Windows\\Start Menu",
				"%PUBLIC%\\Desktop",
				"%USERPROFILE%\\Desktop", 
		};
		String [] envnames = {"USERPROFILE", "PUBLIC", "ProgramData", "APPDATA"};
		HashMap<String, String> envmap = new HashMap<>();
		
		for (String envname : envnames) {
			envmap.put(envname, System.getenv(envname)); 
		}
		
		
		for(String key : envmap.keySet()){
			for (int i = 0; i < folderlist.length; i++) {
				folderlist[i] = folderlist[i].replace("%" + key + "%", envmap.get(key));
			}
		}
		
		shFolders = folderlist; 
		
	}

	private List<Item> items;
	@Override
	public void collect() {
		items = new ArrayList<>();
		for(String shFolder : shFolders){
			try {
				FileCollector fc = new FileCollector(shFolder);
				fc.collect();
				items.addAll(fc.getItemList());
			} catch (IOException e) {
				logger.log(Level.INFO, "Special Folder not exists: " + shFolder, e);
			}
		}
		
	}

	@Override
	public List<Item> getItemList() {
		return items;
	}

	


}
