
package ldap.account.migration.cloud;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.Map;

import ldap.account.migration.bean.Factor;
import ldap.account.migration.bean.RestResponse;
import ldap.account.migration.bean.User;
import lds.account.migration.constants.ConstantUtils;

import org.apache.commons.lang3.text.StrSubstitutor;
import org.apache.http.client.utils.URIBuilder;
import org.apache.log4j.Logger;
import com.fasterxml.jackson.databind.ObjectMapper;

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


	public CloudUser(User user, String apiURL, String apiKey) {
		this.user = user;
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


}
