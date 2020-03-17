
package lds.account.migration.constants;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * The Constants class is used to store all the Connection variables
 * 
 * @author PWC-AC
 */
public class ConstantUtils {

	/** Property of connection to OKTA */
	public static final String APPLICATION_JSON = "application/json";
	public static final String CACHE_CONTROL = "no-cache";
	public static final String USERS = "/users";
	public static final String USER = "/users/{oktaId}";
	public static final String ENROLL_FACTOR = "/users/{oktaId}/factors";
	public static final String ASSIGN_APP = "/apps/{appId}/users";
	public static final String ASSIGN = "assign";
	public static final String UPDATE = "update";
	public static final String FACTOR = "factor";
	public static final String DELETE = "delete";
	public static final String SUSPEND_CALL = "suspend";
	public static final String CREATE = "create";
	public static final String ADD = "add";
	public static final String GROUPS = "/groups";
	public static final String GROUP = "/groups/{groupId}";
	public static final String SUSPEND = "/users/{oktaId}/lifecycle/suspend";
	public static final String UNSUSPEND = "/users/{oktaId}/lifecycle/unsuspend";

	// Okta limits field lengths, these values are used to determine the length to truncate
	public static final int FIRSTNAME_MAX_LENGTH = 50;
	public static final int LASTNAME_MAX_LENGTH = 50;
	public static final String SEND_EMAIL = "sendEmail";
	public static final String OKTA_ID = "oktaId";
	public static final String LOGIN = "login";
	public static final String NAME = "name";
	public static final String ID = "id";
	public static final String MOBILE_PHONE = "mobilePhone";
	public static final String ERROR_SUMMARY = "errorSummary";
	public static final String STATUS = "status";

}
