package ldap.account.testOkta;

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
		
		create();

	}
	/**
	 * Method to test Create User
	 * @throws Exception
	 */
	public static void create() throws Exception {
		LOG.info("Entering create");
		OktaMigrator migrator = new OktaMigrator();

		String data = "{\"profile\":{\"countryCode\":\"IN\",\"userType\":\"\",\"displayName\":\"Test User6\",\"email\":\"testuser6@gmail.com\",\"employeeNumber\":\"\",\"firstName\":\"Test\",\"lastName\":\"User6\",\"login\":\"testuser06\",\"mobilePhone\":\"+919888888888\",\"preferredLanguage\":\"en\",\"secondEmail\":\"\",\"credentials\":{\"password\":{\"value\":\"Oktatest66\"}}}";
        String results = migrator.createUser(apiUrl, apiKey, data, false);
	    LOG.debug(results);
	    LOG.info("Exiting create");
	}
}
