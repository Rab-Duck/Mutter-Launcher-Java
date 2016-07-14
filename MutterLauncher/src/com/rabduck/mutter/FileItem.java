package com.rabduck.mutter;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.swing.Icon;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileSystemView;

public class FileItem implements Item {
	private Path path;
	private Icon icon;
	private final boolean bIconCached = false;

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
		return path.toAbsolutePath().toString();
	}

	@Override
	public String getItemName() {
		// TODO Auto-generated method stub
		return path.getFileName().toString();
	}

	@Override
	public void setItemPath(String fullPath) {
		// TODO Auto-generated method stub
		setItemPath(Paths.get(fullPath));
	}

	public void setItemPath(Path fullPath) {
		// TODO Auto-generated method stub
		this.path = fullPath;
		if(bIconCached){
			icon = FileSystemView.getFileSystemView().getSystemIcon(new File(getItemPath()));
		}
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
		
		return FileSystemView.getFileSystemView().getSystemIcon(new File(getItemPath()));
		// for OS X ?
        // JFileChooser jfc = new JFileChooser();
		// return jfc.getUI().getFileView(jfc).getIcon(new File(getItemPath()));
	}

}
