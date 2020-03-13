package com.lambda.okta.util;

import java.io.IOException;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.lambda.okta.constants.OktaConstantUtils;
import com.lambda.okta.restapi.HttpPostWithEntity;

/**
 *
 */
public class APIUtil extends HttpPost {
	/** Classname variable to store name of the class */
	private static final String CLASSNAME = APIUtil.class
			.getSimpleName();

	/** Logger to log the results */
	private static final Logger LOGGER = LogManager.getLogger(CLASSNAME);

  private static CloseableHttpClient client = HttpClientBuilder.create().build();

	/**
	 * This method sends out POST request to the REST API
	 * 
	 * @param hostURL
	 * @param requestBody
	 * @param token
	 *
	 * @param endpoint    API endpoint
	 * @param inputString API payload
	 * @param authzToken  IDCS auth token
	 * @return JSONObject with response details
	 * @throws Exception 
	 */
	public static CloseableHttpResponse sendPostRequest(String hostURL, String requestBody, String token) 
			throws Exception {
		final StringBuilder stringBuilder = new StringBuilder();
		final String methodName = "/sendPostRequest";
		LOGGER.info("Entering "
				+ stringBuilder.delete(0, stringBuilder.length()).append(CLASSNAME).append(methodName).toString());
		HttpPostWithEntity httpPost = new HttpPostWithEntity(hostURL);
		if (StringUtils.isNotEmpty(requestBody)) {
			StringEntity body = new StringEntity(requestBody, ContentType.APPLICATION_JSON);
			httpPost.setEntity(body);
		}
		LOGGER.info("Entering "
				+ stringBuilder.delete(0, stringBuilder.length()).append(CLASSNAME)
				.append(methodName).toString());
		return callAPI(httpPost, token);
	}

	/**
	   * This method sends out a request to the REST API
	   * @param httpRequest
	   * @param token
	   *
	   * @return response
	 * @throws Exception 
	   */
	  public static CloseableHttpResponse callAPI(HttpRequestBase httpRequest, String token)
	    throws Exception {
	    final StringBuilder stringBuilder = new StringBuilder();
	    final String methodName = "/callAPI";
	    
	    LOGGER.info("Entering "
	      + stringBuilder.delete(0, stringBuilder.length())
	      .append(CLASSNAME).append(methodName).toString());
	    CloseableHttpResponse response = null;
	   try {
	      httpRequest.addHeader(OktaConstantUtils.CONTENT_TYPE,
	        OktaConstantUtils.APPL_JSON);
	      httpRequest.addHeader(OktaConstantUtils.AUTHORIZATION, token);
	      httpRequest.addHeader(OktaConstantUtils.CHARSET, OktaConstantUtils.UTF);
	      response = client.execute(httpRequest);
	      LOGGER.info("Exiting "
					+ stringBuilder.delete(0, stringBuilder.length()).append(CLASSNAME)
					.append(methodName).toString());
	      return response;
	    } 
	   
	   catch (Exception genExp) {
	    	LOGGER.error("Exception "
	    		      + stringBuilder.delete(0, stringBuilder.length())
	    		      .append(CLASSNAME).append(genExp.getMessage()).toString());
	    	throw genExp;
	    } 
	  }
	  
	  public static void closeClientConnection() throws IOException {
		  if (null != client) {
			  client.close();
		  }
	  }
}