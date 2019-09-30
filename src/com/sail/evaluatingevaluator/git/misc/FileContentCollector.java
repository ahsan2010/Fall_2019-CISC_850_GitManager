package com.sail.evaluatingevaluator.git.misc;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.List;

import org.apache.commons.io.IOUtils;

import com.sail.evaluatingevaluator.process.ProcessUtility;

public class FileContentCollector {

	public static void main(String[] args) throws IOException, InterruptedException {
		// TODO Auto-generated method stub
		String output = FileContentCollector.getFileContent("7ff4a71abcdd55da1470b42c0f579df995d319ea","FrameworkInfoLoader\\src\\frameworkinfoloader\\Activator.java","E:\\codeCompletionEvaluation\\FrameworkInfoCollector");
		System.out.println(output);
	}
	public static String getFileContent(String commit,String fileName,String repositoryPath) throws IOException, InterruptedException{
		
		ProcessBuilder pb = new ProcessBuilder("cmd","/C","git","blame","-l",commit,fileName);
		pb.directory(new File(repositoryPath));
		Process process = pb.start();
		
		String output = ProcessUtility.output(process.getInputStream());
		String errorOutput = ProcessUtility.output(process.getErrorStream());
		System.out.println("Error: "+errorOutput);
		List<String> lineList = IOUtils.readLines(new StringReader(output));
		int errCode = process.waitFor();
	
		return output;
	}

}
