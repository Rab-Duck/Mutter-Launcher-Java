/**
 * 
 */
package com.rabduck.mutter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author paradisaea
 *
 */
public class AnyFolderCollector implements AppCollector {
	private static Logger logger = Logger.getLogger(com.rabduck.mutter.AnyFolderCollector.class.getName());
	private static EnvManager envmngr;
	
	private String [] anyFolders = null;
	
	public AnyFolderCollector(){
		try {
			envmngr = EnvManager.getInstance();
		} catch (IOException e) {
			logger.log(Level.SEVERE, "Env file I/O error:", e);
			ErrorDialog.showErrorDialog("Env file I/O error:", e, true);
			System.exit(-1);
		}
		anyFolders = envmngr.getAnyFolderList();
	}

	private List<Item> items;
	/* 
	 * @see AppCollector#collect()
	 */
	@Override
	public void collect() {
		items = new ArrayList<>();
		String [] pathargs;
		String pathExt;
		for(String anyFolder : anyFolders){
			try {
				pathargs = anyFolder.split(";", 2);
				if(pathargs.length < 2 ||  pathargs[1] == null || pathargs[1].equals("")){
					// default value from https://en.wikipedia.org/wiki/Environment_variable#Default_values
					pathExt = ".com;.exe;.bat;.cmd;.vbs;.vbe;.js;.jse;.wsf;.wsh;.msc";
				}
				else{
					pathExt = pathargs[1];
				}
				FileCollector fc = new FileCollector(pathargs[0], Integer.MAX_VALUE, pathExt);
				fc.collect();
				items.addAll(fc.getItemList());
			} catch (IOException e) {
				logger.log(Level.INFO, "Any Folder not exists: " + anyFolder);
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
