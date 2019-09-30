package com.sail.evaluatingevaluator.git.revision;

import java.io.Serializable;

public enum FileChangeType implements Serializable{
	ADDED, DELETED, CHANGED, COPIED, RENAMED; 
}
