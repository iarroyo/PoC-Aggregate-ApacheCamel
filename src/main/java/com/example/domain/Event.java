package com.example.domain;

import java.util.Date;

public class Event {

	public String name;
	private Date date;
	
	public Event(String name){
		this.name=name;
		this.date= new Date();
	}
	
	public String getName(){
		return this.name;
	}
	
	public Date getDate(){
		return this.date;
	}
}
