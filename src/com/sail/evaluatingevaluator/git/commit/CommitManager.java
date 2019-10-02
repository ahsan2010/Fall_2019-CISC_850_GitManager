package com.sail.evaluatingevaluator.git.commit;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import org.apache.commons.io.IOUtils;

import com.csvreader.CsvWriter;
import com.sail.evaluatingevaluator.process.ProcessUtility;

/* Example of a commit
 * Commit SHA: 7d6a91f18f627042e5f436030e36ebe7bb747244
 * Author: Parvez <mua237@mail.usask.ca>
 * Date: Mon Apr 16 02:05:33 2018 -0400
 * 
 * Message: update diff between two revisions and collect the changed lines
 * ...
 * */

//We collect the list of commits using git log command. This gives the newest commit first
public class CommitManager implements Serializable {

	private static String REPOSITORY_PATH = "/Users/mdahasanuzzaman/Documents/Queens_Phd/Fall_2019_Courses/CISC-850/Assignments/hive/";
	private String COMMIT_MESSAGE_FILE = "/Users/mdahasanuzzaman/Documents/Queens_Phd/Fall_2019_Courses/CISC-850/Assignments/Result/commit_message_Full.csv";
	private List<Commit> commitListNewestFirst;
	private String repositoryPath;
	private HashMap<String, Commit> hmSHAtoCommit;
	private CsvWriter writer = null;
	private int totalCommit = 0;
	public CommitManager(String _repositoryPath) {
		this.commitListNewestFirst = new ArrayList();
		this.repositoryPath = _repositoryPath;
		this.hmSHAtoCommit = new HashMap();
		try {
			writer = new CsvWriter(COMMIT_MESSAGE_FILE);
			writer.write("Commit_Id");
			writer.write("Date");
			writer.write("Author_Name");
			writer.write("Commit_Message");
			writer.endRecord();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void add(Commit commit) {
		if (hmSHAtoCommit.containsKey(commit.getSha()) == false) {
			this.commitListNewestFirst.add(commit);
			this.hmSHAtoCommit.put(commit.getSha(), commit);
			System.out.println("Size = " + this.commitListNewestFirst.size());
		} else {
			throw new RuntimeException("Error in duplicate input");
		}
	}

	public boolean hasCommitBySHA(String SHA) {
		if (hmSHAtoCommit.containsKey(SHA))
			return true;
		else
			return false;
	}

	// the goal of this function is to collect all commits and arrange them based on
	// the time
	public void run() throws InterruptedException, IOException {
		ProcessBuilder pb = new ProcessBuilder("git", "log","--date=format:'%Y-%m-%d");
		pb.directory(new File(this.getRepositoryPath()));
		System.out.println("Repository: " + this.getRepositoryPath());
		Process process = pb.start();
		String output = ProcessUtility.output(process.getInputStream());
		int errCode = process.waitFor();
		this.parse(output);
	}

	// commit messages are typically followed by space. We remove those spaces
	private String listToString(List<String> lineList) {
		StringBuilder sb = new StringBuilder("");
		for (String line : lineList) {
			if (line.matches("\\s+") == false)
				sb.append(line.trim());
		}
		return sb.toString();
	}

	private void parse(String output) throws IOException {
		String commitSHA = null;
		String message = null;
		

		// step-1:convert the output into list of lines
		List<String> lineList = IOUtils.readLines(new StringReader(output));

		// step-2: read the commits
		for (int i = 0; i < lineList.size(); i++) {
			String line = lineList.get(i);

			if (line.startsWith("commit ")) {
				ArrayList<String> messageLines = new ArrayList();
				String split[] = line.split("\\s+");
				commitSHA = split[1];
				i++;
				boolean processCommit = false;
				Optional<String> author = Optional.empty();
				Optional<String> date = Optional.empty();
				Optional<String> commitMesssage = Optional.empty();

				while (processCommit != true && i < lineList.size()) {
					if (lineList.get(i).startsWith("Author: ")) {
						author = Optional.of(lineList.get(i).substring("Author: ".length()).trim());
						i++;
					}
					if (lineList.get(i).startsWith("Date: ")) {
						date = Optional.of(lineList.get(i).substring("Date: ".length()).trim());
						i++;
					}
					if (lineList.get(i).startsWith("Merge: ")) {
						i++;
					} else if (lineList.get(i).equals("\\s+") == false) {
						// read the message lines
						for (; i < lineList.size(); i++) {
							line = lineList.get(i);
							if (line.startsWith("commit ")) { // this indicates the start of another commit
								i--;
								processCommit = true;
								break;
							} else {
								messageLines.add(line);
							}
						}
					}
				}
				Commit commit = new Commit(author.get(), date.get().substring(1), this.listToString(messageLines), commitSHA);
				//if(totalCommit <= 500)
				writeCommitMessageInfo(commit);
				System.out.println("Commit " + (++totalCommit) + " " + commit.getDate());
				// this.add(commit);
				// commit.print();

			} else {
				throw new RuntimeException("Error in parsing output");
			}

		}
	}

	public void writeCommitMessageInfo(Commit commit) {
		try {
			writer.write(commit.getSha());
			writer.write(commit.getDate());
			writer.write(commit.getAuthor());
			writer.write(commit.getMessage());
			writer.endRecord();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		System.out.println("Start Commit Manager");
		CommitManager commitManager = new CommitManager(REPOSITORY_PATH);
		try {
			commitManager.run();
			List<Commit> commitList = commitManager.getCommitList();
			System.out.println("Total Commit: " + commitList);
			for (int i = 0; i < commitList.size(); i++) {
				if(i>10)break;
				commitList.get(i).print();
			}
			commitManager.writer.close();
			System.out.println("Program Finishes Successfully");
		} catch (InterruptedException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public HashMap<String, Commit> getHmSHAtoCommit() {
		return hmSHAtoCommit;
	}

	public List<Commit> getCommitListNewestFirst() {
		return commitListNewestFirst;
	}

	// this returns commit list oldest first
	public List<Commit> getCommitList() {
		// we reverse the commitList so that O becomes the oldest commit
		ArrayList<Commit> list = new ArrayList<Commit>(this.getCommitListNewestFirst());
		Collections.reverse(list);
		return list;
	}

	public void save(File file) {
		try {
			// Saving of object in a file
			FileOutputStream fos = new FileOutputStream(file);
			ObjectOutputStream out = new ObjectOutputStream(fos);

			// Method for serialization of object
			out.writeObject(this);

			out.close();
			fos.close();

			System.out.println("Object has been serialized: " + file.getName());
		}

		catch (IOException ex) {
			System.out.println("IOException is caught");
		}

	}

	public static CommitManager load(File file) throws ClassNotFoundException {
		try {
			// Saving of object in a file
			FileInputStream fis = new FileInputStream(file);
			ObjectInputStream in = new ObjectInputStream(fis);

			// Method for serialization of object
			CommitManager commitManager = (CommitManager) in.readObject();
			in.close();
			fis.close();
			System.out.println("Object has been deserialized: " + file.getName());
			return commitManager;
		}

		catch (IOException ex) {
			System.out.println("IOException is caught");
		}
		return null;
	}

	public String getRepositoryPath() {
		return repositoryPath;
	}
}
