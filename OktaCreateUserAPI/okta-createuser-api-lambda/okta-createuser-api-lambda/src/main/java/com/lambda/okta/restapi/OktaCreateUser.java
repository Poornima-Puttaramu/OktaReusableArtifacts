/**
 * Copyright information
 */

package com.lambda.okta.restapi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.SystemUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;

import com.lambda.okta.constants.OktaConstantUtils;
import com.lambda.okta.exception.InternalException;
import com.lambda.okta.util.APIUtil;
import com.lambda.okta.util.GenericUtil;

/**
 * Class OktaCreateUser handles create user functionality
 */
public class OktaCreateUser {
	/** Classname variable to store name of the class */
	private static final String CLASSNAME = OktaCreateUser.class
			.getSimpleName();

	/** Logger to log the results */
	private static final Logger LOGGER = LogManager
			.getLogger(CLASSNAME);

  /** Default constructor */
  public OktaCreateUser() {
    super();
  }

  /**
   * The method to handle functionality of creating a user
   * 
   * @param inputUserObject
   *            Input JSON Object
   * @param token
   *            This token is used to send and authorize a request
   * @return here return type is JSONObject
   * @throws InternalException
   */
  public JSONObject createUser(JSONObject inputUserObject, String token)
    throws InternalException {
    final StringBuilder stringBuilder = new StringBuilder();
    final String methodName = "/createUser";
    LOGGER.info("Entering "
      + stringBuilder.delete(0, stringBuilder.length())
      .append(CLASSNAME).append(methodName).toString());
    String hostURL = "";
    BufferedReader bufferedReader = null;
    InputStreamReader inputReader = null;
    InputStream inputStream = null;

    JSONObject responseObject = null;
    CloseableHttpResponse response = null;

    hostURL = SystemUtils.getEnvironmentVariable(OktaConstantUtils.API_URL,"");

    LOGGER.info(stringBuilder.delete(0, stringBuilder.length())
      .append(CLASSNAME).append(methodName).append("Host URL :")
      .append(hostURL));

    try {

      if (null != token && null != inputUserObject) {
        final String requestBody = inputUserObject.toString();
        // Submitting the request as POST
        response = APIUtil.sendPostRequest(hostURL, requestBody, token);
        String requestResponse = "";
        if (null != response) {
          final int responseCode = response.getStatusLine()
            .getStatusCode();
          LOGGER.info(stringBuilder
            .delete(0, stringBuilder.length())
            .append(CLASSNAME).append(methodName)
            .append(" Response Code :")
            .append(responseCode).toString());
          inputStream = response.getEntity().getContent();
          inputReader = new InputStreamReader(inputStream);
          bufferedReader = new BufferedReader(inputReader);
          String line = "";
          while ((line = bufferedReader.readLine()) != null) {
            requestResponse = line;
          }
          if (responseCode == OktaConstantUtils.STATUS_CODE_OK) {
            Map<String, String> successResponse = new HashMap<String, String>();
            successResponse.put(OktaConstantUtils.RESPONSE_CODE, OktaConstantUtils.STATUS_CODE_SUCCESS);
            successResponse.put(OktaConstantUtils.RESPONSE_MSG, OktaConstantUtils.STATUS_SUCCESS_MESSAGE);
            responseObject = new JSONObject(successResponse);  
            LOGGER.info(stringBuilder.delete(0, stringBuilder.length()).append(CLASSNAME)
					.append(methodName).append(" Response Object :").append(responseObject).toString());
          } else {
            Map<String, String> errorDetails = GenericUtil.parseErrorResponse(requestResponse);
            final String errorCode = errorDetails.get(OktaConstantUtils.ERR_CODE);
            final String errorMessage = errorDetails.get(OktaConstantUtils.ERR_MSG);
            final String errorDescription = errorDetails.get(OktaConstantUtils.ERR_LINK);
            LOGGER.info(stringBuilder.delete(0, stringBuilder.length()).append(CLASSNAME)
              .append(methodName).append(" errorCode :").append(errorCode).toString());
            LOGGER.info(stringBuilder.delete(0, stringBuilder.length()).append(CLASSNAME)
              .append(methodName).append(" errorMessage :").append(errorMessage).toString());
            LOGGER.info(stringBuilder.delete(0, stringBuilder.length()).append(CLASSNAME)
              .append(methodName).append(" errorDescription :")
              .append(errorDescription).toString());
            Map<String, String> failedResponse = new HashMap<String, String>();
            failedResponse.put(OktaConstantUtils.RESPONSE_CODE, errorCode);
            failedResponse.put(OktaConstantUtils.RESPONSE_MSG, errorMessage);
            failedResponse.put(OktaConstantUtils.RESPONSE_DESC, errorDescription);
            responseObject = new JSONObject(failedResponse);
          }
        }
      }
    } catch (Exception exp) {
      LOGGER.error(stringBuilder.delete(0, stringBuilder.length())
        .append(CLASSNAME).append(methodName).append("Exception :")
        .append(exp.getMessage()));
      InternalException interExp = new InternalException(
        OktaConstantUtils.INTERNAL_ERROR_CODE, exp.getMessage());
      throw interExp;
    } finally {
      if (null != response) {
        try {
          response.close();
        } catch (IOException ioExp) {
          LOGGER.error(
            stringBuilder.delete(0, stringBuilder.length())
            .append(CLASSNAME)
            .append(OktaConstantUtils.IO_EXCEPTION)
            .append(ioExp.getMessage()), ioExp);
        }
      }
      if (null != inputStream) {
        try {
          inputStream.close();
        } catch (IOException ioExp) {
          LOGGER.error(
            stringBuilder.delete(0, stringBuilder.length())
            .append(CLASSNAME)
            .append(OktaConstantUtils.IO_EXCEPTION)
            .append(ioExp.getMessage()), ioExp);
        }
      }
      if (null != inputReader) {
        try {
          inputReader.close();
        } catch (IOException ioExp) {
          LOGGER.error(
            stringBuilder.delete(0, stringBuilder.length())
            .append(CLASSNAME)
            .append(OktaConstantUtils.IO_EXCEPTION)
            .append(ioExp.getMessage()), ioExp);
        }
      }
      if (null != bufferedReader) {
        try {
          bufferedReader.close();
        } catch (IOException ioExp) {
          LOGGER.error(
            stringBuilder.delete(0, stringBuilder.length())
            .append(CLASSNAME)
            .append(OktaConstantUtils.IO_EXCEPTION)
            .append(ioExp.getMessage()), ioExp);
        }
      }
    }
    LOGGER.info("Exiting "
      + stringBuilder.delete(0, stringBuilder.length())
      .append(CLASSNAME).append(methodName).toString());
    return responseObject;
  }
}
