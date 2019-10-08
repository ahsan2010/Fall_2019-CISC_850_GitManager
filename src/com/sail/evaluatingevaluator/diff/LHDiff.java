package com.sail.evaluatingevaluator.diff;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.io.IOUtils;

import com.sail.evaluatingevaluator.config.Config;
import com.sail.evaluatingevaluator.process.ProcessUtility;

public class LHDiff {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		LHDiff lhdiff = new LHDiff();
		long start = System.currentTimeMillis();
		
			try {
				String output = lhdiff.diff("/home/parvez/file1.txt", "/home/parvez/file2.txt");
				System.out.println("Output of lhdiff: "+output);
				LHDiffChangeDescriptor lhDiffChangeDescriptor =new LHDiffChangeDescriptor(output, "/home/parvez/file1.txt", "/home/parvez/file2.txt");
				lhDiffChangeDescriptor.print();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		long end = System.currentTimeMillis();
		System.out.println("Total Time Required: "+ ((end-start)/(60*1000.0f))+" m");
	}
	
	public String diff(String oldFile, String newFile) throws IOException, InterruptedException {
		//ProcessBuilder pb = new ProcessBuilder("java","-jar",Config.LHDIFF_PATH,oldFile,newFile);
		//Process process = pb.start();		
		//String output = ProcessUtility.output(process.getInputStream());
		//int errCode = process.waitFor();
		return "";
	}

}
