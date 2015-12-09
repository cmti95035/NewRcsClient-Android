package mobisocial.musubi.service;

import com.twilio.sdk.TwilioRestClient;
import com.twilio.sdk.TwilioRestException;
import com.twilio.sdk.resource.factory.MessageFactory;
import com.twilio.sdk.resource.instance.Account;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.List;

public class TwilioService {
	/** The Constant ACCOUNT_SID. Find it at twilio.com/user/account */
	public static final String ACCOUNT_SID = "AC3884f762bc8d2b8c2c261e2fd2842063";

	/** The Constant AUTH_TOKEN. Find it at twilio.com/user/account */
	public static final String AUTH_TOKEN = "78bf682283522452895faa8fb82ab7bb";

	public static final String FROM_NUMBER = "(408) 610-1268";

	// Create a rest TWILIO_CLIENT
	public static TwilioRestClient TWILIO_CLIENT = new TwilioRestClient(
			ACCOUNT_SID, AUTH_TOKEN);

	// Get the main account (The one we used to authenticate the TWILIO_CLIENT)
	public static Account MAIN_ACCOUNT = TWILIO_CLIENT.getAccount();

	/**
	 * method to send a SMS message
	 * 
	 * @param recipientNumber
	 * @param msg
	 */
	public static boolean sendSMS(String recipientNumber, String msg) {
		try {
			// Send an sms (using the new messages endpoint)
			MessageFactory messageFactory = MAIN_ACCOUNT.getMessageFactory();
			List<NameValuePair> messageParams = new ArrayList<NameValuePair>();
			messageParams.add(new BasicNameValuePair("To", recipientNumber));
			messageParams.add(new BasicNameValuePair("From", FROM_NUMBER));
			messageParams.add(new BasicNameValuePair("Body", msg));

			System.out.println("Will send a message to " + recipientNumber
					+ " with message: " + msg);
			messageFactory.create(messageParams);
			return true;
		} catch (TwilioRestException trex) {
			System.out.println("Error observed: " + trex.getMessage());
			return false;
		} catch (Exception e) {
			return false;
		}
	}
}
