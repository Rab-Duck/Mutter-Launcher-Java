package com.rabduck.mutter;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.swing.Icon;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileSystemView;

public class FileItem implements Item{
	private static final long serialVersionUID = 3561176450115531753L;
	private String path;
	private transient Icon icon;
	private final boolean bIconCached = false;

	public int type;
	public FileItem(String fullPath) {
		super();
		setItemPath(fullPath);
	}

	public FileItem(Path fullPath) {
		super();
		setItemPath(fullPath);
	}

	@Override
	public String toString() {
		return getItemName();
	}
	
	@Override
	public String getItemPath() {
		// TODO Auto-generated method stub
		return path;
	}

	@Override
	public String getItemName() {
		// TODO Auto-generated method stub
		return Paths.get(path).getFileName().toString();
	}

	@Override
	public void setItemPath(String fullPath) {
		// TODO Auto-generated method stub
		type = Item.TYPE_NORMAL;
		path = fullPath;
		if(bIconCached){
			icon = FileSystemView.getFileSystemView().getSystemIcon(new File(path));
		}
	}

	public void setItemPath(Path fullPath) {
		// TODO Auto-generated method stub
		setItemPath(fullPath.toAbsolutePath().toString());
	}

	@Override
	public boolean execute(String option) throws ExecException {
		try {
			Desktop.getDesktop().open(new File(getItemPath()));
		} catch (IOException e) {
			e.printStackTrace();
			throw new ExecException(e);
		}
		return false;
	}

	@Override
	public Icon getIcon() {
		if(icon != null){
			return icon;
		}
		
		// reference: 
		//		How do I get a file's icon in Java? - Stack Overflow
		//		http://stackoverflow.com/questions/4363251/how-do-i-get-a-files-icon-in-java
		return FileSystemView.getFileSystemView().getSystemIcon(new File(path));
		// for OS X ?
        // JFileChooser jfc = new JFileChooser();
		// return jfc.getUI().getFileView(jfc).getIcon(new File(path));
	}

	@Override
	public int getType(){
		return type;
	}
	
	@Override
	public void setType(int type){
		this.type = type;
	}
	@Override
	public Item copy(){
		return new FileItem(this.getItemPath());
	}

	@Override
	public boolean historyEquals(Item item) {
		if (item instanceof FileItem) {
			return item.getItemPath().equals(this.getItemPath());
		}
		return false;
	}
}
