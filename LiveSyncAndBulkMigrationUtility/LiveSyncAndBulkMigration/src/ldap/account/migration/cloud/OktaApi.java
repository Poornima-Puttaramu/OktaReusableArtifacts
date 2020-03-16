
package ldap.account.migration.cloud;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;

import ldap.account.migration.bean.RestResponse;
import lds.account.migration.constants.ConstantUtils;

import org.apache.http.client.utils.URIBuilder;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * The OktaApi class is a customized API class which connects to OKTA
 * 
 * @author PWC-AC
 */
public class OktaApi {
	private ObjectMapper mapper = new ObjectMapper();
	private static final Logger LOG = Logger.getLogger(OktaApi.class.getName());

	/**
	 * This method is called from createCloudUser method in CloudUser class This method sets method name as "POST" and
	 * calls the callToOkta method
	 * 
	 * @param builder
	 *            - URIBuilder - OKTA's URL to be called
	 * @param json
	 *            - String - User object as String
	 * @param paramMap
	 *            - Map - Parameter Map
	 * @return - response -RestResponse
	 * @throws URISyntaxException
	 * @throws IOException
	 */
	public RestResponse postToOkta(URIBuilder builder, String json, String apiKey) throws URISyntaxException,
			IOException, Exception {
		LOG.debug("Calling callToOkta method from postToOkta method");
		return callToOkta("POST", builder, json, apiKey);
	}

	/**
	 * This method sets method name as "PUT" and calls the callToOkta method
	 * 
	 * @param builder
	 *            - URIBuilder - OKTA's URL to be called
	 * @param json
	 *            - String - User object as String
	 * @param apiKey
	 *            - String - apiKey
	 * @return response
	 * @throws URISyntaxException
	 * @throws IOException
	 */
	public RestResponse putToOkta(URIBuilder builder, String json, String apiKey) throws URISyntaxException,
			IOException, Exception {
		return callToOkta("PUT", builder, json, apiKey);
	}

	/**
	 * This method sets method name as "GET" and calls the callToOkta method
	 * 
	 * @param uri
	 *            - URIBuilder - OKTA's URL to be called
	 * @param paramMap
	 *            - Map - Parameters to be passed
	 * @return response -RestResponse
	 * @throws URISyntaxException
	 * @throws IOException
	 */
	public RestResponse getToOkta(URIBuilder uri, String apiKey) throws URISyntaxException, IOException, Exception {
		LOG.debug("Calling callToOkta method from getToOkta method");
		return callToOkta("GET", uri, null, apiKey);
	}

	/**
	 * This method sets method name as "DELETE" and calls the callToOkta method
	 * 
	 * @param uri
	 *            - URIBuilder - OKTA's URL to be called
	 * @return response - RestResponse
	 * @throws URISyntaxException
	 * @throws
	 */
	public RestResponse deleteToOkta(URIBuilder uri, String apiKey) throws URISyntaxException, IOException, Exception {
		LOG.debug("Calling callToOkta method from deleteToOkta method");
		return callToOkta("DELETE", uri, null, apiKey);
	}

	/**
	 * This method establishes the connection to OKTA
	 * 
	 * @param method
	 *            - String - Method to be called in the Request
	 * @param uri
	 *            - URIBuilder - OKTA's URL to be called
	 * @param json
	 *            - String - User object as String
	 * @param paramMap
	 * @return response - RestResponse
	 * @throws Exception
	 */
	private RestResponse callToOkta(String method, URIBuilder uri, String json, String apiKey)
			throws URISyntaxException, IOException, Exception {
		LOG.debug("Entering method callToOkta");
		HttpURLConnection conn = null;
		JsonNode results = null;
		RestResponse retVal = null;
		OutputStream os = null;
		try {
			conn = (HttpURLConnection) uri.build().toURL().openConnection();
			conn.setConnectTimeout(5000);
			conn.setReadTimeout(5000);
			conn.setRequestMethod(method);
			conn.setRequestProperty("Accept", ConstantUtils.APPLICATION_JSON);
			conn.setRequestProperty("Content-Type", ConstantUtils.APPLICATION_JSON);
			conn.setRequestProperty("Cache-control", ConstantUtils.CACHE_CONTROL);
			conn.setRequestProperty("Authorization", apiKey);
			if (json != null) {
				conn.setDoOutput(true);
				os = conn.getOutputStream();
				os.write(json.getBytes());
				os.flush();
			}
			switch (conn.getResponseCode()) {
			case HttpURLConnection.HTTP_CREATED:
			case HttpURLConnection.HTTP_OK:
				results = getContent(conn.getInputStream());
			case HttpURLConnection.HTTP_NO_CONTENT:
				break;
			case 429:
				conn.disconnect();
				return pauseAndTryAgain(conn.getHeaderFieldLong("x-rate-limit-reset", 0L), method, uri, json, apiKey);
			default:
				results = getContent(conn.getErrorStream());

			}
			retVal = new RestResponse(conn.getResponseCode(), conn.getResponseMessage(), results);
		} catch (URISyntaxException e) {
			LOG.error("URISyntaxException occured in callToOkta ::" + e.getMessage());
			throw e;
		} catch (IOException e) {
			LOG.error("IOException occured in callToOkta ::" + e.getMessage());
			throw e;
		} catch (Exception e) {
			LOG.error("Exception occured in callToOkta ::" + e.getMessage());
			throw e;
		} finally {
			try {
				if (conn != null) {
					conn.disconnect();
				}
			} catch (Exception ex) {
				LOG.error("Exception in closing the Connection " + ex.getMessage());
			}
			try {
				if (os != null) {
					os.close();
				}

			} catch (Exception ex) {
				LOG.error("Exception in closing the output stream " + ex.getMessage());
			}
		}

		LOG.debug("Exiting method callToOkta");
		return retVal;

	}

	/**
	 * This method writes the Connection results as the JsonNode.
	 * 
	 * @param stream
	 *            - InputStream
	 * @return result - JsonNode
	 * @throws Exception
	 */
	private JsonNode getContent(InputStream stream) throws Exception {
		LOG.debug("Entering method getContent");
		BufferedReader in;
		JsonNode result = null;
		try {
			in = new BufferedReader(new InputStreamReader(stream));

			String line;
			StringBuffer content = new StringBuffer();
			while ((line = in.readLine()) != null) {
				content.append(line);
			}
			in.close();
			if (content != null) {
				String wellFormedJson = com.google.json.JsonSanitizer.sanitize(content.toString());
				result = mapper.readValue(wellFormedJson, JsonNode.class);
			}
		} catch (JsonParseException e) {
			LOG.error("JsonParseException occured in getContent" + e.getMessage());
			throw e;
		} catch (JsonMappingException e) {
			LOG.error("JsonMappingException occured in getContent" + e.getMessage());
			throw e;
		} catch (IOException e) {
			LOG.error("IOException occured in getContent" + e.getMessage());
			throw e;
		} catch (Exception e) {
			LOG.error("Exception occured in getContent" + e.getMessage());
			throw e;
		} finally {
			try {
				if (stream != null) {
					stream.close();
				}
			} catch (Exception e) {
				LOG.error("Exception in closing InputStream in getContent method" + e.getMessage());
			}
		}
		LOG.debug("Exiting method getContent");
		return result;
	}

	/**
	 * This method tries to reconnect to OKTA by returning callToOkta method
	 * 
	 * @param limitReset
	 *            - long
	 * @param method
	 *            - String - Method to be called in the Request
	 * @param uri
	 *            - URIBuilder - OKTA's URL to be called
	 * @param json
	 *            - String - User object as String
	 * @return response - RestResponse
	 * @throws URISyntaxException
	 * @throws IOException
	 */
	private RestResponse pauseAndTryAgain(long limitReset, String method, URIBuilder uri, String json, String apiKey)
			throws URISyntaxException, IOException, Exception {
		LOG.debug("Entering method pauseAndTryAgain");
		long timeToSleep = Math.abs(limitReset - (System.currentTimeMillis() / 1000)) + 5;
		try {
			Thread.sleep(timeToSleep);
		} catch (InterruptedException e) {
			LOG.error("Exception occured in pauseAndTryAgain " + e.getMessage());
			return null;
		}
		LOG.debug("Exiting method pauseAndTryAgain");
		return callToOkta(method, uri, json, apiKey);
	}

}
