package com.sail.evaluatingevaluator.diff;

import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.io.IOUtils;
/** Sample Input Output
parvez@parvez-XPS-8700:~$ java -jar lhdiff.jar file1.txt file2.txt
LHDiff version: 1.0 (Type help for getting usage details)
1,1
2,2
3,_
4,3
5,4
*/
public class LHDiffChangeDescriptor {

	private HashSet<Integer> addedLines; //in the new file
	private HashSet<Integer> deletedLines; //in the old file.
	private HashSet<Integer> changedLines; //in the new line. To get the mapping , use the hmBackwardLineMapping
	private HashMap<Integer,Integer> hmForwardLineMapping;
	private HashMap<Integer,Integer> hmBackwardLineMapping;
	
	public LHDiffChangeDescriptor(String output, String oldFile, String newFile) {
		this.addedLines = new HashSet();
		this.changedLines = new HashSet();
		this.deletedLines = new HashSet();
		this.hmForwardLineMapping = new HashMap();
		this.hmBackwardLineMapping = new HashMap();
		this.parse(output, oldFile, newFile);
		
	}
	private void parse(String output, String oldFile, String newFile) {
		//step-1:convert the output into list of lines
		try {
			List<String> lineList = IOUtils.readLines(new StringReader(output));
			for(String line:lineList) {
				if(line.length()>0 && line.startsWith("LHDiff version:")==false) {
					String split[] = line.split(",");
					if(split[0].equals(split[1])){
						//lines not changed
						this.hmForwardLineMapping.put(Integer.parseInt(split[0]),Integer.parseInt(split[1]));
						this.hmBackwardLineMapping.put(Integer.parseInt(split[1]),Integer.parseInt(split[0]));
					}
					else if(split[1].equals("_")) {
						this.deletedLines.add(Integer.parseInt(split[0]));
					}
					else {
						this.changedLines.add(Integer.parseInt(split[1]));
						this.hmForwardLineMapping.put(Integer.parseInt(split[0]),Integer.parseInt(split[1]));
						this.hmBackwardLineMapping.put(Integer.parseInt(split[1]),Integer.parseInt(split[0]));
					}
				}
				
			}
			
			//now determine added lines in the new file
			List<String> newFileLineList = IOUtils.readLines(new FileReader(newFile));
			//System.out.println("New File Line List: "+newFileLineList.size()+"  "+this.changedLines);
			for(int i=0;i<newFileLineList.size();i++) {
				if(hmBackwardLineMapping.containsKey(i+1)==false) {
					this.addedLines.add(i+1);
				}
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	public HashSet<Integer> getAddedLines() {
		return addedLines;
	}
	public HashSet<Integer> getDeletedLines() {
		return deletedLines;
	}
	public HashSet<Integer> getChangedLines() {
		return changedLines;
	}
	public HashMap<Integer, Integer> getHmForwardLineMapping() {
		return hmForwardLineMapping;
	}
	public HashMap<Integer, Integer> getHmBackwardLineMapping() {
		return hmBackwardLineMapping;
	}
	public void print() {
		System.out.println("Added Lines: "+this.addedLines);
		System.out.println("Deleted Lines: "+this.deletedLines);
		System.out.println("Changed Lines: "+this.changedLines);
	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
