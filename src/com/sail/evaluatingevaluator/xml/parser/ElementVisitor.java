package com.sail.evaluatingevaluator.xml.parser;

import java.util.ArrayList;

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
	
	public ElementVisitor(XMLReader xmlReader) throws Exception {
		this.xmlReader = xmlReader;
	}

	StringBuffer sb = null;
	String [] classNameList = new String [20];
	int totalClass = -1;
	public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
		if (qName.equals("comment")) {
			System.out.println("Comment Got " + atts.getValue("type"));
			comment = true;
		}
		if(qName.equals("function")) {
			//System.out.println("This is function: " + uri.toString());
			sb = new StringBuffer();
			insideFunction = true;
		}
		if(qName.equals("Parameter_list")){
			insideParameter = true;
		}
		if(insideParameter && qName.equals("type")) {
			insideParameterType = true;
		}
		if(qName.equals("class")){
			insideClass = true;
		}
		if(insideClass && qName.equals("name")) {
			insideClassName = true;
		}
		commentJustEnd = false;
	}

	public void endElement(String uri, String localName, String qName) throws SAXException {
		if(qName.equals("comment")){
			commentJustEnd = true;
		}
		if(qName.equals("function")){
			insideFunction = false;
			System.out.println(sb.toString());
		}
		if(qName.equals("class")) {
			--totalClass;
		}
	}

	public void characters(char[] buffer, int start, int length) {
		String temp = new String(buffer, start, length);
		if(insideFunction) {
			temp = temp.trim();
			if(temp.length() > 0) {
				if(comment == false)
				sb.append(temp + " ");
			}
		}
		if(comment == true) {
			System.out.println(temp);
			comment = false;
		}
		if(insideClassName == true) {
			totalClass ++;
			classNameList[totalClass] = temp;
			
		}
		if(insideParameterType == true) {
			
		}
	}

}
