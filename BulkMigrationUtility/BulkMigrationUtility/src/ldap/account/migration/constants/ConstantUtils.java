
package ldap.account.migration.constants;

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
	public static final String ENROLL_FACTOR = "/users/{oktaId}/factors";
	
	public static final String CREATE = "create";
	public static final String ADD = "add";	
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
