
package ldap.account.migration.cloud;

import ldap.account.migration.bean.User;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

/**
 * The OktaMigrator class is responsible for getting the data from external system 
 * and enables it in translating the Users data
 * 
 * @author PWC-AC
 */
public class OktaMigrator {
	private static final Logger LOG = Logger.getLogger(OktaMigrator.class.getName());
	private static final String LOG_LOCATION = "/opt/okta/logs/log4j.properties";

	

	/**
	 * This method is used to create the User Object and pass the User Object as parameter to createUser method
	 * 
	 * @param apiURL
	 *            - String - API URL for accessing Okta
	 * @param apiKey
	 *            - String - API Key for accessing Okta
	 * @param json
	 *            - String - JSON object containing the user profile details
	 * @return
	 */
	public String createUser(String apiURL, String apiKey, String json, boolean loginDisabled) {
		PropertyConfigurator.configure(LOG_LOCATION);
		

		StringBuffer responseBuffer = new StringBuffer();
		
		try {
			String retVal = createUser(new User(json, loginDisabled), apiURL, apiKey);
			responseBuffer.append(retVal);
		} catch (Exception e) {
			LOG.error("Exception while creation of user " + e.getMessage());
			responseBuffer.append("ERROR : ").append(e.getMessage());
		}
		LOG.debug(responseBuffer.toString());

		return responseBuffer.toString();
	}

	/**
	 * @param user
	 *            - User - User details as Object
	 * @param makeFollowOnCalls
	 *            - Boolean - boolean value
	 * @return retVal - String - Returns login value
	 * @throws Exception
	 */
	public String createUser(final User user, final String apiURL, final String apiKey) {
		LOG.debug("Entering Method createUser");

		LOG.debug("Calling OKTA to create user...");
		// Set up CloudUser object
		CloudUser cUser = new CloudUser(user, apiURL, apiKey);
		// Attempt to create user
		String createReturn = cUser.createCloudUser();
		LOG.debug("Calling OKTA to create user finished");
		StringBuffer retVal = new StringBuffer("CREATE ");
		retVal.append(createReturn).append("; ").append(user.getProfileAttr("login"));
		LOG.debug("Recording responses recieved from OKTA..");
		LOG.debug(retVal.toString());
		LOG.debug("Recording responses recieved from OKTA finished");
		LOG.debug("Exiting method createUser");

		return retVal.toString();
	}

}
