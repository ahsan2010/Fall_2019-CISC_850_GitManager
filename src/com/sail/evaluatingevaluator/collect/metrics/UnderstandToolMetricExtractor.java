package com.sail.evaluatingevaluator.collect.metrics;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.csvreader.CsvReader;
import com.sail.evaluatingevaluator.process.ProcessUtility;
import com.sail.model.CommitFileModel;
import com.sail.model.CommitModel;

public class UnderstandToolMetricExtractor {

	// String ROOT =
		// "home/ahsan/Documents/Queens_PHD/Courses/Fall_2019/CISC_850/Assignment/Assignment";
		String COMMIT_INFO_PATH = "/Users/mdahasanuzzaman/Documents/Queens_Phd/Fall_2019_Courses/CISC-850/Assignments/Result/revision_change.csv";
		String COMMIT_BUG_INFO = "/Users/mdahasanuzzaman/Documents/Queens_Phd/Fall_2019_Courses/CISC-850/Assignments/Result/commit_bug_info.csv";

		String OUTPUT_CHANGE_BUG_COMMIIT_INFO = "/Users/mdahasanuzzaman/Documents/Queens_Phd/Fall_2019_Courses/CISC-850/Assignments/Result/output.csv";

		ArrayList<CommitModel> commitList = new ArrayList<CommitModel>();

		public DateTimeFormatter formatterWithHyphen = DateTimeFormat.forPattern("yyyy-MM-dd");

		Set<String> bugFixCommitList = new HashSet<String>();

		public String REVISION_PATH = "/Users/mdahasanuzzaman/Documents/Queens_Phd/Fall_2019_Courses/CISC-850/Assignments/ProjectFolder/";

		public String REPOSITORY = "/Users/mdahasanuzzaman/Documents/Queens_Phd/Fall_2019_Courses/CISC-850/Assignments/hive/";
	
		public void commitBugInfo() throws Exception {
		CsvReader reader = new CsvReader(COMMIT_BUG_INFO);
		reader.readHeaders();

		while (reader.readRecord()) {
			String commitId = reader.get("Commit_ID");
			String commitDate = reader.get("Date");
			String isBug = reader.get("Is_Bug");

			if (isBug.equals("True")) {
				bugFixCommitList.add(commitId);
			}

		}
		System.out.println(
				"Finish reading bugfix commit data. Total bugFixed Commit = [" + bugFixCommitList.size() + "]");
	}

	public void readCommitInformation() throws Exception {
		CsvReader reader = new CsvReader(COMMIT_INFO_PATH);
		reader.readHeaders();

		String previous = "";
		CommitModel cm = null;
		while (reader.readRecord()) {
			String commitId = reader.get("Commit_Id");
			String commitDate = reader.get("Commit_Date");
			String commitFileName = reader.get("Changed_File");
			String commitChangeType = reader.get("Change_Type");

			if (!commitChangeType.equals("CHANGED")) {
				continue;
			}
			String subSystem = commitFileName.substring(0, commitFileName.lastIndexOf("/"));

			if (!commitId.equals(previous)) {
				if (previous.trim().length() <= 0) {
				} else {
					commitList.add(cm);
				}
				cm = new CommitModel();
				// System.out.println(commitId);
			}

			cm.setCommitId(commitId);
			cm.setCommitDate(commitDate);

			CommitFileModel fm = new CommitFileModel();
			fm.setChangeType(commitChangeType);
			fm.setFileName(commitFileName);
			fm.setSubSystem(subSystem);
			cm.getChangedFileList().add(fm);
			previous = commitId;
		}
		commitList.sort(new Comparator<CommitModel>() {
			@Override
			public int compare(CommitModel o1, CommitModel o2) {
				DateTime o1Date = formatterWithHyphen.parseDateTime(o1.getCommitDate());
				DateTime o2Date = formatterWithHyphen.parseDateTime(o2.getCommitDate());
				return o1Date.compareTo(o2Date);
			}
		});

		System.out.println("Total number of commits = [" + commitList.size() + "] ");
		System.out.println("First Commit : " + commitList.get(0).getCommitDate());
		System.out.println("Last Commit : " + commitList.get(commitList.size() - 1).getCommitDate());
	}
	
	public static String getContent(String commit_sha, String filePath, String repositoryPath) throws IOException, InterruptedException {
		ProcessBuilder pb = new ProcessBuilder("git","show",commit_sha+":"+filePath);
		pb.directory(new File(repositoryPath));
		Process process = pb.start();
		String output = ProcessUtility.output(process.getInputStream());
		int errCode = process.waitFor();
		return output;
	}
	
	public void calculateUnderstandMetrics() throws Exception{
		long start = System.currentTimeMillis();
		for (int i =  commitList.size() - 1; i > 0; i--) {
			CommitModel previousCommit = commitList.get(i - 1);
			CommitModel currentCommit = commitList.get(i);
			/*ProcessBuilder pbCheckout = new ProcessBuilder("git", "checkout", currentCommit.getCommitId());
			pbCheckout.directory(new File(REPOSITORY));
			Process processCheckout = pbCheckout.start();
			String ouputCheckout = ProcessUtility.output(processCheckout.getErrorStream());*/
			
			for (CommitFileModel cm : currentCommit.getChangedFileList()) {
				//System.out.println(cm.getFileName());
				String output = getContent(currentCommit.getCommitId(), cm.getFileName(), REPOSITORY);
				//System.out.println(output);
				FileUtils.writeStringToFile(new File(REVISION_PATH+currentCommit.getCommitId()+File.separator+cm.getFileName()), output);
			}
			System.out.println("Finish Commit: " + i);
			
		}
	}
	
	public void checkUnderstandCommnand() throws Exception{
		
		/*List<String> createDB = new ArrayList<String>();
		createDB.add("./und");
		createDB.add("create");
		createDB.add("-languages");
		createDB.add("java");
		createDB.add("/Users/mdahasanuzzaman/Documents/Queens_Phd/Fall_2019_Courses/ahsan_project.udb");
		//createDB.add("exit");
		
		
		ProcessBuilder pbCreateDB = new ProcessBuilder(createDB);
		pbCreateDB.directory(new File("/Users/mdahasanuzzaman/Documents/Queens_Phd/Fall_2019_Courses/CISC-850/UnderstandContent/MacOS/"));
		
		Process processCreateDB = pbCreateDB.start();
		//processCreateDB.wait();*/
		
		/*
		 * ./und -db /Users/mdahasanuzzaman/Documents/Queens_Phd/Fall_2019_Courses/ahsan_project.udb create -languages java add /Users/mdahasanuzzaman/Documents/Queens_Phd/Fall_2019_Courses/CISC-850/Assignments/ProjectFolder/6ca839704c6c1c8f7f84a80373d0f81062b28f37/ settings -metrics all -metricsOutputFile /Users/mdahasanuzzaman/Documents/Queens_Phd/Fall_2019_Courses/myMetrics.csv  analyze metrics
		 */
		
		List<String> commands = new ArrayList<String>();
		commands.add("./und");
		commands.add("-db");
		commands.add("/Users/mdahasanuzzaman/Documents/Queens_Phd/Fall_2019_Courses/ahsan_project.udb");
		commands.add("create");
		commands.add("-languages");
		commands.add("java");
		commands.add("add");
		commands.add("/Users/mdahasanuzzaman/Documents/Queens_Phd/Fall_2019_Courses/CISC-850/Assignments/ProjectFolder/6ca839704c6c1c8f7f84a80373d0f81062b28f37/");
		commands.add("settings");
		commands.add("-metrics");
		commands.add("all");
		commands.add("-metricsOutputFile");
		commands.add("/Users/mdahasanuzzaman/Documents/Queens_Phd/Fall_2019_Courses/myMetrics.csv");
		commands.add("analyze");
		commands.add("metrics");
		//commands.add("quit");
		
		ProcessBuilder pbCheckout = new ProcessBuilder(commands).redirectErrorStream(true);
		pbCheckout.directory(new File("/Users/mdahasanuzzaman/Documents/Queens_Phd/Fall_2019_Courses/CISC-850/UnderstandContent/MacOS/"));
		
		Process processCheckout = pbCheckout.start();
		
		
		String ouputCheckout = ProcessUtility.output(processCheckout.getErrorStream());
		String inputCheckout = ProcessUtility.output(processCheckout.getInputStream());
		
		//System.out.println("I " + inputCheckout);
		System.out.println("E "+ouputCheckout);
		
		
	}
	
	public static void main(String[] args) throws Exception{
		UnderstandToolMetricExtractor ob = new UnderstandToolMetricExtractor();
		//ob.readCommitInformation();
		//ob.commitBugInfo();
		//ob.calculateUnderstandMetrics();
		ob.checkUnderstandCommnand();
		System.out.println("Program finishes sucessfully");
	}
	
}
