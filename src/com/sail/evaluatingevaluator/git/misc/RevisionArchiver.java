package com.sail.evaluatingevaluator.git.misc;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.sail.evaluatingevaluator.config.Config;
import com.sail.evaluatingevaluator.git.commit.Commit;
import com.sail.evaluatingevaluator.git.commit.CommitManager;
import com.sail.evaluatingevaluator.git.revision.RevisionFileChangeDescriptor;
import com.sail.evaluatingevaluator.git.revision.RevisionFileChangeDescriptorManager;
import com.sail.evaluatingevaluator.process.ProcessUtility;

public class RevisionArchiver {

	private String repositoryPath;
	private String archiveFolderPath;
	private CommitManager commitManager;
	private RevisionFileChangeDescriptorManager revisionFileChangeDescriptorManager;
	
	public RevisionArchiver(String _repositoryPath, String _archiveFolderPath, CommitManager _commitManager, RevisionFileChangeDescriptorManager _revFileChangeDescriptorManager) {
		this.repositoryPath = _repositoryPath;
		this.archiveFolderPath = _archiveFolderPath;
		this.commitManager = _commitManager;
		this.revisionFileChangeDescriptorManager = _revFileChangeDescriptorManager;
	}
	
	public String getRepositoryPath() {
		return repositoryPath;
	}

	public void setRepositoryPath(String repositoryPath) {
		this.repositoryPath = repositoryPath;
	}

	public String getArchiveFolderPath() {
		return archiveFolderPath;
	}

	public void setArchiveFolderPath(String archiveFolderPath) {
		this.archiveFolderPath = archiveFolderPath;
	}

	public CommitManager getCommitManager() {
		return commitManager;
	}

	public void setCommitManager(CommitManager commitManager) {
		this.commitManager = commitManager;
	}

	//archive all revisions from start to endIndex-1
	public void run(int startIndex, int endIndex) throws IOException, InterruptedException {
		//move to the revision base folder path
		System.out.println("Revisions need to be extracted: "+(endIndex-startIndex));
		int count =0;
		List<Commit> commitList = commitManager.getCommitList();
		for(int i=startIndex;i<commitManager.getCommitList().size()&& i<endIndex;i++) {
			Commit commit = commitList.get(i);
			RevisionFileChangeDescriptor revisionFileChangeDescriptor = revisionFileChangeDescriptorManager.getRevisionFileChangeDescriptorList().get(i);
			System.out.println("["+i+"] " + revisionFileChangeDescriptor.getFilesChangePairList().size() + " SHA: "+commit.getSha());
			{
				String archiveFilePath = this.archiveFolderPath+File.separator+commit.getSha()+".zip";
				
				//now checkout the revision in this folder
				ProcessBuilder pb = new ProcessBuilder("git","archive","--format","zip","--output",archiveFilePath,commit.getSha());
				pb.directory(new File(repositoryPath));
				Process process = pb.start();		
				String output = ProcessUtility.output(process.getInputStream());
				int errCode = process.waitFor();
				if(errCode!=0) {
					throw new RuntimeException("Obtaining ErrorCode Not 0 in : RevisionArchiver.java");
				}
				count++;
			}
		}
		System.out.println("Count: "+count);
	}
	
	//archive all revision in the given repository
	public void run() throws IOException, InterruptedException {
		this.run(0,this.commitManager.getCommitList().size());
	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		CommitManager commitManager = new CommitManager(Config.REPOSITORY_PATH);
		try {
			commitManager.run();
			RevisionFileChangeDescriptorManager revisionFileChangeDescriptorManager = new RevisionFileChangeDescriptorManager(commitManager);
			revisionFileChangeDescriptorManager.run();
			RevisionArchiver  revisionArchiver = new RevisionArchiver(Config.REPOSITORY_PATH,Config.REPOSITORY_REVISION_PATH,commitManager, revisionFileChangeDescriptorManager) ;
			revisionArchiver.run();
		} catch (InterruptedException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
