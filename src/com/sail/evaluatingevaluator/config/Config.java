package com.sail.evaluatingevaluator.config;

import java.io.File;

import org.eclipse.jdt.core.dom.QualifiedName;
import org.jrubyparser.parser.ReOptions;

public class Config {

	//subject systems: antlr4, commons-lang,jEdit, jgit, log4j, pdfbox, storm
	public static  String ROOT_PATH = "/home/local/SAIL/parvezku01/research/CodeCompletionEvaluation/repositories";
	public static  String REPOSITORY_NAME = "commons-lang"; 
	public static  String REPOSITORY_PATH = "/home/ahsan/Documents/Queens_PHD/Courses/Fall_2019/CISC_850/Assignment/Assignment/hive/";
	public static  String REPOSITORY_REVISION_PATH = ROOT_PATH + File.separator + REPOSITORY_NAME + "_revisions";
	public static  String REPOSITORY_DATA_PATH = ROOT_PATH + File.separator + REPOSITORY_NAME+"_data";
	public static  String REPOSITORY_OUTPUT_PATH = REPOSITORY_DATA_PATH+File.separator+"output";
	
	//the following file contain list of all method call entries for all revisions of a repository
	public static  String METHOD_CALL_ENTRIES_PATH = ROOT_PATH+File.separator+REPOSITORY_NAME+"_data"+File.separator+REPOSITORY_NAME+"_revision_mce";
	public static  String COMMIT_DATA_PATH = ROOT_PATH+File.separator+REPOSITORY_NAME+"_data"+File.separator+REPOSITORY_NAME+"_commit.dat";
	public static  String REVISION_DATA_PATH = ROOT_PATH+File.separator+REPOSITORY_NAME+"_data"+File.separator+REPOSITORY_NAME+"_revision.dat";
	public static  String METHOD_CALL_ENTRIES_BASH_PATH = ROOT_PATH+File.separator+REPOSITORY_NAME+"_data"+File.separator+REPOSITORY_NAME+"_bash.sh";
	
	public static final int MAXIMUM_REVISION_PER_RUN = 100;
	
	//Ignore the remaining entries. We already considered only the java file
	public static  String REPOSITORY_BLAME_PATH = ROOT_PATH + File.separator+REPOSITORY_NAME+"_blame";
	public static  String REPOSITORY_BLAME_INDEX_PATH = ROOT_PATH + File.separator+REPOSITORY_NAME+"_blame"+File.separator+"blame_index.txt";
	
	public static  String PER_REVISION_DATA_PATH = ROOT_PATH + File.separator + REPOSITORY_NAME + "_per_revision_data";
	public static  String EXTERNAL_DEPENDENCY_PATH = ROOT_PATH + File.separator+ REPOSITORY_NAME + "_dependencies";
	public static  String CHANGE_INSTANCES_PATH = ROOT_PATH + File.separator+ REPOSITORY_NAME + "_change";
	public static  String FREQUENCY_PATH = ROOT_PATH + File.separator+ REPOSITORY_NAME + "_frequency";
	
	public static final String[] FILE_EXTENSIONS = {".java"};
	public static void init(String repositoryName) {
		REPOSITORY_NAME = repositoryName; 
		
		ROOT_PATH = "/home/local/SAIL/parvezku01/research/CodeCompletionEvaluation/repositories";
		REPOSITORY_PATH = ROOT_PATH + File.separator + REPOSITORY_NAME;
		REPOSITORY_REVISION_PATH = ROOT_PATH + File.separator + REPOSITORY_NAME + "_revisions";
		REPOSITORY_DATA_PATH = ROOT_PATH + File.separator + REPOSITORY_NAME+"_data";
		REPOSITORY_OUTPUT_PATH = REPOSITORY_DATA_PATH+File.separator+"output";
		
		//the following file contain list of all method call entries for all revisions of a repository
		METHOD_CALL_ENTRIES_PATH = ROOT_PATH+File.separator+REPOSITORY_NAME+"_data"+File.separator+REPOSITORY_NAME+"_revision_mce";
		COMMIT_DATA_PATH = ROOT_PATH+File.separator+REPOSITORY_NAME+"_data"+File.separator+REPOSITORY_NAME+"_commit.dat";
		REVISION_DATA_PATH = ROOT_PATH+File.separator+REPOSITORY_NAME+"_data"+File.separator+REPOSITORY_NAME+"_revision.dat";
		METHOD_CALL_ENTRIES_BASH_PATH = ROOT_PATH+File.separator+REPOSITORY_NAME+"_data"+File.separator+REPOSITORY_NAME+"_bash.sh";
		
		//Ignore the remaining entries. We already considered only the java file
		REPOSITORY_BLAME_PATH = ROOT_PATH + File.separator+REPOSITORY_NAME+"_blame";
		REPOSITORY_BLAME_INDEX_PATH = ROOT_PATH + File.separator+REPOSITORY_NAME+"_blame"+File.separator+"blame_index.txt";
		
		PER_REVISION_DATA_PATH = ROOT_PATH + File.separator + REPOSITORY_NAME + "_per_revision_data";
		EXTERNAL_DEPENDENCY_PATH = ROOT_PATH + File.separator+ REPOSITORY_NAME + "_dependencies";
		CHANGE_INSTANCES_PATH = ROOT_PATH + File.separator+ REPOSITORY_NAME + "_change";
		FREQUENCY_PATH = ROOT_PATH + File.separator+ REPOSITORY_NAME + "_frequency";
	}
	
	//public static final String FRAMEWORKS[] = {"javax.swing.","java.awt.","java."};
	
	public static final String FRAMEWORKS[] = {"javax.swing.","java.awt.","java.util.","java.math.","java.nio.",
			"java.net.","java.security.","java.sql.","java.text.","java.rmi.","java.beans.",
			"java.time."};
	
	//public static final String LHDIFF_PATH = "/home/parvez/lhdiff.jar";
	//public static final String COMMIT_RESULT = "/home/local/SAIL/parvezku01/research/CodeCompletionEvaluation"+File.separator+REPOSITORY_NAME+"_commit.csv";
	//public static final String CLASSICAL_RESUL = "/home/parvez/research/historic_evaluation"+File.separator+REPOSITORY_NAME+"_classic.csv";
	
	public static boolean fileExtensionMatches(String fileName) {
		for(String extension:FILE_EXTENSIONS) {
			if(fileName.endsWith(extension))
				return true;
		}
		return false;
	}
	public static boolean isInteresting(String qualifiedTypeName) {
		/*for(String prefix:FRAMEWORKS) {
			if(qualifiedTypeName.startsWith(prefix)) return true;
		}
		return false;*/
		//return true;
		if(qualifiedTypeName.startsWith("java.")||qualifiedTypeName.startsWith("javax."))  {
			return true;
		}
		else {
			return false;
		}
		/*if(qualifiedTypeName.startsWith("javax.")|| qualifiedTypeName.startsWith("java.util.")|| qualifiedTypeName.startsWith("java.io.")||
				qualifiedTypeName.startsWith("java.awt.")){
					return true;
				}
		else return false;*/
		//return true;
	}
	public static String getRepositoryFolderName() {
		File file = new File(Config.REPOSITORY_PATH);
		return file.getName();
	}
}
