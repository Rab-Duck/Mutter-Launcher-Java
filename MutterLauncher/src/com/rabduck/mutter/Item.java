package com.rabduck.mutter;

import javax.swing.Icon;

public interface Item {
	@Override
	String toString();
	String getItemName();
	void setItemPath(String path);
	String getItemPath();
	Icon getIcon();
	boolean execute(String option) throws ExecException;
}
