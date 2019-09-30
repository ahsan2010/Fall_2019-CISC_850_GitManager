package com.sail.evaluatingevaluator.git.revision;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.io.IOUtils;

import com.sail.evaluatingevaluator.config.Config;
import com.sail.evaluatingevaluator.git.commit.Commit;
import com.sail.evaluatingevaluator.git.commit.CommitManager;
import com.sail.evaluatingevaluator.process.ProcessUtility;

public class RevisionFileChangeDescriptorManager implements Serializable{

	//collect all file change pairs in all revisions
	private ArrayList<RevisionFileChangeDescriptor> revisionFileChangeDescriptorList;
	private CommitManager commitManager;
	
	public RevisionFileChangeDescriptorManager(CommitManager _commitManager) {
		this.revisionFileChangeDescriptorList = new ArrayList();
		this.commitManager = _commitManager;
	}
	
	public void run() throws IOException, InterruptedException {
		
		//Step-1: First commit is the old commit. For the very first commit all files have been added. 
		//So we need to determine list of all files in that particular revision and include them in the added file list
		List<Commit> commitList = commitManager.getCommitList();
		Commit firstCommit = commitList.get(0);
		ArrayList<String> fileList = RevisionFileListCollector.getFileList(firstCommit.getSha(),commitManager.getRepositoryPath());
		ArrayList<FileChangePair> fileChangePairList = new ArrayList();
		for(String file:fileList) {
			FileChangePair fileChangePair = new FileChangePair(FileChangeType.ADDED);
			fileChangePair.setNewFile(file);
			fileChangePair.setOldFile(null);
			fileChangePairList.add(fileChangePair);
		}
		String newCommitSHA = firstCommit.getSha();
		String oldCommitSHA = null;
		RevisionFileChangeDescriptor firstRevisionFileChangeDescriptor = new RevisionFileChangeDescriptor(fileChangePairList,oldCommitSHA,newCommitSHA);
		this.revisionFileChangeDescriptorList.add(firstRevisionFileChangeDescriptor);
		System.out.println("Total Commits: "+commitList.size());

		//Step-2: We need to build RevisionFileChangeDescriptor for the remaining revisions (r>0)
		for(int commitIndex=1;commitIndex<commitList.size();commitIndex++) {
			String currentCommitSHA  = commitList.get(commitIndex).getSha();
			String previousCommitSHA = commitList.get(commitIndex-1).getSha();
			
			//System.out.println("current: "+commitList.get(commitIndex).getDate());
			//System.out.println("old: "+commitList.get(commitIndex-1).getDate());
			
			ProcessBuilder pb = new ProcessBuilder("git","diff","-C","-M","-w","--no-color","--name-status",previousCommitSHA,currentCommitSHA);
			System.out.println("Command: "+"git"+" diff"+" -C"+" -M"+" -w"+" --no-color"+" --name-status "+previousCommitSHA+" "+currentCommitSHA);
			pb.directory(new File(commitManager.getRepositoryPath()));
			Process process = pb.start();
			String output = ProcessUtility.output(process.getInputStream());
			int errCode = process.waitFor();
			System.out.println(output);
			RevisionFileChangeDescriptor revisionFileChangeDescriptor = this.parse(output, commitIndex, commitList);
			this.revisionFileChangeDescriptorList.add(revisionFileChangeDescriptor);
		}
		
	}
	
	public RevisionFileChangeDescriptor parse(String output, int commitIndex, List<Commit> commitList) throws IOException {
		//step-1:convert the output into list of lines 
		List<String> lineList = IOUtils.readLines(new StringReader(output));
		List<FileChangePair> fileChangePairList = new ArrayList();
		for(String line:lineList) {
			System.out.println(line);
			if(line.length()>0 && line.matches("\\s+")==false){
				String split[] = line.split("\\s+");
				
				if(split[0].equals("A") && split[1].trim().endsWith(".java")) {
					FileChangePair fileChangePair = new FileChangePair(FileChangeType.ADDED);
					fileChangePair.setNewFile(split[1]);
					fileChangePairList.add(fileChangePair);
				}
				else if(split[0].equals("D") && split[1].trim().endsWith(".java")) {
					FileChangePair fileChangePair = new FileChangePair(FileChangeType.DELETED);
					fileChangePair.setOldFile(split[1]);
					fileChangePairList.add(fileChangePair);
				}
				else if(split[0].equals("M") && split[1].trim().endsWith(".java")) {
					FileChangePair fileChangePair = new FileChangePair(FileChangeType.CHANGED);		
					fileChangePair.setOldFile(split[1]);
					fileChangePair.setNewFile(split[1]);
					fileChangePairList.add(fileChangePair);
				}
				else if(split[0].startsWith("R") && split[1].trim().endsWith(".java")) {
					FileChangePair fileChangePair = new FileChangePair(FileChangeType.RENAMED);
					fileChangePair.setOldFile(split[1]);		
					fileChangePair.setNewFile(split[2]);
					fileChangePairList.add(fileChangePair);
				}
				else if(split[0].startsWith("C") && split[1].trim().endsWith(".java")) {
					FileChangePair fileChangePair = new FileChangePair(FileChangeType.COPIED);
					fileChangePair.setOldFile(split[1]);
					fileChangePair.setNewFile(split[2]);
					fileChangePairList.add(fileChangePair);
				}
				else {
					//System.out.println(line);
					//what is happening here?
					//throw new RuntimeException("Exception in parsing file change information");
				}
			}
		}
		String oldCommitSHA = commitList.get(commitIndex-1).getSha();
		String newCommitSHA = commitList.get(commitIndex).getSha();
	
		RevisionFileChangeDescriptor revisionFileChangeDescriptor = new RevisionFileChangeDescriptor(fileChangePairList,oldCommitSHA,newCommitSHA);
		return revisionFileChangeDescriptor;
	}
	
	public ArrayList<RevisionFileChangeDescriptor> getRevisionFileChangeDescriptorList() {
		return revisionFileChangeDescriptorList;
	}

	public CommitManager getCommitManager() {
		return commitManager;
	}

	public void print() {
		for(RevisionFileChangeDescriptor rfc:this.revisionFileChangeDescriptorList) {
			rfc.print();
		}
	}
	
	public void save(File file) {
	    try
        {    
            //Saving of object in a file 
            FileOutputStream fos = new FileOutputStream(file); 
            ObjectOutputStream out = new ObjectOutputStream(fos); 
              
            // Method for serialization of object 
            out.writeObject(this); 
              
            out.close(); 
            fos.close(); 
              
            System.out.println("Object has been serialized: "+file.getName());
        } 
          
        catch(IOException ex) 
        { 
            System.out.println("IOException is caught"); 
        } 
		
	}
	public static RevisionFileChangeDescriptorManager load(File file) throws ClassNotFoundException {
	    try
        {    
            //Saving of object in a file 
            FileInputStream fis = new FileInputStream(file); 
            ObjectInputStream in = new ObjectInputStream(fis); 
              
            // Method for serialization of object 
            RevisionFileChangeDescriptorManager revisionFileChangeDescriptorManager  = (RevisionFileChangeDescriptorManager)in.readObject(); 
            in.close(); 
            fis.close(); 
            System.out.println("Object has been deserialized: "+file.getName());
            return revisionFileChangeDescriptorManager;  
        } 
          
        catch(IOException ex) 
        { 
            System.out.println("IOException is caught"); 
        } 
	    return null;
	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		CommitManager commitManager = new CommitManager(Config.REPOSITORY_PATH);
		try {
			commitManager.run();
			RevisionFileChangeDescriptorManager revisionFileChangeDescriptorManager = new RevisionFileChangeDescriptorManager(commitManager);
			System.out.println("Total Changes: " + revisionFileChangeDescriptorManager.getRevisionFileChangeDescriptorList().size());
			revisionFileChangeDescriptorManager.run();
			revisionFileChangeDescriptorManager.print();
			System.out.println("Total Revision File Change List: "+revisionFileChangeDescriptorManager.getRevisionFileChangeDescriptorList().size());
			commitManager.save(new File(Config.COMMIT_DATA_PATH));
			revisionFileChangeDescriptorManager.save(new File(Config.REVISION_DATA_PATH));
			
		} catch (InterruptedException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
