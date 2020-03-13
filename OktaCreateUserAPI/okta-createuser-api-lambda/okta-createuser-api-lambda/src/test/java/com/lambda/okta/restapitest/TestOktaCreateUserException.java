package com.lambda.okta.restapitest;

import org.apache.commons.lang3.SystemUtils;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import com.lambda.okta.constants.OktaConstantUtils;
import com.lambda.okta.exception.InternalException;
import com.lambda.okta.restapi.OktaCreateUser;
import com.lambda.okta.util.APIUtil;

/**
 * A simple test harness for locally invoking your Lambda function handler.
 */
@RunWith(PowerMockRunner.class)
@PowerMockIgnore({ "javax.management.*", "javax.net.ssl.*" })
@PrepareForTest({ SystemUtils.class })
public class TestOktaCreateUserException {
	/** Classname variable to store name of the class */
	private static final String CLASSNAME = TestOktaCreateUserException.class
			.getSimpleName();

	/** Logger to log the results */
	private static final Logger LOGGER = LogManager.getLogger(CLASSNAME);

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
	 * @throws Exception
	 * 
	 */
	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		PowerMockito.mockStatic(SystemUtils.class);
		PowerMockito.mockStatic(APIUtil.class);

		// Static Classes to be mocked
		PowerMockito.mockStatic(HttpClientBuilder.class);

		PowerMockito.mock(com.lambda.okta.restapi.HttpPostWithEntity.class);

		// System environment variables Mocked
		PowerMockito.when(SystemUtils.getEnvironmentVariable(OktaConstantUtils.API_URL, "")).thenReturn(hostUrl);

	}

		
	@Test
	(expected = InternalException.class)
	public void testCreateException() throws Exception {
		final StringBuilder stringBuilder = new StringBuilder();
		final String methodName = "/testCreateException";
		LOGGER.info("Entering "
				+ stringBuilder.delete(0, stringBuilder.length())
				.append(CLASSNAME).append(methodName).toString());
		String inputUserObject = "{\"profile\": {\"firstName\":\"Test\",\"lastName\":\"User\",\"email\":\"testUser@pwc.com\"}}";
		JSONParser parser = new JSONParser();
		JSONObject jsonBody = null;
		jsonBody = (JSONObject) parser.parse(inputUserObject);
		String token = "ggggg";
		String requestBody = "{\"profile\": {\"firstName\":\"Test\",\"lastName\":\"User\",\"email\":\"testUser@pwc.com\"}}";
		PowerMockito.whenNew(com.lambda.okta.restapi.HttpPostWithEntity.class).withArguments(hostUrl).thenReturn(httpPost);
		JSONObject returnObj = user.createUser(jsonBody, token);
		LOGGER.info("Exiting "
				+ stringBuilder.delete(0, stringBuilder.length())
				.append(CLASSNAME).append(methodName).toString());
	}
	

}