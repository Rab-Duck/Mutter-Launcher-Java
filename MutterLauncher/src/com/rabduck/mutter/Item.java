package com.rabduck.mutter;


public interface Item {
	@Override
	String toString();
	String getItemName();
	void setItemPath(String path);
	String getItemPath();
	boolean execute(String option) throws ExecException;
}
