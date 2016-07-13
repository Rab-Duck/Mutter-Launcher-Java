package com.rabduck.mutter;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FileCollector implements AppCollector {

	private static Logger logger = Logger.getLogger(com.rabduck.mutter.FileCollector.class.getName());

	public FileCollector(String dirPath) throws IOException {
		super();
		this.dirPath = Paths.get(dirPath);
		this.depth = Integer.MAX_VALUE;
		this.pathExt = ".*";
		extMatcher = null;
		if(!Files.exists(this.dirPath)){
			throw new IOException();
		}
	}
	public FileCollector(String dirPath, int depth, String pathExt) throws IOException {
		super();
		this.dirPath = Paths.get(dirPath);
		this.depth = depth;
		this.pathExt = pathExt;
		setExtMatcher(pathExt);
		if(!Files.exists(this.dirPath)){
			throw new IOException();
		}
	}
	private void setExtMatcher(String pathExt){
		if(pathExt == null || pathExt.equals("")){
			throw new IllegalArgumentException("pathExt is " + pathExt);
		}
		String [] extlist = pathExt.split(";");
		String glob = "glob:**{";
		for (int i = 0 ; i < extlist.length; i++) {
			glob += extlist[i];
			if(i < extlist.length-1){
				glob += ",";
			}
		}
		glob += "}";
		extMatcher = FileSystems.getDefault().getPathMatcher(glob);
		return;
	}

	private List<Item> items = null;
	private Path dirPath=null;
	private int depth=Integer.MAX_VALUE;
	private String pathExt;
	private PathMatcher extMatcher;
	

	@Override
	public void collect() {
		items = new ArrayList<>();
        try {
			Files.walkFileTree(dirPath, EnumSet.noneOf(FileVisitOption.class), depth,
					new SimpleFileVisitor<Path>() {
						@Override
						public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
							if(attrs.isRegularFile() && Files.isReadable(file)){
								if(extMatcher == null || extMatcher.matches(file)){
									logger.log(Level.FINEST, file.toAbsolutePath().toString());
									items.add(new FileItem(file));
								}
							}
							return FileVisitResult.CONTINUE;
						}

						@Override
						public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
							logger.log(Level.FINER, "Failed:" + file.toAbsolutePath());
							return FileVisitResult.SKIP_SUBTREE;
						}
			});
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public List<Item> getItemList() {
		return items;
	}

}
