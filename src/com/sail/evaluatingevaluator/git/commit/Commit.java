package com.sail.evaluatingevaluator.git.commit;

import java.io.Serializable;

public class Commit implements Serializable{

	public String author;
	public String date;
	public String message;
	public String sha;
	
	public Commit() {
		
	}
	public Commit(String author, String date, String message, String sha) {
		super();
		this.author = author;
		this.date = date;
		this.message = message;
		this.sha = sha;
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	
	public String getSha() {
		return sha;
	}

	public void setSha(String sha) {
		this.sha = sha;
	}

	public void print() {
		System.out.println("Commit SHA: "+this.getSha());
		System.out.println("Author: "+this.getAuthor());
		System.out.println("Date: "+this.getDate());
		System.out.println("Message: "+this.getMessage());
	}
}
