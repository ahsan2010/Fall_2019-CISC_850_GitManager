package com.sail.evaluatingevaluator.xml.parser;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.StringReader;
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
public class Parser {
	StringBuffer sb = new StringBuffer();
	public void readFileContent() throws Exception{
		String path = "/Users/mdahasanuzzaman/Documents/Queens_Phd/Fall_2019_Courses/CISC-850/Assignments/Result/DebryPlocy_Output-2.xml";
		BufferedReader reader = new BufferedReader(new FileReader(path));
		String line = "";	
		while((line = reader.readLine()) != null) {
			sb.append(line);
		}
	}
	
	public Map<String,MethodCommentModel> getClassInformation(String xml) throws Exception{
		Map<String,MethodCommentModel> methodListClass = new HashMap<String,MethodCommentModel>();
		ParseXml(xml,methodListClass);
		return methodListClass;
	}
	
	public void ParseXml (String xml,Map<String,MethodCommentModel> methodListClass) throws Exception {
		        SAXParserFactory spf = SAXParserFactory.newInstance();
		        SAXParser sp = spf.newSAXParser();
		        XMLReader xr = sp.getXMLReader();		       
		        xr.setContentHandler(new ElementVisitor(xr,methodListClass));		      
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
		Parser p = new Parser();
		p.readFileContent();
		Map<String,MethodCommentModel> methodListClass = p.getClassInformation(p.sb.toString());
		 for(String key : methodListClass.keySet()) {
		    	System.out.println(key);
		    	MethodCommentModel mm = methodListClass.get(key);
		    	System.out.println(mm.getMethodSignature());
				System.out.println("OutComment: " + mm.getOuterComment());
				System.out.println("InnerComment: " + mm.getInnerComment());
				System.out.println("Body: " + mm.getMethodBody());
				System.out.println("----------------------");
		    }
		System.out.println("Program finishes successfully");
	}
}