package com.sail.evaluatingevaluator.xml.parser;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.StringReader;

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
	public void ParseXml (String xml) throws Exception {			
				
		        SAXParserFactory spf = SAXParserFactory.newInstance();
		        SAXParser sp = spf.newSAXParser();
		        XMLReader xr = sp.getXMLReader();		       
		        xr.setContentHandler(new ElementVisitor(xr));		      
		        try {
		        	xr.parse(new InputSource(new StringReader(xml)));
		           } catch (SAXException e) {
		            e.printStackTrace();
		        }   
		      
	}
	public static void main(String[] args) throws Exception{
		Parser p = new Parser();
		p.readFileContent();
		p.ParseXml(p.sb.toString());
		System.out.println("Program finishes successfully");
	}
}