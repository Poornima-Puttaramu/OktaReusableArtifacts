
package com.okta.scim.server.ldap.connector;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/***
 * ScimServiceHelper class holding constants Code is under review for Iteration-6
 * 
 * @author PWC-AC
 */
public class SCIMServiceHelper {

	/*** Constant to specify connector properties file */
	public static final String CONF_FILENAME = "connector.properties";
	/*** Constant to specify Path to the connector properties file */
	public static final String CONF_FILEPATH = "/opt/connector";
	/*** Constant to specify Path to the log4j properties file */
	public static final String LOG4J_PROPERTIES_LOCATION = "/opt/connector/log4j.properties";
	/** Constant to specify Default email */
	public static final String DEFAULT_EMAIL = "{default_email_address}";
	/** Constant to specify Default preferredLanguage */
	public static final String DEFAULT_LANGUAGE = "en";
	/** Constant to specify Default Country */
	public static final String DEFAULT_COUNTRY = "USA";
	/** Constant to identify mobile number */
	public static final String MOBILE_PHONE = "mobilePhone";
	/** Constant to identify smsCountry */
	public static final String SMS_COUNTRY = "smsCountry";
	/** Constant to specify schema name */
	public static final String UD_SCHEMA_NAME = "custom";
	/** Constant to specify User Resource field */
	public static final String USER_RESOURCE = "user";
	/** Constant to specify Group resource field */
	public static final String GROUP_RESOURCE = "group";
	/** Constant to specify ID of an entity */
	public static final String ID = "id";
	/** Constant to specify Object Class */
	public static final String OBJECT_CLASS = "objectClass";
	/** Constant to specify Comma literal */
	public static final String COMMA = ",";
	/** Constant to specify Members */
	public static final String MEMBERS2 = "members";
	/** Constant to specify literal Secondary */
	public static final String SECONDARY = "secondary";
	/** Constant to specify literal Primary */
	public static final String PRIMARY = "primary";
	/** Constant to specify literal PhoneNumbers */
	public static final String PH_NUMBERS = "phoneNumbers";
	/** Constant to specify literal givenName */
	public static final String GIVEN_NAME = "givenName";
	/** Constant to specify literal familyName */
	public static final String FAMILY_NAME = "familyName";
	/** Constant to specify literal pwd */
	public static final String PSWD = "password";
	/** Constant to specify literal userName */
	public static final String USER_NAME = "userName";
	/** Constant to specify literal secondary email */
	public static final String SECONDARY_EMAIL = "secondaryEmail";
	/** Constant to specify literal primary email */
	public static final String PRIMARY_EMAIL = "primaryEmail";
	/** Constant to specify literal email */
	public static final String EMAIL = "email";
	/** Constant to identify literal name */
	public static final String NAME = "name";
	/** Constant to identify literal countryCode */
	public static final String COUNTRY_CODE = "countryCode";
	/** Constant to identify literal countryCode */
	public static final String LOCALE = "locale";
	/** Constant to identify literal SMSUnformatted */
	public static final String SMS_UNFORMATTED = "SMSUnformatted";
	/** Constant to identify literal AccountID */
	public static final String ACCOUNT_ID = "accountID";
	/** Constant to identify literal displayName */
	public static final String DISPLAY_NAME = "displayName";

	/**
	 * These attributes are used in update to fetch the Attrs from LDAP Starting
	 */
	/** Constant to identify literal EmailAddress */
	public static final String EMAIL_ADDRESS = "emailAddress";
	/** Constant to identify literal workforceID */
	public static final String WORKFORCE_ID = "workforceID";
	/** Constant to identify literal preferredlanguage */
	public static final String PREFERREDLANGUAGE = "preferredlanguage";
	/** Constant to identify literal accountid */
	public static final String ACCOUNTID = "accountid";
	/** Constant to identify literal preferredName */
	public static final String PREFERRED_NAME = "preferredName";
	/** Constant to identify literal sn */
	public static final String SN = "sn";
	/** Constant to identify literal givenname */
	public static final String GIVENNAME = "givenname";
	/** Constant to identify literal cn */
	public static final String CN = "cn";
	
	/** Custom Properties defined in the Properties file */
	public static final List<String> CUSTOM_PROPERTIES = Collections.unmodifiableList(Arrays.asList(
			"OPP.userCustomMap.locale",
			"OPP.userCustomMap.preferredName"));

	/** Core Properties defined in the Properties file */
	public static final List<String> CORE_PROPERTIES = Collections.unmodifiableList(Arrays.asList(
			"OPP.userCoreMap.login", "OPP.userCoreMap.familyName", "OPP.userCoreMap.givenName",
			"OPP.userCoreMap.formattedName", "OPP.userCoreMap.id", "OPP.userCoreMap.password",
			"OPP.userCoreMap.primaryEmail", "OPP.userCoreMap.mobilePhone"));

	/** Group Properties defined in the Properties file */
	public static final List<String> GROUP_PROPERTIES = Collections.unmodifiableList(Arrays.asList(
			"OPP.groupCoreMap.id", "OPP.groupCoreMap.member"));

	/** Constants for decrypt */
	public static final String HASH_ALGO = "PBKDF2WithHmacSHA1";
	public static final String ENCRYPTION_ALGO = "AES";
	public static final String PADDING_SCHEMES = "AES/CBC/PKCS5Padding";
	public static final String ENCODING = "UTF-8";
	/** Constants for decrypt finished */

	/** Constants for SECURE_RANDOM_INSTANCE */
	public static final String SECURE_RANDOM_INSTANCE = "SHA1PRNG";
}
