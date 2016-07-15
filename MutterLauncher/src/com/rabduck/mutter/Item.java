package com.rabduck.mutter;

import java.io.Serializable;

import javax.swing.Icon;

public interface Item extends Serializable{
	static final int TYPE_NORMAL = 0;
	static final int TYPE_HISTORY = 1;
	static final int TYPE_FIX = 10;
	
	@Override
	String toString();
	boolean historyEquals(Item item);

	String getItemName();
	void setItemPath(String path);
	String getItemPath();

	Item copy();
	void setType(int type);
	int getType();
	Icon getIcon();
	boolean execute(String option) throws ExecException;
}
