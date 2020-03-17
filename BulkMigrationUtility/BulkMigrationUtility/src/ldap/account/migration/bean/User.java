
package ldap.account.migration.bean;

import java.util.Objects;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;

/**
 * The User class is used to create the User Object
 * 
 * @author PWC-AC
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class User {
	private static final Logger LOG = Logger.getLogger(User.class.getName());
	@JsonIgnore
	private boolean loginDisabled = false;
	private JsonNode data;
	private ObjectMapper mapper;

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof User))
			return false;
		User user = (User) o;
		return Objects.equals(data, user.data);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.data);
	}

	/**
	 * Constructor
	 * 
	 * @param json
	 * @param loginDisabled
	 * @throws Exception
	 */
	public User(String json, boolean loginDisabled) throws Exception {
		LOG.debug("User constructor with loginDisabled field");
		this.loginDisabled = loginDisabled;
		transformJson(json, true);
	}


	/**
	 * Here we pull in the user data as a string and convert it into a JSON tree object
	 * 
	 * @param json
	 *            - String
	 * @throws Exception
	 */
	private void transformJson(String json, boolean create) throws Exception {

		mapper = new ObjectMapper();
		try {
			this.data = mapper.readTree(json);
		} catch (Exception e) {
			LOG.error("Error in the reading the User object");
			throw e;
		}
		
	}

	/**
	 * This method returns the user object as a JSON string. If there is an error while processing the JSON it will
	 * return null.
	 * 
	 * @return
	 * @throws Exception
	 */
	public String getUserAsString() throws Exception {
		String retVal = null;
		try {
			retVal = this.mapper.writeValueAsString(this.data);
		} catch (Exception e) {
			LOG.error("Error in converting the user as String");
			throw e;
		}
		return retVal;
	}

	/**
	 * This method attempts to retrieve a provided attribute from the user object and returns its value. If the key does
	 * not exist, null is returned.
	 * 
	 * @param attribute
	 * @return
	 */
	public String getProfileAttr(String attribute) {
		JsonNode node = this.data.get("profile");
		// Perform check if node is null before accessing text
		if (node != null) {
			node = node.get(attribute);
			if(node != null) {
				return node.textValue();
			}
		}
		LOG.debug("Failed to get profile attribute " + attribute);
		return null;
	}

	/**
	 * Returns whether login is disabled for the user
	 * 
	 * @return
	 */
	public boolean isLoginDisabled() {
		return this.loginDisabled;
	}

	/**
	 * This method modifies the value for a specified attribute. Throws NullPointerException if attribute or value is
	 * null.
	 * 
	 * @param attribute
	 * @param value
	 */
	public void setProfileAttr(String attribute, String value) {
		if (attribute == null || value == null) {
			throw new IllegalArgumentException("Either attribute or value is null");
		}
		// Even if the attribute doesn't exist on the profile node, this should create it
		((ObjectNode) this.data.get("profile")).put(attribute, new TextNode(value));
	}

	/**
	 * This method helps to remove the unwanted Attributes from JSON
	 * 
	 * @param attribute
	 *            -Attribute to be removed from Profile Object
	 */
	public void removeProfileAttr(String attribute) {
		if (attribute == null) {
			throw new IllegalArgumentException("Attribute is null");
		}
		ObjectNode node = (ObjectNode) this.data.get("profile");
		if(node != null) {
			node.remove(attribute);
		}
	}

	/**
	 * This method checks to see that login, and password values are not null and not empty
	 * strings. 
	 * More attributes can be added based on requirement
	 * 
	 * @return
	 */
	public String validate() {
		LOG.debug("Validating the profile and credentials");
		String missing = "";
		
		if (!isValidString(getProfileAttr("login"))) {
			missing += " login";
		}
		if (!isValidString(this.data.get("credentials").get("password").get("value").textValue())) {
			missing += " password";
		}
		return missing;
	}

	/**
	 * This method returns whether the given string is not null and not an empty string
	 * 
	 * @param str
	 * @return
	 */
	private boolean isValidString(String str) {
		LOG.debug("Checking is Valid String or not for Strings under the scope of validation ");
		return str != null && !str.isEmpty();
	}

}
