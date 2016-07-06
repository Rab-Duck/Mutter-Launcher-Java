package com.rabduck.mutter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author rab-duck
 *
 */
public class SHFolderCollector implements AppCollector  {
	private String [] shFolders = null;
	
	public SHFolderCollector(){
		String [] folderlist = {
				"%APPDATA%\\Microsoft\\Internet Explorer\\Quick Launch",
				"%PUBLIC%\\Desktop",
				"%USERPROFILE%\\Desktop", 
				"%ProgramData%\\Microsoft\\Windows\\Start Menu",
				"%USERPROFILE%\\AppData\\Roaming\\Microsoft\\Windows\\Start Menu"
		};
		String [] envnames = {"USERPROFILE", "PUBLIC", "ProgramData", "APPDATA"};
		HashMap<String, String> envmap = new HashMap<>();
		
		for (String envname : envnames) {
			envmap.put(envname, System.getenv(envname)); 
		}
		
		
		
//		envmap.entrySet().stream().forEach(e -> {
//			for (int i = 0; i < folderlist.length; i++) {
//				folderlist[i] = folderlist[i].replace("%" + e.getKey() + "%", e.getValue());
//			}
//		});;

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
				System.out.println("Special Folder not exists: " + shFolder);
				// e.printStackTrace();
			}
		}
		
	}

	@Override
	public List<Item> getItemList() {
		return items;
	}

	


}
