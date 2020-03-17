
package ldap.account.migration.bean;

import java.util.Objects;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;

/**
 * The group class is used to create the group Object
 * 
 * @author PWC-AC
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Group {
	private static final Logger LOG = Logger.getLogger(Group.class.getName());
	private JsonNode data;
	private ObjectMapper mapper;

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof Group))
			return false;
		Group group = (Group) o;
		return Objects.equals(data, group.data);
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
	public Group(String json) throws Exception {
		// Here we pull in the group data as a string and convert it into a JSON tree object
		mapper = new ObjectMapper();
		try {
			this.data = mapper.readTree(json);
		} catch (Exception e) {
			LOG.error("Error in the reading the Group object");
			throw e;
		}
	}

	/**
	 * This method returns the group object as a JSON string. If there is an error while processing the JSON it will
	 * return null.
	 * 
	 * @return
	 * @throws Exception 
	 */
	public String getGroupAsString() throws Exception {
		String dataString="";
		try {
			dataString= this.mapper.writeValueAsString(this.data);
		} catch (Exception e) {
			LOG.error("Error in converting the group as String");
			throw e;
		}
		return dataString;
	}

	/**
	 * This method attempts to retrieve a provided attribute from the group object and returns its value. If the key
	 * does not exist, null is returned.
	 * 
	 * @param attribute
	 * @return
	 */
	public String getProfileAttr(String attribute) {
		JsonNode node = this.data.get("profile").get(attribute);
		// Perform check if node is null before accessing text
		if (node != null) {
			return node.textValue();
		} else {
			LOG.warn("Failed to get profile attribute " + attribute);
			return null;
		}
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
		((ObjectNode) this.data.get("profile")).put(attribute, new TextNode(value));
	}

}
