/**
 * 
 * Copyright information
 * 
 * 
 * */

package com.lambda.okta.constants;

/**
 * All constants are declared here and referenced in other java files
 */
public final class OktaConstantUtils {

	/** Constant for ISSUER_URL */
	public static final String ISSUER_URL = "IssuerURL";

	/** Constant for AUDIENCE_URL */
	public static final String AUDIENCE_URL = "Audience";

	/** Constant for ALLOW_ORIGIN */
	public static final String ALLOW_ORIGIN = "Access-Control-Allow-Origin";

	/** Constant for holding IOException */
	public static final String IO_EXCEPTION = " IOException = ";

	/** Constant for HEADERS */
	public static final String HEADERS = "headers";

	/** Constant for STATUS_CODE */
	public static final String STATUS_CODE = "statusCode";

	/** Constant for BODY */
	public static final String BODY = "body";

	/** Constant for ALL */
	public static final String ALL = "*";

	/** Constant for API_URL */
	public static final String API_URL = "ApiUrl";

	/** Constant for POST */
	public static final String POST = "POST";

	/** Constant for CONTENT_TYPE */
	public static final String CONTENT_TYPE = "Content-Type";

	/** Constant for APPL_JSON */
	public static final String APPL_JSON = "application/json";

	/** Constant for CHARSET */
	public static final String CHARSET = "charset";

	/** Constant for UTF */
	public static final String UTF = "utf-8";

	/** Constant for CONTENT_LENGTH */
	public static final String CONTENT_LENGTH = "Content-Length";

	/** Constant for AUTHORIZATION */
	public static final String AUTHORIZATION = "Authorization";

	/** Constant for TOKEN_TYPE */
	public static final String TOKEN_TYPE = "TokenType";

	/** Constant for TOKEN_TYPE_ADMIN */
	public static final String TOKEN_TYPE_ADMIN = "Admin";

	/** Constant for TOKEN_TYPE_ACCESS */
	public static final String TOKEN_TYPE_ACCESS = "Bearer";

	/** Constant for ERR_CODE */
	public static final String ERR_CODE = "errorCode";

	/** Constant for ERR_MSG */
	public static final String ERR_MSG = "errorSummary";

	/** Constant for ERR_CAUSES */
	public static final String ERR_CAUSES = "errorCauses";

	/** Constant for ERR_LINK */
	public static final String ERR_LINK = "errorLink";
	
	/** Constant for INTERNAL_ERROR_CODE */
	public static final String INTERNAL_ERROR_CODE = "1000";

	/** Constant for INTERNAL_ERROR_MESSAGE */
	public static final String INTERNAL_ERROR_MESSAGE = "User Creation Failed";

	/** Constant for RESPONSE_CODE */
	public static final String RESPONSE_CODE = "ResponseCode";

	/** Constant for RESPONSE_MSG */
	public static final String RESPONSE_MSG = "ResponseMessage";
	
	/** Constant for RESPONSE_DESC */
	public static final String RESPONSE_DESC = "ResponseDescription";
	
	/** Constant for SUCCESS STATUS CODE */
	public static final String STATUS_CODE_SUCCESS = "200";

	/** Constant for SUCCESS STATUS CODE OK */
	public static final int STATUS_CODE_OK = 200;

	/** Constant for RESPONSE_MSG */
	public static final String STATUS_SUCCESS_MESSAGE = "User Created Successfully";

	/** Constant for PROFILE */
	public static final String PROFILE = "profile";

	/** Constant to hold PARAMS */
	public static final String PARAMS = "params";

	/** Constant to hold HEADER */
	public static final String HEADER = "header";

	/** Constant for BODY_JSON */
	public static final String BODY_JSON = "body-json";

	/** Constant for SSWS_ADMIN */
	public static final String SSWS_ADMIN = "SSWS ";

	/** Constant for SSWS_ACCESS */
	public static final String BEARER = "Bearer ";

	/** Constant for INVALID_TOKEN_ERROR_CODE */
	public static final String INVALID_TOKEN_ERROR_CODE = "E0000011";

	/** Constant for INVALID_TOKEN_ERROR_MSG */
	public static final String INVALID_TOKEN_ERROR_MSG = "Invalid token provided.";

	/**
	 * Default constructor
	 */
	private OktaConstantUtils() {
		super();
	}
}
