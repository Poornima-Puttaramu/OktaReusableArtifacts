<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
    <%@ page isELIgnored="false" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@page import="java.util.ArrayList"%>
<%@ page import="com.okta.custom.logout.util.AWSOperations" %>
<%@page import="com.okta.custom.logout.util.LogoutHelper" %>
<%@ page language="java" import="java.util.*" %> 
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<%
final AWSOperations awsOperations = new AWSOperations();
Map oktaDetails = awsOperations.getOktaMap();
final String oktaBaseURL = (String)oktaDetails.get(LogoutHelper.OKTA_BASE_URL);
final String cimaURL = (String)oktaDetails.get(LogoutHelper.CIMA_LOGOUT_URL);
final String targetLists = (String)request.getAttribute(LogoutHelper.TARGETS);
%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Logout Page</title>
 <!-- Bootstrap core CSS -->
        <link href="<%=request.getContextPath() %>/resources/css/bootstrap.min.css" rel="stylesheet">
</head>
<body>
<div class="container">
 <div  class="row justify-content-center align-items-center">
 <div class='centertext'><img class="mb-4" src="<%=request.getContextPath() %>/resources/images/Loading.gif" width="50%"></div>
</div>
</div>
</body>
 <script src="<%=request.getContextPath() %>/resources/js/jquery-3.3.1.js"></script>
<script type="text/javascript">
var appTarget = '<%=targetLists%>';

$(document).ready(function(){
	
	var oktaURL = '<%=oktaBaseURL%>';		
	var cimaURL = '<%=cimaURL%>';
	$.ajax({
	  url: oktaURL + '/api/v1/sessions/me',
	  type: 'DELETE',
	  xhrFields: { withCredentials: true },
	  accept: 'application/json'
	}).done(function(data) { 
		// redirect to CIMA Logout URL
		window.location.replace(cimaURL);		
	})
	.fail(function(xhr, textStatus, error) {
	
	});
	
	
	var logoutURLS = appTarget.split("|");	
	for(var index=0;index<logoutURLS.length-1;index++)
	{	
		$.ajax({
			  url: logoutURLS[index],
			  type: 'GET',
			  async: false,
			  xhrFields: { withCredentials: true },
			  accept: 'application/json'
			}).done(function(data) {       		
				
			})
			.fail(function(xhr, textStatus, error) {
				
			});
		
	}
});
</script>

</html>