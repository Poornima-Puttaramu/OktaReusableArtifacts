/**
 * Copyright information
 */

package com.lambda.okta.util;

import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.lambda.okta.constants.OktaConstantUtils;

/**
 * GenericUtil class handles null check
 */
public class GenericUtil {

  /** Constant variable to hold name of the class */
  private static final String CLASSNAME = GenericUtil.class.getSimpleName();

  /** Logger to log the results */
  private static final Logger LOGGER = LogManager.getLogger(CLASSNAME);

  /**
   * Default constructor
   */
  private GenericUtil() {
    super();
  }

  /**
   * Return true if the given string is empty.
   *
   * @param toCheck input string to check
   * @return true if empty, else false
   */
  public static boolean isEmptyString(String toCheck) {
    return null == toCheck || toCheck.trim().length() == 0;
  }

  /**
   * Method which parses the error response received from OKTA and returns a Map
   * with Error Code and Error Summary details.
   *
   * @param errorMessage the error message
   * @return the map
   * @throws Exception
   */
  public static Map<String, String> parseErrorResponse(final String errorMessage) throws Exception {
    final StringBuilder stringBuilder = new StringBuilder();
    final String methodName = "/parseErrorResponse";
    LOGGER.info("Entering "
      + stringBuilder.delete(0, stringBuilder.length()).append(CLASSNAME).append(methodName).toString());
    Map<String, String> errResponse = null;
    try {
      if (null != errorMessage && errorMessage.contains(OktaConstantUtils.ERR_CODE)
        && errorMessage.contains(OktaConstantUtils.ERR_MSG)) {
        errResponse = new HashMap<String, String>();
        final JSONObject jsonResult = new JSONObject(errorMessage);
        final String errorCode = (String) jsonResult.get(OktaConstantUtils.ERR_CODE);
        final String errorSummary = (String) jsonResult.get(OktaConstantUtils.ERR_MSG);
        errResponse.put(OktaConstantUtils.ERR_CODE, errorCode);
        errResponse.put(OktaConstantUtils.ERR_MSG, errorSummary);
        if (jsonResult.has(OktaConstantUtils.ERR_CAUSES)) {
          JSONArray errCause = jsonResult.getJSONArray(OktaConstantUtils.ERR_CAUSES);
          if (null != errCause && errCause.length() > 0) {
            final JSONObject errDescription = errCause.getJSONObject(0);
            if (errDescription.has(OktaConstantUtils.ERR_MSG)) {
              final String errorDescription = errDescription.getString(OktaConstantUtils.ERR_MSG);
              errResponse.put(OktaConstantUtils.ERR_LINK, errorDescription);
            }
          }
        }
      }
    } catch (Exception exp) {
      LOGGER.error(stringBuilder.delete(0, stringBuilder.length()).append(CLASSNAME).append(methodName)
        .append(" Exception = ").append(exp.getMessage()), exp);
      throw exp;
    }
    LOGGER.info("Exiting " + stringBuilder.delete(0, stringBuilder.length()).append(CLASSNAME).append(methodName));
    return errResponse;
  }
}