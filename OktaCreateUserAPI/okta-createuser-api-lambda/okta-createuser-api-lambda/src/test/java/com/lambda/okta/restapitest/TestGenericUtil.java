package com.lambda.okta.restapitest;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.powermock.api.mockito.PowerMockito;

import com.lambda.okta.constants.OktaConstantUtils;
import com.lambda.okta.util.GenericUtil;

/**
 *
 */
public class TestGenericUtil {

  /**
   *
   */
  @Before
  public void setUp() {

  }

  /**
   *
   */
  @After
  public void tearDown() {

  }

  /*
   * Testing Conditon(s): Default
   */
  /**
   *
   */
  @Test
  public void testMethodIsEmptyString0Branch0() {
    // Get expected result and result
    boolean expResult = true;
    boolean result = GenericUtil.isEmptyString(null);

    // Check Return value
    assertEquals(expResult, result);

  }

  /*
   * Testing Conditon(s): Default
   */
  /**
   *
   */
  @Test
  public void testMethodIsEmptyString0Branch1() {
    // Get expected result and result
    boolean expResult = true;
    boolean result = GenericUtil.isEmptyString("");

    // Check Return value
    assertEquals(expResult, result);

  }

  /*
   * Testing Conditon(s): Default
   */
  /**
   *
   */
  @Test
  public void testMethodIsEmptyString0Branch2() {
    // Get expected result and result
    boolean expResult = false;
    boolean result = GenericUtil.isEmptyString("test");

    // Check Return value
    assertEquals(expResult, result);

  }

  /*
   * Testing Conditon(s): if: (null != errorMessage &&
   * errorMessage.contains(OktaConstants.ERR_CODE) &&
   * errorMessage.contains(OktaConstants.ERR_MSG)), if:
   * (jsonResult.has(OktaConstants.ERR_CAUSES)), if: (null != errCause &&
   * errCause.length() > 0), if: (errDescription.has(OktaConstants.ERR_MSG))
   */
  /**
   * @throws Exception
   *
   */
  @Test
  public void testMethodParseErrorResponse1Branch0() throws Exception {
    // Get expected result and result
    String inputString = "{\"errorCode\":\"12220\",\"errorSummary\":\"testmessage\"}";
    Map expResult = new HashMap();
    expResult.put(OktaConstantUtils.ERR_CODE, "12220");
    expResult.put(OktaConstantUtils.ERR_MSG, "testmessage");
    Map result = GenericUtil.parseErrorResponse(inputString);

    // Check Return value
    assertEquals(expResult, result);
  }

  /*
   * Testing Conditon(s): if: (null != errorMessage &&
   * errorMessage.contains(OktaConstants.ERR_CODE) &&
   * errorMessage.contains(OktaConstants.ERR_MSG)), if:
   * (jsonResult.has(OktaConstants.ERR_CAUSES)), if: (null != errCause &&
   * errCause.length() > 0), else: Not (errDescription.has(OktaConstants.ERR_MSG))
   */
  /**
   * @throws Exception
   *
   */
  @Test
  public void testMethodParseErrorResponse1Branch1() throws Exception {
    // Get expected result and result
    String inputString = null;
    Map expResult = null;
    Map result = GenericUtil.parseErrorResponse(inputString);

    // Check Return value
    assertEquals(expResult, result);
  }

  /*
   * Testing Conditon(s): if: (null != errorMessage &&
   * errorMessage.contains(OktaConstants.ERR_CODE) &&
   * errorMessage.contains(OktaConstants.ERR_MSG)), if:
   * (jsonResult.has(OktaConstants.ERR_CAUSES)), else: Not (null != errCause &&
   * errCause.length() > 0)
   */
  /**
   * @throws Exception
   *
   */
  @Test
  public void testMethodParseErrorResponse1Branch2() throws Exception {
    // Get expected result and result
    String inputString = "{\"errorCode\":\"12220\"}";
    Map expResult = new HashMap();
    expResult.put(OktaConstantUtils.ERR_CODE, "12220");
    // expResult.put(OktaConstants.ERR_MSG, "testmessage");
    Map result = GenericUtil.parseErrorResponse(inputString);
    // Check Return value
    assertEquals(null, result);
  }

  /*
   * Testing Conditon(s): if: (null != errorMessage &&
   * errorMessage.contains(OktaConstants.ERR_CODE) &&
   * errorMessage.contains(OktaConstants.ERR_MSG)), else: Not
   * (jsonResult.has(OktaConstants.ERR_CAUSES))
   */
  /**
   * @throws Exception
   *
   */
  @Test
  public void testMethodParseErrorResponse1Branch3() throws Exception {
    // Get expected result and result
    String inputString = "{\"errorSummary\":\"testmessage\"}";
    Map expResult = new HashMap();
    expResult.put(OktaConstantUtils.ERR_MSG, "testmessage");
    Map result = GenericUtil.parseErrorResponse(inputString);
    // Check Return value
    assertEquals(null, result);
  }

  /**
   * @throws Exception
   *
   */
  @Test
  public void testMethodParseErrorCausesResponse1Branch2() throws Exception {
    // Get expected result and result
    String inputString = "{\"errorCode\":\"12220\",\"errorSummary\":\"test\",\"errorCauses\": [{\"errorSummary\": \"testerrorSummary\"}]}";
    Map expResult = new HashMap();
    expResult.put(OktaConstantUtils.ERR_CODE, "12220");
    expResult.put(OktaConstantUtils.ERR_MSG, "test");
    expResult.put(OktaConstantUtils.ERR_LINK, "testerrorSummary");
    Map result = GenericUtil.parseErrorResponse(inputString);
    // Check Return value
    assertEquals(expResult, result);
  }

  /**
   * @throws Exception
   *
   */
  @Test(expected = Exception.class)
  public void testMethodParseErrorCausesResponse1Branch3() throws Exception {
    String inputString = "{\"errorCode\":\"12220\",\"errorSummary\":\"test\",\"errorCauses\": [{\"errorSummary\": \"testerrorSummary\"}]}";
    PowerMockito.when(GenericUtil.parseErrorResponse(inputString)).thenThrow(Exception.class);
  }

}
