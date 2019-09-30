package com.sail.evaluatingevaluator.git.revision;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;

import com.sail.evaluatingevaluator.config.Config;
import com.sail.evaluatingevaluator.git.commit.CommitManager;
import com.sail.evaluatingevaluator.process.ProcessUtility;

public class RevisionFileContentCollector implements Serializable{

	//given a revision number (commit SHA) and a path to file (is it a relative path), this class collects file content
	
	private CommitManager commitManager;
	
	public RevisionFileContentCollector(CommitManager _commitManager) {
		this.commitManager = _commitManager;
	}
	
	public String getContent(String commit_sha, String filePath) throws IOException, InterruptedException {
		ProcessBuilder pb = new ProcessBuilder("git","show",commit_sha+":"+filePath);
		pb.directory(new File(commitManager.getRepositoryPath()));
		Process process = pb.start();
		String output = ProcessUtility.output(process.getInputStream());
		int errCode = process.waitFor();
		return output;
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		System.out.println("Running commit manager ...");
		CommitManager commitManager = new CommitManager(Config.REPOSITORY_PATH);
		try {
			commitManager.run();
			System.out.println("Running revision file change manager ...");
			RevisionFileChangeDescriptorManager revisionFileChangeDescriptorManager = new RevisionFileChangeDescriptorManager(commitManager);
			System.out.println("Total Changes: " + revisionFileChangeDescriptorManager.getRevisionFileChangeDescriptorList().size());
			revisionFileChangeDescriptorManager.run();
			revisionFileChangeDescriptorManager.print();
			System.out.println("Total Revision File Change List: "+revisionFileChangeDescriptorManager.getRevisionFileChangeDescriptorList().size());
		
			RevisionFileContentCollector revFileContentCollector = new RevisionFileContentCollector(commitManager);
			String output = revFileContentCollector.getContent("67dc8e977188c953fadc182543d31672d22d0442","ParameterRecommender/src/main/java/com/srlab/parameter/node/ParameterContent.java");
		} catch (InterruptedException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
