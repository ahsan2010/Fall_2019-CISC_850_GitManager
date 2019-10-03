package com.sail.model;

import java.util.ArrayList;

public class CommitModel {

	public String commitId;
	public String commitDate;
	
	
	ArrayList<CommitFileModel> changedFileList = new ArrayList<CommitFileModel>();
 	
	public String getCommitId() {
		return commitId;
	}
	public void setCommitId(String commitId) {
		this.commitId = commitId;
	}
	public String getCommitDate() {
		return commitDate;
	}
	public void setCommitDate(String commitDate) {
		this.commitDate = commitDate;
	}
	public ArrayList<CommitFileModel> getChangedFileList() {
		return changedFileList;
	}
	public void setChangedFileList(ArrayList<CommitFileModel> changedFileList) {
		this.changedFileList = changedFileList;
	}
	
}
