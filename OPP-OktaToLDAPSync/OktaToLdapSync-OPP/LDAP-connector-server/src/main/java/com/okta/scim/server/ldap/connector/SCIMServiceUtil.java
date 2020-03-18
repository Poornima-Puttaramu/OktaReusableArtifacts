
package com.okta.scim.server.ldap.connector;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.spec.KeySpec;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.UUID;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;
import javax.xml.bind.DatatypeConverter;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.ldap.core.DistinguishedName;
import org.springframework.ldap.core.LdapRdn;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.okta.scim.server.exception.OnPremUserManagementException;
import com.okta.scim.util.exception.InvalidDataTypeException;
import com.okta.scim.util.model.Email;
import com.okta.scim.util.model.Membership;
import com.okta.scim.util.model.Name;
import com.okta.scim.util.model.PhoneNumber;
import com.okta.scim.util.model.SCIMFilter;
import com.okta.scim.util.model.SCIMFilterAttribute;
import com.okta.scim.util.model.SCIMFilterType;
import com.okta.scim.util.model.SCIMGroup;
import com.okta.scim.util.model.SCIMUser;

/**
 * Utilities Class for SCIM Connector Implementation Code is under review for Iteration-6
 * 
 * @author @author AC PwC
 */
@SuppressWarnings("deprecation")
public class SCIMServiceUtil {
	private static final Logger LOGGER = Logger.getLogger(SCIMServiceUtil.class.getName());

	private boolean useFilePersistence = true;
	private String userCustomUrn;
	private Map<String, String> ldapUserCore = new HashMap<String, String>();
	private Map<String, String[]> ldapUserCustom = new HashMap<String, String[]>();
	private Map<String, String> ldapGroupCore = new HashMap<String, String>();
	private Hashtable<String, String> env = new Hashtable<>();
	private String ldapUserDn;
	private String ldapBaseDn;
	private String ldapUserPre;
	private boolean useEntireUsername;
	private String[] ldapGroupClass;
	private String[] ldapUserClass;
	private int nextGroupId;
	private int ldapRetryParam;
	private int ldapSleepTime;

	/**
	 * This method is used to get the initialized values from initLdapVars method in ServiceImpl.java
	 * 
	 * @param ldapUserCore
	 *            - Map
	 * @param ldapGroupCore
	 *            -Map
	 * @param ldapUserCustom
	 *            -Map
	 * @param userCustomUrn
	 *            - String
	 * @param env
	 *            -String
	 * @param useEntireUsername
	 *            -Boolean
	 * @param ldapGroupClass
	 *            -String[]
	 * @param ldapUserClass
	 *            -String []
	 * @param ldapUserDn
	 *            -String
	 * @param ldapBaseDn
	 *            - String
	 * @param ldapUserPre
	 *            - String
	 * @param nextGroupId
	 *            - int
	 * @param ldapRetryParam
	 *            - int
	 * @param ldapSleepTime
	 *            - int
	 */
	public void setVar(Map<String, String> ldapUserCore, Map<String, String> ldapGroupCore,
			Map<String, String[]> ldapUserCustom, String userCustomUrn, Hashtable<String, String> env, boolean useEntireUsername,
			String[] ldapGroupClass, String[] ldapUserClass, String ldapUserDn, String ldapBaseDn, String ldapUserPre,
			int nextGroupId, int ldapRetryParam, int ldapSleepTime) {
		final String methodName = "setVar";
		LOGGER.info(methodName + " Entering");
		this.userCustomUrn = userCustomUrn;
		this.ldapUserCore = ldapUserCore;
		this.ldapGroupCore = ldapGroupCore;
		this.ldapUserCustom = ldapUserCustom;
		this.env = env;
		this.ldapUserDn = ldapUserDn;
		this.ldapGroupClass = ldapGroupClass;
		this.ldapUserClass = ldapUserClass;
		this.useEntireUsername = useEntireUsername;
		this.ldapBaseDn = ldapBaseDn;
		this.ldapUserPre = ldapUserPre;
		this.nextGroupId = nextGroupId;
		this.ldapRetryParam = ldapRetryParam;
		this.ldapSleepTime = ldapSleepTime;
		LOGGER.info(methodName + " Exiting");
	}

	/**
	 * Generate the next if for a resource
	 * 
	 * @param resourceType
	 * @return
	 */
	protected String generateNextId(final String resourceType) {
		final String methodName = "generateNextId";
		LOGGER.info(methodName + " Entering");
		String nextID = "";
		if (useFilePersistence) {
			nextID = UUID.randomUUID().toString();
			LOGGER.debug("nextID for group is " + nextID);
		} else if (resourceType.equals(SCIMServiceHelper.GROUP_RESOURCE)) {
			nextID = Integer.toString(nextGroupId++);
			LOGGER.debug("nextID for group is " + nextID);
		}
		LOGGER.info(methodName + " Exiting");
		return nextID;
	}

	/**
	 * This method is used to get the values from Attributes
	 * 
	 * @param map
	 * @param lookup
	 * @param attrs
	 * @return
	 * @throws NamingException
	 */
	private String getValueFromAttrs(final String map, final String lookup, Attributes attrs) throws NamingException {
		final String methodName = "getValueFromAttrs";
		LOGGER.info(methodName + " Entering");
		String value = "";
		if (null != lookup && null != attrs) {
			Attribute attr = attrs.get(lookup);
			if (null != attr) {
				Object val = attr.get();
				if (null != val) {
					value = val.toString();
				}
			}
		} else {
			LOGGER.warn("[" + methodName + "] Connector.properties did not have a " + map + " entry for userCoreMap.");
		}
		LOGGER.info(methodName + " Exiting");
		return value;
	}

	/**
	 * Helper function to get appropriate LDAP dn name as per the configs
	 * 
	 * @param name
	 *            - the name to process
	 * @return returns the appropriate dn to use for LDAP
	 */
	protected String getUserDnName(final String name) {
		final String methodName = "getUserDnName";
		LOGGER.info(methodName + " Entering");
		String retName = "";
		if (useEntireUsername) {
			retName = name;
		} else {
			retName = name.split("@")[0];
		}
		LOGGER.info(methodName + " Exiting");
		return retName;
	}

	/**
	 * This method returns a list of users which match the filter criteria.
	 * 
	 * @param filter
	 *            the SCIM filter
	 * @return list of users that match the filter
	 */
	protected List<SCIMUser> getUserByFilter(final SCIMFilter filter) throws NamingException, Exception {
		final String methodName = "getUserByFilter";
		LOGGER.info(methodName + " Entering");
		List<SCIMUser> users = new ArrayList<SCIMUser>();
		SCIMFilterType filterType = filter.getFilterType();
		if (filterType.equals(SCIMFilterType.EQUALS)) {
			LOGGER.debug("Equality Filter");
			users = getUsersByEqualityFilter(filter);
		} else if (filterType.equals(SCIMFilterType.OR)) {
			LOGGER.debug("OR Filter");
			users = getUsersByOrFilter(filter);
		} else {
			LOGGER.error("The Filter " + filter + " contains a condition that is not supported");
		}
		LOGGER.info(methodName + " Exiting");
		return users;
	}

	/**
	 * This is an example for how to deal with an OR filter. An OR filter consists of multiple sub equality filters.
	 * 
	 * @param filter
	 *            the OR filter with a set of sub filters expressions
	 * @return list of users that match any of the filters
	 */
	private List<SCIMUser> getUsersByOrFilter(final SCIMFilter filter) throws NamingException, Exception {
		final String methodName = "getUsersByOrFilter";
		LOGGER.info(methodName + " Entering");
		/**
		 * An OR filter would contain a list of filter expression. Each expression is a SCIMFilter by itself.
		 */
		List<SCIMFilter> subFilters = filter.getFilterExpressions();
		LOGGER.info("[" + methodName + "] Searching on OR Filter : " + subFilters);
		List<SCIMUser> users = new ArrayList<SCIMUser>();
		LdapContext ctx = getLdapContext();
		if (ctx == null) {
			throw new NamingException("Ldap context could not be fetched");
		}
		NamingEnumeration<?> namingEnum = null;
		try {
			String dn = ldapUserDn + ldapBaseDn;
			ctx.setRequestControls(null);
			SearchControls controls = new SearchControls();
			controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
			String ldapFilter = "";
			String primaryEmailLookup = ldapUserCore.get(SCIMServiceHelper.PRIMARY_EMAIL);
			String secondaryEmailLookup = ldapUserCore.get(SCIMServiceHelper.SECONDARY_EMAIL);
			/** Loop through the sub filters to evaluate each of them. */
			for (SCIMFilter subFilter : subFilters) {
				/** Name of the sub filter (email) */
				String fieldName = subFilter.getFilterAttribute().getAttributeName();
				/** Value (abc@def.com) */
				String value = subFilter.getFilterValue();
				/** For all the users, check if any of them have this email */
				if (SCIMServiceHelper.EMAIL.equalsIgnoreCase(fieldName)) {
					ldapFilter = "(|(" + primaryEmailLookup + "=" + value + ")(" + secondaryEmailLookup + "=" + value
							+ "))";
					namingEnum = ctx.search(dn, ldapFilter, controls);
					while (namingEnum.hasMore()) {
						SearchResult result = (SearchResult) namingEnum.next();
						Attributes attrs = result.getAttributes();
						SCIMUser user = constructUserFromAttrs(attrs);
						users.add(user);
					}
				}
			}
		} catch (NamingException ex) {
			LOGGER.error("Exception while fetching users, " + ex.getMessage());
			throw ex;
		} catch (Exception ex) {
			LOGGER.error("Exception while fetching users, " + ex.getMessage());
			throw ex;
		} finally {
			if (null != ctx) {
				ctx.close();
			}
			if (null != namingEnum) {
				namingEnum.close();
			}
		}
		LOGGER.info("[" + methodName + "] Users found: " + users.size());
		LOGGER.info(methodName + " Exiting");
		return users;
	}

	/**
	 * This is an example of how to deal with an equality filter.
	 * <p>
	 * If you choose a custom field/complex field (name.familyName) or any other singular field (userName/externalId),
	 * you should get an equality filter here.
	 * 
	 * @param filter
	 *            the EQUALS filter
	 * @return list of users that match the filter
	 */
	private List<SCIMUser> getUsersByEqualityFilter(final SCIMFilter filter) throws NamingException, Exception {
		final String methodName = "getUsersByEqualityFilter";
		LOGGER.info(methodName + " Entering");
		String fieldName = filter.getFilterAttribute().getAttributeName();
		String value = filter.getFilterValue();
		LdapContext ctx = getLdapContext();
		if (ctx == null) {
			throw new NamingException("Ldap context could not be fetched");
		}
		List<SCIMUser> users = new ArrayList<SCIMUser>();
		try {
			String dn = ldapUserDn + ldapBaseDn;
			LOGGER.info("[getUsersByEqualityFilter] setting dn:: : " + dn);
			ctx.setRequestControls(null);
			SearchControls controls = new SearchControls();
			controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
			String ldapFilter = "";
			LOGGER.info("Equality Filter : Field Name [ " + fieldName + " ]. Value [ " + value + " ]");

			if (SCIMServiceHelper.USER_NAME.equalsIgnoreCase(fieldName)) {
				LOGGER.debug("Username");
				String usernameLookup = ldapUserCore.get(SCIMServiceHelper.USER_NAME);
				if (usernameLookup != null) {
					ldapFilter = "(" + usernameLookup + "=" + value + ")";
				} else {
					LOGGER.warn("[" + methodName
							+ "] Connector.properties did not have a userName entry for userCoreMap.");
				}
			} else if (SCIMServiceHelper.ID.equalsIgnoreCase(fieldName)) {
				LOGGER.debug("ID");
				String idLookup = ldapUserCore.get(SCIMServiceHelper.ID);
				if (idLookup != null) {
					ldapFilter = "(" + idLookup + "=" + value + ")";
				} else {
					LOGGER.warn("[" + methodName + "] Connector.properties did not have a id entry for userCoreMap.");
				}
			} else if (SCIMServiceHelper.NAME.equalsIgnoreCase(fieldName)) {
				LOGGER.debug("name");
				String subFieldName = filter.getFilterAttribute().getSubAttributeName();
				if (subFieldName != null) {
					if (SCIMServiceHelper.FAMILY_NAME.equalsIgnoreCase(subFieldName)) {
						String familyNameLookup = ldapUserCore.get(SCIMServiceHelper.FAMILY_NAME);
						if (familyNameLookup != null) {
							ldapFilter = "(" + familyNameLookup + "=" + value + ")";
						} else {
							LOGGER.warn("[" + methodName
									+ "] Connector.properties did not have a familyName entry for userCoreMap.");
						}
					} else if (SCIMServiceHelper.GIVEN_NAME.equalsIgnoreCase(subFieldName)) {
						String givenNameLookup = ldapUserCore.get(SCIMServiceHelper.GIVEN_NAME);
						if (givenNameLookup != null) {
							ldapFilter = "(" + givenNameLookup + "=" + value + ")";
						} else {
							LOGGER.warn("[" + methodName
									+ "] Connector.properties did not have a givenName entry for userCoreMap.");
						}
					}
				}
			} else if (filter.getFilterAttribute().getSchema().equalsIgnoreCase(userCustomUrn)) {
				/**
				 * Check that the Schema name is the Custom Schema name to /* process the filter for custom fields
				 */
				LOGGER.debug("Custom");
				String[] keys = ldapUserCustom.keySet().toArray(new String[ldapUserCustom.size()]);
				String[] configLine;
				/**
				 * "urn:okta:onprem_app:1.0:user:custom:departmentName eq " someValue""
				 */
				/** Get the custom properties map (SchemaName -> JsonNode) */
				for (int i = 0; i < keys.length; i++) {
					configLine = (String[]) ldapUserCustom.get(keys[i]);
					if (configLine[2].equalsIgnoreCase(fieldName)) {
						ldapFilter = "(" + keys[i] + "=" + value + ")";
						break;
					}
				}
			}
			if (!ldapFilter.isEmpty()) {
				ArrayList<Attributes> queryResults = queryLDAP(dn, ldapFilter);
				for (int i = 0; i < queryResults.size(); i++) {
					SCIMUser user = constructUserFromAttrs(queryResults.get(i));
					users.add(user);
				}
			}
		} catch (NamingException ex) {
			LOGGER.error("Exception while fetching users, " + ex.getMessage());
			throw ex;
		} catch (Exception ex) {
			LOGGER.error("Exception while fetching users, " + ex.getMessage());
			throw ex;
		} finally {
			if (null != ctx) {
				ctx.close();
			}
		}
		LOGGER.info(methodName + " Exiting");
		return users;
	}

	/**
	 * Method is used to initialize the LDAP connection context.
	 * 
	 * @return Returns the LdapContext object
	 * @throws NamingException
	 *             NamingException when the connection establishment fails
	 */
	protected LdapContext getLdapContext() throws NamingException {
		final String methodName = "getLdapContext";
		LOGGER.info(methodName + " Entering");
		LdapContext ctx = null;
		int retry = 1;
		while (retry < ldapRetryParam) {
			try {
				ctx = new InitialLdapContext(env, null);
				break;
			} catch (NamingException e) {
				LOGGER.error("Exception while initializing connection with LDAP : " + e.getMessage());
				try {
					Thread.sleep(ldapSleepTime);
				} catch (InterruptedException ex) {
					LOGGER.error("Exception while waiting " + ex.getMessage());
				}
				if (retry < ldapRetryParam) {
					retry++;
					continue;
				} else {
					throw new NamingException("LDAP connection tried for max attempt of " + ldapRetryParam + " times");
				}
			}
		}
		LOGGER.info(methodName + " Exiting");
		return ctx;
	}

	/**
	 * Constructs Attributes from a SCIMUser object. Only deals with base attributes, calls constructCustomAttrsFromUser
	 * to add custom values to Attributes.
	 * 
	 * @param user
	 *            - SCIMUser object to pull values from
	 * @param update
	 *            - is this getting called by update or create
	 * @return fully built Attributes Object
	 * @throws InvalidDataTypeException
	 * @throws NamingException
	 */
	protected Attributes constructAttrsFromUser(final SCIMUser user, final boolean update, final boolean handleBlanks)
			throws InvalidDataTypeException, NamingException, Exception {
		final String methodName = "constructAttrsFromUser";
		LOGGER.info(methodName + " Entering");
		String[] keys = ldapUserCore.keySet().toArray(new String[ldapUserCore.size()]);
		Attributes attrs = new BasicAttributes(true);
		Attribute objclass = new BasicAttribute("objectClass");
		Object value = null;
		Attribute attr;
		for (int i = 0; i < ldapUserClass.length; i++) {
			LOGGER.debug("Constructing LDAP Attribute class Object");
			objclass.add(ldapUserClass[i]);
		}
		LOGGER.debug("Constructing Attribute");
		for (int i = 0; i < keys.length; i++) {
			String attrType = ldapUserCore.get(keys[i]);
			attr = new BasicAttribute(attrType);

			if (keys[i].equals(SCIMServiceHelper.USER_NAME)) {
				value = user.getUserName();

			} else if (keys[i].equals(SCIMServiceHelper.FAMILY_NAME)) {
				value = user.getName().getLastName();

				if (value == null) {
					continue;
				}
			} else if (keys[i].equals(SCIMServiceHelper.GIVEN_NAME)) {
				value = user.getName().getFirstName();

				/** Not a mandatory attr for Ldap */
				if (value == null) {
					continue;
				}
			} else if (keys[i].equals("formatted")) {
				value = user.getName().getFormattedName();

				/** Not a mandatory attr for Ldap */
				if (value == null) {
					continue;
				}
			} else if (keys[i].equals(SCIMServiceHelper.ID)) {
				value = user.getId();
			}
			/**
			 * if we're doing an update, we always want the attr in place, either it will have a value or if it doesn't
			 * then having a no value attr will remove it from the ldap entry
			 */
			else if (keys[i].equals(SCIMServiceHelper.MOBILE_PHONE) && (user.getPhoneNumbers() != null || update)) {
				value = user.getPhoneNumbers();
				if (value == null) {
					continue;
				}
			} else if (keys[i].equals("emails") && (user.getEmails() != null || update)) {
				attrs.put(attr);
				continue;
			} else if (keys[i].equals(SCIMServiceHelper.PSWD) && (user.getPassword() != null)) {
				attrs.put(attr);
				continue;
			} else {
				LOGGER.debug("The Key - Value doesnt seem to be in scope " + keys[i].toString()
						+ " or its value is null/empty");
				continue;
			}
			if (null == value) {
				LOGGER.debug("value is empty");
			} else {
				attr.add(value.toString());
			}
			attrs.put(attr);
		}
		/** Special cases for attributes that are not simple values */
		if (user.getPassword() != null && ldapUserCore.get(SCIMServiceHelper.PSWD) != null) {
			Attribute passwd = attrs.get(ldapUserCore.get(SCIMServiceHelper.PSWD));
			passwd.add(user.getPassword());
			attrs.put(passwd);
		}
		user.setPassword("");
		
		if (user.getEmails() != null && ldapUserCore.get(SCIMServiceHelper.PRIMARY_EMAIL) != null) {
			Attribute primaryEmailAttr = new BasicAttribute(ldapUserCore.get(SCIMServiceHelper.PRIMARY_EMAIL));
			Attribute secondaryEmailAttr = new BasicAttribute(ldapUserCore.get(SCIMServiceHelper.SECONDARY_EMAIL));
			Object[] emails = user.getEmails().toArray();
			for (int i = 0; i < emails.length; i++) {
				Email email = (Email) emails[i];
				if (email != null && email.isPrimary()) {
					if (email.getValue() != null && email.getValue().equals(SCIMServiceHelper.DEFAULT_EMAIL)) {
						LOGGER.debug("User is having a default Email address,hence not storing the Email value in IDV");
					} else {
						primaryEmailAttr.add(email.getValue());
						attrs.put(primaryEmailAttr);
					}
				} else if (!email.isPrimary() && ldapUserCore.get(SCIMServiceHelper.SECONDARY_EMAIL) != null) {
					secondaryEmailAttr.add(email.getValue());
					attrs.put(secondaryEmailAttr);
				}
			}
		}
		attrs.put(objclass);
		LOGGER.info(methodName + " Exiting");
		return constructCustomAttrsFromUser(user, attrs, update, handleBlanks);
	}

	/**
	 * This method is used to convert 2 digit country code to 3 digit Country code
	 * 
	 * @param country
	 * @return
	 */
	protected String convert2to3DigitCountryCode(String country) throws MissingResourceException, Exception {
		String methodName = "convert2to3DigitCountryCode";
		LOGGER.info(methodName + " Entering");
		String iso3Country = "";
		if (country != null && country.length() == 3) {
			iso3Country = country;
		} else if (country != null && country.length() == 2) {
			try {
				Locale locale = new Locale("en", country);
				iso3Country = locale.getISO3Country().toUpperCase();

			} catch (MissingResourceException e) {
				LOGGER.error("MissingResource Exception occurred in converting 2 to 3 digit country code");
			} catch (Exception e) {
				LOGGER.error("Exception occurred in converting 2 to 3 digit country code");
			}
		}
		if (country == null) {
			iso3Country = SCIMServiceHelper.DEFAULT_COUNTRY;
			LOGGER.debug("The country code for IDV will be default as none recieved from OKTA.");
		}
		if (StringUtils.isBlank(iso3Country)) {
			iso3Country = SCIMServiceHelper.DEFAULT_COUNTRY;
			LOGGER.debug("The country code for IDV will be default as recieved blank value from OKTA.");
		}
		LOGGER.debug("The country for IDV will be - " + iso3Country);
		LOGGER.info(methodName + " Exiting ");
		return iso3Country;
	}

	/**
	 * This method is used to split the mobilePhone to smsCountry and National Number code
	 * 
	 * @param country
	 *            -String
	 * @return
	 * @throws NumberParseException
	 */
	protected Map<String, String> splitPhoneNumber(String phone) throws NumberParseException {
		String methodName = "splitPhoneNumber";
		LOGGER.info(methodName + " Entering");
		PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
		Map<String, String> mobilePhoneMap = new HashMap<String, String>();
		if (phone != null && phone.startsWith("+")) {
			try {
				/** phone must begin with '+' */
				com.google.i18n.phonenumbers.Phonenumber.PhoneNumber numberProto = phoneUtil.parse(phone, "");
				String nationalNumber = Long.toString(numberProto.getNationalNumber());
				String smsCountryName = phoneUtil.getRegionCodeForNumber(numberProto);
				mobilePhoneMap.put(SCIMServiceHelper.MOBILE_PHONE, nationalNumber);
				mobilePhoneMap.put(SCIMServiceHelper.SMS_COUNTRY, smsCountryName);

			} catch (NumberParseException e) {
				LOGGER.error("NumberParseException was thrown: " + e.toString());
			}
		} else {
			LOGGER.error("MobilePhone provided in OKTA is not in proper format.It should startsWith '+' ");
		}
		LOGGER.info(methodName + " Exiting");
		return mobilePhoneMap;
	}

	/**
	 * This method is used to format the DateTime field to the IDV supported DateTime format
	 * 
	 * @param time
	 * @return
	 * @throws ParseException
	 *             , Exception
	 */
	protected String formatDateTime(String time) throws ParseException, Exception {
		String inputPattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";//input pattern from Okta
		String outputPattern = "yyyyMMddHHmmss'Z'";//output pattern in IDV
		Date inputDate = null;
		String outputDate = "";
		try {
			SimpleDateFormat inputFormatter = new SimpleDateFormat(inputPattern);
			SimpleDateFormat outputFormatter = new SimpleDateFormat(outputPattern);
			inputDate = inputFormatter.parse(time);
			outputDate = outputFormatter.format(inputDate);
		} catch (ParseException e) {
			LOGGER.warn("ParseException occured in formatting the DateTime field");
		} catch (Exception e) {
			LOGGER.warn("Exception occured in formatting the DateTime field");
		}
		return outputDate;
	}

	/**
	 * Adds Attribute objs to supplied attrs made from SCIMUser object. Uses mappings for custom attributes from
	 * properties file.
	 * 
	 * @param user
	 *            - SCIMUser object to pull values from
	 * @param attrs
	 *            - Attributes to add to SCIMUser object
	 * @param update
	 *            -
	 * @return fully built Attributes Object
	 * @throws InvalidDataTypeException
	 * @throws NamingException
	 */
	private Attributes constructCustomAttrsFromUser(final SCIMUser user, final Attributes attrs, final boolean update,
			final boolean handleBlanks) throws InvalidDataTypeException, NamingException, MissingResourceException,
			Exception {
		final String methodName = "constructCustomAttrsFromUser";
		LOGGER.info(methodName + " Entering");
		String[] keys = ldapUserCustom.keySet().toArray(new String[ldapUserCustom.size()]);
		String[] configLine;
		String[] emptyArr = new String[0];
		String[] parentNames = emptyArr;
		Attribute customAttr;
		Object value;

		/**
		 * For each custom attribute mapping in properties, get the appropriate custom value and put it in an Attribute
		 * obj
		 */
		for (int i = 0; i < keys.length; i++) {
			configLine = ldapUserCustom.get(keys[i]);
			parentNames = emptyArr;
			if (configLine.length > 3) {
				parentNames = Arrays.copyOfRange(configLine, 3, configLine.length);
			}
			/**
			 * if an attribute is double mapped and update is true then just /* remove the existing and replace with
			 * custom attr /* this handles the Ldap id case
			 */
			if (update && attrs.get(keys[i]) != null) {
				attrs.remove(keys[i]);
			}
			if (attrs.get(keys[i]) != null) {
				customAttr = attrs.get(keys[i]);
			} else {
				customAttr = new BasicAttribute(keys[i]);
			}

			if (configLine[0].equals("int")) {
				value = user.getCustomIntValue(configLine[1], configLine[2], parentNames);
				/** custom attrs not a mandatory attr for Ldap */
				if (handleBlanks == Boolean.FALSE && value == null) {
					continue;
				}
			} else if (configLine[0].equals("boolean")) {
				value = user.getCustomBooleanValue(configLine[1], configLine[2], parentNames);
				/** custom attrs not a mandatory attr for Ldap */
				if (value == null) {
					continue;
				}
			} else if ("string".equals(configLine[0])) {
				value = user.getCustomStringValue(configLine[1], configLine[2], parentNames);
				/** custom attrs not a mandatory attr for Ldap Create */
				if (handleBlanks == Boolean.FALSE && ((value == null) || value.toString().length() == 0)) {

					continue;
				}

			} else if ("double".equals(configLine[0])) {
				value = user.getCustomDoubleValue(configLine[1], configLine[2], parentNames);
				/** custom attrs not a mandatory attr for Ldap Create */
				if (handleBlanks == Boolean.FALSE && value == null) {

					continue;
				}
			} else
				throw new OnPremUserManagementException("OPP Connector Exception",
						"Unexpected type for Custom attrs in config: " + Arrays.toString(configLine));

			if (value != null) {
				customAttr.add(value.toString());
				attrs.put(customAttr);
			} else if (value == null && handleBlanks == Boolean.TRUE) {
				customAttr.add(null);
				attrs.put(customAttr);
			} else {
				LOGGER.debug("Value is null - for create().This is unexpected.");
			}
		}
				
		if (attrs.get(SCIMServiceHelper.PREFERREDLANGUAGE) != null) {
			if (attrs.get(SCIMServiceHelper.PREFERREDLANGUAGE).get() != null) {
				String lang = ((String) attrs.get(SCIMServiceHelper.PREFERREDLANGUAGE).get());
				String language = lang.substring(0, 2);
				attrs.put(SCIMServiceHelper.PREFERREDLANGUAGE, language);
			} else {
				attrs.put(SCIMServiceHelper.PREFERREDLANGUAGE, SCIMServiceHelper.DEFAULT_LANGUAGE);
			}
		}

		LOGGER.info(methodName + " Exiting");
		return attrs;
	}

	/**
	 * Pulls values for base user attributes from Attributes obj and sets it in SCIMUser obj. Calls
	 * constructUserFromCustomAttrs to handle custom attributes. Mappings obtained from properties file.
	 * 
	 * @param attrs
	 *            - Attributes to add to SCIMUser object
	 * @return fully built SCIMUser object
	 * @throws NamingException
	 */
	public SCIMUser constructUserFromAttrs(final Attributes attrs) throws NamingException {
		final String methodName = "constructUserFromAttrs";
		LOGGER.info(methodName + " Entering");
		/**
		 * create objects, pull in values from attrs using mapping from properties file.
		 */
		SCIMUser user = new SCIMUser();
		String formattedNameLookup = ldapUserCore.get("formatted");
		String formattedName = getValueFromAttrs("formatted", formattedNameLookup, attrs);
		String snLookup = ldapUserCore.get(SCIMServiceHelper.FAMILY_NAME);
		String sn = getValueFromAttrs(SCIMServiceHelper.FAMILY_NAME, snLookup, attrs);
		String givenNameLookup = ldapUserCore.get(SCIMServiceHelper.GIVEN_NAME);
		String givenName = getValueFromAttrs(SCIMServiceHelper.GIVEN_NAME, givenNameLookup, attrs);
		Name fullName = new Name(formattedName, sn, givenName);
		user.setName(fullName);
		String idLookup = ldapUserCore.get(SCIMServiceHelper.ID);
		String id = getValueFromAttrs(SCIMServiceHelper.ID, idLookup, attrs);
		user.setId(id);
		String userNameLookup = ldapUserCore.get(SCIMServiceHelper.USER_NAME);
		String userName = getValueFromAttrs(SCIMServiceHelper.USER_NAME, userNameLookup, attrs);
		user.setUserName(userName);
		ArrayList<PhoneNumber> phoneNums = new ArrayList<PhoneNumber>();
		String phoneNumsAttrLookup = ldapUserCore.get(SCIMServiceHelper.MOBILE_PHONE);
		Attribute phoneNumsAttr = null;
		if (phoneNumsAttrLookup != null) {
			phoneNumsAttr = attrs.get(phoneNumsAttrLookup);
		} else {
			LOGGER.warn("[" + methodName + "] Connector.properties did not have phoneNumbers entry for userCoreMap.");
		}
		/** set their password to empty string */
		user.setPassword("");
		/**
		 * for each phone number, parse line from attrs and build PhoneNumber obj
		 */
		if (phoneNumsAttr != null) {
			for (int i = 0; i < phoneNumsAttr.size(); i++) {
				String phoneNum = phoneNumsAttr.get(i).toString();
				if (phoneNum != null) {
					PhoneNumber.PhoneNumberType type = PhoneNumber.PhoneNumberType.valueOf("MOBILE");
					PhoneNumber numEntry = new PhoneNumber(phoneNum, type, true);
					phoneNums.add(numEntry);
				}
			}
			user.setPhoneNumbers(phoneNums);
		}
		ArrayList<Email> emails = new ArrayList<Email>();
		String primaryEmailLookup = ldapUserCore.get(SCIMServiceHelper.PRIMARY_EMAIL);
		String primaryEmail = getValueFromAttrs(SCIMServiceHelper.PRIMARY_EMAIL, primaryEmailLookup, attrs);
		;
		Email primaryEmailEntry = new Email(primaryEmail, SCIMServiceHelper.PRIMARY, true);
		emails.add(primaryEmailEntry);
		String secondaryEmailLookup = ldapUserCore.get(SCIMServiceHelper.SECONDARY_EMAIL);
		String secondaryEmail = getValueFromAttrs(SCIMServiceHelper.SECONDARY_EMAIL, secondaryEmailLookup, attrs);
		Email secondaryEmailEntry = new Email(secondaryEmail, SCIMServiceHelper.SECONDARY, false);
		emails.add(secondaryEmailEntry);
		user.setEmails(emails);
		user.setActive(true);
		LOGGER.info(methodName + " Exiting");
		return constructUserFromCustomAttrs(user, attrs);
	}

	/**
	 * Adds custom Attributes to given SCIMUser object. Pulls mapping for custom attrs from properties file.
	 * 
	 * @param user
	 *            - SCIMUser object to add custom attributes to.
	 * @param attrs
	 *            - Attributes to add to SCIMUser object
	 * @return fully built SCIMUser object
	 * @throws NamingException
	 */
	private SCIMUser constructUserFromCustomAttrs(final SCIMUser user, final Attributes attrs) throws NamingException {
		final String methodName = "constructUserFromCustomAttrs";
		LOGGER.info(methodName + " Entering");
		String[] keys = ldapUserCustom.keySet().toArray(new String[ldapUserCustom.size()]);
		String[] configLine;
		String[] emptyArr = new String[0];
		String[] parentNames = emptyArr;
		Attribute customAttr;
		Object value = "";
		/**
		 * Iterates through all mapped custom attrs from properties file and sets value in user obj.
		 */
		for (int i = 0; i < keys.length; i++) {
			configLine = ldapUserCustom.get(keys[i]);
			parentNames = emptyArr;
			if (configLine.length > 3)
				parentNames = Arrays.copyOfRange(configLine, 3, configLine.length);
			customAttr = attrs.get(keys[i]);
			if (customAttr != null) {
				value = customAttr.get();
				if (configLine[0].equals("int"))
					user.setCustomIntValue(configLine[1], configLine[2], Integer.parseInt(value.toString()),
							parentNames);
				else if (configLine[0].equals("boolean"))
					user.setCustomBooleanValue(configLine[1], configLine[2], Boolean.valueOf(value.toString()),
							parentNames);
				else if (configLine[0].equals("string"))
					user.setCustomStringValue(configLine[1], configLine[2], (String) value, parentNames);
				else if (configLine[0].equals("double"))
					user.setCustomDoubleValue(configLine[1], configLine[2], Double.parseDouble(value.toString()),
							parentNames);
				else
					throw new OnPremUserManagementException("OPP Connector Exception",
							"Unexpected type for Custom attrs in config: " + Arrays.toString(configLine));
			} else {
				LOGGER.warn("[constructUserFromCustomAttrs] LDAP did not have value for " + keys[i] + ".");
			}
		}
		LOGGER.info(methodName + " Exiting");
		return user;
	}

	/**
	 * Builds the Attributes object to insert into LDAP, uses mappings pulled from properties file.
	 * 
	 * @param group
	 *            - SCIMGroup object to build Attributes object from.
	 * @return Attributes object that resulted from SCIMGroup object
	 */
	public Attributes constructAttrsFromGroup(final SCIMGroup group) throws NamingException, Exception {
		final String methodName = "constructAttrsFromGroup";
		LOGGER.info(methodName + " Entering");
		Attributes attrs = new BasicAttributes(true);
		ArrayList<Membership> memberList = new ArrayList<Membership>();
		String[] keys = ldapGroupCore.keySet().toArray(new String[ldapGroupCore.size()]);
		Attribute attr;
		Object value = null;
		LOGGER.info("[" + methodName + "] constructing Attrs from group " + group.getDisplayName());
		Attribute objclass = new BasicAttribute("objectClass");
		SCIMFilter filter = new SCIMFilter();
		SCIMFilterAttribute filterAttr = new SCIMFilterAttribute();
		SCIMFilterType filterType = SCIMFilterType.EQUALS;
		filter.setFilterType(filterType);
		filterAttr.setAttributeName(SCIMServiceHelper.USER_NAME);
		filter.setFilterAttribute(filterAttr);
		for (int i = 0; i < ldapGroupClass.length; i++) {
			objclass.add(ldapGroupClass[i]);
		}
		for (int i = 0; i < keys.length; i++) {
			String attrType = ldapGroupCore.get(keys[i]);
			attr = new BasicAttribute(attrType);
			if (keys[i].equals(SCIMServiceHelper.ID)) {
				value = group.getId();
			} else if (keys[i].equals(SCIMServiceHelper.MEMBERS2) && (group.getMembers() != null)) {
				attrs.put(attr);
				continue;
			} else {
				LOGGER.info(" There's no value matching according to rules. Moving onto next...");
			}
			if (value != null) {
				attr.add(value.toString());
			}
			attrs.put(attr);
		}
		Attribute member = attrs.get(ldapGroupCore.get(SCIMServiceHelper.MEMBERS2));
		attrs.put(objclass);
		/**
		 * builds dn from all members, assumes the members are located in the /* same area as users. /* TODO: trim down
		 * the dups comming from Okta, happens when group push /* is enabled for a group, assign app to one group,
		 * unassign, then /* assign to another group with same users, their external IDS will be /* different
		 **/
		if (group.getMembers() != null && ldapGroupCore.get(SCIMServiceHelper.MEMBERS2) != null) {
			Object[] members = group.getMembers().toArray();
			for (int i = 0; i < members.length; i++) {
				Membership mem = (Membership) members[i];
				String dnUsername = getUserDnName(mem.getDisplayName());
				filter.setFilterValue(mem.getDisplayName());
				List<SCIMUser> result = getUsersByEqualityFilter(filter);
				String name = ldapUserPre + dnUsername + "," + ldapUserDn + ldapBaseDn;
				DistinguishedName dn = new DistinguishedName(name);
				/**
				 * check that the member exists in the cache/ldap before making them a member of a group
				 */
				if (result.size() == 1) {
					member.add(dn.encode());
					memberList.add(mem);
				}
			}
			/**
			 * Remove the member attr from the ldap query obj if there are no members to insert
			 */
			if (memberList.size() == 0) {
				attrs.remove(ldapGroupCore.get(SCIMServiceHelper.MEMBERS2));
			}
		}
		LOGGER.info(methodName + " Exiting");
		return attrs;
	}

	/**
	 * Helper function that constructs a SCIMGroup object from Attributes fetched from Ldap. Uses mappings from
	 * properties file to set fields in SCIMGroup obj.
	 * 
	 * @param attrs
	 *            - attributes to build SCIMGroup
	 * @return the SCIMGroup object that the attrs created
	 * @throws NamingException
	 */
	public SCIMGroup constructGroupFromAttrs(final Attributes attrs) throws NamingException {
		final String methodName = "constructGroupFromAttrs";
		LOGGER.info(methodName + " Entering");
		/** Create objs/get mappings from config file. */
		String ldapFilter = "";
		String searchDN = ldapUserDn + ldapBaseDn;
		SCIMGroup group = new SCIMGroup();
		String cn = "";
		if (null != attrs) {
			Attribute attr = attrs.get("cn");
			if (null != attr && null != attr.get()) {
				cn = attr.get().toString();
			}
		}
		LOGGER.debug("[" + methodName + "] Constructing Group " + cn + " from Attrs.");
		ArrayList<Membership> memberList = new ArrayList<Membership>();
		String memberAttrLookup = ldapGroupCore.get(SCIMServiceHelper.MEMBERS2);
		Attribute memberAttr = null;
		ArrayList<Attributes> queryResult;
		if (memberAttrLookup != null)
			memberAttr = attrs.get(memberAttrLookup);
		else
			LOGGER.warn("[" + methodName + "] Connector.properties did not have members entry for groupCoreMap.");
		String idLookup = ldapGroupCore.get(SCIMServiceHelper.ID);
		String id = "";
		if (idLookup != null && null != attrs) {
			Attribute attr = attrs.get(idLookup);
			if (null != attr) {
				Object val = attr.get();
				if (null != val) {
					id = val.toString();
				}
			}
		} else
			LOGGER.warn("[" + methodName + "] Connector.properties did not have id entry for groupCoreMap.");
		group.setDisplayName(cn);
		group.setId(id);
		if (memberAttr != null) {
			for (int i = 0; i < memberAttr.size(); i++) {
				String memberDn = memberAttr.get(i).toString();
				DistinguishedName dn = new DistinguishedName(memberDn);
				LdapRdn memberCn = dn.getLdapRdn(ldapUserPre.split("=")[0]);
				ldapFilter = "(" + ldapUserPre + memberCn.getValue() + ")";
				queryResult = queryLDAP(searchDN, ldapFilter);
				/** Should only return one result */
				if (queryResult.size() == 1) {
					SCIMUser result = constructUserFromAttrs(queryResult.get(0));
					/**
					 * Searches through cache to retrieve ids for group members,used in SCIMGroup
					 */
					if (result != null) {
						Membership memHolder = new Membership(result.getId(), result.getUserName());
						memberList.add(memHolder);
					}
				}
			}
			group.setMembers(memberList);
		}
		LOGGER.info(methodName + " Exiting");
		return group;
	}

	/**
	 * queryLDAPForUserUpdateOnly
	 * 
	 * @param dn
	 * @param filter
	 * @return
	 * @throws NamingException
	 */

	public ArrayList<Attributes> queryLDAPForUserUpdateOnly(final String dn, final String filter)
			throws NamingException {
		final String methodName = "queryLDAPForUserUpdateOnly";
		LOGGER.info(methodName + " Entering");
		ArrayList<Attributes> results = new ArrayList<Attributes>();
		LdapContext ctx = getLdapContext();
		if (null == ctx) {
			throw new NamingException("Ldap context could not be fetched");
		}
		NamingEnumeration<?> namingEnum = null;
		try {
			ctx.setRequestControls(null);
			SearchControls controls = new SearchControls();
			controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
			LOGGER.debug("[queryLDAP] dn=" + dn + " filter=" + filter);
			namingEnum = ctx.search(dn, filter, controls);
			while (namingEnum.hasMore()) {
				SearchResult result = (SearchResult) namingEnum.next();
				Attributes attrs = result.getAttributes();
				String fullName = result.getName();
				if (fullName.contains(",")) {
					String containerName = fullName.substring(fullName.lastIndexOf(",") + 1);
					attrs.put("containerName", containerName);
				} else
					attrs.put("containerName", "");

				results.add(attrs);
			}
		} catch (Exception ex) {
			LOGGER.error("Exception while fetching users, " + ex.getMessage());
		} finally {
			if (null != ctx) {
				ctx.close();
			}
			if (null != namingEnum) {
				namingEnum.close();
			}
		}
		LOGGER.info(methodName + " Exiting");
		return results;
	}

	/**
	 * This methos is used to queryLDAP
	 * 
	 * @param dn
	 * @param filter
	 * @return
	 * @throws NamingException
	 */
	public ArrayList<Attributes> queryLDAP(final String dn, final String filter) throws NamingException {
		final String methodName = "queryLDAP";
		LOGGER.info(methodName + " Entering");
		ArrayList<Attributes> results = new ArrayList<Attributes>();
		LdapContext ctx = getLdapContext();
		if (ctx == null) {
			throw new NamingException("Ldap context could not be fetched");
		}
		NamingEnumeration<?> namingEnum = null;
		try {
			ctx.setRequestControls(null);
			SearchControls controls = new SearchControls();
			controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
			LOGGER.debug("[queryLDAP] dn=" + dn + " filter=" + filter);
			namingEnum = ctx.search(dn, filter, controls);
			while (namingEnum.hasMore()) {
				SearchResult result = (SearchResult) namingEnum.next();
				Attributes attrs = result.getAttributes();
				result.getName();
				results.add(attrs);
			}
		} catch (Exception ex) {
			LOGGER.error("Exception while fetching users, " + ex.getMessage());
		} finally {
			if (null != ctx) {
				ctx.close();
			}
			if (null != namingEnum) {
				namingEnum.close();
			}
		}
		LOGGER.info(methodName + "Exiting");
		return results;
	}

	/**
	 * This method is used to read the text as string from the File
	 * 
	 * @param filePathName
	 * @return
	 * @throws IOException
	 */
	private static String readFileAsString(String filePathName) throws IOException {
		String text = "";
		text = new String(Files.readAllBytes(Paths.get(filePathName)));
		return text;
	}

	/**
	 * The Method decrypts based on a encrypted text and a encoded salt passed
	 * 
	 * @param str
	 * @param salt
	 * @param keyFileLocation
	 * @return
	 * @throws Exception
	 */
	public String decrypt(String str, String salt, String keyFileLocation) throws Exception {
		if (StringUtils.isEmpty(str) || StringUtils.isEmpty(salt) || StringUtils.isEmpty(keyFileLocation)) {
			throw new IOException("Input variables are empty.");
		}
		try {
			byte[] saltByte = DatatypeConverter.parseBase64Binary(salt);
			String passKey = readFileAsString(keyFileLocation);
			byte[] ciphertext = DatatypeConverter.parseBase64Binary(str);
			if (StringUtils.isNotEmpty(passKey) && null != ciphertext) {
				if (ciphertext.length < 32) {
					return null;
				}

				byte[] iv = Arrays.copyOfRange(ciphertext, 0, 16);
				byte[] ct = Arrays.copyOfRange(ciphertext, 16, ciphertext.length);

				SecretKeyFactory factory = SecretKeyFactory.getInstance(SCIMServiceHelper.HASH_ALGO);
				KeySpec spec = new PBEKeySpec(passKey.toCharArray(), saltByte, 65536, 256);
				SecretKey tmp = factory.generateSecret(spec);
				if (null != tmp) {
					SecretKey secret = new SecretKeySpec(tmp.getEncoded(), SCIMServiceHelper.ENCRYPTION_ALGO);
					Cipher cipher = Cipher.getInstance(SCIMServiceHelper.PADDING_SCHEMES);

					cipher.init(Cipher.DECRYPT_MODE, secret, new javax.crypto.spec.IvParameterSpec(iv));
					byte[] plaintext = cipher.doFinal(ct);
					return new String(plaintext, SCIMServiceHelper.ENCODING);
				}
			}

		} catch (Exception e) {
			LOGGER.error("Exception in understanding the user user for connection." + e.getMessage(), e);
		}
		return null;
	}

}
