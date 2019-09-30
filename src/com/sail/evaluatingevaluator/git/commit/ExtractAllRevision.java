package com.sail.evaluatingevaluator.git.commit;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.csvreader.CsvReader;
import com.csvreader.CsvWriter;
import com.sail.evaluatingevaluator.git.revision.FileChangePair;
import com.sail.evaluatingevaluator.git.revision.FileChangeType;
import com.sail.evaluatingevaluator.git.revision.RevisionFileChangeDescriptor;
import com.sail.evaluatingevaluator.git.revision.RevisionFileListCollector;
import com.sail.evaluatingevaluator.process.ProcessUtility;

public class ExtractAllRevision {

	
	private String ROOT  = "/Users/mdahasanuzzaman/Documents/Queens_Phd/Fall_2019_Courses/CISC-850/Assignments/";
	//private String ROOT  = "/home/local/SAIL/ahsan/Documents/Fall_2019/CISC-850/Assignment/";
	private String TAR_TEMP_FILE = ROOT + "TarTemp/";
	private String COMMIT_MESSAGE_FILE = ROOT+"/Result/commit_id_date.csv";
	private String REVISION_FILE = ROOT + "GitRevisions/";
	private String REPOSITORY_PATH = ROOT + "hive/";
	ArrayList<Commit>commitList = new ArrayList<Commit>();
	public  DateTimeFormatter formatterWithHyphen = DateTimeFormat.forPattern("yyyy-MM-dd");
	
	private String    FILE_CHANGE_FILE = ROOT+"Result/revision_change.csv";
	private CsvWriter writerChangeInfo = null;	
	
	public ExtractAllRevision() {
		try {
			writerChangeInfo = new CsvWriter(FILE_CHANGE_FILE);
			writerChangeInfo.write("Commit_Id");
			writerChangeInfo.write("Commit_Date");
			writerChangeInfo.write("Changed_File");
			writerChangeInfo.write("Change_Type");
			writerChangeInfo.endRecord();
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public void readAllCommitInfo() throws Exception{
		CsvReader reader = new CsvReader(COMMIT_MESSAGE_FILE);
		reader.setSafetySwitch(false);
		reader.readHeaders();
		int totalCommit = 0;
		while(reader.readRecord()) {
			String commitId = reader.get("Commit_Id");
			String commitDate = reader.get("Date");
			Commit commit = new Commit();
			commit.setDate(commitDate);
			commit.setSha(commitId);
			commitList.add(commit);
			System.out.println("Commit = " + (++totalCommit) + " " + commit.getDate());
		}
		commitList.sort(new Comparator<Commit>() {
			@Override
			public int compare(Commit o1, Commit o2) {
				DateTime o1Date = formatterWithHyphen.parseDateTime(o1.getDate());
				DateTime o2Date = formatterWithHyphen.parseDateTime(o2.getDate());		
				return o1Date.compareTo(o2Date);
			}
		});
	}
	public String listToString(String words[]) {
		String result = "";
		for(String word : words) {
			result += word + " ";
		}
		result = result.trim();
		
		return result;
	}
	
	public RevisionFileChangeDescriptor parse(String output, int commitIndex, List<Commit> commitList) throws IOException {
		//step-1:convert the output into list of lines 
		List<String> lineList = IOUtils.readLines(new StringReader(output));
		List<FileChangePair> fileChangePairList = new ArrayList();
		for(String line:lineList) {
			//System.out.println(line);
			if(line.length()>0 && line.matches("\\s+")==false){
				String split[] = line.split("\\s+");
				
				if(split[0].equals("A") && split[1].trim().endsWith(".java")) {
					FileChangePair fileChangePair = new FileChangePair(FileChangeType.ADDED);
					fileChangePair.setNewFile(split[1]);
					fileChangePairList.add(fileChangePair);
				}
				else if(split[0].equals("D") && split[1].trim().endsWith(".java")) {
					FileChangePair fileChangePair = new FileChangePair(FileChangeType.DELETED);
					fileChangePair.setOldFile(split[1]);
					fileChangePairList.add(fileChangePair);
				}
				else if(split[0].equals("M") && split[1].trim().endsWith(".java")) {
					FileChangePair fileChangePair = new FileChangePair(FileChangeType.CHANGED);		
					fileChangePair.setOldFile(split[1]);
					fileChangePair.setNewFile(split[1]);
					fileChangePairList.add(fileChangePair);
				}
				else if(split[0].startsWith("R") && split[1].trim().endsWith(".java")) {
					FileChangePair fileChangePair = new FileChangePair(FileChangeType.RENAMED);
					fileChangePair.setOldFile(split[1]);		
					fileChangePair.setNewFile(split[2]);
					fileChangePairList.add(fileChangePair);
				}
				else if(split[0].startsWith("C") && split[1].trim().endsWith(".java")) {
					FileChangePair fileChangePair = new FileChangePair(FileChangeType.COPIED);
					fileChangePair.setOldFile(split[1]);
					fileChangePair.setNewFile(split[2]);
					fileChangePairList.add(fileChangePair);
				}
				else {
					//System.out.println(line);
					//what is happening here?
					//throw new RuntimeException("Exception in parsing file change information");
				}
			}
		}
		String oldCommitSHA = commitList.get(commitIndex-1).getSha();
		String newCommitSHA = commitList.get(commitIndex).getSha();
	
		RevisionFileChangeDescriptor revisionFileChangeDescriptor = new RevisionFileChangeDescriptor(fileChangePairList,oldCommitSHA,newCommitSHA);
		return revisionFileChangeDescriptor;
	}
	
	public static ArrayList<String> getAllFilesPathFromDirectoryRecursively(String dirName, ArrayList<String> filesList) {

		try {
			File directory = new File(dirName);

			File[] fList = directory.listFiles();

			for (File file : fList) {
				if (file.isFile()) {
					filesList.add(file.getAbsolutePath());

				} else if (file.isDirectory()) {
					// System.out.println(file.getAbsolutePath());
					getAllFilesPathFromDirectoryRecursively(file.getAbsolutePath(), filesList);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return filesList;

	}
	
	public void commitMessageInformationExtraction(RevisionFileChangeDescriptor revisionFileChangeDescriptor,int commitIndex, List<Commit> commitList, boolean flagFilterChanged) throws Exception{
		
		Commit commit= commitList.get(commitIndex);
		
		String archiveFilePath = TAR_TEMP_FILE + commit.getSha() + ".tar.gz" ;
		
		//now checkout the revision in this folder
		ProcessBuilder pb = new ProcessBuilder("/bin/sh", "-c", "git archive "+ commit.getSha()+" | gzip > "+archiveFilePath);
		//System.out.println(pb.command().toString().replace(",", ""));
		
		//ProcessBuilder pb = new ProcessBuilder("git","archive","--format","tar","--output",archiveFilePath,commitList.get(i).getSha());
		pb.directory(new File(REPOSITORY_PATH));
		Process process = pb.start();		
		String output = ProcessUtility.output(process.getErrorStream());
		
		//System.out.println(output);
		int errCode = process.waitFor();
		if(errCode!=0) {
			System.out.println();
		}
	
		String outputFolderName = REVISION_FILE + commit.getSha() + "_" + commit.getDate();
		
		ProcessBuilder pbMake = new ProcessBuilder("mkdir",outputFolderName);
		Process processMake = pbMake.start();
		ProcessUtility.output(processMake.getErrorStream());
		processMake.waitFor();
		
		ProcessBuilder pbExtract = new ProcessBuilder("tar", "-xvzf",archiveFilePath,"-C",outputFolderName);
		Process processExtract= pbExtract.start();
		String outputStream = ProcessUtility.output(processExtract.getErrorStream());
		processExtract.waitFor();
		
		//find . -type f ! -name '*.txt' -delete
		ProcessBuilder pbFilterJava = new ProcessBuilder("find", outputFolderName,"-type","f","!","-name","*.java","-delete");
		Process processFilterJava= pbFilterJava.start();
		String errorJavaFilter = ProcessUtility.output(processFilterJava.getErrorStream());
		processFilterJava.waitFor();
		
		
		ProcessBuilder pbRemove = new ProcessBuilder("rm", "-f", archiveFilePath);
		Process processRemove = pbRemove.start();
		processRemove.waitFor();
		
		///Users/mdahasanuzzaman/Documents/Queens_Phd/Fall_2019_Courses/CISC-850/Assignments/hive/ql/src/java/org/apache/hadoop/hive/ql/exec/FileSinkOperator.java

		///Users/mdahasanuzzaman/Documents/Queens_Phd/Fall_2019_Courses/CISC-850/Assignments/GitRevisions/be7e270c27d84362f44b8e9a079f56dd046d6ffd/ql/src/java/org/apache/hadoop/hive/ql/exec/FileSinkOperator.java

		
		if(flagFilterChanged) {
			ArrayList<String> filesList = new ArrayList<String>();
			getAllFilesPathFromDirectoryRecursively(outputFolderName, filesList);
			
			Set<String> filteredFiles = new HashSet<String>();
			
			for(FileChangePair fileChangePair:revisionFileChangeDescriptor.getFilesChangePairList()) {
				String fileLocation = fileChangePair.getNewFile();
				//System.out.println(fileLocation);
				filteredFiles.add(fileLocation);
			}
			for(String filePath : filesList) {
				String directoryFilePath = filePath.substring((outputFolderName).length()+1).trim();
				if(!filteredFiles.contains(directoryFilePath)){
					ProcessBuilder pbRemoveFile = new ProcessBuilder("rm", "-f", filePath);
					Process processRemoveFile = pbRemoveFile.start();
					processRemoveFile.waitFor();
					//System.out.println(directoryFilePath);
				}
				
			}
		}
	}
	
	
	public void extractRevisionFromCommit() throws Exception{
		/*
		//git archive 45c09bfe58c37bbf7965af25bdd4fa5c37c0908f | gzip >../45c09bfe58c37bbf7965af25bdd4fa5c37c0908f.tar.gz
		Commit cm = new Commit();
		Commit cm2 = new Commit();
		cm.setSha("65da10218eaac25c47a69f6965002a1ac4bfcac3");
		cm2.setSha("be7e270c27d84362f44b8e9a079f56dd046d6ffd");
		commitList.add(cm);
		commitList.add(cm2);*/
		
		
		readAllCommitInfo();
		
		ArrayList<String> fileList = RevisionFileListCollector.getFileList(commitList.get(0).getSha(),REPOSITORY_PATH);
		ArrayList<FileChangePair> fileChangePairList = new ArrayList();
		for(String file:fileList) {
			FileChangePair fileChangePair = new FileChangePair(FileChangeType.ADDED);
			fileChangePair.setNewFile(file);
			fileChangePair.setOldFile(null);
			fileChangePairList.add(fileChangePair);
		}
		
		String newCommitSHA = commitList.get(0).getSha();
		String oldCommitSHA = null;
		RevisionFileChangeDescriptor firstRevisionFileChangeDescriptor = new RevisionFileChangeDescriptor(fileChangePairList,oldCommitSHA,newCommitSHA);
		
		commitMessageInformationExtraction(null, 0, commitList, false);
		for(int commitIndex = 1 ; commitIndex < 1000; commitIndex++) {
			
			Commit previousCommit 		= commitList.get(commitIndex-1);
			Commit currentCommit 		= commitList.get(commitIndex);
			
			String previousCommitSHA 	= previousCommit.getSha();
			String currentCommitSHA 	= currentCommit.getSha();
						
			ProcessBuilder pb = new ProcessBuilder("git","diff","-C","-M","-w","--no-color","--name-status",previousCommitSHA,currentCommitSHA);
			//System.out.println("Command: "+"git"+" diff"+" -C"+" -M"+" -w"+" --no-color"+" --name-status "+previousCommitSHA+" "+currentCommitSHA);
			pb.directory(new File(REPOSITORY_PATH));
			Process process = pb.start();
			String output = ProcessUtility.output(process.getInputStream());
			int errCode = process.waitFor();
			
			RevisionFileChangeDescriptor revisionFileChangeDescriptor = this.parse(output, commitIndex, commitList);			
			System.out.println("Finish Commit = " + (commitIndex + 1));
			//revisionFileChangeDescriptor.print();
			writeCommitFileChangeInfo(revisionFileChangeDescriptor,currentCommit);
			commitMessageInformationExtraction(revisionFileChangeDescriptor, commitIndex, commitList, true);
		}
		writerChangeInfo.close();
		
	}
	
	public void writeCommitFileChangeInfo(RevisionFileChangeDescriptor revisionFileChangeDescriptor, Commit currentCommit) throws Exception{
		try {
			for(FileChangePair fileChangePair:revisionFileChangeDescriptor.getFilesChangePairList()) {
				writerChangeInfo.write(currentCommit.getSha());
				writerChangeInfo.write(currentCommit.getDate());
				writerChangeInfo.write(fileChangePair.getFileChangeType().toString());
				writerChangeInfo.write(fileChangePair.getNewFile());
				writerChangeInfo.endRecord();
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) throws Exception{
		ExtractAllRevision ob = new ExtractAllRevision();
		//ob.readAllCommitInfo();
		ob.extractRevisionFromCommit();
		System.out.println("Program finishes successfully");
	}
}
