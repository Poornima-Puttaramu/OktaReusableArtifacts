package com.lambda.okta.restapitest;

import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.lang3.SystemUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicStatusLine;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.lambda.okta.constants.OktaConstantUtils;
import com.lambda.okta.exception.InternalException;
import com.lambda.okta.restapi.LambdaFunctionHandler;
import com.lambda.okta.restapi.OktaCreateUser;
import com.lambda.okta.util.APIUtil;
import com.lambda.okta.util.GenericUtil;

/**
 *
 */
@RunWith(PowerMockRunner.class)
@PowerMockIgnore({ "javax.management.*", "javax.net.ssl.*" })
@PrepareForTest({ SystemUtils.class, APIUtil.class })
public class TestOktaCreateUser {

	/**
	 * 
	 */
	@Mock
	private com.lambda.okta.restapi.HttpPostWithEntity httpPost;

	/**
	 * 
	 */
	private final String hostUrl = "http://url/api/v1";
	/**
	 * 
	 */
	@Rule
	public final ExpectedException exception = ExpectedException.none();
	/**
	 * 
	 */
	@InjectMocks
	private OktaCreateUser user;
	/**
	 * 
	 */
	private final String inputUserObject = "{\"profile\": {\"firstName\":\"Test\",\"lastName\":\"User\",\"email\":\"testUser@pwc.com\"}}";
	/**
	 * 
	 */
	@Mock
	CloseableHttpResponse response;

	InputStream input = null;

	/** Logger to log the results */
	private static final Logger LOGGER = LogManager.getLogger(TestOktaCreateUser.class);

	/** Classname variable to store name of the class */
	private static final String CLASSNAME = LambdaFunctionHandler.class.getSimpleName();

	/**
	 * @throws Exception
	 */
	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		PowerMockito.mockStatic(SystemUtils.class);
		PowerMockito.mockStatic(GenericUtil.class);

		// Static Classes to be mocked
		PowerMockito.mockStatic(HttpClientBuilder.class);
		PowerMockito.when(SystemUtils.getEnvironmentVariable(OktaConstantUtils.API_URL, "")).thenReturn(hostUrl);
	}

	/**
	 * @throws IOException
	 * @throws InternalException
	 * 
	 */
	@Test
	public void testCreate() throws IOException, InternalException {
		String inputUserObject = "{\"profile\": {\"firstName\":\"Test\",\"lastName\":\"User\",\"email\":\"testUser@pwc.com\"}}";
		JSONParser parser = new JSONParser();
		JSONObject jsonBody = null;
		StatusLine statusLine = new BasicStatusLine(HttpVersion.HTTP_1_1, HttpStatus.SC_OK, "FINE!");
		HttpEntity entity = Mockito.mock(HttpEntity.class);
		final StringBuilder stringBuilder = new StringBuilder();
		final String methodName = "/testCreate";
		LOGGER.info("Entering "
				+ stringBuilder.delete(0, stringBuilder.length()).append(CLASSNAME).append(methodName).toString());

		try {
			PowerMockito.mockStatic(APIUtil.class);
			when(response.getStatusLine()).thenReturn(statusLine);
			when(response.getEntity()).thenReturn(entity);
			ClassLoader classLoader = getClass().getClassLoader();
			File file = new File(classLoader.getResource("result.txt").getFile());
			input = new FileInputStream(file);
			when(entity.getContent()).thenReturn(input);

			PowerMockito.whenNew(com.lambda.okta.restapi.HttpPostWithEntity.class).withArguments(hostUrl).thenReturn(httpPost);
			jsonBody = (JSONObject) parser.parse(inputUserObject);
			String token = "ggggg";
			input.close();
			OktaCreateUser user = new OktaCreateUser();
			user.createUser(jsonBody, token);
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
	 * @throws IOException
	 * @throws InternalException
	 * 
	 */
	@SuppressWarnings("unchecked")
	@Test
	// (expected = InternalException.class)
	public void testCreateExp() throws IOException, InternalException {

		JSONParser parser = new JSONParser();
		JSONObject jsonBody = null;
		StatusLine statusLine = new BasicStatusLine(HttpVersion.HTTP_1_1, HttpStatus.SC_OK, "FINE!");
		HttpEntity entity = Mockito.mock(HttpEntity.class);
		final StringBuilder stringBuilder = new StringBuilder();
		final String methodName = "/testCreateExp";
		LOGGER.info("Entering "
				+ stringBuilder.delete(0, stringBuilder.length()).append(CLASSNAME).append(methodName).toString());

		try {
			PowerMockito.mockStatic(APIUtil.class);
			when(response.getStatusLine()).thenReturn(statusLine);
			when(response.getEntity()).thenReturn(entity);
			ClassLoader classLoader = getClass().getClassLoader();
			File file = new File(classLoader.getResource("result.txt").getFile());
			input = new FileInputStream(file);
			when(entity.getContent()).thenReturn(input);

			PowerMockito.whenNew(com.lambda.okta.restapi.HttpPostWithEntity.class).withArguments(hostUrl).thenReturn(httpPost);
			jsonBody = (JSONObject) parser.parse(inputUserObject);
			String token = "ggggg";
			input.close();
			OktaCreateUser user = new OktaCreateUser();
			user.createUser(jsonBody, token);
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

}
