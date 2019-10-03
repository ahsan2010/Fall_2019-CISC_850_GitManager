package com.sail.evaluatingevaluator.xml.parser;

import java.util.HashMap;
import java.util.Map;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

public class ElementVisitor extends DefaultHandler {

	private XMLReader xmlReader;
	boolean comment = false;
	boolean commentJustEnd = false;
	boolean insideFunction = false;
	boolean insideParameter = false;
	boolean insideParameterType = false;
	boolean insideClass = false;
	boolean insideClassName = false;
	boolean insidefunctionName = false;
	boolean insideType = false;
	boolean insideName = false;
	
	public Map<String,MethodCommentModel> moethodListClass;
	
	
	public ElementVisitor(XMLReader xmlReader, Map<String,MethodCommentModel> moethodListClass) throws Exception {
		this.xmlReader = xmlReader;
		this.moethodListClass = moethodListClass;
	}

	StringBuffer sbComment = null;
	StringBuffer sbParameter = null;
	StringBuffer sbInnerComment = null;
	StringBuffer sbOuterComment = new StringBuffer();
	StringBuffer sbFunctionName = null;
	StringBuffer sbFunctionBody = null;
	
	String [] classNameList = new String [20];
	int totalClass = -1;
	
	boolean typeJustClosed = false;
	boolean flagFirst = false;
	
	MethodCommentModel mm = null;
	
	public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
		if (qName.equals("comment")) {
			//System.out.println("Comment Got " + atts.getValue("type"));
			if(!insideFunction) {
				sbComment = new StringBuffer();
			}
			comment = true;
		}
		if(qName.equals("function")) {
			//System.out.println("This is function: " + uri.toString());
			mm = new MethodCommentModel();
			sbInnerComment = new StringBuffer();
			sbFunctionName = new StringBuffer();
			sbFunctionBody = new StringBuffer();
			insideFunction = true;
			flagFirst = true;
			
			if(commentJustEnd == true) {
				sbOuterComment = sbComment;
			}
		}
		if(qName.equals("parameter_list")){
			insideParameter = true;
			sbParameter = new StringBuffer();
		}
		if(insideParameter && qName.equals("type")) {
			insideParameterType = true;
		}
		if(qName.equals("class")){
			insideClass = true;
		}
		if(qName.equals("name")) {
			insideName = true;
			if(insideClass) {
				insideClassName = true;
			}
			if(insideFunction){
				insidefunctionName = true;
			}
			
		}else {
			typeJustClosed = false;
		}
		if(qName.equals("type")) {
			insideType = true;
		}
		commentJustEnd = false;
	}

	public void endElement(String uri, String localName, String qName) throws SAXException {
		if(qName.equals("comment")){
			commentJustEnd = true;
		}
		if(qName.equals("function")){
			String methodSignature = classNameList[totalClass] +"."+ sbFunctionName+"-"+sbParameter;
			mm.setMethodSignature(methodSignature);
			mm.setMethodBody(sbFunctionBody.toString());
			mm.setInnerComment(sbInnerComment.toString());
			mm.setOuterComment(sbOuterComment.toString());
			insideFunction = false;
			moethodListClass.put(methodSignature, mm);
			
			
			sbOuterComment = new StringBuffer();
			
		}
		if(qName.equals("class")) {
			--totalClass;
			insideClassName = false;
		}
		if(qName.equals("Parameter_list")) {
			insideParameter = false;
			insideParameterType = false;
		}
		if(qName.equals("type")) {
			insideType = false;
			typeJustClosed = true;
		}
		if(qName.equals("name")) {
			insideName = false;
		}
	}
	public void characters(char[] buffer, int start, int length) {
		String temp = new String(buffer, start, length);
		if(insideFunction) {
			temp = temp.trim();
			if(temp.length() > 0) {
				if(comment == false)
				sbFunctionBody.append(temp + " ");
			}
			if((insidefunctionName && typeJustClosed)) {
				sbFunctionName.append(temp);
				insidefunctionName = false;
				flagFirst = false;
			}
		}
		if(comment == true) {
			if(insideFunction == true) {
				sbInnerComment.append(temp +" ");
			}else {
				sbComment.append(temp+" ");
			}
			comment = false;
		}
		if(insideClassName == true) {
			totalClass ++;
			classNameList[totalClass] = temp;
			insideClassName = false;
			insideClass = false;
		}
		
		if(insideName && insideParameterType) {
			//System.out.println("PARAMETER == " + temp);
			sbParameter.append(temp+" ");
			insideParameterType = false;
		}
	}
}
