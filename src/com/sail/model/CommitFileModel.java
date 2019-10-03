package com.sail.model;

public class CommitFileModel {

	public String fileName;
	public String subSystem;
	public String changeType;
	public String lookupCommit;
	public String lookupCommitDate;
	
	
	public String getLookupCommit() {
		return lookupCommit;
	}
	public void setLookupCommit(String lookupCommit) {
		this.lookupCommit = lookupCommit;
	}
	public String getLookupCommitDate() {
		return lookupCommitDate;
	}
	public void setLookupCommitDate(String lookupCommitDate) {
		this.lookupCommitDate = lookupCommitDate;
	}
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public String getSubSystem() {
		return subSystem;
	}
	public void setSubSystem(String subSystem) {
		this.subSystem = subSystem;
	}
	public String getChangeType() {
		return changeType;
	}
	public void setChangeType(String changeType) {
		this.changeType = changeType;
	}
	
}
