package com.okta.scim.server.ldap.connector;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.UUID;
import javax.annotation.PostConstruct;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.ldap.LdapContext;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import com.okta.scim.server.capabilities.UserManagementCapabilities;
import com.okta.scim.server.exception.DuplicateGroupException;
import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;
import com.okta.scim.server.exception.EntityNotFoundException;
import com.okta.scim.server.exception.OnPremUserManagementException;
import com.okta.scim.server.service.SCIMOktaConstants;
import com.okta.scim.server.service.SCIMService;
import com.okta.scim.util.exception.InvalidDataTypeException;
import com.okta.scim.util.model.Email;
import com.okta.scim.util.model.PaginationProperties;
import com.okta.scim.util.model.PhoneNumber;
import com.okta.scim.util.model.SCIMFilter;
import com.okta.scim.util.model.SCIMGroup;
import com.okta.scim.util.model.SCIMGroupQueryResponse;
import com.okta.scim.util.model.SCIMUser;
import com.okta.scim.util.model.SCIMUserQueryResponse;

/**
 * SCIM Connector Implementation Class Code is under review for Iteration-6
 * 
 * @author AC PwC
 */
public class SCIMServiceImpl implements SCIMService {
	/** Absolute path for users.json set in the dispatcher-servlet.xml */
	private String usersFilePath;
	/** Absolute path for groups.json set in the dispatcher-servlet.xml */
	private String groupsFilePath;
	/** LDAP settings */
	private String ldapBaseDn;
	private String ldapGroupDn;
	private String ldapUserDn;
	private String ldapUserPre;
	private String ldapGroupPre;
	private String ldapUserFilter;
	private String ldapGroupFilter;
	private String ldapInitialContextFactory;
	private String ldapUrl;
	private String ldapSecurityAuthentication;
	private String ldapSecurityPrincipal;
	private String ldapSecurityCredentials;
	private String[] ldapUserClass;
	private String ldapUserClassAsString;
	private int ldapSleepTime;
	private int ldapRetryParam;
	private String ldapCurrentUserContainer;
	private String ldapCurrentUniqueKeyField;
	private String[] ldapGroupClass;
	private String ldapGroupClassAsString;
	private boolean useEntireUsername;
	private Map<String, String> ldapUserCore = new HashMap<String, String>();
	private Map<String, String[]> ldapUserCustom = new HashMap<String, String[]>();
	private Map<String, String> ldapGroupCore = new HashMap<String, String>();
	/** This is the name of the App which is On Premise */
	private String appName;
	private static final Logger LOGGER = Logger.getLogger(SCIMServiceImpl.class.getName());
	private Map<String, SCIMUser> userMap = new HashMap<String, SCIMUser>();
	private int nextGroupId;
	private String userCustomUrn;
	private String ldapEncryptPwd;
	private String ldapSaltKey;
	private String ldapSecurityKeyFilePath;
	private String appSchemaName;
	private boolean useFilePersistence = true;
	private int nextUserId;
	private String logLocation = SCIMServiceHelper.LOG4J_PROPERTIES_LOCATION;

	private Hashtable<String, String> env = new Hashtable<String, String>();
	SCIMServiceUtil scimUtil = new SCIMServiceUtil();

	@PostConstruct
	public void afterCreation() throws Exception {
		final String methodName = "afterCreation";
		LOGGER.info(methodName + " Entering");
		LOGGER.info("[" + methodName + "] Initializing connector...");

		initLdapVars();
		LOGGER.info("[" + methodName + "] Imported config from connector.properties.");
		userCustomUrn = SCIMOktaConstants.CUSTOM_URN_PREFIX + appName + SCIMOktaConstants.CUSTOM_URN_SUFFIX
				+ SCIMServiceHelper.UD_SCHEMA_NAME;
		env.put(Context.INITIAL_CONTEXT_FACTORY, ldapInitialContextFactory);
		env.put(Context.PROVIDER_URL, ldapUrl);
		env.put(Context.SECURITY_AUTHENTICATION, ldapSecurityAuthentication);
		env.put(Context.SECURITY_PRINCIPAL, ldapSecurityPrincipal);
		env.put(Context.SECURITY_CREDENTIALS, ldapSecurityCredentials);
		nextUserId = 100;
		nextGroupId = 1000;
		LOGGER.info(methodName + " Exiting - Connector initialized and waiting for tasks.");
	}

	/**
	 * Helper function that pulls data from properties file.
	 * 
	 * @throws ConfigurationException
	 */
	private void initLdapVars() throws ConfigurationException, NoSuchElementException, FileNotFoundException {
		final String methodName = "initLdapVars";
		LOGGER.info(methodName + " Entering");
		Properties logProperties = new Properties();
		InputStream logStream = null;
		try {
			logStream = Files.newInputStream(Paths.get(logLocation));
			logProperties.load(logStream);
		} catch (IOException e) {
			LOGGER.error("Error in getting the log file from connector");
		} finally {
			if (null != logStream) {
				try {
					logStream.close();
				} catch (IOException e) {
					LOGGER.error("Exception while closing Log File stream");
				}
			}
		}
		PropertyConfigurator.configure(logProperties);
		Properties config;
		InputStream stream = null;
		try {
			stream = Files.newInputStream(Paths.get(SCIMServiceHelper.CONF_FILEPATH+"/"+SCIMServiceHelper.CONF_FILENAME));
			config = new Properties();
			config.load(stream);
			appName = config.getProperty("OPP.appName");
			appSchemaName = config.getProperty("OPP.SchemaName");
			ldapBaseDn = config.getProperty("ldap.baseDn");
			ldapGroupDn = config.getProperty("ldap.groupDn");
			ldapUserDn = config.getProperty("ldap.userDn");
			ldapGroupPre = config.getProperty("ldap.groupPre");
			ldapUserPre = config.getProperty("ldap.userPre");
			ldapUserFilter = config.getProperty("ldap.userFilter");
			ldapGroupFilter = config.getProperty("ldap.groupFilter");
			ldapEncryptPwd = config.getProperty("ldap.encryptedPassword");
			ldapSaltKey = config.getProperty("ldap.encryptedSaltKey");
			ldapSecurityKeyFilePath = config.getProperty("ldap.ldapSecurityKeyFilePath");
			ldapSecurityCredentials = scimUtil.decrypt(ldapEncryptPwd, ldapSaltKey, ldapSecurityKeyFilePath);
			ldapInitialContextFactory = config.getProperty("ldap.initialContextFactory");
			ldapUrl = config.getProperty("ldap.url");
			ldapSecurityAuthentication = config.getProperty("ldap.securityAuthentication");
			ldapSecurityPrincipal = config.getProperty("ldap.securityPrincipal");
			ldapUserClassAsString = config.getProperty("ldap.userClass");
			ldapUserClass = ldapUserClassAsString.split(SCIMServiceHelper.COMMA);
			ldapCurrentUserContainer = config.getProperty("ldap.currentUserContainer");
			ldapCurrentUniqueKeyField = config.getProperty("ldap.currentUniqueKeyField");
			ldapGroupClassAsString = config.getProperty("ldap.groupClass");
			ldapGroupClass = ldapGroupClassAsString.split(SCIMServiceHelper.COMMA);
			ldapRetryParam = Integer.parseInt(config.getProperty("ldap.retryParam"));
			ldapSleepTime = Integer.parseInt(config.getProperty("ldap.sleepTimeParam"));
			useEntireUsername = Boolean.parseBoolean(config.getProperty("ldap.useEntireUsername"));

			if (config != null) {
				/** Constructing ldapUserCustom Map from Properties-- Starting */
				for (String customPropertyName : SCIMServiceHelper.CUSTOM_PROPERTIES) {
					if (customPropertyName != null && !customPropertyName.isEmpty()) {
						String customAttribute = config.getProperty(customPropertyName);
						if (customAttribute != null && !customAttribute.isEmpty()) {
							insertintoCustomMap(customAttribute, ldapUserCustom);
						}
					}
				}
				/** Constructing ldapUserCustom Map from Properties -- Completed */

				/** Constructing ldapUserCore Map from Properties -- Starting */
				for (String corePropertyName : SCIMServiceHelper.CORE_PROPERTIES) {
					if (corePropertyName != null && !corePropertyName.isEmpty()) {
						String coreAttribute = config.getProperty(corePropertyName);
						if (coreAttribute != null && !coreAttribute.isEmpty()) {
							insertintoCoreMap(coreAttribute, ldapUserCore);
						}
					}
				}
				/** Constructing ldapUserCore Map from Properties -- Completed */

				/** Constructing ldapGroupCore Map from Properties -- Starting */
				for (String groupPropertyName : SCIMServiceHelper.GROUP_PROPERTIES) {
					if (groupPropertyName != null && !groupPropertyName.isEmpty()) {
						String groupAttribute = config.getProperty(groupPropertyName);
						if (groupAttribute != null && !groupAttribute.isEmpty()) {
							insertintoGroupMap(groupAttribute, ldapGroupCore);
						}
					}
				}
			}
			/** Constructing ldapGroupCore Map from Properties -- Completed */

			/** Sending the Variables to be reused in the SCIMServiceUtil.java */
			scimUtil.setVar(ldapUserCore, ldapGroupCore, ldapUserCustom, userCustomUrn, env, useEntireUsername,
					ldapGroupClass, ldapUserClass, ldapUserDn, ldapBaseDn, ldapUserPre, nextGroupId, ldapRetryParam,
					ldapSleepTime);
		} catch (ConfigurationException | NoSuchElementException | FileNotFoundException e) {
			handleGeneralException(e);
			throw e;
		} catch (Exception e) {
			handleGeneralException(e);
		} finally {
			if (null != stream) {
				try {
					stream.close();
				} catch (IOException e) {
					LOGGER.error("Exception while closing input stream");
				}
			}
		}
		LOGGER.info(methodName + " Exiting");
	}

	/**
	 * This method is used to insert Custom Properties into ldapUserCustom Map
	 * 
	 * @param attribute
	 * @param ldapUserCustom
	 */
	private void insertintoCustomMap(String attribute, Map<String, String[]> ldapUserCustom) {
		final String methodName = "insertintoCustomMap";
		LOGGER.info(methodName + " Entering");
		if (attribute.contains(SCIMServiceHelper.COMMA)) {
			String[] customAttrAsArray = attribute.split(SCIMServiceHelper.COMMA);
			ldapUserCustom.put(customAttrAsArray[0].trim(),
					Arrays.copyOfRange(customAttrAsArray, 1, customAttrAsArray.length));
		} else {
			LOGGER.error("Custom Attributes are not defined properly in Properties file");
		}
		LOGGER.info(methodName + " Exiting");
	}

	/**
	 * This method is used to insert Core Properties into ldapUserCore Map
	 * 
	 * @param attribute
	 * @param ldapUserCore
	 */
	private void insertintoCoreMap(String attribute, Map<String, String> ldapUserCore) {
		final String methodName = "insertintoCoreMap";
		LOGGER.info(methodName + " Entering");
		if (attribute.contains(SCIMServiceHelper.COMMA)) {
			String[] coreAttrAsArray = attribute.split(SCIMServiceHelper.COMMA);
			ldapUserCore.put(coreAttrAsArray[0].trim(), coreAttrAsArray[1].trim());
		} else {
			LOGGER.error("Core Attributes are not defined properly in Properties file");
		}
		LOGGER.info(methodName + " Exiting");
	}

	/**
	 * This method is used to insert Group Properties into ldapGroupCore Map
	 * 
	 * @param attribute
	 * @param ldapUserCore
	 */
	private void insertintoGroupMap(String attribute, Map<String, String> ldapUserCore) {
		final String methodName = "insertintoGroupMap";
		LOGGER.info(methodName + " Entering");
		if (attribute.contains(SCIMServiceHelper.COMMA)) {
			String[] groupAttrAsArray = attribute.split(SCIMServiceHelper.COMMA);
			ldapGroupCore.put(groupAttrAsArray[0].trim(), groupAttrAsArray[1].trim());
		} else {
			LOGGER.error("Group Attributes are not defined properly in Properties file");
		}
		LOGGER.info(methodName + " Exiting");
	}

	/**
	 * This method is used to validate the attribute if it contains Blank values
	 * 
	 * @param attr
	 *            - Attribute
	 * @return
	 */
	private boolean validateAttribute(Attribute attr) {
		final String methodName = "validateAttribute";
		LOGGER.info(methodName + " Entering");
		Boolean attrFlag = false;
		if (attr != null && attr.contains("")) {
			attrFlag = true;
		}
		LOGGER.info(methodName + " Exiting");
		return attrFlag;
	}

	/**
	 * Methods from OKTA provided skeleton SDK code for override.
	 */
	public String getUsersFilePath() {
		return usersFilePath;
	}

	public void setUsersFilePath(String usersFilePath) {
		this.usersFilePath = usersFilePath;
	}

	public String getGroupsFilePath() {
		return groupsFilePath;
	}

	public void setGroupsFilePath(String groupsFilePath) {
		this.groupsFilePath = groupsFilePath;
	}

	/**
	 * End Methods from OKTA provided skeleton SDK code for override.
	 */

	/**
	 * This method creates a user. All the standard attributes of the SCIM User can be retrieved by using the getters on
	 * the SCIMStandardUser member of the SCIMUser object.
	 * <p/>
	 * If there are custom schemas in the SCIMUser input, you can retrieve them by providing the name of the custom
	 * property. (Example : SCIMUser.getStringCustomProperty("schemaName", "customFieldName")), if the property of
	 * string type.
	 * <p/>
	 * This method is invoked when a POST is made to /Users with a SCIM payload representing a user to be created.
	 * <p/>
	 * NOTE: While the user's group memberships will be populated by Okta, according to the SCIM Spec
	 * (http://www.simplecloud.info/specs/draft-scim-core -schema-01.html#anchor4) that information should be considered
	 * read-only. Group memberships should only be updated through calls to createGroup or updateGroup.
	 * 
	 * @param user
	 *            SCIMUser representation of the SCIM String payload sent by the SCIM client.
	 * @return the created SCIMUser.
	 * @throws OnPremUserManagementException
	 */
	@Override
	public SCIMUser createUser(SCIMUser user) throws OnPremUserManagementException {

		final String methodName = "createUser";
		LOGGER.info(methodName + " Entering");
		String id = generateNextId(SCIMServiceHelper.USER_RESOURCE);
		user.setId(id);
		String dnUsername;

		if (userMap == null) {

			throw new OnPremUserManagementException("OPP Connector Exception",
					"Cannot create the user. The userMap is null", null);
		}
		LdapContext ctx = null;
		dnUsername = scimUtil.getUserDnName(user.getUserName());
		NamingEnumeration<String> namingEnum = null;
		try {
			ctx = scimUtil.getLdapContext();
			if (ctx == null) {
				throw new NamingException("Ldap context could not be fetched");
			}
			LOGGER.debug("Constructing Attributes from SCIM User Object recieved..");
			Attributes attrs = scimUtil.constructAttrsFromUser(user, Boolean.TRUE, Boolean.FALSE);

			LOGGER.debug("Constructing Attributes from SCIM User Object recieved..Finished");
			String debugKeys = "";
			Attributes deleteAttrs = new BasicAttributes();
			BasicAttribute uniqueKeyForAccount = null;
			if (attrs != null) {
				namingEnum = attrs.getIDs();
				if (namingEnum != null) {
					while (namingEnum.hasMore()) {
						String key = namingEnum.next();
						if (StringUtils.isNotBlank(key)) {
							Attribute attr = attrs.get(key);
							Boolean attrFlag = validateAttribute(attr);
							if (attrFlag) {
								deleteAttrs.put(attr);
							}
						}
					}
				}
				uniqueKeyForAccount = (BasicAttribute) attrs.get(ldapCurrentUniqueKeyField);
				user.setId(uniqueKeyForAccount.get(0).toString());

				LOGGER.debug("[" + methodName + "] Username: " + user.getUserName() + "Attributes " + debugKeys
						+ "Proceeding to insert into directory");

				String dn = ldapUserPre + dnUsername + SCIMServiceHelper.COMMA + ldapCurrentUserContainer
						+ SCIMServiceHelper.COMMA + ldapUserDn + ldapBaseDn;

				LOGGER.debug("[" + methodName + "] User with dn " + dn + " is being inserted");
				ctx.createSubcontext(dn, attrs);
				if (deleteAttrs != null) {
					ctx.modifyAttributes(dn, LdapContext.REMOVE_ATTRIBUTE, deleteAttrs);
				}
				LOGGER.debug("[" + methodName + "] User " + user.getName().getFormattedName()
						+ " successfully inserted into Directory Service.");
			}
		} catch (NamingException | InvalidDataTypeException e) {
			handleGeneralException(e);
			LOGGER.error(e.getMessage());

			throw new OnPremUserManagementException("OPP Connector Exception", e.getMessage(), e);
		} catch (Exception e) {
			handleGeneralException(e);
		} finally {
			try {
				if (null != ctx) {
					ctx.close();
				}
				if (null != namingEnum) {
					namingEnum.close();
				}
			} catch (NamingException ex) {
				LOGGER.error("Exception while closing ldap context : " + ex.getMessage());
				throw new OnPremUserManagementException("OPP Connector Exception", ex.getMessage(), ex);
			}
		}
		LOGGER.info(methodName + " Exiting");
		return user;
	}

	/**
	 * This method updates a user.
	 * <p/>
	 * This method is invoked when a PUT is made to /Users/{id} with the SCIM payload representing a user to be updated.
	 * <p/>
	 * NOTE: While the user's group memberships will be populated by Okta, according to the SCIM Spec
	 * (http://www.simplecloud.info/specs/draft-scim-core -schema-01.html#anchor4) that information should be considered
	 * read-only. Group memberships should only be updated through calls to createGroup or updateGroup.
	 * 
	 * @param id
	 *            the id of the SCIM user.
	 * @param user
	 *            SCIMUser representation of the SCIM String payload sent by the SCIM client.
	 * @return the updated SCIMUser.
	 * @throws OnPremUserManagementException
	 */
	public SCIMUser updateUser(String id, SCIMUser user) throws OnPremUserManagementException, EntityNotFoundException {
		final String methodName = "updateUser";
		LOGGER.info(methodName + " Entering");
		LOGGER.debug("[updateUser] Updating user: " + user.getName().getFormattedName());
		Boolean updateFlag = false;
		String dnUsername = "", oldDNUsername, oldDN = "", newDN = "";
		SCIMUser oldUser;
		LdapContext ctx = null;
		try {
			ctx = scimUtil.getLdapContext();
			if (ctx == null) {
				throw new NamingException("Ldap context could not be fetched");
			}
			String searchDN = ldapUserDn + ldapBaseDn;

			String idLookup = ldapUserCore.get(SCIMServiceHelper.ID).toString();
			String ldapFilter = "(" + idLookup + "=" + id + ")";
			ArrayList<Attributes> queryResults = scimUtil.queryLDAPForUserUpdateOnly(searchDN, ldapFilter);

			/** Checking if the user is already having updated values in IDV */
			if (null != user.getPassword()) {
				LOGGER.debug("User " + user.getUserName()
						+ " Creds came with this Update request hence going with the blind update and not comparing..");
				updateFlag = Boolean.TRUE;
			} else {
				LOGGER.debug("User " + user.getUserName()
						+ " Creds did not come with this update..Going with Compare..");
				updateFlag = compareUserObjects(queryResults, user);
			}
			/**
			 * Proceed to update the user in IDV if updateFlag=true else do not proceed to Update
			 */
			if (updateFlag) {
				if (queryResults.size() == 1) {
					oldUser = scimUtil.constructUserFromAttrs(queryResults.get(0));
					Attributes oldAttrs = (Attributes) queryResults.get(0);
					BasicAttribute ba = (BasicAttribute) oldAttrs.get(SCIMServiceHelper.OBJECT_CLASS);
					BasicAttribute baContainer = (BasicAttribute) oldAttrs.get("containerName");
					String sContainer = (String) baContainer.get(0);
					LOGGER.debug("[" + methodName + "] Container is " + sContainer);
					if (sContainer.length() > 0) {
						oldDNUsername = scimUtil.getUserDnName(oldUser.getUserName());
						oldDN = ldapUserPre + oldDNUsername + SCIMServiceHelper.COMMA + sContainer
								+ SCIMServiceHelper.COMMA + ldapUserDn + ldapBaseDn;
						dnUsername = scimUtil.getUserDnName(user.getUserName());
						newDN = ldapUserPre + dnUsername + SCIMServiceHelper.COMMA + sContainer
								+ SCIMServiceHelper.COMMA + ldapUserDn + ldapBaseDn;
					} else {
						oldDNUsername = scimUtil.getUserDnName(oldUser.getUserName());
						oldDN = ldapUserPre + oldDNUsername + SCIMServiceHelper.COMMA + ldapUserDn + ldapBaseDn;
						dnUsername = scimUtil.getUserDnName(user.getUserName());
						newDN = ldapUserPre + dnUsername + SCIMServiceHelper.COMMA + ldapUserDn + ldapBaseDn;

					}
					Attributes attrs = scimUtil.constructAttrsFromUser(user, Boolean.TRUE, Boolean.TRUE);
					if (user.isActive()) {
						/** Detecting uname change and renaming context */
						LOGGER.debug("NEW DNUsername of the User is ::" + dnUsername);
						LOGGER.debug("OLD DNUsername of the User is ::" + oldDNUsername);
						if (!dnUsername.equals(oldDNUsername)) {
							LOGGER.info("[" + methodName
									+ "] User's DN in LDAP has changed from previous value, renaming...");
							ctx.rename(oldDN, newDN);
						}
						LOGGER.info("[updateUser] User " + user.getUserName() + " is still active, modifying user.");
						attrs.remove(ldapUserCore.get(SCIMServiceHelper.ID).toString());
						attrs.remove("objectclass");
						attrs.put(ba);
						/** Add back old objectclass */
						Map<String, String> debugKeys = new HashMap<String, String>();
						Map<String, String> removedKeys = new HashMap<String, String>();
						Attributes deleteAttrs = new BasicAttributes();
						if (attrs != null) {
							NamingEnumeration<String> namingEnum = attrs.getIDs();
							if (namingEnum != null) {
								while (namingEnum.hasMore()) {
									String key = namingEnum.next();
									if (StringUtils.isNotBlank(key)) {
										Attribute attr = attrs.get(key);
										String value = null;
										/**
										 * Removing userpassword to print in the logs
										 */
										if (!key.equals("userpassword")) {
											value = (String) attrs.get(key).get();
											debugKeys.put(key, value);
										}
										if (debugKeys.get(key) == null || debugKeys.get(key).equals("")) {
											debugKeys.remove(key);
										}
										/**
										 * Checking the Null and Empty values of Attributes and deleting the attrs from
										 * IDV
										 */
										Boolean attrFlag = validateAttribute(attr);
										if (attrFlag) {
											LOGGER.debug("Key :: " + key + " attr :: " + attr.toString() + " ID :: "
													+ attr.getID());
											
											/**
											 * Comparing with LDAP attributes(queryResults) and deleting the blanked out
											 * values
											 */
											if (queryResults != null) {
												for (Attributes oldattrName : queryResults) {
													if (oldattrName != null) {
														if (oldattrName.get(attr.getID()) != null) {
															/** Setting the blanked out values to Attributes */
															deleteAttrs.put(attr);
															removedKeys.put(key, "");
														} else {
															attrs.remove(attr.getID());
														}
													}
												}

											}
										}
										if (attr.size() > 0) {
											LOGGER.debug("Key for modification " + key);
										} else {
											LOGGER.error("Key for modification is not proper " + key);
										}
									}
								}
							}
							/**
							 * Comparing and Removing the Blank values while printing in logs
							 */
							MapDifference<String, String> difference = Maps.difference(debugKeys, removedKeys);
							Map<String, String> mapDiff = difference.entriesOnlyOnLeft();
							LOGGER.debug("Before modifying attributes");
							ctx.modifyAttributes(newDN, LdapContext.REPLACE_ATTRIBUTE, attrs);

							if (deleteAttrs != null) {
								LOGGER.debug("Before modifying delete attributes");
								ctx.modifyAttributes(newDN, LdapContext.REMOVE_ATTRIBUTE, deleteAttrs);
							}
							if (removedKeys.size() > 0) {
								LOGGER.debug("["
										+ methodName
										+ "] User "
										+ user.getName().getFormattedName()
										+ " Attributes recieved as updated-to-blank from Okta and hence removed from the Directory Service are: "
										+ removedKeys.keySet().toString());
							}
							LOGGER.debug("[" + methodName + "] User " + user.getName().getFormattedName()
									+ " successfully modified in Directory Service with attributes: "
									+ mapDiff.toString());

							if (null != namingEnum) {
								namingEnum.close();
							}
						}

					} else {
						LOGGER.debug("User " + user.getUserName() + " is being Deactivated");
						Attributes deleteAttrs = new BasicAttributes();
						if (attrs != null) {
							NamingEnumeration<String> deleteNamingEnum = attrs.getIDs();
							/**
							 * Checking the Null and Empty values of Attributes and deleting the attrs from IDV
							 */
							if (deleteNamingEnum != null) {
								while (deleteNamingEnum.hasMore()) {
									String key = deleteNamingEnum.next();
									if (StringUtils.isNotBlank(key)) {
										Attribute attr = attrs.get(key);
										Boolean attrFlag = validateAttribute(attr);
										if (attrFlag) {
											deleteAttrs.put(attr);
										}
									}
								}
							}
							ctx.modifyAttributes(newDN, LdapContext.REPLACE_ATTRIBUTE, attrs);
							if (deleteAttrs != null) {
								ctx.modifyAttributes(newDN, LdapContext.REMOVE_ATTRIBUTE, deleteAttrs);
							}
							LOGGER.info("[" + methodName + "] User " + user.getName().getFormattedName()
									+ " successfully modified.");

						}
					}
				} else {
					LOGGER.error("[" + methodName + "] Connector did not find 1 user with id: " + id + ")");
					throw new OnPremUserManagementException("OPP Connector Exception - ID not found in Directory", id);
				}
			} else {
				LOGGER.debug("[" + methodName + "] User " + user.getUserName()
						+ " already has the updated values - No need to  Update User Attributes in IDV");
			}
		} catch (InvalidDataTypeException | NamingException | IllegalArgumentException | NoSuchElementException e) {
			handleGeneralException(e);
			throw new OnPremUserManagementException("OPP Connector Exception", e.getMessage(), e);
		} catch (Exception e) {
			handleGeneralException(e);
		} finally {
			try {
				if (null != ctx) {
					ctx.close();
				}
			} catch (NamingException ex) {
				LOGGER.error("Exception while closing ldap context : " + ex.getMessage());
				throw new OnPremUserManagementException("OPP Connector Exception", ex.getMessage(), ex);
			}
		}
		LOGGER.info(methodName + " Exiting");
		return user;
	}

	/**
	 * Get all the users.
	 * <p/>
	 * This method is invoked when a GET is made to /Users In order to support pagination (So that the client and the
	 * server are not overwhelmed), this method supports querying based on a start index and the maximum number of
	 * results expected by the client. The implementation is responsible for maintaining indices for the SCIM Users.
	 * 
	 * @param pageProperties
	 *            denotes the pagination properties
	 * @param filter
	 *            denotes the filter
	 * @return the response from the server, which contains a list of users along with the total number of results,
	 *         start index and the items per page
	 * @throws com.okta.scim.server.exception.OnPremUserManagementException
	 */
	public SCIMUserQueryResponse getUsers(PaginationProperties pageProperties, SCIMFilter filter)
			throws OnPremUserManagementException {
		final String methodName = "getUsers(PaginationProperties, SCIMFilter)";
		LOGGER.info(methodName + " Entering");
		List<SCIMUser> users = new ArrayList<SCIMUser>();
		SCIMUserQueryResponse response = new SCIMUserQueryResponse();
		try {
			if (filter != null) {
				/** Get users based on a filter */
				users = scimUtil.getUserByFilter(filter);
				/** construct a SCIMUserQueryResponse and set */
				/**
				 * The total results in this case is set to the number of users. /* But it may be possible that /* there
				 * are more results than what is being returned => /* totalResults > users.size();
				 **/
				response.setTotalResults(users.size());
				/** Actual results which need to be returned */
				response.setScimUsers(users);
				/** The input has page properties => Set the start index. */
				if (pageProperties != null) {
					response.setStartIndex(pageProperties.getStartIndex());
				}
			} else {
				response = getUsers(pageProperties);
			}
		} catch (NamingException e) {
			handleGeneralException(e);
			LOGGER.error(e.getMessage());
			throw new OnPremUserManagementException("OPP Connector Exception", e.getMessage(), e);
		} catch (Exception e) {
			handleGeneralException(e);
		}
		LOGGER.info(methodName + " Exiting");
		return response;
	}

	/**
	 * This method is invoked when a GET is made to /Users In order to support pagination
	 * 
	 * @param pageProperties
	 *            denotes the pagination properties
	 * @param filter
	 *            denotes the filter
	 * @return the response from the server, which contains a list of users along with the total number of results,
	 *         start index and the items per page
	 * @throws com.okta.scim.server.exception.OnPremUserManagementException
	 */
	private SCIMUserQueryResponse getUsers(final PaginationProperties pageProperties) throws NamingException {
		final String methodName = "getUsers(PaginationProperties)";
		LOGGER.info(methodName + " Entering");
		SCIMUserQueryResponse response = new SCIMUserQueryResponse();
		LOGGER.debug("[" + methodName + "] UserMap size: " + userMap.size());

		ArrayList<Attributes> unprocessedUsers = scimUtil.queryLDAP(ldapUserDn + ldapBaseDn, ldapUserFilter);
		List<SCIMUser> processedUsers = new ArrayList<SCIMUser>();
		for (int i = 0; i < unprocessedUsers.size(); i++) {
			SCIMUser user = scimUtil.constructUserFromAttrs(unprocessedUsers.get(i));
			processedUsers.add(user);
		}
		int totalResults = processedUsers.size();
		if (pageProperties != null) {
			/** Set the start index to the response. */
			response.setStartIndex(pageProperties.getStartIndex());
		}

		response.setTotalResults(totalResults);

		response.setScimUsers(processedUsers);
		LOGGER.info(methodName + " Exiting");
		return response;
	}

	/**
	 * Get a particular user.
	 * <p/>
	 * This method is invoked when a GET is made to /Users/{id}
	 * 
	 * @param id
	 *            the Id of the SCIM User
	 * @return the user corresponding to the id
	 * @throws com.okta.scim.server.exception.OnPremUserManagementException
	 */
	@Override
	public SCIMUser getUser(String id) throws OnPremUserManagementException, EntityNotFoundException {
		final String methodName = "getUser(id)";
		LOGGER.info(methodName + " Entering");
		LOGGER.info("[" + methodName + "] Id: " + id);
		String searchDN = ldapUserDn + ldapBaseDn;
		String idLookup = ldapUserCore.get(SCIMServiceHelper.ID).toString();
		String ldapFilter = "(" + idLookup + "=" + id + ")";
		SCIMUser user = null;
		try {
			ArrayList<Attributes> queryResults = scimUtil.queryLDAP(searchDN, ldapFilter);
			if (queryResults.size() >= 1) {
				user = scimUtil.constructUserFromAttrs(queryResults.get(0));
				LOGGER.info("[getUser] User found with id: " + id);
			} else {
				throw new EntityNotFoundException();
			}
		} catch (NamingException e) {
			handleGeneralException(e);
			throw new OnPremUserManagementException("OPP Connector Exception", e.getMessage(), e);
		} catch (Exception e) {
			handleGeneralException(e);
		}
		LOGGER.info(methodName + " EXiting");
		return user;
	}

	/**
	 * This method creates a group. All the standard attributes of the SCIM group can be retrieved by using the getters
	 * on the SCIMStandardGroup member of the SCIMGroup object.
	 * <p/>
	 * If there are custom schemas in the SCIMGroup input, you can retrieve them by providing the name of the custom
	 * property. (Example : SCIMGroup.getCustomProperty("schemaName", "customFieldName"))
	 * <p/>
	 * This method is invoked when a POST is made to /Groups with a SCIM payload representing a group to be created.
	 * 
	 * @param group
	 *            SCIMGroup representation of the SCIM String payload sent by the SCIM client
	 * @return the created SCIMGroup
	 * @throws com.okta.scim.server.exception.OnPremUserManagementException
	 */
	@Override
	public SCIMGroup createGroup(SCIMGroup group) throws OnPremUserManagementException, DuplicateGroupException {
		final String methodName = "createGroup";
		LOGGER.info(methodName + " Entering");
		LOGGER.debug("[" + methodName + "] Creating group: " + group.getDisplayName());
		// String id =scimUtil.generateNextId(SCIMServiceHelper.GROUP_RESOURCE);
		group.setId(group.getDisplayName());
		LdapContext ctx = null;
		NamingEnumeration<String> namingEnum = null;
		try {
			ctx = scimUtil.getLdapContext();
			if (ctx == null) {
				throw new NamingException("Ldap context could not be fetched");
			}
			Attributes attrs = scimUtil.constructAttrsFromGroup(group);

			String debugKeys = "";
			namingEnum = attrs.getIDs();

			LOGGER.debug("[" + methodName + "] Groupname: " + group.getDisplayName() + "Attributes " + debugKeys
					+ "Proceeding to insert into directory");

			String dn = ldapGroupPre + group.getDisplayName() + SCIMServiceHelper.COMMA + ldapGroupDn + ldapBaseDn;
			LOGGER.debug("[" + methodName + "] dn: " + dn);

			ctx.createSubcontext(ldapGroupPre + group.getDisplayName() + SCIMServiceHelper.COMMA + ldapGroupDn
					+ ldapBaseDn, attrs);
			LOGGER.info("[" + methodName + "] Group " + group.getDisplayName() + " successfully created.");
		} catch (NamingException e) {
			handleGeneralException(e);
			throw new OnPremUserManagementException("OPP Connector Exception", e.getMessage(), e);
		} catch (Exception e) {
			handleGeneralException(e);
		} finally {
			try {

				if (null != ctx) {
					ctx.close();
				}
				if (null != namingEnum) {
					namingEnum.close();
				}
			} catch (NamingException ex) {
				LOGGER.error("Exception while closing ldap context : " + ex.getMessage());
				throw new OnPremUserManagementException("OPP Connector Exception", ex.getMessage(), ex);
			}
		}
		LOGGER.info("[" + methodName + "] Exiting");
		return group;
	}

	/**
	 * This method updates a group.
	 * <p/>
	 * This method is invoked when a PUT is made to /Groups/{id} with the SCIM payload representing a group to be
	 * updated.
	 * 
	 * @param id
	 *            the id of the SCIM group
	 * @param group
	 *            SCIMGroup representation of the SCIM String payload sent by the SCIM client
	 * @return the updated SCIMGroup
	 * @throws com.okta.scim.server.exception.OnPremUserManagementException
	 */
	public SCIMGroup updateGroup(String id, SCIMGroup group) throws OnPremUserManagementException {
		final String methodName = "updateGroup";
		LOGGER.info(methodName + " Entering");
		LOGGER.info("[" + methodName + "] Updating Group: " + group.getDisplayName());
		String searchDN = ldapGroupDn + ldapBaseDn;
		String oldDN = "";
		String idLookup = ldapGroupCore.get(SCIMServiceHelper.ID);
		String ldapFilter = "(" + idLookup + "=" + id + ")";
		SCIMGroup oldGroup;
		LdapContext ctx = null;
		try {
			ctx = scimUtil.getLdapContext();
			if (ctx == null) {
				throw new NamingException("Ldap context could not be fetched");
			}
			ArrayList<Attributes> queryResults = scimUtil.queryLDAP(searchDN, ldapFilter);
			if (queryResults.size() >= 1) {
				oldGroup = scimUtil.constructGroupFromAttrs(queryResults.get(0));
				oldDN = ldapGroupPre + oldGroup.getDisplayName() + SCIMServiceHelper.COMMA + ldapGroupDn + ldapBaseDn;
				ctx.destroySubcontext(oldDN);
				LOGGER.info("[" + methodName + "] Group " + oldGroup.getDisplayName()
						+ " successfully deleted from Directory Service.");
			} else {
				throw new EntityNotFoundException();
			}
			Attributes attrs = scimUtil.constructAttrsFromGroup(group);
			ctx.createSubcontext(ldapGroupPre + group.getDisplayName() + SCIMServiceHelper.COMMA + ldapGroupDn
					+ ldapBaseDn, attrs);
			LOGGER.info("[" + methodName + "] Group " + group.getDisplayName() + " successfully re-created.");
		} catch (NamingException e) {
			handleGeneralException(e);
			throw new OnPremUserManagementException("OPP Connector Exception", e.getMessage(), e);
		} catch (Exception e) {
			handleGeneralException(e);
		} finally {
			try {

				if (null != ctx) {
					ctx.close();
				}
			} catch (NamingException ex) {
				LOGGER.error("Exception while closing ldap context : " + ex.getMessage());
				throw new OnPremUserManagementException("OPP Connector Exception", ex.getMessage(), ex);
			}
		}
		LOGGER.info(methodName + " Exiting");
		return group;
	}

	/**
	 * Get all the groups.
	 * <p/>
	 * This method is invoked when a GET is made to /Groups In order to support pagination (So that the client and the
	 * server) are not overwhelmed, this method supports querying based on a start index and the maximum number of
	 * results expected by the client. The implementation is responsible for maintaining indices for the SCIM groups.
	 * 
	 * @param pageProperties
	 * @see com.okta.scim.util.model.PaginationProperties An object holding the properties needed for pagination -
	 *      startindex and the count.
	 * @return SCIMGroupQueryResponse the response from the server containing the total number of results, start index
	 *         and the items per page along with a list of groups
	 * @throws com.okta.scim.server.exception.OnPremUserManagementException
	 */
	@Override
	public SCIMGroupQueryResponse getGroups(PaginationProperties pageProperties) throws OnPremUserManagementException {
		final String methodName = "getGroups";
		LOGGER.info(methodName + " Entering");
		SCIMGroupQueryResponse response = new SCIMGroupQueryResponse();
		LOGGER.info("[getGroups]");
		try {
			ArrayList<Attributes> unprocessedGroups = scimUtil.queryLDAP(ldapGroupDn + ldapBaseDn, ldapGroupFilter);
			List<SCIMGroup> processedGroups = new ArrayList<SCIMGroup>();
			for (int i = 0; i < unprocessedGroups.size(); i++) {
				SCIMGroup group = scimUtil.constructGroupFromAttrs(unprocessedGroups.get(i));
				processedGroups.add(group);
			}
			int totalResults = processedGroups.size();
			if (pageProperties != null) {

				response.setStartIndex(pageProperties.getStartIndex());
			}
			response.setTotalResults(totalResults);

			response.setScimGroups(processedGroups);
		} catch (NamingException e) {
			handleGeneralException(e);
			LOGGER.error(e.getMessage());
			throw new OnPremUserManagementException("OPP Connector Exception", e.getMessage(), e);
		} catch (Exception e) {
			handleGeneralException(e);
		}
		LOGGER.info(methodName + " Exiting");
		return response;
	}

	/**
	 * Get a particular group.
	 * <p/>
	 * This method is invoked when a GET is made to /Groups/{id}
	 * 
	 * @param id
	 *            the Id of the SCIM group
	 * @return the group corresponding to the id
	 * @throws com.okta.scim.server.exception.OnPremUserManagementException
	 */
	public SCIMGroup getGroup(String id) throws OnPremUserManagementException {
		final String methodName = "getGroup";
		LOGGER.info(methodName + " Entering");
		String searchDN = ldapGroupDn + ldapBaseDn;
		String idLookup = ldapGroupCore.get(SCIMServiceHelper.ID);
		String ldapFilter = "(" + idLookup + "=" + id + ")";
		SCIMGroup group = null;
		try {
			ArrayList<Attributes> queryResults = scimUtil.queryLDAP(searchDN, ldapFilter);

			if (queryResults.size() >= 1) {
				group = scimUtil.constructGroupFromAttrs(queryResults.get(0));
			} else {
				throw new EntityNotFoundException();
			}
			LOGGER.info(methodName + " Exiting");
		} catch (NamingException e) {
			handleGeneralException(e);
			throw new OnPremUserManagementException("OPP Connector Exception", e.getMessage(), e);
		} catch (Exception e) {
			handleGeneralException(e);
		}
		return group;
	}

	/**
	 * Delete a particular group.
	 * <p/>
	 * This method is invoked when a DELETE is made to /Groups/{id}
	 * 
	 * @param id
	 *            the Id of the SCIM group
	 * @throws OnPremUserManagementException
	 */
	public void deleteGroup(String id) throws OnPremUserManagementException, EntityNotFoundException {
		final String methodName = "deleteGroup";
		LOGGER.info(methodName + " Entering");
		LOGGER.debug("[deleteGroup] Id: " + id);
		String searchDN = ldapGroupDn + ldapBaseDn;
		String idLookup = ldapGroupCore.get(SCIMServiceHelper.ID);
		String ldapFilter = "(" + idLookup + "=" + id + ")";
		SCIMGroup oldGroup;
		LdapContext ctx = null;
		try {
			ArrayList<Attributes> queryResults = scimUtil.queryLDAP(searchDN, ldapFilter);
			ctx = scimUtil.getLdapContext();
			if (ctx == null) {
				throw new NamingException("Ldap context could not be fetched");
			}
			if (queryResults.size() >= 1) {
				oldGroup = scimUtil.constructGroupFromAttrs(queryResults.get(0));
				ctx.destroySubcontext(ldapGroupPre + oldGroup.getDisplayName() + SCIMServiceHelper.COMMA + ldapGroupDn
						+ ldapBaseDn);
				LOGGER.info("[" + methodName + "] Group found with id: " + id);
			} else {
				LOGGER.info("[" + methodName + "] No Group found with id: " + id + "");
			}
		} catch (NamingException e) {
			handleGeneralException(e);
			throw new OnPremUserManagementException("OPP Connector Exception", e.getMessage(), e);
		} catch (Exception e) {
			handleGeneralException(e);
		} finally {
			try {

				if (null != ctx) {
					ctx.close();
				}
			} catch (NamingException ex) {
				LOGGER.error("Exception while closing ldap context : " + ex.getMessage());
				throw new OnPremUserManagementException("OPP Connector Exception", ex.getMessage(), ex);
			}
		}
		LOGGER.info(methodName + " Exiting");
	}

	/**
	 * Get all the Okta User Management capabilities that this SCIM Service has implemented.
	 * <p/>
	 * This method is invoked when a GET is made to /ServiceProviderConfigs. It is called only when you are testing or
	 * modifying your connector configuration from the Okta Application instance UM UI. If you change the return values
	 * at a later time please re-test and re-save your connector settings to have your new return values respected.
	 * <p/>
	 * These User Management capabilities help customize the UI features available to your app instance and tells Okta
	 * all the possible commands that can be sent to your connector.
	 * 
	 * @return all the implemented User Management capabilities.
	 */
	public UserManagementCapabilities[] getImplementedUserManagementCapabilities() {

		LOGGER.info("Fetching UserManagementCapabilities");
		return new UserManagementCapabilities[] { UserManagementCapabilities.PUSH_NEW_USERS,
				UserManagementCapabilities.PUSH_PROFILE_UPDATES, UserManagementCapabilities.PUSH_PASSWORD_UPDATES,
				UserManagementCapabilities.PUSH_USER_DEACTIVATION, UserManagementCapabilities.REACTIVATE_USERS };
	}

	/**
	 * *This method is used to handle General Exceptions
	 * 
	 * @param e
	 */
	private void handleGeneralException(Exception e) {
		StringWriter errors = new StringWriter();
		e.printStackTrace(new PrintWriter(errors));
		LOGGER.error(e.getMessage());
		LOGGER.debug(errors.toString());
	}

	/**
	 * Generate Next ID
	 * 
	 * @param resourceType
	 * @return
	 */
	private String generateNextId(String resourceType) {
		if (useFilePersistence) {
			return UUID.randomUUID().toString();
		}
		if (resourceType.equals(SCIMServiceHelper.USER_RESOURCE)) {
			return Integer.toString(nextUserId++);
		}
		if (resourceType.equals(SCIMServiceHelper.GROUP_RESOURCE)) {
			return Integer.toString(nextGroupId++);
		}
		return null;
	}

	/**
	 * This method is used to compare user attributes from LDAP and OKTA during Update call
	 * 
	 * @param queryResults
	 *            - ArrayList of User Attributes from IDV
	 * @param user
	 *            - User Object from OKTA
	 * @return returnFlag - Boolean
	 * @throws NamingException
	 */
	private boolean compareUserObjects(ArrayList<Attributes> queryResults, SCIMUser user) throws NamingException,
			NoSuchElementException, IllegalArgumentException, Exception {
		final String methodName = "compareUserObjects";
		LOGGER.info(methodName + " Entering");
		boolean resultFlag = false;
		HashMap<String, String> oldAttrMap = new HashMap<>();
		HashMap<String, String> diffAttrMap = new HashMap<>();
		try {
			if (queryResults != null) {
				/** Getting the attributes from IDV and setting it into map */
				for (Attributes oldattrName : queryResults) {
					if (oldattrName != null) {
						oldAttrMap.put(
								SCIMServiceHelper.USER_NAME,
								(oldattrName.get(SCIMServiceHelper.CN) != null) ? (String) oldattrName.get(
										SCIMServiceHelper.CN).get() : "");
						oldAttrMap.put(
								SCIMServiceHelper.GIVENNAME,
								(oldattrName.get(SCIMServiceHelper.GIVENNAME) != null) ? (String) oldattrName.get(
										SCIMServiceHelper.GIVENNAME).get() : "");
						oldAttrMap.put(
								SCIMServiceHelper.FAMILY_NAME,
								(oldattrName.get(SCIMServiceHelper.SN) != null) ? (String) oldattrName.get(
										SCIMServiceHelper.SN).get() : "");
						oldAttrMap.put(
								SCIMServiceHelper.DISPLAY_NAME,
								(oldattrName.get(SCIMServiceHelper.PREFERRED_NAME) != null) ? (String) oldattrName.get(
										SCIMServiceHelper.PREFERRED_NAME).get() : "");
						oldAttrMap.put(
								SCIMServiceHelper.ACCOUNT_ID,
								(oldattrName.get(SCIMServiceHelper.ACCOUNTID) != null) ? (String) oldattrName.get(
										SCIMServiceHelper.ACCOUNTID).get() : "");
						oldAttrMap.put(SCIMServiceHelper.EMAIL_ADDRESS,
								(oldattrName.get(SCIMServiceHelper.EMAIL_ADDRESS) != null) ? (String) oldattrName
										.get(SCIMServiceHelper.EMAIL_ADDRESS).get() : "");
						
					}
				}
			}
			/** Getting the CustomProperties from OKTA and converting to map */
			if (user != null) {
				JsonNode json = user.getCustomPropertiesMap().get(appSchemaName);
				ObjectMapper mapper = new ObjectMapper();
				HashMap<String, String> newOktaAttrMap = new HashMap<String, String>();
				if (json != null) {
					try {
						newOktaAttrMap = mapper.convertValue(json, new TypeReference<HashMap<String, String>>() {
						});
					} catch (IllegalArgumentException e) {
						LOGGER.error("IllegalArgumentException occured in converting customProperties");
						throw e;
					}
				}
				/** Converting the countryCode to 3 digit to compare with IDV */
				if (newOktaAttrMap.get(SCIMServiceHelper.COUNTRY_CODE) != null) {
					String country = "";
					country = scimUtil.convert2to3DigitCountryCode(newOktaAttrMap.get(SCIMServiceHelper.COUNTRY_CODE));
					LOGGER.debug("country value after conversion is " + country);
					newOktaAttrMap.put(SCIMServiceHelper.COUNTRY_CODE, country);
				}
				/** Converting the locale to 2 digit to compare with IDV */
				if (newOktaAttrMap.get(SCIMServiceHelper.LOCALE) != null) {
					String language = "";
					language = (String) (newOktaAttrMap.get(SCIMServiceHelper.LOCALE).substring(0, 2));
					LOGGER.debug("language value after conversion is " + language);
					newOktaAttrMap.put(SCIMServiceHelper.LOCALE, language);
				}
				/** Setting the basic attributes to the new Okta map to compare */
				if (user.getEmails() != null && ldapUserCore.get(SCIMServiceHelper.PRIMARY_EMAIL) != null) {
					Attribute primaryEmailAttr = new BasicAttribute(ldapUserCore.get(SCIMServiceHelper.PRIMARY_EMAIL));
					Object[] emails = user.getEmails().toArray();
					for (int i = 0; i < emails.length; i++) {
						Email email = (Email) emails[i];
						if (email != null && email.isPrimary()) {
							if (email.getValue() != null && email.getValue().equals(SCIMServiceHelper.DEFAULT_EMAIL)) {
								LOGGER.debug("User " + user.getUserName()
										+ " is having a default Email address,Comparing with blank value in IDV");
								primaryEmailAttr.add("");
							} else {
								primaryEmailAttr.add(email.getValue());
							}
						}
					}
					/** Setting the Email attribute to the new Okta map to compare */
					newOktaAttrMap.put(SCIMServiceHelper.EMAIL_ADDRESS, primaryEmailAttr.get().toString());
				}
				/** Setting the userName attribute to the new Okta map to compare */
				newOktaAttrMap.put(SCIMServiceHelper.USER_NAME, user.getUserName());
				/** Setting the givenname attribute to the new Okta map to compare */
				newOktaAttrMap.put(SCIMServiceHelper.GIVENNAME, user.getName().getFirstName());
				/** Setting the familyName attribute to the new Okta map to compare */
				newOktaAttrMap.put(SCIMServiceHelper.FAMILY_NAME, user.getName().getLastName());
				/** Setting the mobilePhone attribute to the new Okta map to compare */
				if (user.getPhoneNumbers() != null) {
					Object[] phoneNums = user.getPhoneNumbers().toArray();
					String number = "";
					for (int i = 0; i < phoneNums.length; i++) {
						LOGGER.info("iteration" + i);
						PhoneNumber num = (PhoneNumber) phoneNums[i];
						if (num.isPrimary()) {
							number = num.getValue();
							break;
						}
					}
					if (number != null && !number.isEmpty()) {
						Map<String, String> phoneMap = scimUtil.splitPhoneNumber(number);
						newOktaAttrMap
								.put(SCIMServiceHelper.MOBILE_PHONE, phoneMap.get(SCIMServiceHelper.MOBILE_PHONE));
						newOktaAttrMap.put(SCIMServiceHelper.SMS_UNFORMATTED,
								phoneMap.get(SCIMServiceHelper.MOBILE_PHONE));
					}
				} else {
					LOGGER.debug("Empty mobile number");
					newOktaAttrMap.put(SCIMServiceHelper.MOBILE_PHONE, "");
					newOktaAttrMap.put(SCIMServiceHelper.SMS_UNFORMATTED, "");
				}

				/** Iterating the newOktaAttrMap with oldAttrs and finding the differences */
				for (String attrName : newOktaAttrMap.keySet()) {
					if (oldAttrMap.get(attrName) != null && newOktaAttrMap.get(attrName) != null) {
						if ((oldAttrMap.get(attrName).equals(newOktaAttrMap.get(attrName)))) {
							diffAttrMap.put(attrName, newOktaAttrMap.get(attrName));
						}
					}
				}
				/** Comparing New Attributes map and Difference Attr Map Size */
				if (diffAttrMap.size() == newOktaAttrMap.size()) {
					resultFlag = false;
					LOGGER.info("User " + user.getUserName()
							+ " is having same attribute values,setting resultFlag as " + resultFlag);
				}
				if (diffAttrMap.size() < newOktaAttrMap.size()) {
					resultFlag = true;
					LOGGER.info("User " + user.getUserName()
							+ " is having different attribute values,setting resultFlag as " + resultFlag);
				}
			} else {
				LOGGER.error("User Object is NULL");

			}
		} catch (NamingException e) {
			LOGGER.error("NamingException occured in compareUserObjects");
			throw e;
		} catch (NoSuchElementException e) {
			LOGGER.error("NoSuchElementException occured in compareUserObjects");
			throw e;
		} catch (Exception e) {
			LOGGER.error("General Exception occured in compareUserObjects");
			throw e;
		}

		LOGGER.info(methodName + " Exiting");
		return resultFlag;
	}

}
