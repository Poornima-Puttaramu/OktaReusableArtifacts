package com.lambda.okta.restapitest;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.lang3.SystemUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpVersion;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicStatusLine;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONException;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.amazonaws.services.lambda.runtime.Context;
import com.lambda.okta.constants.OktaConstantUtils;
import com.lambda.okta.exception.InternalException;
import com.lambda.okta.restapi.LambdaFunctionHandler;
import com.lambda.okta.restapi.OktaCreateUser;
import com.lambda.okta.util.APIUtil;

/**
 * A simple test harness for locally invoking your Lambda function handler.
 **/
@RunWith(PowerMockRunner.class)
@PowerMockIgnore({ "javax.management.*", "javax.net.ssl.*" })
@PrepareForTest({ SystemUtils.class, APIUtil.class })
public class TestCreateUserLambdaFunctionHandler {
	/** Classname variable to store name of the class */
	private static final String CLASSNAME = TestCreateUserLambdaFunctionHandler.class
			.getSimpleName();

	/** Logger to log the results */
	private static final Logger LOGGER = LogManager.getLogger(CLASSNAME);

	/**
   *
   */
	@Mock
	private Context context;

	/**
   *
   */
	@Mock
	private OutputStream outputStream;

	/**
   *
   */
	private final String hostUrl = "http://url/api/v1";

	/**
   *
   */
	@InjectMocks
	OktaCreateUser oktaCreateUser = new OktaCreateUser();

	/**
   *
   */
	InternalException internalException = new InternalException("", "");

	/**
  *
  */
	CloseableHttpResponse response = Mockito.mock(CloseableHttpResponse.class);
	
	InputStream input = Mockito.mock(InputStream.class);

	/**
	 * @throws Exception
	 */
	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		// Static Classes to be mocked
		PowerMockito.mockStatic(SystemUtils.class);
		// Static Classes to be mocked
		PowerMockito.mockStatic(HttpClientBuilder.class);
		PowerMockito.mock(com.lambda.okta.restapi.HttpPostWithEntity.class);
		// System environment variables Mocked
		PowerMockito.when(
				SystemUtils.getEnvironmentVariable(OktaConstantUtils.API_URL, ""))
				.thenReturn(hostUrl);
	}

	/**
	 * testLambdaFunctionHandler() method which tests the getToken method
	 * LambdaFunctionHandler
	 * 
	 * @throws Exception
	 * 
	 * @throws JSONException
	 */
	@Test
	public void testLambdaFunction() throws Exception {
		final StringBuilder stringBuilder = new StringBuilder();
	    final String methodName = "/testLambdaFunction";
	    LOGGER.info("Entering "
	      + stringBuilder.delete(0, stringBuilder.length())
	      .append(CLASSNAME).append(methodName).toString());
		try {
			ClassLoader classLoader = getClass().getClassLoader();
			File file = new File(classLoader.getResource(
					"createuser_success.txt").getFile());
			input = new FileInputStream(file);
			LambdaFunctionHandler handler = new LambdaFunctionHandler();
			String oktaToken = "testtoken";
			String oktaJsonBody = "{\"firstName\":\"p\",\"lastName\":\"m\",\"email\":\"p.m@gmail.com\",\"login\":\"p.m@gmail.com\"}";
			JSONParser parser = new JSONParser();
			JSONObject inputObj = (JSONObject) parser.parse(oktaJsonBody);
			String outputBody = "{\"ResponseCode\": \"200\",\"ResponseMessage\": \"User Created Successfully\"}";
			JSONObject outputObj = (JSONObject) parser.parse(outputBody);
			PowerMockito.whenNew(OktaCreateUser.class).withNoArguments()
					.thenReturn(oktaCreateUser);
			HttpEntity entity = Mockito.mock(HttpEntity.class);
			StatusLine statusLine = new BasicStatusLine(HttpVersion.HTTP_1_1,
					OktaConstantUtils.STATUS_CODE_OK, "FINE!");

			when(response.getStatusLine()).thenReturn(statusLine);
			when(response.getEntity()).thenReturn(entity);
			when(response.getEntity().getContent()).thenReturn(input);
			handler.handleRequest(input, outputStream, context);
			assertNotNull(outputStream);
		} catch (InternalException intExp) {
			LOGGER.error(stringBuilder.delete(0, stringBuilder.length())
					.append(CLASSNAME).append(methodName).append("InternalException :")
					.append(intExp));
		} catch (ParseException parseExp) {
			LOGGER.error(stringBuilder.delete(0, stringBuilder.length())
					.append(CLASSNAME).append(methodName).append("ParseException :")
					.append(parseExp));
		}

		catch (Exception genExp) {
			LOGGER.error(stringBuilder.delete(0, stringBuilder.length())
					.append(CLASSNAME).append(methodName).append("Exception :")
					.append(genExp));
			InternalException interExp = new InternalException(
					OktaConstantUtils.INTERNAL_ERROR_CODE, genExp.getMessage());
			throw interExp;
		}

		finally {
			input.close();
			response.close();
		}
		LOGGER.info("Exiting "
				+ stringBuilder.delete(0, stringBuilder.length())
				.append(CLASSNAME).append(methodName).toString());
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void testLambdaFunctionIOException() throws Exception {
		final StringBuilder stringBuilder = new StringBuilder();
	    final String methodName = "/testLambdaFunction_IOException";
	    LOGGER.info("Entering "
	      + stringBuilder.delete(0, stringBuilder.length())
	      .append(CLASSNAME).append(methodName).toString());
		try {
		ClassLoader classLoader = getClass().getClassLoader();
		File file = new File(classLoader.getResource("createuser_success.txt")
				.getFile());
		LambdaFunctionHandler handler = new LambdaFunctionHandler();
		String oktaToken = "testtoken";
		String oktaJsonBody = "{\"firstName\":\"p\",\"lastName\":\"m\",\"email\":\"p.m@gmail.com\",\"login\":\"p.m@gmail.com\"}";
		JSONParser parser = new JSONParser();
		JSONObject inputObj = (JSONObject) parser.parse(oktaJsonBody);

		String outputBody = "{\"ResponseCode\": \"200\",\"ResponseMessage\": \"User Created Successfully\"}";
		JSONObject outputObj = (JSONObject) parser.parse(outputBody);
		PowerMockito.whenNew(OktaCreateUser.class).withNoArguments()
				.thenReturn(oktaCreateUser);
		HttpEntity entity = Mockito.mock(HttpEntity.class);
		StatusLine statusLine = new BasicStatusLine(HttpVersion.HTTP_1_1,
				OktaConstantUtils.STATUS_CODE_OK, "FINE!");

		when(response.getStatusLine()).thenReturn(statusLine);
		when(response.getEntity()).thenReturn(entity);
		when(response.getEntity().getContent()).thenReturn(input);
		input.close();
		handler.handleRequest(input, outputStream, context);
		} catch (InternalException intExp) {
			LOGGER.error(stringBuilder.delete(0, stringBuilder.length())
					.append(CLASSNAME).append(methodName).append("InternalException :")
					.append(intExp));
		} catch (ParseException parseExp) {
			LOGGER.error(stringBuilder.delete(0, stringBuilder.length())
					.append(CLASSNAME).append(methodName).append("ParseException :")
					.append(parseExp));
		}

		catch (Exception genExp) {
			LOGGER.error(stringBuilder.delete(0, stringBuilder.length())
					.append(CLASSNAME).append(methodName).append("Exception :")
					.append(genExp));
			InternalException interExp = new InternalException(
					OktaConstantUtils.INTERNAL_ERROR_CODE, genExp.getMessage());
			throw interExp;
		}

		finally {
			input.close();
			response.close();
		}
		LOGGER.info("Exiting "
				+ stringBuilder.delete(0, stringBuilder.length())
				.append(CLASSNAME).append(methodName).toString());
	}
	
	@Test
 	public void testLambdaFunctionBearer() throws Exception {
	  final StringBuilder stringBuilder = new StringBuilder();
		final String methodName = "/testLambdaFunction_Bearer";
		LOGGER.info("Entering "
				+ stringBuilder.delete(0, stringBuilder.length())
				.append(CLASSNAME).append(methodName).toString());
 		try {
 	  ClassLoader classLoader = getClass().getClassLoader();
 	   File file = new File(classLoader.getResource("createuser_success_bearer.txt").getFile());
 	   input = new FileInputStream(file);
 		LambdaFunctionHandler handler = new LambdaFunctionHandler();
 		JSONParser parser = new JSONParser();

 		String outputBody = "{\"ResponseCode\": \"200\",\"ResponseMessage\": \"User Created Successfully\"}";
 				
 		JSONObject outputObj = (JSONObject) parser.parse(outputBody);
 		PowerMockito.whenNew(OktaCreateUser.class).withNoArguments().thenReturn(oktaCreateUser);
 		HttpEntity entity = Mockito.mock(HttpEntity.class);
 		StatusLine statusLine = new BasicStatusLine(HttpVersion.HTTP_1_1, OktaConstantUtils.STATUS_CODE_OK, "FINE!");

 		when(response.getStatusLine()).thenReturn(statusLine);
 		when(response.getEntity()).thenReturn(entity);
 		when(response.getEntity().getContent()).thenReturn(input);
 		
 		handler.handleRequest(input, outputStream, context);
 		assertNotNull(outputStream);
 		}
 		
 		catch (InternalException intExp) {
			LOGGER.error(stringBuilder.delete(0, stringBuilder.length())
					.append(CLASSNAME).append(methodName).append("InternalException :")
					.append(intExp));
		} catch (ParseException parseExp) {
			LOGGER.error(stringBuilder.delete(0, stringBuilder.length())
					.append(CLASSNAME).append(methodName).append("ParseException :")
					.append(parseExp));
		}

		catch (Exception genExp) {
			LOGGER.error(stringBuilder.delete(0, stringBuilder.length())
					.append(CLASSNAME).append(methodName).append("Exception :")
					.append(genExp));
			InternalException interExp = new InternalException(
					OktaConstantUtils.INTERNAL_ERROR_CODE, genExp.getMessage());
			throw interExp;
		}

		finally {
			input.close();
			response.close();
		}
 		LOGGER.info("Exiting "
				+ stringBuilder.delete(0, stringBuilder.length())
				.append(CLASSNAME).append(methodName).toString());
 	}
	
	@Test
 	public void testLambdaFunctionIOException1() throws Exception {
	  final StringBuilder stringBuilder = new StringBuilder();
		final String methodName = "/testLambdaFunction_IOException_1";
		LOGGER.info("Entering "
				+ stringBuilder.delete(0, stringBuilder.length())
				.append(CLASSNAME).append(methodName).toString());
 	try {
	  ClassLoader classLoader = getClass().getClassLoader();
	   File file = new File(classLoader.getResource("createuser_success.txt").getFile());
	   input = new FileInputStream(file);
 		LambdaFunctionHandler handler = new LambdaFunctionHandler();
 		JSONParser parser = new JSONParser();

 		String outputBody = "{\"ResponseCode\": \"200\",\"ResponseMessage\": \"User Created Successfully\"}";
 		JSONObject outputObj = (JSONObject) parser.parse(outputBody);
 		PowerMockito.whenNew(OktaCreateUser.class).withNoArguments().thenReturn(oktaCreateUser);
 		HttpEntity entity = Mockito.mock(HttpEntity.class);
 		StatusLine statusLine = new BasicStatusLine(HttpVersion.HTTP_1_1, OktaConstantUtils.STATUS_CODE_OK, "FINE!");
 		String token = "testtoken";
 		when(response.getStatusLine()).thenReturn(statusLine);
 		when(response.getEntity()).thenReturn(entity);
 		when(response.getEntity().getContent()).thenReturn(input);
 		PowerMockito.when(oktaCreateUser.createUser(outputObj, token)).thenThrow(InternalException.class);
 		input.close();
 		handler.handleRequest(input, outputStream, context);
 	}
 	
 	catch (InternalException intExp) {
		LOGGER.error(stringBuilder.delete(0, stringBuilder.length())
				.append(CLASSNAME).append(methodName).append("InternalException :")
				.append(intExp));
	} catch (ParseException parseExp) {
		LOGGER.error(stringBuilder.delete(0, stringBuilder.length())
				.append(CLASSNAME).append(methodName).append("ParseException :")
				.append(parseExp));
	}

	catch (Exception genExp) {
		LOGGER.error(stringBuilder.delete(0, stringBuilder.length())
				.append(CLASSNAME).append(methodName).append("Exception :")
				.append(genExp));
		InternalException interExp = new InternalException(
				OktaConstantUtils.INTERNAL_ERROR_CODE, genExp.getMessage());
		throw interExp;
	}

	finally {
		input.close();
		response.close();
	}
 	LOGGER.info("Exiting "
			+ stringBuilder.delete(0, stringBuilder.length())
			.append(CLASSNAME).append(methodName).toString());
 	}

}
