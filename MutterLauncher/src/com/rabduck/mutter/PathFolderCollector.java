package com.rabduck.mutter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * 
 */

/**
 * @author paradisaea
 *
 */
public class PathFolderCollector implements AppCollector {
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
				System.out.println("PATH Folder not exists: " + pathFolder);
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
