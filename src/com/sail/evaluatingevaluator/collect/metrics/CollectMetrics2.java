package com.sail.evaluatingevaluator.collect.metrics;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.csvreader.CsvReader;
import com.csvreader.CsvWriter;
import com.sail.evaluatingevaluator.diff.GitDiff;
import com.sail.evaluatingevaluator.diff.GitDiffChangeDescriptor;
import com.sail.evaluatingevaluator.process.ProcessUtility;
import com.sail.evaluatingevaluator.xml.parser.MethodCommentModel;
import com.sail.evaluatingevaluator.xml.parser.Parser2;
import com.sail.model.ChangeMetrics;
import com.sail.model.CommitFileModel;
import com.sail.model.CommitModel;

public class CollectMetrics2 {

	// String ROOT =
	// "home/ahsan/Documents/Queens_PHD/Courses/Fall_2019/CISC_850/Assignment/Assignment";
	String COMMIT_INFO_PATH = "/home/ahsan/Documents/Queens_PHD/Courses/Fall_2019/CISC_850/Assignment/Assignment/Result/revision_change.csv";
	String COMMIT_BUG_INFO = "/home/ahsan/Documents/Queens_PHD/Courses/Fall_2019/CISC_850/Assignment/Assignment/Result/commit_bug_info.csv";

	String OUTPUT_CHANGE_BUG_COMMIIT_INFO = "/home/ahsan/Documents/Queens_PHD/Courses/Fall_2019/CISC_850/Assignment/Assignment/Result/output_change_metric_per_component_Full_New_Approach.csv";

	ArrayList<CommitModel> commitList = new ArrayList<CommitModel>();

	public DateTimeFormatter formatterWithHyphen = DateTimeFormat.forPattern("yyyy-MM-dd");

	Set<String> bugFixCommitList = new HashSet<String>();

	public String REVISION_PATH = "/home/ahsan/Documents/Queens_PHD/Courses/Fall_2019/CISC_850/Assignment/Assignment/GitRevisions/";

	public String REPOSITORY = "/home/ahsan/Documents/Queens_PHD/Courses/Fall_2019/CISC_850/Assignment/Assignment/hive/";

	CsvWriter writer = new CsvWriter(OUTPUT_CHANGE_BUG_COMMIIT_INFO);

	public CollectMetrics2() {
		try {
			writer.write("Commit_Id");
			writer.write("Commit_Date");
			writer.write("Changed_File");
			writer.write("ConsistentChange");
			writer.write("InConsistentChange");
			writer.write("ConsistentBugFix");
			writer.write("InConsistentBugFix");
			writer.endRecord();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

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

	public String getXML(String commitId, String fileLocation) throws Exception {

		String xmlString = "";

		ProcessBuilder pb = new ProcessBuilder("srcml", "--position", fileLocation);
		Process process = pb.start();
		xmlString = ProcessUtility.output(process.getInputStream());
		int errCode = process.waitFor();

		ProcessUtility.output(process.getErrorStream());

		return xmlString;
	}

	public String getXML(String commitId, List<String> commands) throws Exception {

		String xmlString = "";

		ProcessBuilder pb = new ProcessBuilder(commands);
		Process process = pb.start();
		String output = ProcessUtility.output(process.getInputStream());
		int errCode = process.waitFor();
		ProcessUtility.output(process.getErrorStream());
		xmlString = output;
		return xmlString;
	}

	public boolean isChange(String prevString, String currentString) throws Exception {
		boolean result = false;
		if (!prevString.trim().equals(currentString.trim())) {
			return true;
		}
		return false;
	}
	boolean isSomethingChange(Set<Integer> checkSet, Set<Integer> mainSet) {
		boolean result = false;

		for (Integer v : mainSet) {
			if (checkSet.contains(v)) {
				return true;
			}
		}

		return false;
	}

	public ChangeMetrics calculateChangeMetricsII(String commitId, ArrayList<MethodCommentModel> mmList,
			Set<Integer> chagedArrayList) throws Exception {

		ChangeMetrics cMetric = new ChangeMetrics();

		for (MethodCommentModel m : mmList) {
			// Comment Check
			Set<Integer> commentLineSet = new HashSet<Integer>();
			Set<Integer> methodLineSet = new HashSet<Integer>();

			if (m.getCommentString().length() > 0) {
				String comWords[] = m.getCommentString().split("_");
				for (String comWord : comWords) {
					if (comWord.trim().length() > 0) {
						String tuples[] = comWord.split("-");
						Integer start = Integer.parseInt(tuples[0]);
						Integer end = Integer.parseInt(tuples[1]);
						for (int i = start; i <= end; i++) {
							commentLineSet.add(i);
						}
					}
				}
			}
			if (m.getFucntionString().length() > 0) {
				String comWords[] = m.getFucntionString().split("_");
				for (String comWord : comWords) {
					if (comWord.trim().length() > 0) {
						String tuples[] = comWord.split("-");
						Integer start = Integer.parseInt(tuples[0]);
						Integer end = Integer.parseInt(tuples[1]);
						for (int i = start; i <= end; i++) {
							methodLineSet.add(i);
						}
					}
				}
			}
			boolean commentChange = isSomethingChange(commentLineSet, chagedArrayList);
			boolean functionChange = isSomethingChange(methodLineSet, chagedArrayList);

			if ((commentChange) && functionChange) {
				cMetric.numberConsistentChange++;
			} else if (commentChange || functionChange) {
				cMetric.numberInConsistentChange++;
			}
		}
		if (bugFixCommitList.contains(commitId)) {
			cMetric.numberConsistentBugFix = cMetric.getNumberConsistentChange();
			cMetric.numberInConsistentBugFix = cMetric.getNumberInConsistentChange();
		}
		return cMetric;
	}

	public void extractMetricsII() throws Exception {
		long start = System.currentTimeMillis();
		GitDiff gitDiff = new GitDiff();
		Parser2 p = new Parser2();
		for (int i =  commitList.size() - 1; i > 0; i--) {

			try {

				CommitModel previousCommit = commitList.get(i - 1);
				CommitModel currentCommit = commitList.get(i);

				ProcessBuilder pbCheckout = new ProcessBuilder("git", "checkout", currentCommit.getCommitId());
				pbCheckout.directory(new File(REPOSITORY));
				Process processCheckout = pbCheckout.start();
				String ouputCheckout = ProcessUtility.output(processCheckout.getErrorStream());
				
				
				ProcessBuilder pbCheckoutStatus = new ProcessBuilder("git", "status");
				Process processCheckoutStatus = pbCheckoutStatus.start();
				String ouputCheckoutStatus = ProcessUtility.output(processCheckoutStatus.getInputStream());
				
				
				int errCodeCheckout = processCheckout.waitFor();
				
				Map<String, Set<Integer>> diffFilesList = new HashMap<String, Set<Integer>>();
				List<String> srcmlCommands = new ArrayList<String>();
				srcmlCommands.add("srcml");
				srcmlCommands.add("--position");

				for (CommitFileModel cm : currentCommit.getChangedFileList()) {
					String fileLocation = REPOSITORY + cm.getFileName();

					srcmlCommands.add(fileLocation);
					String output = gitDiff.diffMyVersion(previousCommit.getCommitId(), currentCommit.getCommitId(),
							cm.getFileName(), REPOSITORY);
					GitDiffChangeDescriptor gitDiffChangeDescriptor = new GitDiffChangeDescriptor(output);
					Set<Integer> changedLineList = gitDiffChangeDescriptor.getAddedLines();
					diffFilesList.put(cm.getFileName(), changedLineList);
				}
				String currentXML = getXML(currentCommit.getCommitId(), srcmlCommands);
				Map<String, ArrayList<MethodCommentModel>> methodListClass = p.getClassInformation(currentXML);
				
				for (String fileName : diffFilesList.keySet()) {
					try{
						String methodListKey = (REPOSITORY + fileName).substring(1);
						ChangeMetrics cMetric = calculateChangeMetricsII(currentCommit.getCommitId(),
								methodListClass.get(methodListKey), diffFilesList.get(fileName));

						writer.write(currentCommit.getCommitId());
						writer.write(currentCommit.getCommitDate());
						writer.write(fileName);
						writer.write(Integer.toString(cMetric.getNumberConsistentChange()));
						writer.write(Integer.toString(cMetric.getNumberInConsistentChange()));
						writer.write(Integer.toString(cMetric.getNumberConsistentBugFix()));
						writer.write(Integer.toString(cMetric.getNumberInConsistentBugFix()));
						writer.endRecord();
					}catch(Exception e){
						e.printStackTrace();
						//System.exit(0);
					}

				}
				System.out.println("Finish Commit " + (i + 1));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		ProcessBuilder pbCheckoutMaster = new ProcessBuilder("git", "checkout", "-");
		pbCheckoutMaster.directory(new File(REPOSITORY));
		Process processCheckoutMaster = pbCheckoutMaster.start();
		int errCodeCheckoutMaster = processCheckoutMaster.waitFor();
		long end = System.currentTimeMillis();
		long change = (end - start) / 1000;
		System.out.println("Time take: " + change);
		writer.close();
	}

	public static void main(String[] args) throws Exception {
		CollectMetrics2 ob = new CollectMetrics2();
		ob.commitBugInfo();
		ob.readCommitInformation();
		ob.extractMetricsII();
		System.out.println("Program finishes successfully");
	}

}
