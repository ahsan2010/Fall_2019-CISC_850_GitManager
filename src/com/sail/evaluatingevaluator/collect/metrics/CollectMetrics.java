package com.sail.evaluatingevaluator.collect.metrics;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.csvreader.CsvReader;
import com.csvreader.CsvWriter;
import com.sail.evaluatingevaluator.process.ProcessUtility;
import com.sail.evaluatingevaluator.xml.parser.MethodCommentModel;
import com.sail.evaluatingevaluator.xml.parser.Parser;
import com.sail.model.ChangeMetrics;
import com.sail.model.CommitFileModel;
import com.sail.model.CommitModel;

public class CollectMetrics {

	String COMMIT_INFO_PATH = "/home/ahsan/Documents/Queens_PHD/Courses/Fall_2019/CISC_850/Assignment/Assignment/Result/revision_change_Lookup.csv";
	String COMMIT_BUG_INFO = "/home/ahsan/Documents/Queens_PHD/Courses/Fall_2019/CISC_850/Assignment/Assignment/Result/commit_bug_info.csv";
	
	String OUTPUT_CHANGE_BUG_COMMIIT_INFO = "/home/ahsan/Documents/Queens_PHD/Courses/Fall_2019/CISC_850/Assignment/Assignment/Result/output_change_metric_per_component.csv";
	
	ArrayList<CommitModel> commitList = new ArrayList<CommitModel>();
	
	public DateTimeFormatter formatterWithHyphen = DateTimeFormat.forPattern("yyyy-MM-dd");

	Set<String> bugFixCommitList = new HashSet<String>();

	public String REVISION_PATH = "/home/ahsan/Documents/Queens_PHD/Courses/Fall_2019/CISC_850/Assignment/Project_Sample_Commits/Project_Sample_Commits/";

	
	CsvWriter writer = new CsvWriter(OUTPUT_CHANGE_BUG_COMMIIT_INFO);
	
	
	public CollectMetrics(){
		try{
			writer.write("Commit_Id");
			writer.write("Commit_Date");
			writer.write("Changed_File");
			writer.write("ConsistentChange");
			writer.write("InConsistentChange");
			writer.write("ConsistentBugFix");
			writer.write("InConsistentBugFix");
			writer.endRecord();
		}catch(Exception e){
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

			String subSystem = commitFileName.substring(0, commitFileName.lastIndexOf("/"));

			String lookUpCommit = reader.get("Lookup_Commit");
			String lookUpCommitDate = reader.get("Lookup_Commit_Date");

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
			fm.setLookupCommit(lookUpCommit);
			fm.setLookupCommitDate(lookUpCommitDate);
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

	public String getXML(String fileLocation) throws Exception {
		String xmlString = "";
		ProcessBuilder pb = new ProcessBuilder("srcml", fileLocation);
		// System.out.println("Command: "+"git"+" diff"+" -C"+" -M"+" -w"+"
		// --no-color"+" --name-status "+previousCommitSHA+"
		// "+currentCommitSHA);
		Process process = pb.start();
		String output = ProcessUtility.output(process.getInputStream());
		int errCode = process.waitFor();

		List<String> lineList = IOUtils.readLines(new StringReader(output));

		for (String line : lineList) {
			xmlString += line + "\n";
		}
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

	public void extractMetrics() throws Exception {
		for (int i = 0; i < 9; i++) {
			CommitModel CurrentCommit = commitList.get(i);
			String currentLocation = REVISION_PATH + CurrentCommit.getCommitId() + "_" + CurrentCommit.getCommitDate()
					+ "/";
			
			/*if(!CurrentCommit.getCommitId().equals("69fbb764e52050bb7697878c1ba909e8f7d63d84")){
				continue;
			}*/
			
			for (CommitFileModel cm : CurrentCommit.getChangedFileList()) {
				
				try {
					String previousLocation = REVISION_PATH + cm.getLookupCommit() + "_" + cm.getLookupCommitDate()
							+ "/";
					String filePath = cm.getFileName();

					String previousXML = getXML(previousLocation + filePath);
					String currentXML = getXML(currentLocation + filePath);

					//System.out.println("Previous: " + (previousLocation + filePath));
					//System.out.println("Current: " + (currentLocation + filePath));
					
					Parser p = new Parser();
					Map<String, MethodCommentModel> previosClassInfo = p.getClassInformation(previousXML);
					Map<String, MethodCommentModel> currentClassInfo = p.getClassInformation(currentXML);
					ChangeMetrics cMetric = calculateChangeMetrics(CurrentCommit, previosClassInfo, currentClassInfo);

					/*
					System.out.println(CurrentCommit.getCommitId());
					
					System.out.println("ConsistentChange: " + cMetric.getNumberConsistentChange());
					System.out.println("InConsistentChange: " + cMetric.getNumberInConsistentChange());
					System.out.println("ConsistentBugFix: " + cMetric.getNumberConsistentBugFix());
					System.out.println("InConsistentBugFix: " + cMetric.getNumberInConsistentBugFix());

					System.out.println("------------------");*/

					writer.write(CurrentCommit.getCommitId());
					writer.write(CurrentCommit.getCommitDate());
					writer.write(cm.getFileName());
					writer.write(Integer.toString(cMetric.getNumberConsistentChange()));
					writer.write(Integer.toString(cMetric.getNumberInConsistentChange()));
					writer.write(Integer.toString(cMetric.getNumberConsistentBugFix()));
					writer.write(Integer.toString(cMetric.getNumberInConsistentBugFix()));
					writer.endRecord();
					
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
					break;
				}
			}
			// break;			
			System.out.println("Finish Commit " + (i+1));
		}
		writer.close();
	}

	public static void main(String[] args) throws Exception {
		CollectMetrics ob = new CollectMetrics();
		ob.commitBugInfo();
		ob.readCommitInformation();
		ob.extractMetrics();
		System.out.println("Program finishes successfully");
	}

}
