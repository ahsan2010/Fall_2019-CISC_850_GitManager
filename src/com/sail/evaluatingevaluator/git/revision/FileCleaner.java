package com.sail.evaluatingevaluator.git.revision;

import java.io.File;

import com.sail.evaluatingevaluator.config.Config;

//This class responsible for deleting all files except Java 
public class FileCleaner {

	private File root;
	public FileCleaner( File _root) {
		this.root = _root;
	}
	public void run() {
		File listFiles[] = root.listFiles();
		for(int i=0;i<listFiles.length;i++) {
			System.out.println("Progress: "+i+"/"+listFiles.length);
			this.clean(listFiles[i]);
		}
	}
	private void clean(File file) {
		if(file.isFile() && file.getName().endsWith(".java")==false) {
			file.delete();
		}
		else if(file.isDirectory()){
			File children[] = file.listFiles();
			for(File child:children) {
				this.clean(child);
			}
		}
	}
	public static void main(String args[]) {
		FileCleaner fileCleaner = new FileCleaner(new File(Config.REPOSITORY_PATH));
		fileCleaner.run();
	}
	
}
