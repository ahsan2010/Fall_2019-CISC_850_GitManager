package com.sail.evaluatingevaluator.git.revision;

import java.io.Serializable;
import java.util.Optional;

public class FileChangePair implements Serializable{
	private String oldFile;
	private String newFile;
	private FileChangeType fileChangeType;
	
	public FileChangePair(FileChangeType _fileChangeType) {
		this.oldFile = null;
		this.newFile = null;
		this.fileChangeType = _fileChangeType;
	}

	public String getOldFile() {
		return oldFile;
	}

	public void setOldFile(String oldFile) {
		this.oldFile = oldFile;
	}

	public String getNewFile() {
		return newFile;
	}

	public void setNewFile(String newFile) {
		this.newFile = newFile;
	}
	
	public FileChangeType getFileChangeType() {
		return fileChangeType;
	}

	@Override
	public String toString() {
		return "FileChangePair [oldFile=" + this.getOldFile() + ", newFile=" + this.getNewFile() + ", fileChangeType=" + this.getFileChangeType()
				+ "]";
	}
}
