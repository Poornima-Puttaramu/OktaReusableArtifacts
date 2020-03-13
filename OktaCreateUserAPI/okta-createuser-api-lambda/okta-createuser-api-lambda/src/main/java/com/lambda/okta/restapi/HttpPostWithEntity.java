/**
 * 
 * Copyright information
 * 
 * 
 * */
package com.lambda.okta.restapi;

import java.net.URI;

import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;

import com.lambda.okta.constants.OktaConstantUtils;

/**
 *
 */
public class HttpPostWithEntity extends HttpEntityEnclosingRequestBase {

  /**
   * 
   */
  public final static String METHOD_NAME = OktaConstantUtils.POST;

  /**
   * @param uri
   * @throws IllegalArgumentException if the uri is invalid.
   */
  public HttpPostWithEntity(final String uri) {
    super();
    setURI(URI.create(uri));
  }

  @Override
  public String getMethod() {
    return METHOD_NAME;
  }
}