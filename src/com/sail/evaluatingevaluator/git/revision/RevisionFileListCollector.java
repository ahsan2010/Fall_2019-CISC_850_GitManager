package com.sail.evaluatingevaluator.git.revision;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;

import com.sail.evaluatingevaluator.config.Config;
import com.sail.evaluatingevaluator.process.ProcessUtility;


/*
 * The objective is to collect all the files in a particular revision. Commit represents a revision
 */
public class RevisionFileListCollector {

	public static void main(String[] args) {
		/*// TODO Auto-generated method stub
		try {
			ArrayList<String> fileList = RevisionFileListCollector.getFileList("42cd79f0163a0de87c0dc02424b2d019c50e7759",Config.REPOSITORY_PATH);
			fileList.stream().forEach((String s)->{
				System.out.println("File: "+s);
			});
		} catch (IOException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
*/	}
	//repository path points to the cloned repository
	public static ArrayList<String> getFileList(String SHA, String repositoryPath) throws IOException, InterruptedException {
		ProcessBuilder pb = new ProcessBuilder("git","ls-tree","-r","--name-only",SHA);
		pb.directory(new File(repositoryPath));
		Process process = pb.start();		
		String output = ProcessUtility.output(process.getInputStream());
		int errCode = process.waitFor();
		return (parseFileList(output));
	}
	private static ArrayList<String> parseFileList(String output) throws IOException {
		ArrayList<String> fileList = new ArrayList();
		//step-1:convert the output into list of lines 
		List<String> lineList = IOUtils.readLines(new StringReader(output));
		for(String line:lineList) {
			if(line.length()>0 && line.matches("\\s+")==false) {
				fileList.add(line.trim());
			}
		}
		return fileList;
	}
	
}
