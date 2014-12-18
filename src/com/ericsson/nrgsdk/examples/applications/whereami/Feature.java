/*
 * **************************************************************************
 * *                                                                        *
 * * Ericsson hereby grants to the user a royalty-free, irrevocable,        *
 * * worldwide, nonexclusive, paid-up license to copy, display, perform,    *
 * * prepare and have prepared derivative works based upon the source code  *
 * * in this sample application, and distribute the sample source code and  *
 * * derivative works thereof and to grant others the foregoing rights.     *
 * *                                                                        *
 * * ERICSSON DISCLAIMS ALL WARRANTIES WITH REGARD TO THIS SOFTWARE,        *
 * * INCLUDING ALL IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS.       *
 * * IN NO EVENT SHALL ERICSSON BE LIABLE FOR ANY SPECIAL, INDIRECT OR      *
 * * CONSEQUENTIAL DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS    *
 * * OF USE, DATA OR PROFITS, WHETHER IN AN ACTION OF CONTRACT, NEGLIGENCE  *
 * * OR OTHER TORTIOUS ACTION, ARISING OUT OF OR IN CONNECTION WITH THE USE *
 * * OR PERFORMANCE OF THIS SOFTWARE.                                       *
 * *                                                                        *
 * **************************************************************************
 */

package com.ericsson.nrgsdk.examples.applications.whereami;

import javax.swing.ImageIcon;

import com.ericsson.hosasdk.api.HOSAMonitor;
import com.ericsson.hosasdk.api.TpAddress;
import com.ericsson.hosasdk.api.TpHosaSendMessageError;
import com.ericsson.hosasdk.api.TpHosaSendMessageReport;
import com.ericsson.hosasdk.api.TpHosaUIMessageDeliveryStatus;
import com.ericsson.hosasdk.api.fw.P_UNKNOWN_SERVICE_TYPE;
import com.ericsson.hosasdk.api.hui.IpAppHosaUIManager;
import com.ericsson.hosasdk.api.hui.IpHosaUIManager;
import com.ericsson.hosasdk.api.mm.ul.IpUserLocation;
import com.ericsson.hosasdk.api.ui.IpAppUI;
import com.ericsson.hosasdk.api.ui.TpUIEventInfo;
import com.ericsson.hosasdk.api.ui.TpUIEventNotificationInfo;
import com.ericsson.hosasdk.api.ui.TpUIIdentifier;
import com.ericsson.hosasdk.utility.framework.FWproxy;
import com.ericsson.nrgsdk.examples.tools.SDKToolkit;

/**
 * This class implements the logic of the application. It uses processors to
 * interact with Ericsson Network Resource Gateway.
 */
public class Feature{

	private FWproxy itsFramework;

	private IpHosaUIManager itsHosaUIManager;

	private IpUserLocation itsOsaULManager;

	private SMSProcessor itsSMSProcessor;

	private MMSProcessor itsMMSProcessor;

	private LocationProcessor itsLocationProcessor;

	private GUI theGUI;

	private Integer assignmentId;
	
	private Car car;
	private Abonent[] abonents; //maxymalna ilosc 10 abonentow
	private int i = 0;

	/**
	 * Initializes a new instance, without starting interaction with Ericsson
	 * Network Resource Gateway (see start)
	 * 
	 * @param aGUI
	 *            the GUI of the application
	 */
	public Feature(GUI aGUI) {
		theGUI = aGUI;
		aGUI.setTitle("Car manage");
		aGUI.addTab("Description", getDescription());

	}

	/**
	 * Starts interaction with the Ericsson Network Resource Gateway. Note: this
	 * method is intended to be called at most once.
	 */
	protected void start() {
		HOSAMonitor.addListener(SDKToolkit.LOGGER);
		itsFramework = new FWproxy(Configuration.INSTANCE);
        try
        {
    		itsHosaUIManager = (IpHosaUIManager) itsFramework
    				.obtainSCF("SP_HOSA_USER_INTERACTION");
    		itsOsaULManager = (IpUserLocation) itsFramework
    				.obtainSCF("P_USER_LOCATION");
        }
        catch (P_UNKNOWN_SERVICE_TYPE anException)
        {
            System.err.println("Service not found. Please refer to the Ericsson Network Resource Gateway User Guide for "
                            + "a list of which applications that are able to run on which test tools\n"
                            + anException);
        }
		itsSMSProcessor = new SMSProcessor(itsHosaUIManager, this);
		itsMMSProcessor = new MMSProcessor(itsHosaUIManager, this);
		itsLocationProcessor = new LocationProcessor(itsOsaULManager, this);
		System.out.println("Starting SMS notification");
		assignmentId = new Integer(itsSMSProcessor.startNotifications(Configuration.INSTANCE.getProperty("serviceNumber")));
		abonents = new Abonent[10];
	    car = new Car(this);
	}

	/**
	 * Stops interaction with the Ericsson Network Resource Gateway and disposes
	 * of all resources allocated by this instance. Note: this method is
	 * intended to be called at most once.
	 */
	public void stop() {
		System.out.println("Stopping SMS notification");
		if (assignmentId != null) {
			itsSMSProcessor.stopNotifications(assignmentId.intValue());
		}
		assignmentId = null;
		System.out.println("Disposing processor");
		if (itsSMSProcessor != null) {
			itsSMSProcessor.dispose();
		}
		if (itsMMSProcessor != null) {
			itsMMSProcessor.dispose();
		}
		if (itsLocationProcessor != null) {
			itsLocationProcessor.dispose();
		}
		System.out.println("Disposing service manager");
		if (itsHosaUIManager != null) {
			itsFramework.releaseSCF(itsHosaUIManager);
		}
		if (itsOsaULManager != null) {
			itsFramework.releaseSCF(itsOsaULManager);
		}
		System.out.println("Disposing framework");
		if (itsFramework != null) {
			itsFramework.endAccess();
			itsFramework.dispose();
		}
		System.out.println("Stopping Parlay tracing");
		HOSAMonitor.removeListener(SDKToolkit.LOGGER);
		System.exit(0);
	}

	/**
	 * Invoked by the SMSProcessor, when a notification is received.
	 * @throws Exception 
	 */
	protected void smsReceived(String aSender, String aReceiver,
			String aMessageContent) {
		//itsLocationProcessor.requestLocation(aSender);
		System.out.println("Odebrano SMS-a o treœci: " + aMessageContent);
		
		Abonent abonent = checkList(aSender);
		
		/*if (abonent == null)
		{
			abonent = new Abonent(aSender, itsLocationProcessor);
			abonents[i] = abonent;
			i++;
			System.out.println("Dodano abonenta o numerze " + abonent.getNumer());
			abonent.start();
		}*/

		if (aMessageContent.toLowerCase().equals("engine on") && abonent != null && car.getIsClosed() ){
			if(abonent == car.getDriver()){
				//System.out.println("Wyslano MMS od "+Configuration.INSTANCE.getProperty("serviceNumber") + " do " +abonent.getNumer()+" o tresci "+car.getState());
				//itsMMSProcessor.sendMMS(Configuration.INSTANCE.getProperty("serviceNumber"), abonent.getNumer(), new MMSMessageContent().getBinaryContent(), car.getState() ? "Silnik rozgrzewa sie" : "Silnik wy³¹czony");
				itsSMSProcessor.sendSMS(Configuration.INSTANCE.getProperty("serviceNumber"), abonent.getNumer(), car.getEngineOn() ? "Engine is still on" : "Warming up engine");
				car.setEngineOn(true);
			}
			else{
				itsSMSProcessor.sendSMS(Configuration.INSTANCE.getProperty("serviceNumber"), abonent.getNumer(), "You must have driver role to engine on");
			}
		}
		else if (aMessageContent.toLowerCase().equals("driver") && abonent != null){
			if (car.getDriver() != null){
				itsSMSProcessor.sendSMS(Configuration.INSTANCE.getProperty("serviceNumber"), car.getDriver().getNumer(), car.getDriver().getNumer() + " is not a driver now.");
			}
			car.setDriver(abonent);
			itsSMSProcessor.sendSMS(Configuration.INSTANCE.getProperty("serviceNumber"), abonent.getNumer(), abonent.getNumer() + " is a driver.");
		}
		else if ((aMessageContent.toLowerCase().equals("driver") || aMessageContent.toLowerCase().equals("engine on")) && abonent == null){
			itsSMSProcessor.sendSMS(Configuration.INSTANCE.getProperty("serviceNumber"), aSender, "Must register for this option");
		}
		else if (aMessageContent.toLowerCase().equals("register user") && abonent == null ){
			abonent = new Abonent(aSender, itsLocationProcessor);
			abonents[i] = abonent;
			i++;
			System.out.println("Number of add abonent is " + abonent.getNumer());
			abonent.start();
			itsSMSProcessor.sendSMS(Configuration.INSTANCE.getProperty("serviceNumber"), aSender, aSender + " is registered");
		}
		else
			System.out.println("Unknown command");
	}
	
	
	private Abonent checkList(String numer)
	{
		for(int j = 0; j < i; j++){
			if (abonents[j].getNumer().equalsIgnoreCase(numer)){
				return abonents[j];
			}
		}
		return null;
		
	}

	public void locationReceived(String user, float latitude, float longitude) {
		try {
			ImageIcon map = Configuration.INSTANCE.getImage("map.gif");
			int wm = map.getIconWidth();
			int hm = map.getIconHeight();
			ImageIcon phone = Configuration.INSTANCE.getImage("phone.png");
			int wp = phone.getIconWidth();
			int hp = phone.getIconHeight();
			if (latitude < 0) {
				latitude = 0;
			}
			if (latitude > 1) {
				latitude = 1;
			}
			if (longitude < 0) {
				longitude = 0;
			}
			if (longitude > 1) {
				longitude = 1;
			}
			int x = (int) (latitude * wm - wp / 2);
			int y = (int) (longitude * hm - hp / 2);
			Plotter plotter = new Plotter(wm, hm);
			plotter.drawImage(map.getImage(), 0, 0, theGUI);
			plotter.drawImage(phone.getImage(), x, y, theGUI);
			MMSMessageContent messageContent = new MMSMessageContent();
			messageContent.addMedia(plotter.createDataSource());
			itsMMSProcessor.sendMMS(Configuration.INSTANCE.getProperty("serviceNumber"), user, messageContent
					.getBinaryContent(), "Current location");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * @return a descriptive text that explains the application and its
	 *         configuration.
	 */
	private String getDescription() {
		String s = "Press START to connect to the Framework";
		s += " and request the H-OSA Messaging (HUI) services from the Framework.\n";
		s += "\n";
		s += "When the user sends an SMS towards service number "+Configuration.INSTANCE.getProperty("serviceNumber")+" with the content of message: ";
		s += "\"register user\" user can register in system, \n";
		s += "\"driver\" and user is registered in system that gives user driver role, \n";
		s += "\"engine on\" run the engine of the car if the user is driver and user get SMS response showing that engine is warming up.\n";
		s += "\n";
		s += "Press STOP to release resources in the Ericsson Network Resource Gateway and the application.\n";
		return s;
	}


}
