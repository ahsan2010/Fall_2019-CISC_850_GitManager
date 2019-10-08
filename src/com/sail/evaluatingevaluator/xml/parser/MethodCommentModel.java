package com.sail.evaluatingevaluator.xml.parser;

public class MethodCommentModel {

	public String methodSignature = "";
	public String methodName = "";
	public String methodParameter = "";
	public String methodBody = "";
	public String innerComment = "";
	public String outerComment = "";
	
	
	String fucntionString = "";
	String commentString = "";
	
	
	
	
	public String getFucntionString() {
		return fucntionString;
	}
	public void setFucntionString(String fucntionString) {
		this.fucntionString = fucntionString;
	}
	public String getCommentString() {
		return commentString;
	}
	public void setCommentString(String commentString) {
		this.commentString = commentString;
	}
	public String getMethodSignature() {
		return methodSignature;
	}
	public void setMethodSignature(String methodSignature) {
		this.methodSignature = methodSignature;
	}
	public String getMethodName() {
		return methodName;
	}
	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}
	public String getMethodParameter() {
		return methodParameter;
	}
	public void setMethodParameter(String methodParameter) {
		this.methodParameter = methodParameter;
	}
	public String getMethodBody() {
		return methodBody;
	}
	public void setMethodBody(String methodBody) {
		this.methodBody = methodBody;
	}
	public String getInnerComment() {
		return innerComment;
	}
	public void setInnerComment(String innerComment) {
		this.innerComment = innerComment;
	}
	public String getOuterComment() {
		return outerComment;
	}
	public void setOuterComment(String outerComment) {
		this.outerComment = outerComment;
	}
}
