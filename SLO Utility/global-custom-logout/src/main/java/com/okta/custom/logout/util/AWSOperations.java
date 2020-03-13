/*
 * Comcast Copyright Details
 * 
 */

package com.okta.custom.logout.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.JSONObject;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.dynamodbv2.model.ScanResult;
import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.AWSSecretsManagerClientBuilder;
import com.amazonaws.services.secretsmanager.model.GetSecretValueRequest;
import com.amazonaws.services.secretsmanager.model.GetSecretValueResult;

/**
 * The AWSOperations class has the functionality of retrieving the token from AWS SecretsManager and data from DynamoDB
 * tables.
 */
public class AWSOperations {

	/** The Constant LOGGER. */
	private static final Logger LOGGER = Logger.getLogger(AWSOperations.class);

	/** A String representing the class name. */
	private static final String CLASSNAME = AWSOperations.class.getSimpleName();

	/** Map for storing the Okta configurations. */
	private static Map<String, Object> oktaMap = new HashMap<String, Object>();

	/** Map for storing the Application Logout URLs. */
	private static HashMap<String, String> applicationNameVsURLsMap = new HashMap<String, String>();

	/**
	 * Getter method for oktaMap
	 * 
	 * @return HashMap with Okta details
	 */
	public Map<String, Object> getOktaMap() {
		return oktaMap;
	}

	/**
	 * Setter method for oktaMap
	 * 
	 * @param oktaMap
	 */
	public void setOktaMap(Map<String, Object> oktaMap) {
		this.oktaMap = oktaMap;
	}

	/**
	 * Getter method for applicationNameVsURLsMap
	 * 
	 * @return
	 */
	public static HashMap<String, String> getApplicationNameVsURLsMap() {
		return applicationNameVsURLsMap;
	}

	/**
	 * Setter method for applicationNameVsURLsMap
	 * 
	 * @param applicationNameVsURLsMap
	 */
	public static void setApplicationNameVsURLsMap(HashMap<String, String> applicationNameVsURLsMap) {
		AWSOperations.applicationNameVsURLsMap = applicationNameVsURLsMap;
	}

	/**
	 * This method retrieves the token from the AWS SecretsManager based on the token key.
	 * 
	 * @param tokenKey
	 *            Token Key
	 * @return token Token
	 */
	public String getSecret(final String tokenKey) throws Exception {
		String token = null;
		String secret = null;
		final StringBuilder stringBuilder = new StringBuilder();
		final String methodName = LogoutHelper.GET_SECRET;
		LOGGER.info(LogoutHelper.METHOD_OPEN
				+ stringBuilder.delete(0, stringBuilder.length()).append(CLASSNAME).append(methodName).toString());
		// Create a Secrets Manager client
		AWSSecretsManager client = AWSSecretsManagerClientBuilder.standard().build();
		GetSecretValueRequest getSecretValueRequest = new GetSecretValueRequest().withSecretId(tokenKey);
		GetSecretValueResult getSecretValueResult = null;
		try {
			getSecretValueResult = client.getSecretValue(getSecretValueRequest);
		} catch (Exception e) {
			LOGGER.error(stringBuilder.delete(0, stringBuilder.length()).append(CLASSNAME).append(methodName)
					.append(LogoutHelper.EXCEPTION_HOLDER).append(e));
			throw e;
		}
		// Decrypts secret using the associated KMS CMK.
		if (getSecretValueResult.getSecretString() != null && !getSecretValueResult.getSecretString().isEmpty()) {
			secret = getSecretValueResult.getSecretString();
			JSONObject json = new JSONObject(secret);
			Object tokenObj = json.get(tokenKey);
			if (null != tokenObj && tokenObj instanceof String) {
				token = (String) json.get(tokenKey);
			}

		}
		LOGGER.info(LogoutHelper.METHOD_EXIT
				+ stringBuilder.delete(0, stringBuilder.length()).append(CLASSNAME).append(methodName).toString());
		return token;
	}

	/**
	 * Method which retrieves data from the Amazon DynamoDB tables "OktaDetails" and "Application_Details" and store it
	 * in the static Map variables.
	 */
	public void getOktaConfigurations() {
		final StringBuilder stringBuilder = new StringBuilder();
		final String methodName = "/getOktaConfigurations";
		LOGGER.info(LogoutHelper.METHOD_OPEN
				+ stringBuilder.delete(0, stringBuilder.length()).append(CLASSNAME).append(methodName).toString());
		try {
			// Initialize the DynamoDB client
			AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard().build();
			if (null != client) {
				// Retrieve data from the table "OktaDetails"
				final ScanRequest scanRequest = new ScanRequest().withTableName(LogoutHelper.OKTA_DETAILS);
				final ScanResult result = client.scan(scanRequest);
				if (null != result) {
					final List<Map<String, AttributeValue>> items = result.getItems();
					for (int index = 0; index < items.size(); index++) {
						final Map<String, AttributeValue> value = items.get(index);
						for (Map.Entry<String, AttributeValue> mapElement : value.entrySet()) {
							final String key = (String) mapElement.getKey();
							final AttributeValue attrValue = (AttributeValue) mapElement.getValue();
							if (attrValue.getS() != null) {
								oktaMap.put(key, attrValue.getS());
							} else if (attrValue.getN() != null) {
								oktaMap.put(key, attrValue.getN());
							} else if (attrValue.getSS() != null) {
								oktaMap.put(key, attrValue.getSS());
							}
						}
					}
				}
				// Retrieve data from the table "Application_Details"
				ScanRequest appscanRequest = new ScanRequest().withTableName(LogoutHelper.APP_DETAILS);
				ScanResult appResult = client.scan(appscanRequest);
				AttributeValue applicationNameAttributeValue = null;
				String applicationName = null;
				AttributeValue applicationURLAttributeValue = null;
				String applicationURL = null;
				if (null != appResult) {
					List<Map<String, AttributeValue>> appItems = appResult.getItems();
					LOGGER.info(stringBuilder.delete(0, stringBuilder.length()).append(CLASSNAME).append(methodName)
							.append("appItems size ").append(appItems.size()).toString());
					for (Map<String, AttributeValue> value : appItems) {
						if (null == value) {
							continue;
						}
						applicationNameAttributeValue = value.get(LogoutHelper.APPLICATION_NAME);
						applicationURLAttributeValue = value.get(LogoutHelper.APPLICATION_LOGOUT_URL);
						if (null == applicationNameAttributeValue) {
							continue;
						} else {
							applicationName = applicationNameAttributeValue.getS();
						}
						if (null == applicationURLAttributeValue) {
							applicationURL = null;
						} else {
							applicationURL = applicationURLAttributeValue.getS();
						}
						applicationNameVsURLsMap.put(applicationName, applicationURL);
					}
				}
			}
		} catch (Exception exp) {
			exp.printStackTrace();
		}
		LOGGER.info(LogoutHelper.METHOD_EXIT
				+ stringBuilder.delete(0, stringBuilder.length()).append(CLASSNAME).append(methodName).toString());
	}

}
