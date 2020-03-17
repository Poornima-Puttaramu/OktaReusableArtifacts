package lds.account.testOkta;

import org.apache.log4j.Logger;

import ldap.account.migration.cloud.OktaMigrator;
/**
 * Test Class to test the Utility
 *
 */
public class TestUtils {
	
	
	private static  final Logger LOG = Logger.getLogger(TestUtils.class.getName());
	private static String apiUrl = "URL/api/v1";
	private static String apiKey = "SSWS"+"{token}";
	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		 /**Please note this is a test Class/method only */
		//createGroup();
		//addUsersToGroup();
		//removeUserFromGroup();
		//deleteGroup();
		update();
		//suspendUser();
		//unSuspendUser();
		//deActivateUser();
		//deleteUser();

	}

	/**
	 * Method to test Update User
	 * @throws Exception
	 */
	public static void update() throws Exception {
		LOG.info("Entering update");
		OktaMigrator migrator = new OktaMigrator();
		String oktaId = "00uov482mnuTLwMqI0h7";
		String data = "{\"credentials\":{\"password\":{\"value\":\"password7\"}}}";
        String results = migrator.updateUser(apiUrl, apiKey, data, oktaId);
	    LOG.debug(results);
	    LOG.info("Exiting update");
	}
	/**
	 * Method to test suspend User
	 * @throws Exception
	 */
	public static void suspendUser() throws Exception {
		LOG.info("Entering suspend");
		OktaMigrator migrator = new OktaMigrator();
		String data ="00uog5k8auxkHIXSk0h7";
				
        String results = migrator.suspendUser(apiUrl, apiKey, data);
	    LOG.debug(results);
	    LOG.info("Exiting suspend");
	}
	/**
	 * Method to test Unsuspend User
	 * @throws Exception
	 */
	public static void unSuspendUser() throws Exception {
		LOG.info("Entering unsuspend");
		OktaMigrator migrator = new OktaMigrator();
		String data ="00uog5k8auxkHIXSk0h7";
				
        String results = migrator.unSuspendUser(apiUrl, apiKey, data);
	    LOG.debug(results);
	    LOG.info("Exiting unsuspend");
	}
	
	/**
	 * Method to test Activate User
	 * @throws Exception
	 */
	public static void activateUser() throws Exception {
		LOG.info("Entering activate");
		OktaMigrator migrator = new OktaMigrator();
		String oktaId = "00unu8k0x4yCjvXe20h7";
				
        String results = migrator.activateUser(apiUrl, apiKey, oktaId);
	    LOG.debug(results);
	    LOG.info("Exiting activate");
	}
	
	/**
	 * Method to test Delete User
	 * @throws Exception
	 */
	public static void deleteUser() throws Exception {
		LOG.info("Entering delete");
		OktaMigrator migrator = new OktaMigrator();
		String oktaId = "/00uocb4vjrESanRHG0h7";
				
        String results = migrator.deleteUserByOktaId(apiUrl, apiKey, oktaId);
	    LOG.debug(results);
	    LOG.info("Exiting delete");
	}
	
	/**
	 * Method to test Delete User by Login
	 * @throws Exception
	 */
	public static void deleteUserByLogin() throws Exception {
		LOG.info("Entering deleteUserByLogin");
		OktaMigrator migrator = new OktaMigrator();
		String login = "actestuser128";
				
        String results = migrator.deleteUserByLogin(apiUrl, apiKey, login);
	    LOG.debug(results);
	    LOG.info("Exiting deleteUserByLogin");
	}
	
	/**
	 * Method to test Deactivate User
	 * @throws Exception
	 */
	public static void deActivateUser() throws Exception {
		LOG.info("Entering deActivate");
		OktaMigrator migrator = new OktaMigrator();
		String oktaId = "00uog5k8auxkHIXSk0h7";
		
        String results = migrator.deActivateUser(apiUrl, apiKey, oktaId);
	    LOG.debug(results);
	    LOG.info("Exiting deActivate");
	}
	
	/**
	 * Method to test Create Group
	 * @throws Exception
	 */
	public static void createGroup() throws Exception {
		LOG.info("Entering createGroup");
		OktaMigrator migrator = new OktaMigrator();
		String data = "{ " + 
				"\"profile\": { " + 
				"\"name\": \"ACTestGroup6\", " + 
				"\"description\": \"This is a test group\" " + 
				"} " + 
				"}";
        String results = migrator.createGroup(apiUrl, apiKey, data);
	    LOG.debug(results);
	    LOG.info("Exiting createGroup");
	}
	
	/**
	 * Method to test Add User to Group
	 * @throws Exception
	 */
	public static void addUsersToGroup() throws Exception {
		LOG.info("Entering addUsersToGroup");
		OktaMigrator migrator = new OktaMigrator();
		String oktaId = "00unuxo8afnWmTkVn0h7";
		String groupName="ACTestGroup6";
		
        String results = migrator.addUserToGroup(apiUrl, apiKey, groupName,oktaId);
	    LOG.debug(results);
	    LOG.info("Exiting addUsersToGroup");
	}
	
	/**
	 * Method to test Remove User from Group
	 * @throws Exception
	 */
	public static void removeUserFromGroup() throws Exception {
		LOG.info("Entering removeUserFromGroup");
		OktaMigrator migrator = new OktaMigrator();
		String oktaId = "00uocb4m52j3L9gZQ0h7";
		String groupName="ACTestGroup6";
		
        String results = migrator.removeUserFromGroup(apiUrl, apiKey, groupName,oktaId);
	    LOG.debug(results);
	    LOG.info("Exiting removeUserFromGroup");
	}
	
	/**
	 * Method to test Delete Group
	 * @throws Exception
	 */
	public static void deleteGroup() throws Exception {
		LOG.info("Entering deleteGroup");
		OktaMigrator migrator = new OktaMigrator();
		String groupName="ACTestGroup6";
		
        String results = migrator.deleteGroup(apiUrl, apiKey, groupName);
	    LOG.debug(results);
	    LOG.info("Exiting deleteGroup");
	}
	
	/**
	 * Method to test Assign Application to User
	 * @throws Exception
	 */
	public static void assignToApp(String oktaId, String appId){
		LOG.info("Entering assignToApp");
		OktaMigrator migrator = new OktaMigrator();
		String results = migrator.assignToApp(apiUrl, apiKey, appId, oktaId);
		LOG.debug(results);
		LOG.info("Exiting assignToApp");
	}
	
}
