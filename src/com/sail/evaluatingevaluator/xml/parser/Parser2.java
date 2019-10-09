package com.sail.evaluatingevaluator.xml.parser;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 *
 * @author root
 */
public class Parser2 {
	StringBuffer sb = new StringBuffer();
	public void readFileContent() throws Exception{
		String path = "/home/ahsan/Documents/Queens_PHD/Courses/Fall_2019/CISC_850/Assignment/output.xml";
		BufferedReader reader = new BufferedReader(new FileReader(path));
		String line = "";	
		while((line = reader.readLine()) != null) {
			sb.append(line);
			//System.out.println(line);
		}
	}
	
	public Map<String,ArrayList<MethodCommentModel>> getClassInformation(String xml) throws Exception{
		//Map<String,MethodCommentModel> methodListClass = new HashMap<String,MethodCommentModel>();
		
		Map<String,ArrayList<MethodCommentModel>> methodListClass = new HashMap<String,ArrayList<MethodCommentModel>>();
		//ArrayList<MethodCommentModel> methodListClass = new ArrayList<MethodCommentModel>();
		
		ParseXml(xml,methodListClass);
		return methodListClass;
	}
	
	public void ParseXml (String xml, Map<String,ArrayList<MethodCommentModel>> methodListClass) throws Exception {
		        SAXParserFactory spf = SAXParserFactory.newInstance();
		        SAXParser sp = spf.newSAXParser();
		        XMLReader xr = sp.getXMLReader();		       
		        xr.setContentHandler(new ElementVisitor2(xr,methodListClass));		      
		        try {
		        	xr.parse(new InputSource(new StringReader(xml)));
		           } catch (SAXException e) {
		            e.printStackTrace();
		        }   
		    /*for(String key : methodListClass.keySet()) {
		    	System.out.println(key);
		    	MethodCommentModel mm = methodListClass.get(key);
		    	System.out.println(mm.getMethodSignature());
				System.out.println("OutComment: " + mm.getOuterComment());
				System.out.println("InnerComment: " + mm.getInnerComment());
				System.out.println("Body: " + mm.getMethodBody());
				System.out.println("----------------------");
		    }*/
	}
	
	public static void main(String[] args) throws Exception{
		Parser2 p = new Parser2();
		p.readFileContent();
		Map<String,ArrayList<MethodCommentModel>> methodListClass = p.getClassInformation(p.sb.toString());
		
		System.out.println("SIZE: " + methodListClass.size() );
		
		for(String fileName : methodListClass.keySet()){
			for(MethodCommentModel mm : methodListClass.get(fileName)){
				System.out.println(fileName);
				System.out.println("Comment: " + mm.getCommentString());
				System.out.println("Function: " + mm.getFucntionString());
				System.out.println("------------------------------");
			}
		}
		System.out.println("Program finishes successfully");
	}
}