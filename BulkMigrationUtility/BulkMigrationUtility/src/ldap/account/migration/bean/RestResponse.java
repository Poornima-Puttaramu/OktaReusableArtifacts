
package ldap.account.migration.bean;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * The RestResponse is a Bean Class.
 * @author Church of Jesus Christ
 */
public class RestResponse {
	private int responseCode;
	private String responseMessage;
	private JsonNode responseBody;

	public RestResponse(int responseCode, String responseMessage, JsonNode responseBody) {
		this.responseCode = responseCode;
		this.responseMessage = responseMessage;
		this.responseBody = responseBody;
	}

	public int getResponseCode() {
		return responseCode;
	}

	public void setResponseCode(int responseCode) {
		this.responseCode = responseCode;
	}

	public String getResponseMessage() {
		return responseMessage;
	}

	public void setResponseMessage(String responseMessage) {
		this.responseMessage = responseMessage;
	}

	public JsonNode getResponseBody() {
		return responseBody;
	}

	public void setResponseBody(JsonNode responseBody) {
		this.responseBody = responseBody;
	}

}
