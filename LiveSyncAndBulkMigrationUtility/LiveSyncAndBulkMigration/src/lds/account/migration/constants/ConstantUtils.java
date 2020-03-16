
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


	/** Constant set of OKTA supported languages in 3 characters */
	public static final List<String> SUPPORTED_LANGUAGES = Collections.unmodifiableList(Arrays.asList("ces", "cze",
			"dan", "ger", "deu", "grc", "eng", "spa", "fin", "fre", "fra", "hun", "ind", "ita", "jpn", "kor", "may",
			"msa", "nob","dut","nld","dum","pol","por","rum","ron","rus","swe","tha","tur","ukr","vie","chi",
			"cs", "en", "da", "de", "es", "fi", "fr", "hu", "in", "it", "ja", "ko", "ms", "nb", "nl", "pl",
			"pt", "ro", "ru", "sv", "th", "tr", "uk", "vi"));
	// Default 3 letter language
	public static final String DEFAULT_2_LETTER_LANG = "en";
	public static final String DEFAULT_3_LETTER_LANG = "eng";

	// Default SMS
	public static final String DEFAULT_SMS_COUNTRY = "USA";
	public static final String DEFAULT_COUNTRY_CODE = "US";

	// Default email if primary email is null
	public static final String DEFAULT_EMAIL_IF_NULL = "no-reply@churchofjesuschrist.org";

	// Okta limits field lengths, these values are used to determine the length to truncate
	public static final int FIRSTNAME_MAX_LENGTH = 50;
	public static final int LASTNAME_MAX_LENGTH = 50;
	public static final String SEND_EMAIL = "sendEmail";
	public static final String CHURCH_ACCOUNT_ID = "churchAccountID";
	public static final String OKTA_ID = "oktaId";
	public static final String LOGIN = "login";
	public static final String NAME = "name";
	public static final String ID = "id";
	public static final String MOBILE_PHONE = "mobilePhone";
	public static final String ERROR_SUMMARY = "errorSummary";
	public static final String STATUS = "status";

}
