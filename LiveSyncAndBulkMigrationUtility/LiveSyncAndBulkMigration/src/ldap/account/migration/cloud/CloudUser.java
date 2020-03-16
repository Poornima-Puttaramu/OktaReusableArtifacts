
package ldap.account.migration.cloud;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

import ldap.account.migration.bean.Factor;
import ldap.account.migration.bean.Group;
import ldap.account.migration.bean.RestResponse;
import ldap.account.migration.bean.User;
import lds.account.migration.constants.ConstantUtils;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.StrSubstitutor;
import org.apache.http.client.utils.URIBuilder;
import org.apache.log4j.Logger;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The CloudUser class is used or responsible for posting the user data into OKTA
 * 
 * @author PWC-AC
 */
public class CloudUser implements Runnable {
	private User user;
	private OktaApi api = new OktaApi();
	private ObjectMapper mapper = new ObjectMapper();
	private static final Logger LOG = Logger.getLogger(CloudUser.class.getName());
	private String apiURL;
	private String apiKey;
	private String oktaId;
	private String groupId;
	private Group group;
	private String login;

	public CloudUser(String id, String apiURL, String apiKey, boolean isLogin) {
		if (isLogin) {
			this.login = id;
		} else {
			this.oktaId = id;
		}
		this.apiURL = apiURL;
		this.apiKey = apiKey;
	}

	public CloudUser(String oktaId, String apiURL, String apiKey) {
		this.oktaId = oktaId;
		this.apiURL = apiURL;
		this.apiKey = apiKey;
	}

	public CloudUser(User user) {
		this.user = user;
	}

	public CloudUser(User user, String apiURL, String apiKey) {
		this.user = user;
		this.apiURL = apiURL;
		this.apiKey = apiKey;
	}

	public CloudUser(Group group, String apiURL, String apiKey) {
		this.group = group;
		this.apiURL = apiURL;
		this.apiKey = apiKey;
	}

	public CloudUser(String groupId, String oktaId, String apiURL, String apiKey) {
		this.oktaId = oktaId;
		this.groupId = groupId;
		this.apiURL = apiURL;
		this.apiKey = apiKey;
	}

	public void run() {
		try {
			createCloudUser();
		} catch (Exception e) {
			LOG.error("Exception in Run method" + e);
		}
	}

	/**
	 * This method is called from createUser method in OktaMigrator class. This method builds the URL of OKTA and calls
	 * postToOkta and enrollSms methods.
	 * 
	 * @return - String - User details -Okta ID
	 * @throws URISyntaxException
	 * @throws IOException
	 * @throws Exception
	 */
	public String createCloudUser() {
		LOG.debug("Entering Method createCloudUser");
		String validation = user.validate();
		if (!validation.isEmpty()) {
			LOG.error("Required Field is Missing: " + validation);
			return "ERROR: required field missing: " + validation;
		}
		boolean success;
		RestResponse response;
		StringBuffer responseBuffer = new StringBuffer();
		try {
			URIBuilder builder = new URIBuilder(this.apiURL + ConstantUtils.USERS);
			/** Getting the user loginDisabled value and setting the user status in OKTA */
			if (Boolean.toString(user.isLoginDisabled()).equals("true")) {
				builder.addParameter("activate", "false");
			} else {
				builder.addParameter("activate", "true");
			}
			/** Calling postToOkta */
			response = api.postToOkta(builder, user.getUserAsString(), this.apiKey);
			success = response != null && isSuccessCode(response.getResponseCode());
			/** get the userId and pass the URL for enabling factors for this user */
			if (success) {
				this.oktaId = response.getResponseBody().get("id").asText();

				responseBuffer.append("SUCCESS; ").append("objectId: ").append(this.oktaId);
				// Enroll in SMS
				String mobilePhone = user.getProfileAttr("mobilePhone");
				if (mobilePhone != null && !mobilePhone.isEmpty()) {
					// Calling functionality to enroll SMS by default for the user being loaded
					response = enrollSms(this.oktaId, mobilePhone);
					if (response != null && !isSuccessCode(response.getResponseCode())) {
						responseBuffer.append("; ENROLL ERROR; ").append(response.getResponseCode()).append(" ")
								.append(response.getResponseBody().toString());
						LOG.error("enrollSMS failed for user with oktaID: " + this.oktaId);
					} else {
						responseBuffer.append("; ENROLL SUCCESS ");
					}
				}

			} else {
				responseBuffer.append("ERROR; ").append(response.getResponseCode()).append(" ")
						.append(response.getResponseBody().toString());
				LOG.error(responseBuffer.toString());
			}
		} catch (IOException e) {
			String message = "IOException in createCloudUser  " + e.getMessage();
			LOG.error(message);
			responseBuffer.append(message);
		} catch (URISyntaxException e) {
			String message = "URISyntaxException in createCloudUser  " + e.getMessage();
			LOG.error(message);
			responseBuffer.append(message);
		} catch (Exception e) {
			String message = "Error in createCloudUser  " + e.getMessage();
			LOG.error(message);
			responseBuffer.append(message);
		}
		LOG.debug("Exiting Method createCloudUser");
		return responseBuffer.toString();
	}

	/**
	 * Method to activate the cloud user
	 * 
	 * @return Response message
	 * @throws URISyntaxException
	 * @throws IOException
	 * @throws Exception
	 */
	public String activateCloudUser() {
		LOG.debug("Entering activateCloudUser");
		StringBuffer strBuffer = new StringBuffer();
		try {
			if (StringUtils.isNotEmpty(oktaId)) {
				/** Building the URL to Activate Cloud user */
				URIBuilder builder = new URIBuilder(formatUrl(apiURL + ConstantUtils.USER,
						Collections.singletonMap(ConstantUtils.OKTA_ID, oktaId))
						+ "/lifecycle/activate");
				// Adding parameter sendEmail to False to prevent sending emails to the user upon Activation
				builder.addParameter(ConstantUtils.SEND_EMAIL, Boolean.FALSE.toString());//Poornima - change it to params as map in user bean and then fetch value 
				
				RestResponse response = api.postToOkta(builder, null, this.apiKey);
				if (null != response) {
					LOG.debug("Response Code " + response.getResponseCode() + " Response Message :: "
							+ response.getResponseMessage() + " Resp Body :: " + response.getResponseBody().toString());

					JsonNode respBody = response.getResponseBody();
					if (isSuccessCode(response.getResponseCode())) {
						strBuffer.append("User " + oktaId + " has been activated successfully");
					} else {
						if (respBody.hasNonNull("errorSummary")) {
							String errSummary = respBody.get("errorSummary").textValue();
							LOG.debug("Error summary :: " + errSummary);
							strBuffer.append("ERROR: ").append(errSummary);
						}
					}
				} else {
					LOG.error("Response object is null");
					strBuffer.append("ERROR: ").append("Error while activating user");
				}
			} else {
				strBuffer.append("ERROR: ").append("Error while fetching Okta ID for user ");
			}
		} catch (URISyntaxException e) {
			LOG.error("URISyntaxException occured in activateCloudUser " + e.getMessage());
			strBuffer.append("ERROR: ").append(e.getMessage());
		} catch (IOException e) {
			LOG.error("IOException occured in activateCloudUser " + e.getMessage());
			strBuffer.append("ERROR: ").append(e.getMessage());
		} catch (Exception e) {
			LOG.error("Exception occured in activateCloudUser " + e.getMessage());
			strBuffer.append("ERROR: ").append(e.getMessage());
		}
		LOG.debug("Exiting  method activateCloudUser");
		return strBuffer.toString();
	}

	/**
	 * This method is called helps in assigning Application to the User in OKTA
	 * 
	 * @param appId
	 *            - String - Application ID
	 * @param oktaId
	 *            - String - Okta ID
	 * @return response - RestResponse
	 * @throws URISyntaxException
	 * @throws IOException
	 */
	public String assignToApp(String appId, String oktaId) {
		LOG.debug("Entering method assignToApp");
		RestResponse response = null;
		StringBuffer responseBuffer = new StringBuffer();
		try {
			/** Building the URL to assign Application to Cloud user */
			URIBuilder builder = new URIBuilder(formatUrl(this.apiURL + ConstantUtils.ASSIGN_APP,
					Collections.singletonMap("appId", appId)));
			ObjectNode node = mapper.createObjectNode();
			node.put("id", oktaId);
			node.put("scope", "USER");
			// calling postToOkta method
			response = api.postToOkta(builder, mapper.writeValueAsString(node), this.apiKey);
			if (response != null && !isSuccessCode(response.getResponseCode())) {
				responseBuffer.append("APP ASSIGN ERROR; ").append(response.getResponseCode()).append(" ")
						.append(response.getResponseBody().toString());
				LOG.error("APP ASSIGN ERROR with oktaID: " + this.oktaId);
			} else {
				responseBuffer.append("APP ASSIGN SUCCESS ");
			}
		} catch (IOException e) {
			LOG.error("IOException in assignToApp method " + e.getMessage());
			responseBuffer.append("ERROR: ").append(e.getMessage());
		} catch (URISyntaxException e) {
			LOG.error("URISyntaxException in assignToApp method " + e.getMessage());
			responseBuffer.append("ERROR: ").append(e.getMessage());
		} catch (Exception e) {
			LOG.debug("Error in Processing JSON in assignToApp method " + e);
			responseBuffer.append("ERROR: ").append(e.getMessage());
		}
		LOG.debug("Exiting method assignToApp");
		return responseBuffer.toString();
	}

	/**
	 * Method to deActivate cloud user
	 * 
	 * @return Response message
	 * @throws URISyntaxException
	 * @throws IOException
	 * @throws Exception
	 */
	public String deactivateCloudUser() {
		LOG.debug("Entering deActivateCloudUser");
		StringBuffer responseBuffer = new StringBuffer();
		try {
			if (StringUtils.isNotEmpty(this.oktaId)) {
				// Building the URL to deActivate the user
				RestResponse response = api.postToOkta(
						new URIBuilder(formatUrl(apiURL + ConstantUtils.USER,
								Collections.singletonMap(ConstantUtils.OKTA_ID, this.oktaId))
								+ "/lifecycle/deactivate"), null, apiKey);
				if (null != response) {
					LOG.debug("Response Code " + response.getResponseCode() + " Response Message :: "
							+ response.getResponseMessage() + " Resp Body :: " + response.getResponseBody().toString());

					if (isSuccessCode(response.getResponseCode())) {
						responseBuffer.append("DEACTIVATE SUCCESS: ").append(this.oktaId);
					} else {
						responseBuffer.append("DEACTIVATE ERROR: ").append(response.getResponseMessage());
					}
				} else {
					LOG.error("Response object is null");
					responseBuffer.append("DEACTIVATE ERROR: Reponse Object is Null");
				}
			} else {
				responseBuffer.append("ERROR: ").append("Error while fetching Okta ID for user ");
			}
		} catch (Exception e) {
			LOG.error("DEACTIVATE ERROR: " + e.getMessage());
			responseBuffer.append("DEACTIVATE ERROR: ").append(e.getMessage());
		}
		LOG.debug("Exiting deActivateCloudUser");
		return responseBuffer.toString();
	}

	/**
	 * Method to delete cloud user
	 * 
	 * @return Response message
	 * @throws URISyntaxException
	 * @throws IOException
	 * @throws Exception
	 */
	public String deleteCloudUser(boolean isLogin) {
		LOG.debug("Entering deleteCloudUser");
		StringBuffer responseBuffer = new StringBuffer();
		try {
			RestResponse delResponse = null;
			String oktId = "";
			if (isLogin) {
				/** Calling getOktaId method to get the OktaID from login */
				oktId = getOktaId();
			} else {
				oktId = oktaId;
			}
			/** Getting the user from OKTA based on OktaId */
			RestResponse response = api.getToOkta(
					new URIBuilder(formatUrl(apiURL + ConstantUtils.USER,
							Collections.singletonMap(ConstantUtils.OKTA_ID, oktId))), apiKey);
			if (response == null) {
				LOG.error("Response object is null while fetching User");
				responseBuffer.append("ERROR: ").append("Error while fetching User");
			}
			if (!isSuccessCode(response.getResponseCode())) {
				JsonNode respBody = response.getResponseBody();
				if (respBody.hasNonNull("errorSummary")) {
					String errSummary = respBody.get("errorSummary").textValue();
					LOG.debug("Error summary :: " + errSummary);
					responseBuffer.append("ERROR: ").append(errSummary);
				}
			}
			JsonNode jsonResp = response.getResponseBody();
			/** Checking if the response object is null */
			if (null == jsonResp) {
				LOG.error("Response object is null while fetching User status");
				responseBuffer.append("ERROR: ").append("Error while fetching user status");
			}
			/** Checking if the response object is not null and getting the status of User from response */
			if (null != jsonResp && jsonResp.has(ConstantUtils.STATUS)) {
				JsonNode status = jsonResp.get(ConstantUtils.STATUS);
				String userStatus = status.toString();
				userStatus = userStatus.replaceAll("\"", "");
				LOG.debug("Status :: " + userStatus);
				/** Getting the user status and deactivating the user based on status */
				if (StringUtils.isNotBlank(userStatus)) {
					if ("PROVISIONED".equalsIgnoreCase(userStatus) || "ACTIVE".equalsIgnoreCase(userStatus)
							|| "SUSPENDED".equalsIgnoreCase(userStatus)) {
						LOG.debug("Inside Provisioned or Active or Suspended  status. Hence deactivate the user first and then delete the user");
						try {
							/** Deactivating the User */
							RestResponse deActResponse = api.postToOkta(
									new URIBuilder(formatUrl(apiURL + ConstantUtils.USER,
											Collections.singletonMap(ConstantUtils.OKTA_ID, oktaId))
											+ "/lifecycle/deactivate"), null, apiKey);
							if (null != deActResponse && isSuccessCode(deActResponse.getResponseCode())) {
								try {
									/** Deleting the User from OKTA if response from Deactivate is success */
									delResponse = api.deleteToOkta(
											new URIBuilder(formatUrl(apiURL + ConstantUtils.USER,
													Collections.singletonMap(ConstantUtils.OKTA_ID, oktaId))), apiKey);
								} catch (URISyntaxException e) {
									LOG.error("URISyntaxException in calling deleteToOkta from deleteCloudUser -PROVISIONED,Active:::: :: "
											+ e.getMessage());
									responseBuffer.append("ERROR: ").append(e.getMessage());
								} catch (IOException e) {
									LOG.error("IOException in calling deleteToOkta from deleteCloudUser -PROVISIONED,Active:::::: "
											+ e.getMessage());
									responseBuffer.append("ERROR: ").append(e.getMessage());
								}
							} else {
								if (deActResponse != null) {
									JsonNode respBody = deActResponse.getResponseBody();
									responseBuffer.append("ERROR: ").append(
											respBody.get(ConstantUtils.ERROR_SUMMARY).asText());
								}
							}
						} catch (URISyntaxException e) {
							LOG.error("URISyntaxExceptionoccured in calling postToOkta from deleteCloudUser -PROVISIONED,Active:::: "
									+ e.getMessage());
							responseBuffer.append("ERROR: ").append(e.getMessage());
						} catch (IOException e) {
							LOG.error("IOException occured in calling postToOkta from deleteCloudUser -PROVISIONED,Active:::: "
									+ e.getMessage());
							responseBuffer.append("ERROR: ").append(e.getMessage());
						}
					} else if ("DEPROVISIONED".equalsIgnoreCase(userStatus)) {
						/** If the user is already Deactivated then Delete the User */
						LOG.debug("Inside Deprovisioned status. Hence delete the user");
						try {
							delResponse = api.deleteToOkta(
									new URIBuilder(formatUrl(apiURL + ConstantUtils.USER,
											Collections.singletonMap(ConstantUtils.OKTA_ID, oktaId))), apiKey);
						} catch (URISyntaxException e) {
							LOG.error("URISyntaxException occured in calling deleteToOkta from deleteCloudUser -DEPROVISIONED:: "
									+ e.getMessage());
							responseBuffer.append("ERROR: ").append(e.getMessage());
						} catch (IOException e) {
							LOG.error("IOException  occured in calling deleteToOkta from deleteCloudUser -DEPROVISIONED:: "
									+ e.getMessage());
							responseBuffer.append("ERROR: ").append(e.getMessage());
						}
					}
				}
			}
			if (null != delResponse) {
				LOG.debug("Response Code " + delResponse.getResponseCode() + " Response Message :: "
						+ delResponse.getResponseMessage());

				JsonNode respBody = delResponse.getResponseBody();
				if (isSuccessCode(delResponse.getResponseCode())) {
					responseBuffer.append("User " + oktaId + " has been deleted successfully");
				} else {
					if (respBody.hasNonNull(ConstantUtils.ERROR_SUMMARY)) {
						String errSummary = respBody.get(ConstantUtils.ERROR_SUMMARY).textValue();
						LOG.error("Error summary :: " + errSummary);
						responseBuffer.append("ERROR: ").append(errSummary);
					}
				}
			} else {
				LOG.error("Response from deletion of user is null");
				responseBuffer.append("ERROR: ").append("Delete user failed");
			}
		} catch (URISyntaxException e) {
			LOG.error("URISyntaxException occured in deleteCloudUser " + e.getMessage());
			responseBuffer.append("ERROR: ").append(e.getMessage());
		} catch (IOException e) {
			LOG.error("IOException occured in deleteCloudUser " + e.getMessage());
			responseBuffer.append("ERROR: ").append(e.getMessage());
		} catch (Exception e) {
			LOG.error("Exception occured in deleteCloudUser " + e.getMessage());
			responseBuffer.append("ERROR: ").append(e.getMessage());
		}
		LOG.debug("Exiting deleteCloudUser");
		return responseBuffer.toString();
	}

	/**
	 * This method attempts to deactivate and then delete a user. It does not check the current user status when
	 * deleting. Note: If a user is deactivated, it will return DEACTIVATE ERROR: Not Found
	 * 
	 * @return Response message
	 * @throws URISyntaxException
	 * @throws IOException
	 * @throws Exception
	 */
	public String deleteCloudUser() {
		LOG.debug("Entering deleteCloudUser");
		StringBuffer responseBuffer = new StringBuffer();

		String oktaId = "";
		// Check to see if okta ID already exists. If not attempt to look up the user
		if (this.oktaId != null && !this.oktaId.isEmpty()) {
			oktaId = this.oktaId;
		} else if (this.login != null && !this.login.isEmpty()) {
			try {
				oktaId = getOktaId();
			} catch (IOException | URISyntaxException e) {
				LOG.error("Failed to obtain oktaId");
				responseBuffer.append("ERROR: ").append(e.getMessage());
			}
		}
		// Check to see the previous check did not fail
		if (oktaId == null || oktaId.isEmpty()) {
			LOG.error("deleteCloudUser: OktaId and login missing or OktaId lookup failed");
			responseBuffer.append("DELETE ERROR: OktaId and login missing or OktaId lookup failed");
			return responseBuffer.toString();
		}

		// Attempt to deactivate the user
		RestResponse response = null;
		try {
			responseBuffer.append("DEACTIVATE ");
			response = api.postToOkta(
					new URIBuilder(formatUrl(apiURL + ConstantUtils.USER,
							Collections.singletonMap(ConstantUtils.OKTA_ID, oktaId))
							+ "/lifecycle/deactivate"), null, apiKey);
			if (response != null) {
				if (isSuccessCode(response.getResponseCode())) {
					responseBuffer.append("SUCCESS: ").append(oktaId);
				} else {
					responseBuffer.append("ERROR: ").append(response.getResponseMessage());
				}
			} else {
				responseBuffer.append("ERROR: Deactivate response null");
			}
		} catch (Exception e) {
			LOG.error("deleteCloudUser: Exception while deactivating the user: " + e.getMessage());
			responseBuffer.append("ERROR: ").append(e.getMessage());
		}

		// Attempt to delete the user
		try {
			responseBuffer.append("; DELETE ");
			response = api.deleteToOkta(
					new URIBuilder(formatUrl(apiURL + ConstantUtils.USER,
							Collections.singletonMap(ConstantUtils.OKTA_ID, oktaId))), apiKey);
			if (response != null) {
				if (isSuccessCode(response.getResponseCode())) {
					responseBuffer.append("SUCCESS: ").append(oktaId);
				} else {
					responseBuffer.append("ERROR: ").append(response.getResponseMessage());
				}
			} else {
				responseBuffer.append("ERROR: Delete response null");
			}
		} catch (Exception e) {
			LOG.error("deleteCloudUser: Exception while deleting the user: " + e.getMessage());
			responseBuffer.append("ERROR: ").append(e.getMessage());
		}
		LOG.debug("Exiting deleteCloudUser");
		return responseBuffer.toString();
	}

	/**
	 * This method suspends a cloud user in Okta called by the createUser method
	 * 
	 * @param oktaId
	 *            - String -oktaId of the user
	 * @throws URISyntaxException
	 * @throws IOException
	 * @throws Exception
	 */
	public String suspendCloudUser(String oktaId) {
		LOG.debug("Entering Method suspendCloudUser");
		StringBuffer responseBuffer = new StringBuffer();
		/** Checking if oktaId is null or Empty */
		if (oktaId == null || oktaId.isEmpty()) {
			LOG.error("OktaID null or empty");
			responseBuffer.append("ERROR: ").append("Error while fetching OktaID ");
		}

		boolean success;
		RestResponse response;
		try {
			/** Building the URL to Suspend the User */
			URIBuilder builder = new URIBuilder(formatUrl(this.apiURL + ConstantUtils.SUSPEND,
					Collections.singletonMap(ConstantUtils.OKTA_ID, oktaId)));
			response = api.postToOkta(builder, user.getUserAsString(), this.apiKey);

			success = response != null && isSuccessCode(response.getResponseCode());
			/** get the userId and pass the URL for enabling factors for this user */
			if (success) {
				responseBuffer.append("SUSPEND SUCCESS : ").append(oktaId);
			} else {
				responseBuffer.append("SUSPEND ERROR : ").append(oktaId).append(response.getResponseCode()).append(" ")
						.append(response.getResponseBody().toString());
				LOG.error(responseBuffer.toString());
			}
		} catch (URISyntaxException e) {
			LOG.debug("URISyntaxException in suspendCloudUser  " + e.getMessage());
			responseBuffer.append("SUSPEND ERROR : ").append(e.getMessage());
		} catch (IOException e) {
			LOG.debug("IOException in suspendCloudUser  " + e.getMessage());
			responseBuffer.append("SUSPEND ERROR : ").append(e.getMessage());
		} catch (Exception e) {
			LOG.debug("Error in suspendCloudUser  " + e.getMessage());
			responseBuffer.append("SUSPEND ERROR : ").append(e.getMessage());
		}
		LOG.debug("Exiting Method suspendCloudUser");
		return responseBuffer.toString();
	}

	/**
	 * This method is called from updateUser method in OktaMigrator class. This method builds the URL of OKTA and calls
	 * postToOkta method.
	 * 
	 * @return
	 * @throws Exception
	 */
	public String updateCloudUser() {
		LOG.debug("Entering Method updateCloudUser");
		RestResponse response;
		StringBuffer responseBuffer = new StringBuffer();
		try {
			if (user != null) {
				if (this.oktaId != null && StringUtils.isNotEmpty(this.oktaId)) {
					/** Building the URL to Update the User */
					URIBuilder builder = new URIBuilder(formatUrl(this.apiURL + ConstantUtils.USER,
							Collections.singletonMap(ConstantUtils.OKTA_ID, this.oktaId)));
					/** Calling the PostToOkta */
					response = api.postToOkta(builder, user.getUserAsString(), this.apiKey);
					if (null != response) {
						LOG.debug("Response Code " + response.getResponseCode() + " Response Message :: "
								+ response.getResponseMessage() + " Resp Body :: "
								+ response.getResponseBody().toString());
						JsonNode respBody = response.getResponseBody();
						// oktaId = response.getResponseBody().get(Constants.ID).asText();
						/** Getting the mobilePhone value from User profile */
						String mobilePhone = user.getProfileAttr(ConstantUtils.MOBILE_PHONE);
						if (mobilePhone != null && !mobilePhone.isEmpty()) {
							// Calling functionality to enroll SMS for the user if mobile number is updated.
							response = enrollSmsWithUpdatedPhoneNumber(this.oktaId, mobilePhone);
							if (response != null && !isSuccessCode(response.getResponseCode())) {
								addError(response, responseBuffer, ConstantUtils.FACTOR);
							}
						}
						if (isSuccessCode(response.getResponseCode())) {
							responseBuffer.append("User " + this.oktaId + " has been updated successfully");
						} else {
							if (respBody.hasNonNull(ConstantUtils.ERROR_SUMMARY)) {
								String errSummary = respBody.get(ConstantUtils.ERROR_SUMMARY).textValue();
								LOG.error("Error summary :: " + errSummary);
								responseBuffer.append("UPDATE ERROR : ").append(errSummary);
							}
						}
					} else {
						LOG.error("Response object is null");
						responseBuffer.append("UPDATE ERROR : ").append("Error while updating user");
					}
				} else {
					responseBuffer.append("UPDATE ERROR : ").append("Okta ID is not provided");
				}
			}
		} catch (IOException e) {
			LOG.error("IOException in updateCloudUser  " + e.getMessage());
			responseBuffer.append("UPDATE ERROR : ").append(e.getMessage());
		} catch (URISyntaxException e) {
			LOG.error("URISyntaxException in updateCloudUser  " + e.getMessage());
			responseBuffer.append("UPDATE ERROR : ").append(e.getMessage());
		} catch (Exception e) {
			LOG.error("Error in updateCloudUser  " + e.getMessage());
			responseBuffer.append("UPDATE ERROR : ").append(e.getMessage());
		}
		LOG.debug("Exiting Method updateCloudUser");
		
		return responseBuffer.toString();
	}

	
	/**
	 * Method to unsuspend the cloud user
	 * 
	 * @return Response message
	 * @throws URISyntaxException
	 * @throws IOException
	 * @throws Exception
	 */
	public String unSuspendCloudUser() {
		LOG.debug("Entering unSuspendCloudUser");
		StringBuffer responseBuffer = new StringBuffer();
		try {
			// Calling PostToOkta to unsuspend User
			if (StringUtils.isNotBlank(this.oktaId)) {
				RestResponse response = api.postToOkta(
						new URIBuilder(formatUrl(apiURL + ConstantUtils.UNSUSPEND,
								Collections.singletonMap(ConstantUtils.OKTA_ID, this.oktaId))), null, apiKey);
				if (null != response) {
					LOG.debug("Response Code " + response.getResponseCode() + " Response Message :: "
							+ response.getResponseMessage() + " Resp Body :: " + response.getResponseBody().toString());

					JsonNode respBody = response.getResponseBody();
					if (isSuccessCode(response.getResponseCode())) {
						responseBuffer.append("User has been unsuspended successfully. Object ID : " + this.oktaId);
					} else {
						if (respBody.hasNonNull(ConstantUtils.ERROR_SUMMARY)) {
							String errSummary = respBody.get(ConstantUtils.ERROR_SUMMARY).textValue();
							LOG.debug("Error summary :: " + errSummary);
							responseBuffer.append("ERROR : ").append(errSummary);
						}
					}
				} else {
					LOG.error("Response object is null");
					responseBuffer.append("ERROR: ").append("Response object is null");
				}
			} else {
				responseBuffer.append("ERROR: ").append("Error while fetching Okta ID for user ");
			}
		} catch (URISyntaxException e) {
			LOG.error("URISyntaxException occured in unSuspendCloudUser " + e.getMessage());
			responseBuffer.append("ERROR : ").append(e.getMessage());
		} catch (IOException e) {
			LOG.error("IOException occured in unSuspendCloudUser " + e.getMessage());
			responseBuffer.append("ERROR : ").append(e.getMessage());
		} catch (Exception e) {
			LOG.error("Exception occured in unSuspendCloudUser " + e.getMessage());
			responseBuffer.append("ERROR : ").append(e.getMessage());
		}
		LOG.debug("Exiting unSuspendCloudUser");
		return responseBuffer.toString();
	}

	/**
	 * Method to suspend cloud user
	 * 
	 * @return Response message
	 * @throws URISyntaxException
	 * @throws IOException
	 * @throws Exception
	 */
	public String suspendCloudUser() {
		LOG.debug("Entering suspendCloudUser");
		StringBuffer responseBuffer = new StringBuffer();
		try {
			if (StringUtils.isNotBlank(this.oktaId)) {
				RestResponse response = api.postToOkta(
						new URIBuilder(formatUrl(apiURL + ConstantUtils.SUSPEND,
								Collections.singletonMap(ConstantUtils.OKTA_ID, this.oktaId))), null, apiKey);
				if (null != response) {
					LOG.debug("Response Code " + response.getResponseCode() + " Response Message :: "
							+ response.getResponseMessage() + " Resp Body :: " + response.getResponseBody().toString());

					JsonNode respBody = response.getResponseBody();
					if (isSuccessCode(response.getResponseCode())) {
						responseBuffer.append("SUSPEND : " + oktaId);
					} else {
						responseBuffer.append("SUSPEND : " + oktaId + "; ").append(response.getResponseCode())
								.append(" ").append(response.getResponseBody().toString());
						if (respBody.hasNonNull(ConstantUtils.ERROR_SUMMARY)) {
							String errSummary = respBody.get(ConstantUtils.ERROR_SUMMARY).textValue();
							LOG.debug("Error summary :: " + errSummary);

							responseBuffer.append("ERROR : ").append(errSummary);
						}
					}
				} else {
					LOG.error("Response object is null");
					responseBuffer.append("ERROR : ").append("Error while suspending user");
				}
			} else {
				responseBuffer.append("ERROR: ").append("Error while fetching Okta ID for user ");
			}
		} catch (URISyntaxException e) {
			LOG.error("URISyntaxException occured in suspendCloudUser " + e.getMessage());
			responseBuffer.append("ERROR : ").append(e.getMessage());
		} catch (IOException e) {
			LOG.error("IOException occured in suspendCloudUser " + e.getMessage());
			responseBuffer.append("ERROR : ").append(e.getMessage());
		} catch (Exception e) {
			LOG.error("Exception occured in suspendCloudUser " + e.getMessage());
			responseBuffer.append("ERROR : ").append(e.getMessage());
		}
		LOG.debug("Exiting suspendCloudUser");
		return responseBuffer.toString();
	}

	/**
	 * This methos id used to create Group in Okta
	 * 
	 * @return
	 * @throws Exception
	 */
	public String createGroup() {
		LOG.debug("Entering Method createGroup");
		RestResponse response;
		StringBuffer responseBuffer = new StringBuffer();
		try {
			if (group != null) {
				/** Building the URL to create Group */
				URIBuilder builder = new URIBuilder(this.apiURL + ConstantUtils.GROUPS);
				/** Calling PostToOkta method */
				response = api.postToOkta(builder, group.getGroupAsString(), this.apiKey);
				if (null != response) {
					LOG.debug("Response Code " + response.getResponseCode() + " Response Message :: "
							+ response.getResponseMessage() + " Resp Body :: " + response.getResponseBody().toString());
					JsonNode respBody = response.getResponseBody();
					if (isSuccessCode(response.getResponseCode())) {
						responseBuffer
								.append("Group " + group.getProfileAttr(ConstantUtils.NAME) + " created successfully");
					} else {
						if (respBody.hasNonNull(ConstantUtils.ERROR_SUMMARY)) {
							String errSummary = respBody.get(ConstantUtils.ERROR_SUMMARY).textValue();
							LOG.debug("Error summary :: " + errSummary);
							responseBuffer.append("ERROR : ").append(errSummary);
						}
					}
				} else {
					LOG.error("Response object is null");
					responseBuffer.append("ERROR : ").append("Error while creation of group");
				}
			} else {
				responseBuffer.append("ERROR : ").append("Input group object is null");
			}

		} catch (IOException e) {
			LOG.error("IOException in createGroup  " + e.getMessage());
			responseBuffer.append("ERROR : ").append(e.getMessage());
		} catch (URISyntaxException e) {
			LOG.error("URISyntaxException in createGroup  " + e.getMessage());
			responseBuffer.append("ERROR : ").append(e.getMessage());
		} catch (Exception e) {
			LOG.error("Exception in createGroup  " + e.getMessage());
			responseBuffer.append("ERROR : ").append(e.getMessage());
		}
		LOG.debug("Exiting method createGroup");
		return responseBuffer.toString();
	}

	/**
	 * This method is used to add users to the Groups.
	 * 
	 * @return
	 * @throws Exception
	 */
	public String addUserToCloudGroup() {
		LOG.debug("Entering method addUserToCloudGroup");
		RestResponse response;
		StringBuffer responseBuffer = new StringBuffer();
		try {
			if (groupId != null && StringUtils.isNotBlank(groupId)) {
				/** Building URL to addUserToCloudGroup */
				URIBuilder builder = new URIBuilder(this.apiURL + ConstantUtils.GROUPS + "/" + groupId + ConstantUtils.USERS
						+ "/" + this.oktaId);
				/** Calling PutToOkta method */
				response = api.putToOkta(builder, null, this.apiKey);
				if (null != response) {
					LOG.debug("Response Code " + response.getResponseCode() + " Response Message :: "
							+ response.getResponseMessage());

					JsonNode respBody = response.getResponseBody();
					if (isSuccessCode(response.getResponseCode())) {
						responseBuffer.append(this.groupId + " group is added successfully to the User " + this.oktaId);
					} else {
						if (respBody.hasNonNull(ConstantUtils.ERROR_SUMMARY)) {
							String errSummary = respBody.get(ConstantUtils.ERROR_SUMMARY).textValue();
							LOG.debug("Error summary :: " + errSummary);
							responseBuffer.append("ERROR : ").append(errSummary);
						}
					}
				} else {
					LOG.error("Response object is null");
					responseBuffer.append("ERROR : ").append("Error while adding User to group");
				}
			}
		} catch (IOException e) {
			LOG.error("IOException in addUserToCloudGroup  " + e.getMessage());
			responseBuffer.append("ERROR : ").append(e.getMessage());
		} catch (URISyntaxException e) {
			LOG.error("URISyntaxException in addUserToCloudGroup  " + e.getMessage());
			responseBuffer.append("ERROR : ").append(e.getMessage());
		} catch (Exception e) {
			LOG.error("Error in addUserToCloudGroup  " + e.getMessage());
			responseBuffer.append("ERROR : ").append(e.getMessage());
		}
		LOG.debug("Exiting method addUserToCloudGroup");
		return responseBuffer.toString();
	}

	/**
	 * This method is used to remove users from GROUP
	 * 
	 * @return
	 * @throws Exception
	 */
	public String removeUserFromCloudGroup() {
		LOG.debug("Entering method removeUserFromCloudGroup");
		RestResponse response;
		StringBuffer responseBuffer = new StringBuffer();
		try {
			if (groupId != null && StringUtils.isNotBlank(groupId)) {
				/** Building URL to removeUserFromCloudGroup */
				URIBuilder builder = new URIBuilder(this.apiURL + ConstantUtils.GROUPS + "/" + groupId + ConstantUtils.USERS
						+ "/" + this.oktaId);
				/** Calling deleteToOkta to remove user from Group */
				response = api.deleteToOkta(builder, this.apiKey);
				if (null != response) {
					LOG.debug("Response Code " + response.getResponseCode() + " Response Message :: "
							+ response.getResponseMessage());

					JsonNode respBody = response.getResponseBody();
					if (isSuccessCode(response.getResponseCode())) {
						responseBuffer.append(this.groupId + " group successfully removed for the User " + this.oktaId);
					} else {
						if (respBody.hasNonNull(ConstantUtils.ERROR_SUMMARY)) {
							String errSummary = respBody.get(ConstantUtils.ERROR_SUMMARY).textValue();
							LOG.debug("Error summary :: " + errSummary);
							responseBuffer.append("ERROR : ").append(errSummary);
						}
					}
				} else {
					LOG.error("Response object is null");
					responseBuffer.append("ERROR : ").append("Error while removing user from group");
				}
			}
		} catch (IOException e) {
			LOG.error("IOException in removeUserFromCloudGroup  " + e.getMessage());
			responseBuffer.append("ERROR : ").append(e.getMessage());
		} catch (URISyntaxException e) {
			LOG.error("URISyntaxException in removeUserFromCloudGroup  " + e.getMessage());
			responseBuffer.append("ERROR : ").append(e.getMessage());
		} catch (Exception e) {
			LOG.error("Exception in removeUserFromCloudGroup  " + e.getMessage());
			responseBuffer.append("ERROR : ").append(e.getMessage());
		}
		LOG.debug("Exiting method removeUserFromCloudGroup");
		return responseBuffer.toString();
	}

	/**
	 * This method is used to delete group in OKTA
	 * 
	 * @return
	 * @throws Exception
	 */
	public String deleteCloudGroup() {
		LOG.debug("Entering Method deleteCloudGroup");
		RestResponse response;
		StringBuffer responseBuffer = new StringBuffer();
		try {
			if (groupId != null && StringUtils.isNotBlank(groupId)) {
				/** Building URL to deleteCloudGroup */
				URIBuilder builder = new URIBuilder(formatUrl(this.apiURL + ConstantUtils.GROUP,
						Collections.singletonMap("groupId", groupId)));
				/** Calling deleteToOkta delete Group */
				response = api.deleteToOkta(builder, this.apiKey);
				if (null != response) {
					LOG.debug("Response Code " + response.getResponseCode() + " Response Message :: "
							+ response.getResponseMessage());

					JsonNode respBody = response.getResponseBody();
					if (isSuccessCode(response.getResponseCode())) {
						responseBuffer.append(this.groupId + " group successfully deleted");
					} else {
						if (respBody.hasNonNull(ConstantUtils.ERROR_SUMMARY)) {
							String errSummary = respBody.get(ConstantUtils.ERROR_SUMMARY).textValue();
							LOG.debug("Error summary :: " + errSummary);
							responseBuffer.append("ERROR : ").append("errSummary");
						}
					}
				} else {
					LOG.error("Response object is null");
					responseBuffer.append("ERROR : ").append("Error while deleting group");
				}
			}
		} catch (IOException e) {
			LOG.error("IOException in deleteCloudGroup  " + e.getMessage());
			responseBuffer.append("ERROR : ").append(e.getMessage());
		} catch (URISyntaxException e) {
			LOG.error("URISyntaxException in deleteCloudGroup  " + e.getMessage());
			responseBuffer.append("ERROR : ").append(e.getMessage());
		} catch (Exception e) {
			LOG.error("Exception in deleteCloudGroup  " + e.getMessage());
			responseBuffer.append("ERROR : ").append(e.getMessage());
		}
		LOG.debug("Exiting method deleteCloudGroup");
		return responseBuffer.toString();
	}
	
	/**
	 * This method is used to Enroll SMS Factor with updated Phone Number This method deletes the existing SMS Factor
	 * and then enroll SMS with the updated Mobile number if the mobile number is updated during the updateUser
	 * 
	 * @param oktaId
	 *            - Users OktaId
	 * @param phoneNumber
	 *            - Users Mobile Number
	 * @return
	 * @throws IOException
	 * @throws URISyntaxException
	 * @throws Exception
	 */
	public RestResponse enrollSmsWithUpdatedPhoneNumber(String oktaId, String phoneNumber) throws URISyntaxException,
			Exception {
		LOG.debug("Entering method enrollSmsWithUpdatedPhoneNumber");
		RestResponse response = null;
		String factorID = "";
		String factorType = "";
		try {
			if (StringUtils.isNotBlank(oktaId) && StringUtils.isNotBlank(phoneNumber)) {
				/** Getting the list of existing MFA Factors for the user */
				URIBuilder builder = new URIBuilder(this.apiURL + ConstantUtils.USERS + "/" + oktaId + "/factors");
				try {
					/** Calling getToOkta methods to get the list of Factors for the User */
					response = api.getToOkta(builder, apiKey);
				} catch (URISyntaxException | IOException e) {
					LOG.error("URI,IO Exception occured in getting list of enrolled factors" + e.getMessage());
					throw e;
				} catch (Exception e) {
					LOG.error("General Exception occured in getting list of enrolled factors" + e.getMessage());
					throw e;
				}
				if (response != null && isSuccessCode(response.getResponseCode())) {
					JsonNode jsNode = response.getResponseBody();
					if (jsNode != null) {
						Iterator<JsonNode> jsNodeItr = jsNode.elements();
						while (jsNodeItr.hasNext()) {
							JsonNode jsNodeElement = jsNodeItr.next();
							if (jsNodeElement != null) {
								factorType = jsNodeElement.get("factorType").asText();
								/** Extracting the SMS Factor ID from the List of Factors */
								if (factorType.equals("sms")) {
									factorID = jsNodeElement.get(ConstantUtils.ID).asText();
									break;
								}
							}
						}
					} else {
						LOG.debug("User is not assigned with any MFA factors");
					}
					if (StringUtils.isNotBlank(factorID)) {
						/** Deleting the SMS Factor if the user has existing SMS Factor */
						URIBuilder deleteFactorBuilder = new URIBuilder(this.apiURL + ConstantUtils.USERS + "/" + oktaId
								+ "/factors" + "/" + factorID);
						try {
							/** Calling DeleteToOkta if user is assigned with SMS Fcator */
							response = api.deleteToOkta(deleteFactorBuilder, apiKey);
						} catch (URISyntaxException | IOException e) {
							LOG.error("URI,IO Exception occured in deleting factor for the User" + e.getMessage());
							throw e;
						} catch (Exception e) {
							LOG.error("Exception occured in deleting factor to the User" + e.getMessage());
							throw e;
						}
					}
					if (response != null && isSuccessCode(response.getResponseCode())) {
						/** Enrolling the SMS as MFA with the updated Phone number */
						URIBuilder enrollwithUpdated = new URIBuilder(this.apiURL + ConstantUtils.USERS + "/" + oktaId
								+ "/factors");
						enrollwithUpdated.addParameter("updatePhone", "true");
						// Add activate parameter. This will instruct Okta not to send SMS verification
						enrollwithUpdated.addParameter("activate", "true");
						try {
							response = api.postToOkta(enrollwithUpdated,
									mapper.writeValueAsString(new Factor("sms", phoneNumber)), apiKey);
						} catch (URISyntaxException | IOException e) {
							LOG.error("URI,IO Exception occured in enrolling sms with updated phone number"
									+ e.getMessage());
							throw e;
						} catch (Exception e) {
							LOG.error("Exception Ocuured in enrolling sms with updated phone number " + e.getMessage());
							throw e;
						}
					}
				}
			}
		} catch (URISyntaxException e) {
			LOG.error("URISyntaxException in enrollSmsWithUpdatedPhoneNumber method " + e.getMessage());
			throw e;
		} catch (Exception e) {
			LOG.error("Error in Processing JSON in enrollSmsWithUpdatedPhoneNumber method " + e.getMessage());
			throw e;
		}
		LOG.debug("Exiting method enrollSmsWithUpdatedPhoneNumber");
		return response;
	}

	/**
	 * This method helps to provide the error details
	 * 
	 * @param response
	 *            - RestResponse
	 * @param responseBuffer
	 *            - String
	 * @param call
	 *            - String
	 */
	public void addError(RestResponse response, StringBuffer responseBuffer, String call) {
		if (response != null) {
			LOG.error(responseBuffer.toString() + response.getResponseBody().toString());
		} else {
			LOG.error(responseBuffer.toString());
		}
		responseBuffer.append(call).append(" ERROR");
	}

	/**
	 * This method is called from createCloudUser method and helps in enrolling SMS as MFA.
	 * 
	 * @param oktaId
	 *            - String -Users OktaId
	 * @param phoneNumber
	 *            - String - Users Phone Number
	 * @return response - RestResponse
	 * @throws IOException
	 * @throws URISyntaxException
	 * @throws Exception
	 */
	public RestResponse enrollSms(String oktaId, String phoneNumber) throws IOException, URISyntaxException, Exception {
		LOG.debug("Entering method enrollSms");
		RestResponse response = null;
		try {
			URIBuilder builder = new URIBuilder(formatUrl(this.apiURL + ConstantUtils.ENROLL_FACTOR,
					Collections.singletonMap(ConstantUtils.OKTA_ID, oktaId)));
			// Add activate parameter. This will instruct Okta not to send SMS verification
			builder.addParameter("activate", "true");
			// Now enroll them in SMS factor
			response = api.postToOkta(builder, mapper.writeValueAsString(new Factor("sms", phoneNumber)), apiKey);
		} catch (IOException e) {
			LOG.error("IOException in enrollSms method " + e.getMessage());
			throw e;
		} catch (URISyntaxException e) {
			LOG.error("URISyntaxException in enrollSms method " + e.getMessage());
			throw e;
		} catch (Exception e) {
			LOG.error("Error in Processing JSON in enrollSms method " + e.getMessage());
			throw e;
		}
		LOG.debug("Exiting method enrollSms");
		return response;
	}


	/**
	 * @param responseCode
	 *            - int - HTTP Response
	 * @return boolean value
	 */
	private boolean isSuccessCode(int responseCode) {
		return responseCode == HttpURLConnection.HTTP_CREATED || responseCode == HttpURLConnection.HTTP_OK
				|| responseCode == HttpURLConnection.HTTP_NO_CONTENT;
	}

	/**
	 * This method formats the URL with Prefix and Suffix.
	 * 
	 * @param url
	 *            - String
	 * @param params
	 *            - Map
	 * @return String - formatted URL
	 */
	private String formatUrl(String url, Map<String, String> params) {
		return new StrSubstitutor(params).setVariablePrefix('{').setVariableSuffix('}').replace(url);
	}

	/**
	 * This is a helper method to call getOktaId.
	 * 
	 * @param username
	 *            - String - username of the user
	 * @return
	 * @throws Exception
	 */
	public String getOktaId(String username) throws Exception {
		/** Setting LOGIN attr value in the user profile */
		this.user.setProfileAttr(ConstantUtils.LOGIN, username);
		try {
			return getOktaId();
		} catch (Exception e) {
			LOG.error("Exception occured in getOktaId" + e.getMessage());
			throw e;
		}
	}

	/**
	 * This method is used to fetch OktaId of the User.
	 * 
	 * @return - OktaId of the User
	 * @throws
	 * @throws IOException
	 * @throws URISyntaxException
	 * @throws Exception
	 */
	public String getOktaId() throws IOException, URISyntaxException {
		LOG.debug("Entering Method getOktaId");
		if (this.oktaId == null || this.oktaId.isEmpty()) {
			boolean success;
			RestResponse response;
			URIBuilder builder = null;
			StringBuffer responseBuffer = new StringBuffer();
			String login = this.login;
			/** Extracting login value from the User profile if User object is provided */
			if (StringUtils.isBlank(login)) {
				login = this.user.getProfileAttr(ConstantUtils.LOGIN);
			}
			try {
				LOG.debug("Login :: " + login);
				/** Building the URL to get the OktaID based on Login value */
				builder = new URIBuilder(formatUrl(this.apiURL + ConstantUtils.USER,
						Collections.singletonMap(ConstantUtils.OKTA_ID, login)));
				/** Calling getToOkta */
				response = api.getToOkta(builder, this.apiKey);
				success = response != null && isSuccessCode(response.getResponseCode());

				/** get the userId and pass the URL for enabling factors for this user */
				if (success) {
					this.oktaId = response.getResponseBody().get(ConstantUtils.ID).asText();
				} else {
					addError(response, responseBuffer, "GET");
				}
			} catch (URISyntaxException | IOException e) {
				LOG.error("IO,URISyntax Exception occured in getOktaId  " + e.getMessage());
				responseBuffer.append(e);
				throw e;
			} catch (Exception e) {
				LOG.error("Exception occured in getOktaId  " + e.getMessage());
				responseBuffer.append(e);
			}

		}
		LOG.debug("Exiting Method getOktaId");
		return this.oktaId;
	}

	/**
	 * This method is used to set the OktaId in the Constructor
	 * 
	 * @param oktaId
	 */
	public void setOktaId(String oktaId) {
		if (oktaId != null && !oktaId.isEmpty()) {
			this.oktaId = oktaId;
		} else {
			throw new IllegalArgumentException();
		}
	}

}
