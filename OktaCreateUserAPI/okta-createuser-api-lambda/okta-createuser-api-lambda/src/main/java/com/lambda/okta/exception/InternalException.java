
package com.lambda.okta.exception;

/**
 * The Class InternalException.
 */
public class InternalException extends Exception {

  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = -1L;

  /** The err code. */
  private String errCode;
  
/** The err msg. */
  private String errMsg;
  
  

  public String getErrCode() {
	return errCode;
}



public void setErrCode(String errCode) {
	this.errCode = errCode;
}



public String getErrMsg() {
	return errMsg;
}



public void setErrMsg(String errMsg) {
	this.errMsg = errMsg;
}



/**
   * Instantiates a new internal exception.
   * 
   * @param errCode
   *            the err code
   * @param errMsg
   *            the err msg
   */
  public InternalException(String errCode, String errMsg) {
    this.errCode = errCode;
    this.errMsg = errMsg;

  }
}

