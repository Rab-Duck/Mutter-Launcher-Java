package com.rabduck.mutter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javafx.application.Platform;
import javafx.concurrent.Task;

/**
 * 
 */

/**
 * @author Rab-Duck
 *
 */
public class MainCollector extends Task<MainCollector>{
	private final Object syncObj = new Object();
	
	public MainCollector() {
		super();
	}
	
	@Override
	protected MainCollector call() throws Exception {
		collect();
		return this;
	}
	private List<Item> itemList = new ArrayList<>();
	public void collect(){
		List<AppCollector> listApp = new ArrayList<>();
		String [] collectors = {"com.rabduck.mutter.SHFolderCollector", "com.rabduck.mutter.PathFolderCollector"};
		
		try {
			for (String collector : collectors) {
				listApp.add((AppCollector)Class.forName(collector).newInstance());
			}
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
			e.printStackTrace();
			Platform.runLater(() -> {
				ErrorDialog.showErrorDialog("Collector class not found:", e);
			});
			throw new RuntimeException(e);
		}
		
		// ExecutorService executor = Executors.newSingleThreadExecutor();
		ExecutorService executor = Executors.newFixedThreadPool(1);
		
		for (AppCollector app : listApp) {
			executor.execute(app);
		}
		
		executor.shutdown();
		
		try {
			executor.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
			Platform.runLater(() -> {
				ErrorDialog.showErrorDialog("Collector thread await error:", e);
			});
			throw new RuntimeException(e);
		}
		synchronized (syncObj) {
			itemList = null;
			itemList = new ArrayList<>();
			for (AppCollector app : listApp) {
				itemList.addAll(app.getItemList());
			}			
		}
		
		listApp = null;
	}
	
	public List<Item> getAllItemList(){
		synchronized (syncObj) {
			return new ArrayList<Item>(itemList);
		}
	}
	
	public List<Item> grep(String grepStr){
		if(grepStr == null || grepStr.equals("")){
			return getAllItemList();
		}
		
		List<Item> grepList = new ArrayList<>();
		
		synchronized (syncObj) {
			itemList.parallelStream()
				// .filter(item -> {return item.getItemName().contains(grepStr);})
				.filter(item -> {return item.getItemName().toUpperCase(Locale.JAPANESE).matches(".*" + grepStr.toUpperCase(Locale.JAPANESE) + ".*");})
				.forEach(item -> {grepList.add(item);});
		}
		return grepList;
	}

	
}
