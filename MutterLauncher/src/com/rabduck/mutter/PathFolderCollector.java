package com.rabduck.mutter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 
 */

/**
 * @author paradisaea
 *
 */
public class PathFolderCollector implements AppCollector {

	private static Logger logger = Logger.getLogger(com.rabduck.mutter.MainController.class.getName());
	
	private String [] pathFolders = null;
	
	public PathFolderCollector(){
		pathFolders = System.getenv("PATH").split(";");
	}

	private List<Item> items;
	/* 
	 * @see AppCollector#collect()
	 */
	@Override
	public void collect() {
		items = new ArrayList<>();
		for(String pathFolder : pathFolders){
			try {
				String pathExt = System.getenv("PATHEXT");
				if(pathExt == null || pathExt.equals("")){
					// default value from https://en.wikipedia.org/wiki/Environment_variable#Default_values
					pathExt = ".com;.exe;.bat;.cmd;.vbs;.vbe;.js;.jse;.wsf;.wsh;.msc";
				}
				FileCollector fc = new FileCollector(pathFolder, 1, pathExt);
				fc.collect();
				items.addAll(fc.getItemList());
			} catch (IOException e) {
				logger.log(Level.INFO, "PATH Folder not exists: " + pathFolder);
				// e.printStackTrace();
			}
		}
	}

	/* 
	 * @see AppCollector#getItemList()
	 */
	@Override
	public List<Item> getItemList() {
		return items;
	}

}
