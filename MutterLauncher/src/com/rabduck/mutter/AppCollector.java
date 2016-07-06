package com.rabduck.mutter;

import java.util.List;

public interface AppCollector extends Runnable {

	void collect();
	List<Item> getItemList();
	
	@Override
	default void run() {
		collect();		
	}
	
}
