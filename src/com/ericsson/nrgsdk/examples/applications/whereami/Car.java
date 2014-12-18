package com.ericsson.nrgsdk.examples.applications.whereami;

import com.ericsson.nrgsdk.examples.applications.whereami.Configuration;
import com.ericsson.nrgsdk.examples.applications.whereami.Feature;

public class Car 
{
	private Feature parent;
	
	private boolean engineOn;
	
	private boolean isClosed; 
	
	public Abonent driver; 
	
	public Car(Feature parent)
	{
		this.parent = parent;
		this.driver = null;
		this.engineOn = false;
		this.isClosed = true;
		System.out.println("Engine is off");
	}
	
	public void setDriver(Abonent driver){
		this.driver = driver;
		System.out.println("Driver is abonent " + driver.getNumer() );
	}
	
	public Abonent getDriver(){
		return this.driver;
	}
	
	public void setEngineOn(boolean engineOn){
		this.engineOn = engineOn;
		System.out.println("Engine is " + (engineOn ? "on" : "off") );
	}
	
	public boolean getEngineOn(){
		return this.engineOn;
	}
	
	public void setIsClosed(boolean isClosed){
		this.isClosed = isClosed;
		System.out.println("Car is " + (isClosed ? "closed" : "open") );
	}
	
	public boolean getIsClosed(){
		return this.isClosed;
	}
}
