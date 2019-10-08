package com.sail.evaluatingevaluator.xml.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

public class ElementVisitor2 extends DefaultHandler {

	private XMLReader xmlReader;
	boolean comment = false;
	boolean commentJustEnd = false;
	
	boolean commentJustEndForLine = false;
	boolean insideFunction = false;
	
	Map<String,ArrayList<MethodCommentModel>> methodModelPerClassList = new HashMap<String,ArrayList<MethodCommentModel>>(); 
	String fileName = "";
	ArrayList<MethodCommentModel> methodList;
	
	public ElementVisitor2(XMLReader xmlReader, Map<String,ArrayList<MethodCommentModel>> methodModelPerClassList) throws Exception {
		this.xmlReader = xmlReader;
		this.methodModelPerClassList = methodModelPerClassList;
	}
	
	MethodCommentModel mm = null;
	
	String lineStartComment = "";
	String lineEndComment 	= "";
	
	String lineStartFunction = "";
	String lineEndFunction = "";
	
	String commentLineString = "";
	String functionLineString = "";
	
	
	boolean conrerCaseComment = false;	
	boolean takeFirstLine = true;
	
	boolean newClassFileStart = false;
	
	public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
		
		if (qName.equals("unit") &&  atts.getValue("filename") != null) {
			newClassFileStart = true;
			fileName = atts.getValue("filename"); 
		}		
		
		if (qName.equals("comment")) {
			comment = true;
		}		
		if(comment == true){
			lineStartComment = atts.getValue("pos:line");
		}		

		if(qName.equals("function")) {
			insideFunction = true;
		}

		if(insideFunction){
			String line = atts.getValue("pos:line");
			if(line != null){
				if(takeFirstLine){
					lineStartFunction = atts.getValue("pos:line");
					takeFirstLine = false;
				}
			}
			
			if(!comment){
				if(line != null){
					lineEndFunction = line;
				}
			}
			if(comment){
				functionLineString += lineStartFunction + "-" + lineEndFunction + "_";
				conrerCaseComment = true;
			}
		}
		
		if(comment == false && insideFunction && conrerCaseComment){
			String line = atts.getValue("pos:line");
			if(line != null){
				lineStartComment = line;
				lineStartFunction = line;
				conrerCaseComment = false;
			}
		}
		
		if(commentJustEnd == true && insideFunction){
			commentJustEndForLine = true;
			String line = atts.getValue("pos:line");
			commentJustEnd = false;
		}else{
			commentJustEnd = false;
		}
		
		if(commentJustEndForLine == true){
			String line = atts.getValue("pos:line");
			if(line != null){
				if(insideFunction){
					commentLineString += lineStartComment + "-" + line +"_";
				}
				commentJustEndForLine = false;
			}
		}
	}

	public void endElement(String uri, String localName, String qName) throws SAXException {
		
		if (qName.equals("unit")) {
			
			methodModelPerClassList.put(fileName, methodList);
			methodList = new ArrayList<MethodCommentModel>();
			
			newClassFileStart = false;
			System.out.println("Finish Class");
		}	
		
		if(qName.equals("comment")){
			commentJustEnd = true;
			comment = false;
		}
		if(qName.equals("function")){
			insideFunction = false;
			functionLineString += lineStartFunction + "-" + lineEndFunction + "_";

			takeFirstLine = true;
			
			MethodCommentModel mm = new MethodCommentModel();
			mm.setCommentString(commentLineString);
			mm.setFucntionString(functionLineString);
			methodList.add(mm);
			System.out.println("Fun: " + functionLineString);
			commentLineString = "";
			functionLineString = "";
		}
	}
	public void characters(char[] buffer, int start, int length) {
		
	}
}
