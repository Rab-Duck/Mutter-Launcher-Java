package com.rabduck.mutter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.concurrent.Task;

/**
 * @author Rab-Duck
 *
 */
public class MainCollector extends Task<MainCollector>{
	private static Logger logger = Logger.getLogger(com.rabduck.mutter.MainCollector.class.getName());
	
	private static final Object syncObj = new Object();
	private static final boolean bUseParallel = false; 
	
	private EnvManager envmngr;
	private List<Item> itemList = new ArrayList<>();
	private List<Item> historyItemList;

	public MainCollector() {
		super();
		try {
			envmngr = EnvManager.getInstance();
		} catch (IOException e) {
			logger.log(Level.SEVERE, "Env file I/O error:", e);
			ErrorDialog.showErrorDialog("Env file I/O error:", e, false);
			System.exit(-1);
		}
	}
	
	@Override
	protected MainCollector call() throws Exception {
		collect();
		return this;
	}
	public void collect(){
		
		
		List<AppCollector> listApp = new ArrayList<>();
		String [] collectors = {"com.rabduck.mutter.SHFolderCollector", "com.rabduck.mutter.PathFolderCollector", "com.rabduck.mutter.AnyFolderCollector"};
		
		try {
			for (String collector : collectors) {
				listApp.add((AppCollector)Class.forName(collector).newInstance());
			}
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
			e.printStackTrace();
			ErrorDialog.showErrorDialog("Collector class not found:", e, true);
			throw new RuntimeException(e);
		}
		
		// ExecutorService executor = Executors.newSingleThreadExecutor();
		ExecutorService executor = Executors.newFixedThreadPool(envmngr.getIntProperty("CollectThreadNum"));
		
		for (AppCollector app : listApp) {
			executor.execute(app);
		}
		
		executor.shutdown();
		
		try {
			executor.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
			ErrorDialog.showErrorDialog("Collector thread await error:", e, true);
			throw new RuntimeException(e);
		}
		
//		try {
//			Thread.sleep(10000);
//		} catch (InterruptedException e) {
//		}
		
		itemList = null;
		itemList = new ArrayList<>();
		for (AppCollector app : listApp) {
			itemList.addAll(app.getItemList());
		}
		synchronized (syncObj) {
			storeItemList();
		}
		historyItemList = null;
		
		listApp = null;
	}
	
	public boolean cachedCollect(){
		List<Item> cachedItemList = envmngr.getItemList();
		if(cachedItemList == null){
			return false;
		}
		itemList = cachedItemList;
		return true;
	}
	
	public void storeItemList(){
		synchronized (syncObj) {
			envmngr.setItemList(itemList);
		}
	}
	
	public List<Item> getAllItemList() {
		List<Item> allItemList = new ArrayList<>();
		if (historyItemList == null) {
			synchronized (syncObj) {
				historyItemList = envmngr.getExecHistory();
			}
		}
		allItemList.addAll(historyItemList);
		allItemList.addAll(itemList);
		return allItemList;
	}
	
	public List<Item> grep(String grepStr){

		if(grepStr == null || grepStr.equals("")){
			return getAllItemList();
		}
		
		List<Item> grepList = new ArrayList<>();
		
		if(historyItemList == null){
			synchronized (syncObj) {
				historyItemList = envmngr.getExecHistory();
			}
		}
		historyItemList.stream()
		.filter(item -> {return item.getItemName().toUpperCase(Locale.JAPANESE).contains(grepStr.toUpperCase(Locale.JAPANESE));})
		.forEach(item -> {grepList.add(item);});

		if(bUseParallel){
			itemList.parallelStream()
			.filter(item -> {return item.getItemName().toUpperCase(Locale.JAPANESE).contains(grepStr.toUpperCase(Locale.JAPANESE));})
			// .filter(item -> {return item.getItemName().toUpperCase(Locale.JAPANESE).matches(".*" + grepStr.toUpperCase(Locale.JAPANESE) + ".*");})
			.forEach(item -> {grepList.add(item);});
		}
		else{
			itemList.stream()
			.filter(item -> {return item.getItemName().toUpperCase(Locale.JAPANESE).contains(grepStr.toUpperCase(Locale.JAPANESE));})
			// .filter(item -> {return item.getItemName().toUpperCase(Locale.JAPANESE).matches(".*" + grepStr.toUpperCase(Locale.JAPANESE) + ".*");})
			.forEach(item -> {grepList.add(item);});
		}
		return grepList;
	}

	public void setExecHistory(Item execItem){
		final int historyMax = envmngr.getIntProperty("HistoryMax");
		
		Item historyItem = execItem.copy();
		historyItem.setType(Item.TYPE_HISTORY);
		historyItemList.add(0, historyItem);

		int i = 0;
		for (Iterator<Item> iterator = historyItemList.iterator(); iterator.hasNext();i++) {
			Item itrItem = (Item) iterator.next();
			if(i>=historyMax || (i!=0 && itrItem.historyEquals(execItem))){
				iterator.remove();
				i--;
			}
		}

		synchronized (syncObj) {
			envmngr.setExecHistory(historyItemList);
		}
		
	}
}
