package com.ericsson.nrgsdk.examples.applications.whereami;

public class Abonent extends Thread
{
	private String numer;
	
	private LocationProcessor itsLocationProcessor;
	
	public String getNumer() 
	{
		return numer;
	}

	public Abonent(String numer, LocationProcessor aLocationProcessor)
	{
		this.numer = numer;
		itsLocationProcessor = aLocationProcessor;
	}
	
	public void run()
	{
		while (true)
		{
			//System.out.println("Abonent o numerze " + numer + " sprawdza swoj� lokalizacj�");
			//itsLocationProcessor.requestLocation(numer);
			try
			{
				Thread.sleep(20000);
			}
			catch (IllegalArgumentException e)
			{
				System.out.println("IllegalArgumentException" + e.getMessage());
			}
			catch (InterruptedException e)
			{
				System.out.println("InterruptedException" + e.getMessage());
			}
		}
	}
}
