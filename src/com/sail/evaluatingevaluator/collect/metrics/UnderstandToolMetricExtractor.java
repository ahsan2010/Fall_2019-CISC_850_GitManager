package com.sail.evaluatingevaluator.collect.metrics;

import java.io.File;
import java.io.IOException;
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
import com.csvreader.CsvWriter;
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

	public String REVISION_PATH = "/Users/mdahasanuzzaman/Documents/Queens_Phd/Fall_2019_Courses/CISC-850/Assignments/TwoYearsCommitData/";

	public String REPOSITORY = "/Users/mdahasanuzzaman/Documents/Queens_Phd/Fall_2019_Courses/CISC-850/Assignments/hive/";

	DateTime releaseTimeVersion1 = formatterWithHyphen.parseDateTime("2013-02-02");
	DateTime releaseTimeVersionFrom = formatterWithHyphen.parseDateTime("2012-02-02");

	DateTime releaseTimeVersion2 = formatterWithHyphen.parseDateTime("2015-02-15");
	DateTime releaseTimeVersion2From = formatterWithHyphen.parseDateTime("2014-02-14");

	public String twoYearsCommit = "/Users/mdahasanuzzaman/Documents/Queens_Phd/Fall_2019_Courses/CISC-850/Assignments/commit_two_years_files.csv";
	
	public String understandProjectPath = "/Users/mdahasanuzzaman/Documents/Queens_Phd/Fall_2019_Courses/CISC-850/Assignments/UnderstandProjects2/";
	
	public String understandMetricsPath = "/Users/mdahasanuzzaman/Documents/Queens_Phd/Fall_2019_Courses/CISC-850/Assignments/UnderstandMetricsPath2/";
	public void commitBugInfo() throws Exception {

		CsvReader reader = new CsvReader(COMMIT_BUG_INFO);
		reader.readHeaders();

		CsvWriter writer = new CsvWriter(
				"/Users/mdahasanuzzaman/Documents/Queens_Phd/Fall_2019_Courses/CISC-850/Assignments/Result/commit_for_two_years.csv");
		writer.write("Commit_Id");
		writer.write("Commit_Date");
		writer.write("Version_1.0.0");
		writer.endRecord();

		while (reader.readRecord()) {
			String commitId = reader.get("Commit_ID");
			String commitDate = reader.get("Date");
			String isBug = reader.get("Is_Bug");

			DateTime targetDate = formatterWithHyphen.parseDateTime(commitDate);

			if (targetDate.isAfter(releaseTimeVersionFrom) && targetDate.isBefore(releaseTimeVersion1)) {
				writer.write(commitId);
				writer.write(commitDate);
				writer.write("V_1.0.0");
				writer.endRecord();
			}
			if (targetDate.isAfter(releaseTimeVersion2From) && targetDate.isBefore(releaseTimeVersion2)) {
				writer.write(commitId);
				writer.write(commitDate);
				writer.write("V_2.0.0");
				writer.endRecord();
			}

			if (isBug.equals("True")) {
				bugFixCommitList.add(commitId);
			}

		}
		writer.close();
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

	public static String getContent(String commit_sha, String filePath, String repositoryPath)
			throws IOException, InterruptedException {
		ProcessBuilder pb = new ProcessBuilder("git", "show", commit_sha + ":" + filePath);
		//pb.redirectErrorStream(true);
	
		pb.directory(new File(repositoryPath));
		Process process = pb.start();
		String output = ProcessUtility.output(process.getInputStream());
		int errCode = process.waitFor();
		return output;
	}

	public void calculateUnderstandMetrics() throws Exception {
		long start = System.currentTimeMillis();
		for (int i = commitList.size() - 1; i > 0; i--) {
			CommitModel previousCommit = commitList.get(i - 1);
			CommitModel currentCommit = commitList.get(i);
			
			for (CommitFileModel cm : currentCommit.getChangedFileList()) {
				// System.out.println(cm.getFileName());
				String output = getContent(currentCommit.getCommitId(), cm.getFileName(), REPOSITORY);
				// System.out.println(output);
				FileUtils.writeStringToFile(
						new File(REVISION_PATH + currentCommit.getCommitId() + File.separator + cm.getFileName()),
						output);
			}
			System.out.println("Finish Commit: " + i);

		}
	}

	public void checkUnderstandCommnand(String commitId) throws Exception {

		
		List<String> commands = new ArrayList<String>();
		commands.add("./und");
		commands.add("-db");
		commands.add(understandProjectPath + commitId +".udb");
		commands.add("create");
		commands.add("-languages");
		commands.add("java");
		commands.add("add");
		commands.add(REVISION_PATH + commitId);
		commands.add("settings");
		commands.add("-metrics");
		commands.add("all");
		commands.add("-metricsOutputFile");
		commands.add(understandMetricsPath + commitId + ".csv");
		commands.add("analyze");
		commands.add("metrics");
		// commands.add("quit");

		ProcessBuilder pbCheckout = new ProcessBuilder(commands).redirectErrorStream(true);
		pbCheckout.directory(new File(
				"/Users/mdahasanuzzaman/Documents/Queens_Phd/Fall_2019_Courses/CISC-850/UnderstandContent/MacOS/"));

		Process processCheckout = pbCheckout.start();

		String ouputCheckout = ProcessUtility.output(processCheckout.getErrorStream());
		String inputCheckout = ProcessUtility.output(processCheckout.getInputStream());

		//processCheckout.wait();
		
		System.out.println("I " + inputCheckout);
		System.out.println("Finish Command");

	}

	
	public Set<String> readRequiredCommitIdsForTwoYears() throws Exception{
		Set<String> commitIdList = new HashSet<String>();
		CsvReader reader = new CsvReader(twoYearsCommit);
		reader.readHeaders();
		while(reader.readRecord()) {
			String commitId = reader.get("CommitId");
			commitIdList.add(commitId);
		}
		return commitIdList;
	}
	
	public void generateFolderForFiles(CommitModel currentCommit) throws Exception{
		for (CommitFileModel cm : currentCommit.getChangedFileList()) {
			// System.out.println(cm.getFileName());
			String output = getContent(currentCommit.getCommitId(), cm.getFileName(), REPOSITORY);
			// System.out.println(output);
			FileUtils.writeStringToFile(
					new File(REVISION_PATH + currentCommit.getCommitId() + File.separator + cm.getFileName()),
					output);
		}
	}
	
	public void crateUnderstandMetricsForCommit() throws Exception{
		Set<String> commitIdFilteredList = readRequiredCommitIdsForTwoYears();
		int count = 0;
		
		for (int i = 0; i < commitIdFilteredList.size() ; i++) {
			CommitModel currentCommit = commitList.get(i);
			
			if(!commitIdFilteredList.contains(currentCommit.getCommitId())) {
				continue;
			}
			++count;
			checkUnderstandCommnand(currentCommit.getCommitId());
			System.out.println("Finish Commit: " + i + " " + count);
		}
		System.out.println("Total commits = " + count);
	}
	
	public static void main(String[] args) throws Exception {
		UnderstandToolMetricExtractor ob = new UnderstandToolMetricExtractor();
		ob.readCommitInformation();
		// ob.commitBugInfo();
		// ob.calculateUnderstandMetrics();
		//ob.checkUnderstandCommnand();
		 ob.crateUnderstandMetricsForCommit();
		//ob.checkUnderstandCommnand("0a5976b7bf17e3b25432636acfe6a9eef07755a5");
		System.out.println("Program finishes sucessfully");
	}

}
