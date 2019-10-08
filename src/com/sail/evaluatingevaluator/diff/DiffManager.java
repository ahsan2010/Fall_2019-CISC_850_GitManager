package com.sail.evaluatingevaluator.diff;

import java.util.HashMap;
import java.util.List;

import com.sail.evaluatingevaluator.git.commit.CommitManager;

// the objective of this class is to determine all changes in each revision and in each file
public class DiffManager {
	
	private CommitManager commitManager;
	private String repositoryPath;
	private HashMap<String,List<String>> hmChnangeInfo;
	
	public DiffManager(String _repositoryPath,CommitManager _commitManager) {
		this.repositoryPath = _repositoryPath;
		this.commitManager = _commitManager;
		this.hmChnangeInfo = new HashMap();
	}
	
	public void run() {
		
	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
