package com.sail.evaluatingevaluator.git.commit;

import java.util.HashMap;
import java.util.Map;

import com.csvreader.CsvReader;
import com.csvreader.CsvWriter;

public class RevisionFileLookUp {
	
	String COMMIT_INFO_PATH = "/home/ahsan/Documents/Queens_PHD/Courses/Fall_2019/CISC_850/Assignment/Assignment/Result/revision_change.csv";
	String COMMIT_OUT_PATH = "/home/ahsan/Documents/Queens_PHD/Courses/Fall_2019/CISC_850/Assignment/Assignment/Result/revision_change_Lookup.csv";
	
	public String firstCommit = "65da10218eaac25c47a69f6965002a1ac4bfcac3";
	public String firstCommitDate = "2008-09-02";
	
	//First Commit 65da10218eaac25c47a69f6965002a1ac4bfcac3, 2008-09-02
	
	Map<String,String> commitMapper = new HashMap<String,String>();
	
	public void readCommitInfo() throws Exception{
		CsvReader reader = new CsvReader(COMMIT_INFO_PATH);
		CsvWriter writer = new CsvWriter(COMMIT_OUT_PATH);
		
		writer.write("Commit_Id");
		writer.write("Commit_Date");
		writer.write("Change_Type");
		writer.write("Changed_File");
		writer.write("Lookup_Commit");
		writer.write("Lookup_Commit_Date");
		writer.endRecord();
		
		reader.readHeaders();
		
		while(reader.readRecord()){
			String commitId = reader.get("Commit_Id");
			String commitDate = reader.get("Commit_Date");
			String commitFileName = reader.get("Changed_File");
			String commitChangeType = reader.get("Change_Type");
			String commitValue = commitId + "_" + commitDate;
			String targetLookUp = "";
			
			if(commitChangeType.equals("DELETED")){
				continue;
			}
			
			if(!commitMapper.containsKey(commitFileName)){
				commitMapper.put(commitFileName,commitValue);
				targetLookUp = firstCommit + "_" + firstCommitDate;
			}else{
				targetLookUp = commitMapper.get(commitFileName);
			}
			
			if(!commitChangeType.equals("CHANGED")){
				continue;
			}
			
			writer.write(commitId);
			writer.write(commitDate);
			writer.write(commitChangeType);
			writer.write(commitFileName);
			writer.write(targetLookUp.split("_")[0]);
			writer.write(targetLookUp.split("_")[1]);
			writer.endRecord();
		}
		writer.close();
	}
	
	
	public static void main(String[] args) throws Exception{
		RevisionFileLookUp ob = new RevisionFileLookUp();
		ob.readCommitInfo();
		System.out.println("Program finishes successfully");
	}
}
