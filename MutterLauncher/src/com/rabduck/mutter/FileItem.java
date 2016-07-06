package com.rabduck.mutter;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileItem implements Item {
	private Path path;

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

}
