package com.sail.evaluatingevaluator.git.misc;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;

import com.sail.evaluatingevaluator.config.Config;
import com.sail.evaluatingevaluator.process.ProcessUtility;

//extract all zip files inside a given directory with the same name of the zip file
public class ZipFileExtractor {

	public static void run(File file) throws IOException, InterruptedException {
		System.out.println("File: " + file.getAbsolutePath());

		// collect the zip files first
		List<File> zipFileList = new ArrayList();
		for (File child : file.listFiles()) {
			if (child.getName().endsWith(".zip")) {
				zipFileList.add(child);
			}
		}

		// extract zip files in that particular directory
		for (int i = 0; i < zipFileList.size(); i++) {

			File zipFile = zipFileList.get(i);

			System.out.println("Completed: " + i + "/" + zipFileList.size() + " File: " + file.getAbsolutePath());

			{
				String outputPath = zipFile.getParent() + File.separator
						+ zipFile.getName().substring(0, zipFile.getName().length() - ".zip".length());
				ProcessBuilder pb = new ProcessBuilder("unzip", "-d", outputPath, zipFile.getAbsolutePath());
				Process process = pb.start();

				String output = ProcessUtility.output(process.getInputStream());
				String errorOutput = ProcessUtility.output(process.getErrorStream());
				List<String> lineList = IOUtils.readLines(new StringReader(output));
				int errCode = process.waitFor();
			}
		}
	}

	public static void extract(File zipFile, String outputPath) throws IOException, InterruptedException {
		System.out.println("zipfile: " + zipFile.getAbsolutePath() + " Unzip File: " + outputPath);
		ProcessBuilder pb = new ProcessBuilder("unzip", "-d", outputPath, zipFile.getAbsolutePath());
		Process process = pb.start();

		String output = ProcessUtility.output(process.getInputStream());
		String errorOutput = ProcessUtility.output(process.getErrorStream());
		List<String> lineList = IOUtils.readLines(new StringReader(output));
		int errCode = process.waitFor();
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
			ZipFileExtractor.run(new File(Config.REPOSITORY_REVISION_PATH));
		} catch (IOException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
