package com.rabduck.mutter;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

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
	private static Logger logger = Logger.getLogger(com.rabduck.mutter.MainCollector.class.getName());
	
	private final Object syncObj = new Object();
	
	private int execLogIndex = 0;
	private EnvManager envmngr;
	private List<Item> itemList = new ArrayList<>();
	private List<Item> historyItemList;

	public MainCollector() {
		super();
		try {
			envmngr = EnvManager.getInstance();
			historyItemList = envmngr.getExecHistory();
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
		String [] collectors = {"com.rabduck.mutter.SHFolderCollector", "com.rabduck.mutter.PathFolderCollector"};
		
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
			List<Item> allItemList = new ArrayList<>();
			allItemList.addAll(historyItemList);
			allItemList.addAll(itemList);
			return allItemList;
		}
	}
	
	static final boolean bUseParallel = false; 
	public List<Item> grep(String grepStr){

		if(grepStr == null || grepStr.equals("")){
			return getAllItemList();
		}
		
		List<Item> grepList = new ArrayList<>();
		
		historyItemList.stream()
		.filter(item -> {return item.getItemName().toUpperCase(Locale.JAPANESE).contains(grepStr.toUpperCase(Locale.JAPANESE));})
		.forEach(item -> {grepList.add(item);});

		synchronized (syncObj) {
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
		}
		return grepList;
	}

	public void setExecHistory(Item execItem){
		final int historyMax = envmngr.getIntProperty("HistoryMax");
		
		int i = 0;
		for (Iterator<Item> iterator = historyItemList.iterator(); iterator.hasNext();i++) {
			Item itrItem = (Item) iterator.next();
			if(i>=historyMax-1 || itrItem.historyEquals(execItem)){
				iterator.remove();
				i--;
			}
		}
		if(historyMax > 0){
			Item historyItem = execItem.copy();
			historyItem.setType(Item.TYPE_HISTORY);
			historyItemList.add(0, historyItem);
		}

		envmngr.setExecHistory(historyItemList);
		
/*		for (ListIterator<Item> iterator = itemList.listIterator(); iterator.hasNext();) {
			Item itrItem = iterator.next();
			if(itrItem.getType() >= Item.TYPE_FIX){
				continue;
			}
			else if(i == 0 && 
					(itrItem.getType() == Item.TYPE_HISTORY || itrItem.getType() == Item.TYPE_NORMAL)){
				Item historyItem = execItem.copy();
				historyItem.setType(Item.TYPE_HISTORY);
				iterator.previous();
				iterator.add(historyItem);
				iterator.next();
				historyList.add(historyItem);
				i++;
			}
			if(itrItem.getType() == Item.TYPE_HISTORY){
				if(itrItem.historyEquals(execItem)){
					iterator.remove();
					continue;
				}
				else if(++i > historyMax){
					iterator.remove();
				}else{
					historyList.add(itrItem);
				}
				continue;
			}
			if(itrItem.getType() == Item.TYPE_NORMAL){
				break;
			}
		}
*/		

	}
}
