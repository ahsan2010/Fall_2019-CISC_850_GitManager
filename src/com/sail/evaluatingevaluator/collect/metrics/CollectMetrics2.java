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

	public ChangeMetrics calculateChangeMetrics(CommitModel CurrentCommit,
			Map<String, MethodCommentModel> previosClassInfo, Map<String, MethodCommentModel> currentClassInfo)
			throws Exception {
		ChangeMetrics cMetric = new ChangeMetrics();
		boolean bufFixCommit = false;
		if (bugFixCommitList.contains(CurrentCommit.getCommitDate())) {
			bufFixCommit = true;
		}

		for (String key : previosClassInfo.keySet()) {
			if (currentClassInfo.containsKey(key)) {
				// Calculate the changes
				MethodCommentModel prevMethodInfo = previosClassInfo.get(key);
				MethodCommentModel currentMethodInfo = currentClassInfo.get(key);

				boolean outerCommentChange = isChange(prevMethodInfo.getOuterComment(),
						currentMethodInfo.getOuterComment());
				boolean innerCommentChange = isChange(prevMethodInfo.getInnerComment(),
						currentMethodInfo.getInnerComment());
				boolean codeChange = isChange(prevMethodInfo.getMethodBody(), currentMethodInfo.getMethodBody());

				if ((outerCommentChange || innerCommentChange) && codeChange) {
					cMetric.numberConsistentChange++;
				} else if (outerCommentChange || innerCommentChange || codeChange) {
					cMetric.numberInConsistentChange++;
				}

			}

			/*
			 * if(outerCommentChange){ System.out.println("OuterCommentChange");
			 * 
			 * } if(innerCommentChange){
			 * System.out.println("InnerCommentChange"); } if(codeChange){
			 * System.out.println("CodeChange"); }
			 */
			// System.out.println(key + " outerCommentChange = " +
			// outerCommentChange + " innerCommentChange = " +
			// innerCommentChange + " codeChange = " + codeChange);
		}

		if (bufFixCommit) {
			cMetric.numberConsistentBugFix = cMetric.getNumberConsistentChange();
			cMetric.numberInConsistentBugFix = cMetric.getNumberInConsistentChange();
		}

		return cMetric;
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

			// System.out.println(chagedArrayList);
			// System.out.println(m.getCommentString());
			// System.out.println(m.getFucntionString());
			// System.out.println("-----------------");

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

	public void extractMetrics() throws Exception {
		long start = System.currentTimeMillis();
		GitDiff gitDiff = new GitDiff();
		for (int i = commitList.size() - 1; i > commitList.size() - 20; i--) {
			CommitModel previousCommit = commitList.get(i - 1);
			CommitModel CurrentCommit = commitList.get(i);
			String currentLocation = REVISION_PATH + CurrentCommit.getCommitId() + "_" + CurrentCommit.getCommitDate()
					+ "/";

			/*
			 * if(!CurrentCommit.getCommitId().equals(
			 * "69fbb764e52050bb7697878c1ba909e8f7d63d84")){ continue; }
			 */

			ProcessBuilder pbCheckout = new ProcessBuilder("git", "checkout", CurrentCommit.getCommitId());
			pbCheckout.directory(new File(REPOSITORY));
			Process processCheckout = pbCheckout.start();
			int errCodeCheckout = processCheckout.waitFor();
			Map<String, String> diffFiles = new HashMap<String, String>();

			Set<String> locationChangedFiles = new HashSet<String>();
			for (CommitFileModel cm : CurrentCommit.getChangedFileList()) {
				String fileLocation = REPOSITORY + cm.getFileName();
				locationChangedFiles.add(fileLocation);
				String output = gitDiff.diffMyVersion(previousCommit.getCommitId(), CurrentCommit.getCommitId(),
						cm.getFileName(), REPOSITORY);
			}

			for (CommitFileModel cm : CurrentCommit.getChangedFileList()) {

				try {
					String previousLocation = REVISION_PATH + cm.getLookupCommit() + "_" + cm.getLookupCommitDate()
							+ "/";
					String filePath = cm.getFileName();

					String output = gitDiff.diffMyVersion(previousCommit.getCommitId(), CurrentCommit.getCommitId(),
							filePath, REPOSITORY);

					GitDiffChangeDescriptor gitDiffChangeDescriptor = new GitDiffChangeDescriptor(output);
					Set<Integer> changedLineList = gitDiffChangeDescriptor.getAddedLines();

					// System.out.println("Changed lines " +
					// changedLineList.toString());

					// System.out.println(CurrentCommit.getCommitId() + " " +
					// (REPOSITORY + filePath));

					String currentXML = getXML(CurrentCommit.getCommitId(), REPOSITORY + filePath);

					// System.out.println("XML " + currentXML);

					/*
					 * Parser2 p = new Parser2(); ArrayList<MethodCommentModel>
					 * mm = p.getClassInformation(currentXML); ChangeMetrics
					 * cMetric = calculateChangeMetricsII(mm, changedLineList);
					 */

					// System.out.println("File: " + cm.getFileName());

					// ChangeMetrics cMetric = new ChangeMetrics();
					// ChangeMetrics cMetric =
					// calculateChangeMetrics(CurrentCommit, previosClassInfo,
					// currentClassInfo);
					/*
					 * Parser2 p = new Parser2(); Map<String,
					 * MethodCommentModel> previosClassInfo =
					 * p.getClassInformation(previousXML); Map<String,
					 * MethodCommentModel> currentClassInfo =
					 * p.getClassInformation(currentXML); ChangeMetrics cMetric
					 * = calculateChangeMetrics(CurrentCommit, previosClassInfo,
					 * currentClassInfo);
					 */

					/*
					 * System.out.println("Previous: " + (previousLocation +
					 * filePath)); System.out.println("Current: " +
					 * (currentLocation + filePath));
					 * 
					 * 
					 * System.out.println(CurrentCommit.getCommitId());
					 * 
					 * System.out.println("ConsistentChange: " +
					 * cMetric.getNumberConsistentChange());
					 * System.out.println("InConsistentChange: " +
					 * cMetric.getNumberInConsistentChange());
					 * System.out.println("ConsistentBugFix: " +
					 * cMetric.getNumberConsistentBugFix());
					 * System.out.println("InConsistentBugFix: " +
					 * cMetric.getNumberInConsistentBugFix());
					 * 
					 * System.out.println("------------------");
					 */

					/*
					 * writer.write(CurrentCommit.getCommitId());
					 * writer.write(CurrentCommit.getCommitDate());
					 * writer.write(cm.getFileName());
					 * writer.write(Integer.toString(cMetric.
					 * getNumberConsistentChange()));
					 * writer.write(Integer.toString(cMetric.
					 * getNumberInConsistentChange()));
					 * writer.write(Integer.toString(cMetric.
					 * getNumberConsistentBugFix()));
					 * writer.write(Integer.toString(cMetric.
					 * getNumberInConsistentBugFix())); writer.endRecord();
					 */

					/*
					 * System.out.println("Commit: " +
					 * CurrentCommit.getCommitId() + " " +
					 * CurrentCommit.getCommitDate());
					 * System.out.println(currentXML);
					 * System.out.println("---------------------");
					 * System.out.println("Commit: " + cm.getLookupCommit() +
					 * " " + cm.getLookupCommitDate());
					 * System.out.println(previousXML);
					 */

				} catch (Exception e) {
					e.printStackTrace();

				}
			}
			System.out.println("Finish Commit " + (i + 1));
			// break;
		}

		ProcessBuilder pbCheckoutMaster = new ProcessBuilder("git", "checkout", "-");
		pbCheckoutMaster.directory(new File(REPOSITORY));
		Process processCheckoutMaster = pbCheckoutMaster.start();
		int errCodeCheckoutMaster = processCheckoutMaster.waitFor();
		System.out.println("Finish Commit Checking Out	");
		// break;
		long end = System.currentTimeMillis();
		long change = (end - start) / 1000;
		System.out.println("Time take: " + change);
		writer.close();
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
				
				//System.out.println("CHECKOUT OUTPUT " + ouputCheckout);
				//System.out.println("CHECKOUT STATUS " + ouputCheckoutStatus);
				

				Map<String, Set<Integer>> diffFilesList = new HashMap<String, Set<Integer>>();
				List<String> srcmlCommands = new ArrayList<String>();
				srcmlCommands.add("srcml");
				srcmlCommands.add("--position");

				for (CommitFileModel cm : currentCommit.getChangedFileList()) {
					String fileLocation = REPOSITORY + cm.getFileName();

					srcmlCommands.add(fileLocation);
					String output = gitDiff.diffMyVersion(previousCommit.getCommitId(), currentCommit.getCommitId(),
							cm.getFileName(), REPOSITORY);
					//System.out.println(output);
					GitDiffChangeDescriptor gitDiffChangeDescriptor = new GitDiffChangeDescriptor(output);
					Set<Integer> changedLineList = gitDiffChangeDescriptor.getAddedLines();
					//changedLineList.addAll(gitDiffChangeDescriptor.getDeletedLines()); 
					//System.out.println(cm.getFileName() + " " +changedLineList + " " + previousCommit.getCommitId()) ;
					
					diffFilesList.put(cm.getFileName(), changedLineList);
					//break;
				}
				String currentXML = getXML(currentCommit.getCommitId(), srcmlCommands);
				Map<String, ArrayList<MethodCommentModel>> methodListClass = p.getClassInformation(currentXML);
				//System.out.println(currentCommit.getCommitId());
                //System.out.println(currentXML);
				for (String fileName : diffFilesList.keySet()) {
					try{
						//System.out.println("F " + diffFilesList.get(fileName));
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

						/*
						 * System.out.println(currentCommit.getCommitId());
						 * 
						 * System.out.println("ConsistentChange: " +
						 * cMetric.getNumberConsistentChange());
						 * System.out.println("InConsistentChange: " +
						 * cMetric.getNumberInConsistentChange());
						 * System.out.println("ConsistentBugFix: " +
						 * cMetric.getNumberConsistentBugFix());
						 * System.out.println("InConsistentBugFix: " +
						 * cMetric.getNumberInConsistentBugFix());
						 * 
						 * System.out.println("------------------");
						 */
					}catch(Exception e){
						e.printStackTrace();
						//System.exit(0);
					}

				}
				System.out.println("Finish Commit " + (i + 1));
			} catch (Exception e) {
				e.printStackTrace();
			}

			// break;
		}

		ProcessBuilder pbCheckoutMaster = new ProcessBuilder("git", "checkout", "-");
		pbCheckoutMaster.directory(new File(REPOSITORY));
		Process processCheckoutMaster = pbCheckoutMaster.start();
		int errCodeCheckoutMaster = processCheckoutMaster.waitFor();
		System.out.println("Finish Commit Checking Out	");
		// break;
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
