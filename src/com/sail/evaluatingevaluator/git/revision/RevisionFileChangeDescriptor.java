package com.sail.evaluatingevaluator.git.revision;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

//collect all file change pair between two commits or two revisions
public class RevisionFileChangeDescriptor implements Serializable{
	List<FileChangePair> filesChangePairList;
	String oldCommitSHA;
	String newCommitSHA;
	
	public RevisionFileChangeDescriptor(List<FileChangePair> _fileChangePairList,String _oldCommitSHA,String _newCommitSHA) {
		super();
		this.oldCommitSHA = _oldCommitSHA;
		this.newCommitSHA = _newCommitSHA;
		this.filesChangePairList = _fileChangePairList;
	}

	public List<FileChangePair> getFilesChangePairList() {
		return filesChangePairList;
	}

	public String getNewCommitSHA() {
		return newCommitSHA;
	}

	public String getOldCommitSHA() {
		return oldCommitSHA;
	}

	public  boolean hasJavaFile() {
		for(FileChangePair fileChangePair:this.getFilesChangePairList()) {
			if(fileChangePair.getNewFile()!=null && fileChangePair.getNewFile().endsWith(".java")) {
				return true;
			}
			else if(fileChangePair.getOldFile()!=null && fileChangePair.getOldFile().endsWith(".java")) {
				return true;
			}
		}
		return false;
	}
	public void print() {
		System.out.println("Old Commit SHA: "+oldCommitSHA+" New Commit SHA: "+newCommitSHA);
		for(FileChangePair fileChangePair:this.filesChangePairList) {
			System.out.println(fileChangePair);
			
		}
	}
}
