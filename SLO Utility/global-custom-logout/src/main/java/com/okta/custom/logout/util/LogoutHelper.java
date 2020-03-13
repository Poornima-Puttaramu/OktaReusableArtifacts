/*
 * Comcast Copyright Details
 * 
 */

package com.okta.custom.logout.util;

/**
 * The Class LogoutConstants.
 */
public final class LogoutHelper {

	/** The Constant LOGGER_NAME. */
	public static final String LOGGER_NAME = "CUSTOM_LOGOUT";

	/** The Constant STATUS_CODE_SUCCESS. */
	public static final int STATUS_CODE_SUCCESS = 200;

	/** The Constant TIME_3600. */
	public static final int TIME_3600 = 3600;

	/** The Constant TIME_1000. */
	public static final int TIME_1000 = 1000;

	/** The Constant DO_POST. */
	public static final String DO_POST = "/doPost";

	/** The Constant DO_GET. */
	public static final String DO_GET = "/doGet";

	/** The Constant INIT_STARTUP. */
	public static final String INIT_STARTUP = "/initialStartup";

	/** The Constant TARGET_URLS. */
	public static final String TARGET_URLS = "/getTargetURLs";

	/** The Constant GET_TARGETS. */
	public static final String GET_TARGETS = "/getTargets";

	/** The Constant JSON_FROM_OKTA. */
	public static final String JSON_FROM_OKTA = "/getJSONFromOkta";

	/** The Constant JSON_FROM_OKTA. */
	public static final String GET_DURATION = "/getDuration";

	/** The Constant GET_SECRET. */
	public static final String GET_SECRET = "/getSecret";

	/** The Constant METHOD_ENTERING. */
	public static final String METHOD_OPEN = "Entering ";

	/** The Constant METHOD_EXITING. */
	public static final String METHOD_EXIT = "Exiting ";

	/** The Constant EXCEPTION_HOLDER. */
	public static final String EXCEPTION_HOLDER = " e = ";

	/** The Constant LOGOUT_PAGE. */
	public static final String LOGOUT_PAGE = "logout";

	/** The Constant EQUALS_TO. */
	public static final String EQUALS_TO = "=";

	/** The Constant TARGETS. */
	public static final String TARGETS = "targets";

	/** The Constant USER_PAGE. */
	public static final String USER_PAGE = "logoutsuccess";

	/** The Constant TARGET. */
	public static final String TARGET = "target";

	/** The Constant TYPE. */
	public static final String TYPE = "type";

	/** The Constant APP_INSTANCE. */
	public static final String APP_INSTANCE = "AppInstance";

	/** The Constant DISPLAY_NAME. */
	public static final String DISPLAY_NAME = "displayName";

	/** The Constant PIPE. */
	public static final String PIPE = "|";

	/** The Constant TIME_FORMAT. */
	public static final String TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

	/** The Constant PROP_FILE. */
	public static final String PROP_FILE = "SingleLogout.properties";

	/** The Constant OKTA_API. */
	public static final String OKTA_API = "OKTA_API";

	/** The Constant EVENT_TYPE. */
	public static final String EVENT_TYPE = "eventType";

	/** The Constant OUTCOME_RESULT. */
	public static final String OUTCOME_RESULT = "outcome.result";

	/** The Constant AUTHORIZATION. */
	public static final String AUTHORIZATION = "Authorization";

	/** The Constant CSRF_TOKEN. */
	public static final String CSRF_TOKEN = "csrfToken";

	/** The Constant TOKEN. */
	public static final String TOKEN = "token";

	/** The Constant USERID. */
	public static final String USERID = "userid";

	/** The Constant CIMA_URL. */
	public static final String CIMA_LOGOUT_URL = "CIMA_LOGOUT_URL";

	/** The Constant FILTER. */
	public static final String FILTER = "filter";

	/** The Constant SINCE. */
	public static final String SINCE = "since";

	/** The Constant ACTOR_ID_FILTER. */
	public static final String ACTOR_ID_FILTER = "actor.id eq ";

	/** The Constant SYSLOG_HOURS. */
	public static final String SYSLOG_HOURS = "Syslog_API_Hours";

	/** The Constant TOKEN_KEY. */
	public static final String TOKEN_KEY = "TOKEN_KEY";

	/** The Constant OKTA_BASE_URL. */
	public static final String OKTA_BASE_URL = "OKTA_BASE_URL";

	/** The Constant OIDC_CLIENT_ID. */
	public static final String OIDC_CLIENT_ID = "OIDC_CLIENT_ID";

	/** The Constant ISSUER. */
	public static final String ISSUER = "ISSUER";

	/** The Constant OKTA_DETAILS. */
	public static final String OKTA_DETAILS = "OktaDetails";

	/** The Constant APP_DETAILS. */
	public static final String APP_DETAILS = "Application_Details";

	/** The Constant LOG4J_PROP. */
	public static final String LOG4J_PROP = "/WEB-INF/log4j.properties";

	/** The Constant APPLICATION_NAME. */
	public static final String APPLICATION_NAME = "Application_Name";

	/** The Constant APPLICATION_LOGOUT_URL. */
	public static final String APPLICATION_LOGOUT_URL = "Application_Logout_URL";

	/**
	 * Default constructor
	 */
	private LogoutHelper() {
		super();
	}
}
