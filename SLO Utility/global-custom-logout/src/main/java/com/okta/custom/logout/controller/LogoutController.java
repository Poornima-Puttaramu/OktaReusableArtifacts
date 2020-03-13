/*
 * Comcast Copyright Details
 * 
 */

package com.okta.custom.logout.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.okta.custom.logout.util.AWSOperations;
import com.okta.custom.logout.util.LogoutHelper;
import com.google.json.JsonSanitizer;

/**
 * The controller class which has the functionality of retrieving the logout URLs of the applications which the user has
 * logged in in the past few hours. This class gets triggered when an user tries to logout from an application.The
 * functionality of this class is explained below 1.Retrieves the logged in user's OKTA ID from the OKTA Javascript
 * library. 2.Retrieves the admin token from AWS SecretsManager. 3.Invoke the OKTA's SystemLog API using the admin token
 * and retrieves the list of applications the user has logged into in the past few numbers (The number of hours is
 * maintained as a configurable parameter in the properties file). 4.Retrieves the Logout URLs of the applications from
 * the properties file. 5.Passes on the Logout URLs to the user.jsp which has the logic of invoking the Logout URLs over
 * an AJAX call.
 */
@Controller
public class LogoutController {

	/** The Constant LOGGER. */
	private static final Logger LOGGER = Logger.getLogger(LogoutHelper.LOGGER_NAME);

	/** A String representing the class name. */
	private static final String CLASSNAME = LogoutController.class.getSimpleName();

	/**
	 * Entry method of the controller class which renders the Logout page
	 * 
	 * @param model
	 *            Model Object
	 * @param session
	 *            HTTPSession Object
	 * @return LogoutConstants.LOGOUT_PAGE
	 */
	@GetMapping(value = "/")
	public String home(Model model, final HttpSession session) {
		// Generate CSRF token and add it to the session
		SecureRandom rnum = new SecureRandom();
		final int csrfInt = rnum.nextInt();
		String csrfToken = Integer.toString(csrfInt);
		session.setAttribute(LogoutHelper.CSRF_TOKEN, csrfToken);
		// display the logout page
		return LogoutHelper.LOGOUT_PAGE;
	}

	/**
	 * Method which retrieves the Logout URLs of the logged in applications
	 * 
	 * @param reqBody
	 *            RequestBody Object
	 * @param model
	 *            Model Object
	 * @param session
	 *            HTTPSession Object
	 * @param request
	 *            HTTPRequest Object
	 * @return LogoutConstants.USER_PAGE
	 */
	@PostMapping(value = "/user")
	public String getTargetURLs(@RequestBody String reqBody, Model model, final HttpSession session,
			final HttpServletRequest request) {
		final StringBuilder stringBuilder = new StringBuilder();
		final String methodName = LogoutHelper.TARGET_URLS;
		LOGGER.info(LogoutHelper.METHOD_OPEN
				+ stringBuilder.delete(0, stringBuilder.length()).append(CLASSNAME).append(methodName).toString());
		String targets = null;
		try{
		if (null != reqBody) {
			// Validate CSRF Token against the token parameter
			Object storedToken = session.getAttribute(LogoutHelper.CSRF_TOKEN);
			if (null != storedToken && storedToken instanceof String) {
				storedToken = (String) session.getAttribute(LogoutHelper.CSRF_TOKEN);
			}
			final String token = request.getParameter(LogoutHelper.TOKEN);
			if (null != storedToken && ((String) storedToken).equalsIgnoreCase(token)) {
				final String userID = request.getParameter(LogoutHelper.USERID);
				if (null != userID && !userID.isEmpty()) {
					targets = getTargets(userID);
					LOGGER.info(stringBuilder.delete(0, stringBuilder.length()).append(CLASSNAME).append(methodName)
							.append("targets ").append(targets).toString());
				}
			}
		}
		// Set the application logout URLs in the attribute
		model.addAttribute(LogoutHelper.TARGETS, targets);
		}catch(Exception exp){
			LOGGER.error(stringBuilder.delete(0, stringBuilder.length()).append(CLASSNAME).append(methodName)
					.append(LogoutHelper.EXCEPTION_HOLDER).append(exp).toString());
		}
		LOGGER.info(LogoutHelper.METHOD_EXIT
				+ stringBuilder.delete(0, stringBuilder.length()).append(CLASSNAME).append(methodName).toString());
		// display the logoutsuccess page
		return LogoutHelper.USER_PAGE;
	}

	/**
	 * Method which retrieves the Logout URLs of the applications
	 * 
	 * @param userID
	 *            UserID of the user
	 * @return logoutURLs Logout URLs of the applications
	 */
	private String getTargets(final String userID) {
		final StringBuilder stringBuilder = new StringBuilder();
		final String methodName = LogoutHelper.GET_TARGETS;
		LOGGER.info(LogoutHelper.METHOD_OPEN
				+ stringBuilder.delete(0, stringBuilder.length()).append(CLASSNAME).append(methodName).toString());
		Set<String> appNames = new HashSet<String>();
		String logoutURLs = "";
		try {
			if (null != userID && !userID.isEmpty()) {
				final AWSOperations awsOperations = new AWSOperations();
				Map<String, Object> oktaMap = awsOperations.getOktaMap();
				LOGGER.info(stringBuilder.delete(0, stringBuilder.length()).append(CLASSNAME).append(methodName)
						.append("oktaMap ").append(oktaMap).toString());
				if (null != oktaMap && oktaMap.containsKey(LogoutHelper.TOKEN_KEY)) {
					Object tokenKey = oktaMap.get(LogoutHelper.TOKEN_KEY);
					if (null != tokenKey && tokenKey instanceof String) {
						tokenKey = (String) oktaMap.get(LogoutHelper.TOKEN_KEY);
						final String secret = awsOperations.getSecret((String) tokenKey);
						String jsonString = getJSONFromOkta(userID, secret);
						if (null != jsonString && !jsonString.isEmpty()) {
							String wellFormedJson = JsonSanitizer.sanitize(jsonString);
							JSONArray object = (JSONArray) new JSONTokener(wellFormedJson).nextValue();
							for (int outerJsonIndex = 0; outerJsonIndex < object.length(); outerJsonIndex++) {
								final JSONObject jsonObj = (JSONObject) object.get(outerJsonIndex);
								final Object target = jsonObj.get(LogoutHelper.TARGET);
								if (target instanceof JSONObject) {
									LOGGER.info(stringBuilder.delete(0, stringBuilder.length()).append(CLASSNAME)
											.append(methodName).append("JSON Object ").toString());
								} else if (target instanceof JSONArray) {
									JSONArray arr = (JSONArray) target;
									if (null != arr) {
										for (int innerJsonIndex = 0; innerJsonIndex < arr.length(); innerJsonIndex++) {
											JSONObject tarObj = (JSONObject) arr.get(innerJsonIndex);
											final String type = tarObj.getString(LogoutHelper.TYPE);
											if (type != null && type.equalsIgnoreCase(LogoutHelper.APP_INSTANCE)) {
												String displayName = tarObj.getString(LogoutHelper.DISPLAY_NAME);
												appNames.add(displayName);
											}
										}
									}

								}
							}
							final HashMap<String, String> appLogoutList = AWSOperations.getApplicationNameVsURLsMap();
							for (String appName : appNames) {
								LOGGER.info(stringBuilder.delete(0, stringBuilder.length()).append(CLASSNAME)
										.append(methodName).append(" appName = ").append(appName).toString());
								if (null != appLogoutList && appLogoutList.containsKey(appName)) {
									final String logout = appLogoutList.get(appName);
									LOGGER.info(stringBuilder.delete(0, stringBuilder.length()).append(CLASSNAME)
											.append(methodName).append(" logout = ").append(logout).toString());
									logoutURLs = logoutURLs.concat(logout);
									logoutURLs = logoutURLs.concat(LogoutHelper.PIPE);
								}
							}
							LOGGER.info(stringBuilder.delete(0, stringBuilder.length()).append(CLASSNAME)
									.append(methodName).append("logoutURLs ").append(logoutURLs).toString());
						}
					}
				}
			}
		} catch (Exception e) {
			LOGGER.error(stringBuilder.delete(0, stringBuilder.length()).append(CLASSNAME).append(methodName)
					.append(LogoutHelper.EXCEPTION_HOLDER).append(e).toString());
		}
		LOGGER.info(LogoutHelper.METHOD_EXIT
				+ stringBuilder.delete(0, stringBuilder.length()).append(CLASSNAME).append(methodName).toString());
		return logoutURLs;
	}

	/**
	 * Method which calls the Okta's SystemLog API and gets the JSON back
	 * 
	 * @param userID
	 * @param token
	 * @return resultString
	 */
	private String getJSONFromOkta(final String userID, final String token) {
		final StringBuilder stringBuilder = new StringBuilder();
		final String methodName = LogoutHelper.JSON_FROM_OKTA;
		LOGGER.info(LogoutHelper.METHOD_OPEN
				+ stringBuilder.delete(0, stringBuilder.length()).append(CLASSNAME).append(methodName).toString());
		BufferedReader bufferedReader = null;
		InputStreamReader inputReader = null;
		InputStream inputStream = null;
		String resultString = null;
		CloseableHttpClient client = null;
		CloseableHttpResponse response = null;
		try {
			AWSOperations awsOperations = new AWSOperations();
			Map<String, Object> oktaMap = awsOperations.getOktaMap();
			final String finalURL = (String) oktaMap.get(LogoutHelper.OKTA_API);
			LOGGER.info(stringBuilder.delete(0, stringBuilder.length()).append(CLASSNAME).append(methodName)
					.append(" finalURL = ").append(finalURL).toString());
			String eventType = (String) oktaMap.get(LogoutHelper.EVENT_TYPE);
			LOGGER.info(stringBuilder.delete(0, stringBuilder.length()).append(CLASSNAME).append(methodName)
					.append(" eventType = ").append(eventType).toString());
			final String outcomeResult = (String) oktaMap.get(LogoutHelper.OUTCOME_RESULT);
			LOGGER.info(stringBuilder.delete(0, stringBuilder.length()).append(CLASSNAME).append(methodName)
					.append(" outcome.result = ").append(outcomeResult).toString());
			final String syslogHours = (String) oktaMap.get(LogoutHelper.SYSLOG_HOURS);
			LOGGER.info(stringBuilder.delete(0, stringBuilder.length()).append(CLASSNAME).append(methodName)
					.append(" Syslog_API_Hours = ").append(syslogHours).toString());
			// Preparing the request using the request URL and token
			client = HttpClientBuilder.create().build();
			final String sinceDate = getDuration(syslogHours);
			LOGGER.info(stringBuilder.delete(0, stringBuilder.length()).append(CLASSNAME).append(methodName)
					.append(" sinceDate = ").append(sinceDate).toString());
			URIBuilder builder = new URIBuilder(finalURL);
			builder.setParameter(
						LogoutHelper.FILTER,
						LogoutHelper.ACTOR_ID_FILTER + "\"" + StringEscapeUtils.escapeHtml4(userID) 
								+ "\" and eventType eq " + "\"" + StringEscapeUtils.escapeHtml4(eventType) + "\"")
						.setParameter(LogoutHelper.OUTCOME_RESULT, outcomeResult)
						.setParameter(LogoutHelper.SINCE, sinceDate);			
			final HttpGet getRequest = new HttpGet(builder.build());
			getRequest.addHeader(LogoutHelper.AUTHORIZATION, token);
			// Submitting the request as GET
			response = client.execute(getRequest);
			if (null != response) {
				inputStream = response.getEntity().getContent();
				inputReader = new InputStreamReader(inputStream);
				bufferedReader = new BufferedReader(inputReader);
				final int responseCode = response.getStatusLine().getStatusCode();
				LOGGER.info(stringBuilder.delete(0, stringBuilder.length()).append(CLASSNAME).append(methodName)
						.append(" responseCode = ").append(responseCode).toString());
				String line;
				String requestResponse = "";
				while ((line = bufferedReader.readLine()) != null) {
					requestResponse = line;
				}
				if (responseCode == LogoutHelper.STATUS_CODE_SUCCESS) {
					resultString = requestResponse;
				}
			}
		} catch (Exception exp) {
			LOGGER.info(stringBuilder.delete(0, stringBuilder.length()).append(CLASSNAME).append(methodName)
					.append(LogoutHelper.EXCEPTION_HOLDER).append(exp.getMessage()).toString());
		} finally {
			if (null != client) {
				try {
					client.close();
				} catch (IOException ioExp) {
					LOGGER.info(stringBuilder.delete(0, stringBuilder.length()).append(CLASSNAME).append(methodName)
							.append(LogoutHelper.EXCEPTION_HOLDER).append(ioExp.getMessage()).toString());
				}
			}
			if (null != response) {
				try {
					response.close();
				} catch (IOException ioExp) {
					LOGGER.info(stringBuilder.delete(0, stringBuilder.length()).append(CLASSNAME).append(methodName)
							.append(LogoutHelper.EXCEPTION_HOLDER).append(ioExp.getMessage()).toString());
				}
			}
			if (null != inputStream) {
				try {
					inputStream.close();
				} catch (IOException ioExp) {
					LOGGER.info(stringBuilder.delete(0, stringBuilder.length()).append(CLASSNAME).append(methodName)
							.append(LogoutHelper.EXCEPTION_HOLDER).append(ioExp.getMessage()).toString());
				}
			}
			if (null != inputReader) {
				try {
					inputReader.close();
				} catch (IOException ioExp) {
					LOGGER.info(stringBuilder.delete(0, stringBuilder.length()).append(CLASSNAME).append(methodName)
							.append(LogoutHelper.EXCEPTION_HOLDER).append(ioExp.getMessage()).toString());
				}
			}
			if (null != bufferedReader) {
				try {
					bufferedReader.close();
				} catch (IOException ioExp) {
					LOGGER.info(stringBuilder.delete(0, stringBuilder.length()).append(CLASSNAME).append(methodName)
							.append(LogoutHelper.EXCEPTION_HOLDER).append(ioExp.getMessage()).toString());
				}
			}
		}
		LOGGER.info(LogoutHelper.METHOD_EXIT
				+ stringBuilder.delete(0, stringBuilder.length()).append(CLASSNAME).append(methodName).toString());
		return resultString;
	}

	/**
	 * Method which calculates the time which will be used a filter in the SystemLog API. This method pickup the current
	 * time and calculates the new time by subtracting the system log hours from the current time.
	 * 
	 * @param syslogHours
	 * @return duration
	 */
	public String getDuration(String syslogHours) {
		final StringBuilder stringBuilder = new StringBuilder();
		final String methodName = LogoutHelper.GET_DURATION;
		LOGGER.info(LogoutHelper.METHOD_OPEN
				+ stringBuilder.delete(0, stringBuilder.length()).append(CLASSNAME).append(methodName).toString());
		String duration = "";
		try {
			if (null != syslogHours && !syslogHours.isEmpty()) {
				LOGGER.info(LogoutHelper.METHOD_OPEN
						+ stringBuilder.delete(0, stringBuilder.length()).append(CLASSNAME).append(methodName)
								.append("syslogHours ").append(syslogHours).toString());
				final SimpleDateFormat formatter = new SimpleDateFormat(LogoutHelper.TIME_FORMAT);
				final long hours = Long.parseLong(syslogHours);
				// Calculate new time with the formula CurrentTime - SystLog Hours
				final Date date = new Date(System.currentTimeMillis() - LogoutHelper.TIME_3600 * LogoutHelper.TIME_1000
						* hours);
				duration = formatter.format(date);
				LOGGER.info(LogoutHelper.METHOD_OPEN
						+ stringBuilder.delete(0, stringBuilder.length()).append(CLASSNAME).append(methodName)
								.append("duration ").append(duration).toString());
			}
		} catch (Exception exp) {
			LOGGER.info(stringBuilder.delete(0, stringBuilder.length()).append(CLASSNAME).append(methodName)
					.append(LogoutHelper.EXCEPTION_HOLDER).append(exp.getMessage()).toString());
		}
		LOGGER.info(LogoutHelper.METHOD_EXIT
				+ stringBuilder.delete(0, stringBuilder.length()).append(CLASSNAME).append(methodName).toString());
		return duration;
	}
}
