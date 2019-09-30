package com.sail.evaluatingevaluator.git.misc;

import java.io.File;

import com.sail.evaluatingevaluator.config.Config;

// The goal is to preserve only a particular set of files
public class Cleaner {

	public static String[] preserve_extensions = {".java"};
	
	//file should be a file, not a directory
	public static boolean isPreseve(File file) {
		for(String extension:preserve_extensions) {
			if(file.getName().endsWith(extension))
				return true;
		}
		return false;
	}
	
	//file can be a file or directory. Its a recursive call
	public static void clean(File file) {
		if(file.isFile() && !isPreseve(file)) {
			file.delete();
		}
		else if(file.isDirectory()) {
			for(File child:file.listFiles()) {
				Cleaner.clean(child);
			}
		}
	}
	
	//Here, file should be a directory. we assume that each sub-directory under root represents a revision 
	public static void cleanRevisions(File root) {
		if(root.isDirectory()) {
			for(int i=0;i<root.listFiles().length;i++) {
				File file = root.listFiles()[i];
				System.out.println("Clean: "+i+"/"+file.listFiles().length);
				Cleaner.clean(file);
			}
		}
		else {
			System.out.println("Error!!! Not a directory");
		}
	}
	public static void main(String[] args) {
		// Clean the repository after you have finished resolving type binding and collecting the method calls 
		Cleaner.cleanRevisions(new File(Config.REPOSITORY_REVISION_PATH));
	}
}
