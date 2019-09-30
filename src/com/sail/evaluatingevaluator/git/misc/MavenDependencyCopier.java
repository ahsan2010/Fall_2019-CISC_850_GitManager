package com.sail.evaluatingevaluator.git.misc;


import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.sail.evaluatingevaluator.config.Config;


public class MavenDependencyCopier {
	private String repositoryPath;
	
	public MavenDependencyCopier(String _repositorypath) {
		this.repositoryPath = _repositorypath;
	}
	public void copy(String copyLocation) throws IOException, InterruptedException {
		ProcessBuilder pb = new ProcessBuilder("mvn","dependency:copy-dependencies","-DoutputDirectory="+copyLocation);
		pb.directory(new File(repositoryPath));
		Process process = pb.start();
		String output = output(process.getInputStream());
		String errorOutput = output(process.getErrorStream());
		int errCode = process.waitFor();
		System.out.println("Echo command executed, any errors? " + (errCode == 0 ? "No" : "Yes"));
	}
	private String output(InputStream inputStream) throws IOException {

		StringBuilder sb = new StringBuilder();
	    BufferedReader br = null;
		try {
		    br = new BufferedReader(new InputStreamReader(inputStream));
		    String line = null;
		    while ((line = br.readLine()) != null) {
		         sb.append(line + System.getProperty("line.separator"));
		    }

		} finally {
		    br.close();
		}
		return sb.toString();
	}
	
	public static void main(String args[]) {
		MavenDependencyCopier dependencyCopier = new MavenDependencyCopier(Config.REPOSITORY_PATH);
		try {
			dependencyCopier.copy(Config.EXTERNAL_DEPENDENCY_PATH);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}