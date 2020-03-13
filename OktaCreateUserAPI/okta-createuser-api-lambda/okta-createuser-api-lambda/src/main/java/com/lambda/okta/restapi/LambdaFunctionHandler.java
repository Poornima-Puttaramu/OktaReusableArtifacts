/**
 * Copyright information
 */

package com.lambda.okta.restapi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.SystemUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.lambda.okta.constants.OktaConstantUtils;
import com.lambda.okta.exception.InternalException;
import com.okta.jwt.AccessTokenVerifier;
import com.okta.jwt.Jwt;
import com.okta.jwt.JwtVerifiers;

/**
 * The LambdaFunctionHandler class is the entry point for performing create user
 * functionality. This class gets invoked from an AWS Lambda function.
 */
public class LambdaFunctionHandler implements RequestStreamHandler {

	/**
	 * Default constructor
	 */
	public LambdaFunctionHandler() {
		super();
	}

	/** Classname variable to store name of the class */
	private static final String CLASSNAME = LambdaFunctionHandler.class.getSimpleName();

	/** Logger to log the results */
	private static final Logger LOGGER = LogManager.getLogger(CLASSNAME);

	/**
	 * Handles a Lambda Function request
	 * 
	 * @param input   The Lambda Function input stream
	 * @param output  The Lambda function output stream
	 * @param context The Lambda execution environment context object.
	 * @throws IOException
	 */
	public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context) throws IOException {

		final StringBuilder stringBuilder = new StringBuilder();
		final String methodName = "/handleRequest";
		LOGGER.info("Entering "
				+ stringBuilder.delete(0, stringBuilder.length()).append(CLASSNAME).append(methodName).toString());
		BufferedReader bufferReader = null;
		InputStreamReader inputReader = null;
		OutputStreamWriter writer = null;
		JSONObject apiResponse = null;
		CloseableHttpClient client = null;
		CloseableHttpResponse response = null;
		boolean isValidToken = true;
		try {
			apiResponse = new JSONObject();

			JSONParser parser = new JSONParser();
			inputReader = new InputStreamReader(inputStream);
			bufferReader = new BufferedReader(inputReader);
			JSONObject event = (JSONObject) parser.parse(bufferReader);
			String oktaToken = "";
			JSONObject oktaJsonBody = null;
			JSONObject profileObj = new JSONObject();
			if (null != event && !event.isEmpty()) {
				JSONObject params = (JSONObject) event.get(OktaConstantUtils.PARAMS);
				if (null != params && !params.isEmpty()) {
					JSONObject headers = (JSONObject) params.get(OktaConstantUtils.HEADER);
					if (null != headers && !headers.isEmpty()) {
						oktaToken = (String) headers.get(OktaConstantUtils.AUTHORIZATION);
						String tokenType = (String) headers.get(OktaConstantUtils.TOKEN_TYPE);
						if (null != tokenType && !tokenType.isEmpty()) {
							if (tokenType.equalsIgnoreCase(OktaConstantUtils.TOKEN_TYPE_ADMIN)) {
								oktaToken = OktaConstantUtils.SSWS_ADMIN.concat(oktaToken);
							} else if (tokenType.equalsIgnoreCase(OktaConstantUtils.TOKEN_TYPE_ACCESS)) {
								isValidToken = isTokenValid(oktaToken);
								oktaToken = OktaConstantUtils.BEARER.concat(oktaToken);
							} else {
								LOGGER.info(stringBuilder.delete(0, stringBuilder.length())
										.append(CLASSNAME).append(methodName)
										.append(" Token is invalid ").toString());
								Map<String, String> failedResponse = new HashMap<String, String>();
								failedResponse.put(OktaConstantUtils.RESPONSE_CODE,
										OktaConstantUtils.INVALID_TOKEN_ERROR_CODE);
								failedResponse.put(OktaConstantUtils.RESPONSE_MSG,
										OktaConstantUtils.INVALID_TOKEN_ERROR_MSG);
								apiResponse = new JSONObject(failedResponse);
								LOGGER.info(stringBuilder.delete(0, stringBuilder.length())
										.append(CLASSNAME).append(methodName)
										.append(" apiResponse ").append(apiResponse).toString());
								writer = new OutputStreamWriter(outputStream,
										StandardCharsets.UTF_8);
								// writing the response back the calling application
								writer.write(apiResponse.toString());
							}
						} else {
							isValidToken = isTokenValid(oktaToken);
							oktaToken = OktaConstantUtils.BEARER.concat(oktaToken);
						}
					}
				oktaJsonBody = (JSONObject) event.get(OktaConstantUtils.BODY_JSON);
			}
			LOGGER.info(stringBuilder.delete(0, stringBuilder.length())
					.append(CLASSNAME).append(methodName)
					.append(" isValidToken ").append(isValidToken).toString());
			if(isValidToken) {
			profileObj.put(OktaConstantUtils.PROFILE, oktaJsonBody);

			OktaCreateUser oktaCreateUser = new OktaCreateUser();
			LOGGER.debug(stringBuilder.delete(0, stringBuilder.length()).append(CLASSNAME).append(methodName)
					.append(" Before calling create user ").toString());

			// Calling create user functionality
			apiResponse = oktaCreateUser.createUser(profileObj, oktaToken);
			LOGGER.debug(stringBuilder.delete(0, stringBuilder.length()).append(CLASSNAME).append(methodName)
					.append(" After calling create user ").toString());
			LOGGER.debug(stringBuilder.delete(0, stringBuilder.length()).append(CLASSNAME).append(methodName)
					.append(" responseObject ").append(apiResponse).toString());
			writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
			// writing the response back the calling application
			writer.write(apiResponse.toString());
			}
			else {
				LOGGER.info(stringBuilder.delete(0, stringBuilder.length())
						.append(CLASSNAME).append(methodName)
						.append(" Token is invalid ").toString());
				Map<String, String> failedResponse = new HashMap<String, String>();
				failedResponse.put(OktaConstantUtils.RESPONSE_CODE,
						OktaConstantUtils.INVALID_TOKEN_ERROR_CODE);
				failedResponse.put(OktaConstantUtils.RESPONSE_MSG,
						OktaConstantUtils.INVALID_TOKEN_ERROR_MSG);
				apiResponse = new JSONObject(failedResponse);
				LOGGER.info(stringBuilder.delete(0, stringBuilder.length())
						.append(CLASSNAME).append(methodName)
						.append(" apiResponse ").append(apiResponse).toString());
				writer = new OutputStreamWriter(outputStream,
						StandardCharsets.UTF_8);
				// writing the response back the calling application
				writer.write(apiResponse.toString());
			}
			}
		} catch (IOException ioException) {
			LOGGER.error(stringBuilder.delete(0, stringBuilder.length()).append(CLASSNAME).append(methodName)
					.append("IOException :").append(ioException.getMessage()));
			Map<String, String> failedResponse = new HashMap<String, String>();
			failedResponse.put(OktaConstantUtils.RESPONSE_CODE, OktaConstantUtils.INTERNAL_ERROR_CODE);
			failedResponse.put(OktaConstantUtils.RESPONSE_MSG, OktaConstantUtils.INTERNAL_ERROR_MESSAGE);
			apiResponse = new JSONObject(failedResponse);
			LOGGER.error(stringBuilder.delete(0, stringBuilder.length()).append(CLASSNAME).append(methodName)
					.append(" apiResponse ").append(apiResponse).toString());
			writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
			// writing the response back the calling application
			writer.write(apiResponse.toString());
		} catch (InternalException internalExp) {
			LOGGER.error(stringBuilder.delete(0, stringBuilder.length()).append(CLASSNAME).append(methodName)
					.append("InternalException :").append(internalExp.getErrMsg()));
			Map<String, String> failedResponse = new HashMap<String, String>();
			failedResponse.put(OktaConstantUtils.RESPONSE_CODE, internalExp.getErrCode());
			failedResponse.put(OktaConstantUtils.RESPONSE_MSG, internalExp.getErrMsg());
			apiResponse = new JSONObject(failedResponse);
			LOGGER.error(stringBuilder.delete(0, stringBuilder.length()).append(CLASSNAME).append(methodName)
					.append(" apiResponse ").append(apiResponse).toString());
			writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
			// writing the response back the calling application
			writer.write(apiResponse.toString());
		} catch (Exception exception) {
			LOGGER.error(stringBuilder.delete(0, stringBuilder.length()).append(CLASSNAME).append(methodName)
					.append("Exception :").append(exception.getMessage()));
			Map<String, String> failedResponse = new HashMap<String, String>();
			failedResponse.put(OktaConstantUtils.RESPONSE_CODE, OktaConstantUtils.INTERNAL_ERROR_CODE);
			failedResponse.put(OktaConstantUtils.RESPONSE_MSG, OktaConstantUtils.INTERNAL_ERROR_MESSAGE);
			apiResponse = new JSONObject(failedResponse);
			LOGGER.error(stringBuilder.delete(0, stringBuilder.length()).append(CLASSNAME).append(methodName)
					.append(" apiResponse ").append(apiResponse).toString());
			writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
			// writing the response back the calling application
			writer.write(apiResponse.toString());
		} finally {
			if (null != bufferReader) {
				bufferReader.close();
			}
			if (null != writer) {
				writer.close();
			}
			if (null != inputReader) {
				inputReader.close();
			}
			if (null != response) {
				try {
					response.close();
				} catch (IOException ioExp) {
					LOGGER.error(stringBuilder.delete(0, stringBuilder.length()).append(CLASSNAME)
							.append(OktaConstantUtils.IO_EXCEPTION).append(ioExp.getMessage()), ioExp);
				}
			}
			if (null != client) {
				try {
					client.close();
				} catch (IOException ioExp) {
					LOGGER.error(stringBuilder.delete(0, stringBuilder.length()).append(CLASSNAME)
							.append(OktaConstantUtils.IO_EXCEPTION).append(ioExp.getMessage()), ioExp);
				}
			}
		}
		LOGGER.info("Exiting "
				+ stringBuilder.delete(0, stringBuilder.length()).append(CLASSNAME).append(methodName).toString());
	}
	
	/**
	 * 
	 * @param tokenValue
	 * The token to be validated after it is passed
	 * to check if token is valid or not
	 * @return tokenValid
	 * 
	 */
	private boolean isTokenValid(final String tokenValue) {
		boolean tokenValid = false;
		final StringBuilder stringBuilder = new StringBuilder();
		final String methodName = "/isTokenValid";
		LOGGER.info("Entering "
				+ stringBuilder.delete(0, stringBuilder.length())
				.append(CLASSNAME).append(methodName).toString());
		try {
			if(null != tokenValue && !tokenValue.isEmpty()) {
				final String issuerURL = SystemUtils.getEnvironmentVariable(OktaConstantUtils.ISSUER_URL,"");
				final String audience = SystemUtils.getEnvironmentVariable(OktaConstantUtils.AUDIENCE_URL,"");
				
				LOGGER.info(stringBuilder.delete(0, stringBuilder.length())
						.append(CLASSNAME).append(methodName).append("issuerURL :")
						.append(issuerURL));
				
				LOGGER.info(stringBuilder.delete(0, stringBuilder.length())
						.append(CLASSNAME).append(methodName).append("audience :")
						.append(audience));
				
				AccessTokenVerifier jwtVerifier = JwtVerifiers.accessTokenVerifierBuilder().setIssuer(issuerURL)
				 .setAudience(audience).setConnectionTimeout(Duration.ofSeconds(1)) // defaults to 1000ms
				.setReadTimeout(Duration.ofSeconds(1)) // defaults to 1000ms
				.build();
				Jwt jwt = jwtVerifier.decode(tokenValue);
				tokenValid = true;
			}			
		}catch(Exception exception) {
			LOGGER.error(
					stringBuilder.delete(0, stringBuilder.length())
					.append(CLASSNAME)
					.append(methodName)
					.append(exception.getMessage()), exception);
		}
		LOGGER.info(stringBuilder.delete(0, stringBuilder.length())
				.append(CLASSNAME).append(methodName).append("tokenValid ").append(tokenValid).toString());
		LOGGER.info("Exiting "
				+ stringBuilder.delete(0, stringBuilder.length())
				.append(CLASSNAME).append(methodName).toString());
		return tokenValid;
	}
}
