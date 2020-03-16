
package ldap.account.migration.cloud;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;

import ldap.account.migration.bean.Group;
import ldap.account.migration.bean.RestResponse;
import ldap.account.migration.bean.User;
import lds.account.migration.constants.ConstantUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.utils.URIBuilder;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;

/**
 * The OktaMigrator class is responsible for getting the data from external system 
 * and enables it in translating the Users data
 * 
 * @author PWC-AC
 */
public class OktaMigrator {
	private OktaApi api = new OktaApi();
	private ObjectMapper mapper = new ObjectMapper();
	private static final Logger LOG = Logger.getLogger(OktaMigrator.class.getName());
	private static final String LOG_LOCATION = "/opt/okta/logs/log4j.properties";

	/**
	 * This method is used to assign a user to an application in Okta.
	 * 
	 * @param appId
	 *            -Application ID
	 * @param oktaId
	 *            - OktaID of the user
	 * @param apiURL
	 *            - apiURL
	 * @param apiKey
	 *            - apiKey
	 * @return
	 */
	public String assignToApp(final String apiURL, final String apiKey, String appId, String oktaId) {
		PropertyConfigurator.configure(LOG_LOCATION);
		CloudUser cUser = new CloudUser(oktaId, apiURL, apiKey, false);
		String response = cUser.assignToApp(appId, oktaId);
		return response;
	}

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

	/**
	 * This method is used to obtain an Okta ID for a given user
	 * 
	 * @throws IOException
	 * @throws URISyntaxException
	 * @param login
	 * @param apiUrl
	 * @param apiKey
	 * @return
	 */
	public String getOktaId(String login, final String apiURL, final String apiKey) {
		PropertyConfigurator.configure(LOG_LOCATION);
		CloudUser cUser = new CloudUser(login, apiURL, apiKey, true);
		String oktaId = "";
		try {
			oktaId = cUser.getOktaId();
		} catch (IOException | URISyntaxException e) {
			LOG.error("getOktaId ERROR for user: " + login + ": " + e.getMessage());
		}
		return (oktaId != null) ? oktaId : "";
	}

	/**
	 * * This method is used to form the User Object and pass the User Object as parameter to updateUser method
	 * 
	 * @param apiURL
	 *            - String - API URL for accessing Okta
	 * @param apiKey
	 *            - String - API Key for accessing Okta
	 * @param json
	 *            - String - JSON object containing the user profile details
	 * @param oktaId - String - ID of the user which needs to be updated
	 * @return
	 * @throws Exception
	 */
	public String updateUser(String apiURL, String apiKey, String json, String oktaId) {
		PropertyConfigurator.configure(LOG_LOCATION);
		StringBuffer responseBuffer = new StringBuffer();
		try {
			String retVal = updateUser(oktaId, new User(json), apiURL, apiKey);
			responseBuffer.append(retVal);
		} catch (Exception e) {
			LOG.error("Exception while update of user " + e.getMessage());
			responseBuffer.append("ERROR : ").append(e.getMessage());
		}
		LOG.debug(responseBuffer.toString());
		return responseBuffer.toString();
	}

	/**
	 * This method is used to update the User details in Okta.
	 * 
	 * @param apiURL
	 *            - String - API URL for accessing Okta
	 * @param apiKey
	 *            - String - API Key for accessing Okta
	 * @param json
	 *            - String - JSON object containing the user profile details
	 * @return
	 * @throws Exception
	 */
	public String updateUser(final String oktaId, final User user, final String apiURL, final String apiKey) {
		LOG.debug("Entering method updateUser");
		// Set up CloudUser object
		StringBuffer retVal = new StringBuffer("UPDATE ");
		CloudUser cUser = new CloudUser(user, apiURL, apiKey);
		cUser.setOktaId(oktaId);
		
		String updateReturn = "";
		try {
			// Attempt to update user
			updateReturn = cUser.updateCloudUser();
			LOG.debug("Calling OKTA to update user finished");
			retVal.append("SUCCESS: ").append(updateReturn);
		} catch (Exception e) {
			LOG.error("Exception occured in update User" + e.getMessage());
			retVal.append("ERROR: ").append(e.getMessage());
		}
		LOG.debug("Recording responses recieved from OKTA..");
		LOG.debug(retVal.toString());
		LOG.debug("Recording responses recieved from OKTA finished");
		LOG.debug("Exiting method updateUser");

		return retVal.toString();

	}

	/**
	 * This method is used to call the delete User in CloudUser
	 * 
	 * @param oktaId
	 *            - String - User's oktaId
	 * @param makeFollowOnCalls
	 *            - Boolean - boolean value
	 * @return retVal - String - Returns login value
	 * @throws Exception
	 */
	public String deleteUserByOktaId(final String apiURL, final String apiKey, String oktaId) {
		PropertyConfigurator.configure(LOG_LOCATION);
		LOG.debug("Entering method deleteUser ...");
		String deleteReturn = "";
		StringBuffer retVal = new StringBuffer();
		if (StringUtils.isNotBlank(oktaId) && StringUtils.isNotBlank(apiURL) && StringUtils.isNotBlank(apiKey)) {
			LOG.debug("Calling OKTA to deleteUser ...");
			// Set up CloudUser object
			CloudUser cUser = new CloudUser(oktaId, apiURL, apiKey, false);

			// Attempt to delete user
			deleteReturn = cUser.deleteCloudUser();
			if (StringUtils.isNotEmpty(deleteReturn)) {
				LOG.debug("Recording responses recieved from OKTA..");
				LOG.debug(deleteReturn);
				retVal.append("SUCCESS: ").append(deleteReturn);
				LOG.debug("Recording responses recieved from OKTA finished");
			}
		} else {
			retVal.append("ERROR : ").append("Please provide valid inputs");
		}
		LOG.debug("Exiting method deleteUser");
		return retVal.toString();

	}

	/**
	 * This method is used to call the delete User in CloudUser
	 * 
	 * @param login
	 *            - String - User's login
	 * @param makeFollowOnCalls
	 *            - Boolean - boolean value
	 * @return retVal - String - Returns login value
	 * @throws Exception
	 */
	public String deleteUserByLogin(final String apiURL, final String apiKey, String login) {
		PropertyConfigurator.configure(LOG_LOCATION);
		LOG.debug("Entering method deleteUser ...");
		String deleteReturn = "";
		StringBuffer retVal = new StringBuffer();
		if (StringUtils.isNotBlank(login) && StringUtils.isNotBlank(apiURL) && StringUtils.isNotBlank(apiKey)) {
			LOG.debug("Calling OKTA to deleteUser ...");
			// Set up CloudUser object
			CloudUser cUser = new CloudUser(login, apiURL, apiKey, true);

			// Attempt to delete user
			deleteReturn = cUser.deleteCloudUser();
			if (StringUtils.isNotEmpty(deleteReturn)) {
				LOG.debug("Recording responses recieved from OKTA..");
				LOG.debug(deleteReturn);
				retVal.append("SUCCESS: ").append(deleteReturn);
				LOG.debug("Recording responses recieved from OKTA finished");
			}
		} else {
			retVal.append("ERROR : ").append("Please provide valid inputs");
		}
		LOG.debug("Exiting method deleteUser");
		return retVal.toString();

	}

	/**
	 * This method gets the User details from OKTA
	 * 
	 * @param apiURL
	 *            - String - API URL for accessing Okta
	 * @param apiKey
	 *            - String - API Key for accessing Okta
	 * @return users - Set - Returns a set of Users
	 * @throws URISyntaxException
	 * @throws IOException
	 * @throws JsonProcessingException
	 * @throws Exception
	 */
	public Set<User> getUsers(String apiURL, String apiKey) {
		PropertyConfigurator.configure(LOG_LOCATION);
		LOG.debug("Entering method getUsers");
		Set<User> users = new HashSet<>();
		RestResponse response = null;
		try {
			response = api.getToOkta(new URIBuilder(apiURL + ConstantUtils.USERS), apiKey);
			ObjectReader reader = mapper.reader(new TypeReference<Set<User>>() {
			});
			users = reader.readValue(response.getResponseBody());
		} catch (JsonProcessingException e) {
			LOG.error("JsonProcessingException occured in getUsers " + e.getMessage());
		} catch (URISyntaxException e) {
			LOG.error("URISyntaxException occured in handling response" + e.getMessage());
		} catch (IOException e) {
			LOG.error("IOException occured in getUsers " + e.getMessage());
		} catch (Exception e) {
			LOG.error("Exception occured in getUsers " + e.getMessage());
		}

		LOG.debug("Exiting method getUsers");
		return users;
	}

	/**
	 * This method is used to call the ActivateUser in CloudUser
	 * 
	 * @param oktaId
	 *            - String - User's oktaId
	 * @param makeFollowOnCalls
	 *            - Boolean - boolean value
	 * @return retVal - String - Returns login value
	 * @throws Exception
	 */
	public String activateUser(final String apiURL, final String apiKey, String oktaId) {
		PropertyConfigurator.configure(LOG_LOCATION);
		LOG.debug("Entering Method ActivateUser");
		String activateReturn = "";
		StringBuffer retVal = new StringBuffer();
		if (StringUtils.isNotBlank(oktaId) && StringUtils.isNotBlank(apiURL) && StringUtils.isNotBlank(apiKey)) {
			LOG.debug("Calling OKTA to Activate user...");
			// Set up CloudUser object
			CloudUser cUser = new CloudUser(oktaId, apiURL, apiKey);

			// Attempt to Activate user
			activateReturn = cUser.activateCloudUser();
			if (StringUtils.isNotEmpty(activateReturn)) {
				LOG.debug("Recording responses recieved from OKTA..");
				retVal.append("SUCCESS: ").append(activateReturn);
				LOG.debug(activateReturn);
				LOG.debug("Recording responses recieved from OKTA finished");
			}
		} else {
			retVal.append("ERROR: ").append("Invalid Input provided");
		}
		LOG.debug("Exiting method ActivateUser");
		return retVal.toString();

	}

	/**
	 * This method is used to call the deactivateUser in CloudUser
	 * 
	 * @param oktaId
	 *            - String - User's oktaId
	 * @param makeFollowOnCalls
	 *            - Boolean - boolean value
	 * @return retVal - String - Returns login value
	 * @throws Exception
	 */
	public String deActivateUser(final String apiURL, final String apiKey, String oktaId) {
		PropertyConfigurator.configure(LOG_LOCATION);
		LOG.debug("Entering Method deActivateUser");
		String deActivateReturn = "";
		StringBuffer retVal = new StringBuffer();
		if (StringUtils.isNotBlank(oktaId) && StringUtils.isNotBlank(apiURL) && StringUtils.isNotBlank(apiKey)) {
			LOG.debug("Calling OKTA to deActivate user...");
			// Set up CloudUser object
			CloudUser cUser = new CloudUser(oktaId, apiURL, apiKey, false);

			// Attempt to deactivate user
			deActivateReturn = cUser.deactivateCloudUser();
			if (StringUtils.isNotEmpty(deActivateReturn)) {
				LOG.debug("Recording responses recieved from OKTA..");
				retVal.append("SUCCESS: ").append(deActivateReturn);
				LOG.debug(deActivateReturn);
				LOG.debug("Recording responses recieved from OKTA finished");
			}
		} else {
			retVal.append("ERROR: ").append("Invalid input provided");
		}
		LOG.debug("Exiting method deActivateUser");
		return retVal.toString();

	}

	/**
	 * This method is used to fetch the user data from driver and call suspendClouduser in CloudUser
	 * 
	 * @param login
	 *            - String - Users oktaId
	 * @param apiURL
	 *            - String - OKTA's URL
	 * @param apiKey
	 *            - String - OKTA's apiKey
	 * @return
	 * @throws IOException
	 * @throws URISyntaxException
	 * @throws Exception
	 */
	public String suspendUser(final String apiURL, final String apiKey, String oktaId) {
		PropertyConfigurator.configure(LOG_LOCATION);
		LOG.debug("Entering Method suspendUser");
		String suspendReturn = "";
		StringBuffer retVal = new StringBuffer();
		if (StringUtils.isNotBlank(oktaId) && StringUtils.isNotBlank(apiURL) && StringUtils.isNotBlank(apiKey)) {
			// Set up CloudUser object
			CloudUser cUser = new CloudUser(oktaId, apiURL, apiKey, false);
			LOG.debug("Calling OKTA to suspendUser...");
			// Attempt to suspend user
			suspendReturn = cUser.suspendCloudUser();
			if (StringUtils.isNotEmpty(suspendReturn)) {
				LOG.debug("Recording responses recieved from OKTA..");
				retVal.append("SUCCESS : ").append(suspendReturn);
				LOG.debug(suspendReturn);
				LOG.debug("Recording responses recieved from OKTA finished");
			}
		} else {
			retVal.append("ERROR: ").append("Invalid input provided");
		}
		LOG.debug("Exiting method suspendUser");
		return retVal.toString();
	}

	/**
	 * This method is used to fetch the user data from driver and call unSuspendClouduser in CloudUser
	 * 
	 * @param oktaId
	 *            - String - Users oktaId
	 * @param apiURL
	 *            - String - OKTA's URL
	 * @param apiKey
	 *            - String - OKTA's apiKey
	 * @return
	 * @throws IOException
	 * @throws URISyntaxException
	 * @throws Exception
	 */
	public String unSuspendUser(final String apiURL, final String apiKey, String oktaId) {
		PropertyConfigurator.configure(LOG_LOCATION);
		LOG.debug("Entering Method unSuspendUser");
		String activateReturn = "";
		StringBuffer retVal = new StringBuffer();
		if (StringUtils.isNotBlank(oktaId) && StringUtils.isNotBlank(apiURL) && StringUtils.isNotBlank(apiKey)) {
			LOG.debug("Calling OKTA to unsuspendUser...");
			// Set up CloudUser object
			CloudUser cUser = new CloudUser(oktaId, apiURL, apiKey, false);
			LOG.debug("Calling OKTA to unSuspend user...");
			// Attempt to unsuspend user
			activateReturn = cUser.unSuspendCloudUser();
			if (StringUtils.isNotEmpty(activateReturn)) {
				LOG.debug("Recording responses recieved from OKTA..");
				retVal.append("SUCCESS: ").append(activateReturn);
				LOG.debug(activateReturn);
				LOG.debug("Recording responses recieved from OKTA finished");
			}
		} else {
			retVal.append("ERROR: ").append("Invalid input provided");
		}
		LOG.debug("Exiting method unSuspendUser");
		return retVal.toString();

	}

	/**
	 * This method is used to create the data and send as input to createGroup method
	 * 
	 * @param apiURL
	 *            - String - URL
	 * @param apiKey
	 *            - String - apiKey
	 * @param json
	 *            - String - Group JSON object
	 * @return
	 * @throws Exception
	 */
	public String createGroup(String apiURL, String apiKey, String json) {
		PropertyConfigurator.configure(LOG_LOCATION);
		StringBuffer retVal = new StringBuffer();
		try {
			String response = createGroup(new Group(json), apiURL, apiKey);
			retVal.append(response);
		} catch (Exception e) {
			LOG.error("Exception while creation of group " + e.getMessage());
			retVal.append("ERROR: ").append(e.getMessage());
		}
		return retVal.toString();
	}

	/**
	 * This method is used to call createGroup in CloudUser
	 * 
	 * @param group
	 *            - Group Object
	 * @param apiURL
	 *            - String - URL
	 * @param apiKey
	 *            - String - apiKey
	 * @return
	 * @throws Exception
	 */
	public String createGroup(final Group group, final String apiURL, final String apiKey) {
		PropertyConfigurator.configure(LOG_LOCATION);
		LOG.debug("Entering method createGroup");
		String createGroupReturn = "";
		StringBuffer retVal = new StringBuffer();
		if (group != null && StringUtils.isNotBlank(apiURL) && StringUtils.isNotBlank(apiKey)) {
			CloudUser cUser = new CloudUser(group, apiURL, apiKey);

			// Attempt to create Group
			createGroupReturn = cUser.createGroup();
			LOG.debug("Calling OKTA to create group finished");
			retVal.append("SUCCESS: ").append(createGroupReturn);
			LOG.debug("Recording responses recieved from OKTA..");
			LOG.debug(retVal.toString());
			LOG.debug("Recording responses recieved from OKTA finished");
		} else {
			retVal.append("ERROR: ").append("Invalid input provided");
		}
		LOG.debug("Exiting method createGroup");
		return retVal.toString();

	}

	/**
	 * This method is used to call addUserToGroup in CloudUser
	 * 
	 * @param groupName
	 *            - Group Name
	 * @param oktaId
	 *            -User's oktaId
	 * @param apiURL
	 *            - String - OKTA's URL
	 * @param apiKey
	 *            - String - OKTA's apiKey
	 * @return
	 * @throws Exception
	 */
	public String addUserToGroup(final String apiURL, final String apiKey, String groupId, String oktaId) {
		PropertyConfigurator.configure(LOG_LOCATION);
		LOG.debug("Entering method addUserToGroup");
		String addUserToGroupReturn = "";
		StringBuffer retVal = new StringBuffer();
		if (StringUtils.isNotBlank(groupId) && StringUtils.isNotBlank(oktaId) && StringUtils.isNotBlank(apiURL)
				&& StringUtils.isNotBlank(apiKey)) {
			CloudUser cUser = new CloudUser(groupId, oktaId, apiURL, apiKey);
			// Attempt to add user to Group
			addUserToGroupReturn = cUser.addUserToCloudGroup();
			LOG.debug("Calling OKTA to addUserToGroup finished");
			retVal.append("SUCCESS: ").append(addUserToGroupReturn);
			LOG.debug(retVal.toString());
		} else {
			retVal.append("ERROR: ").append("Invalid input provided");
		}
		LOG.debug("Exiting method addUserToGroup");
		return retVal.toString();

	}

	/**
	 * This method calls the removeUserFromCloudGroup and removes the users from the group.
	 * 
	 * @param groupName
	 *            -String - groupName
	 * @param oktaId
	 *            - String - oktaId
	 * @param apiURL
	 *            - String - OKTA's URL
	 * @param apiKey
	 *            - String - OKTA's apiKey
	 * @return
	 * @throws Exception
	 */
	public String removeUserFromGroup(final String apiURL, final String apiKey, String groupId, String oktaId) {
		PropertyConfigurator.configure(LOG_LOCATION);
		LOG.debug("Entering method removeUserFromGroup");
		String removeUserFromGroupReturn = "";
		StringBuffer retVal = new StringBuffer();
		if (StringUtils.isNotBlank(groupId) && StringUtils.isNotBlank(oktaId) && StringUtils.isNotBlank(apiURL)
				&& StringUtils.isNotBlank(apiKey)) {
			CloudUser cUser = new CloudUser(groupId, oktaId, apiURL, apiKey);
			// Attempt to remove user from Group
			removeUserFromGroupReturn = cUser.removeUserFromCloudGroup();
			LOG.debug("Calling OKTA to removeUserFromGroup finished");
			retVal.append("SUCCESS: ").append(removeUserFromGroupReturn);
			LOG.debug(retVal.toString());
		} else {
			retVal.append("ERROR: ").append("Invalid input provided");
		}
		LOG.debug("Exiting method removeUserFromGroup");
		return retVal.toString();

	}

	/**
	 * This method calls the deleteCloudGroup in CloudUser.java
	 * 
	 * @param groupName
	 *            -String - groupName
	 * @param apiURL
	 *            - String - OKTA's URL
	 * @param apiKey
	 *            - String - OKTA's apiKey
	 * @return
	 * @throws Exception
	 */
	public String deleteGroup(final String apiURL, final String apiKey, String groupId) {
		PropertyConfigurator.configure(LOG_LOCATION);
		LOG.debug("Entering method deleteGroup");
		StringBuffer retVal = new StringBuffer();
		if (StringUtils.isNotBlank(groupId) && StringUtils.isNotBlank(apiURL) && StringUtils.isNotBlank(apiKey)) {
			CloudUser cUser = new CloudUser(groupId, null, apiURL, apiKey);
			// Attempt to delete Group
			String deleteGroupReturn = cUser.deleteCloudGroup();
			LOG.debug("Calling OKTA to delete group finished");
			retVal.append("SUCCESS: ").append(deleteGroupReturn);
			LOG.debug(retVal.toString());
		} else {
			retVal.append("ERROR: ").append("Invalid input provided");
		}
		LOG.debug("Exiting method deleteGroup");
		return retVal.toString();

	}

}
