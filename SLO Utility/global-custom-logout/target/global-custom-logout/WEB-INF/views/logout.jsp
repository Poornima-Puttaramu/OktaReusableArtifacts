<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>   
<%@page import="java.util.List"%>
<%@page import="java.util.ArrayList"%>
<%@page import="java.util.Set"%>
<%@page import="java.util.Arrays"%>
<%@ page language="java" import="java.util.*" %> 
<%@ page import = "java.util.ResourceBundle" %>
<%@page import="com.okta.custom.logout.util.LogoutHelper" %>
<%@page import="com.okta.custom.logout.util.AWSOperations" %>
<%@ page session="true" %>
<html>
<head>
<title>Custom Logout Page</title>
 <!-- Bootstrap core CSS -->
        <link href="<%=request.getContextPath() %>/resources/css/bootstrap.min.css" rel="stylesheet">
</head>
<!-- use the Okta widget to power authentication! -->
    <script src="https://ok1static.oktacdn.com/assets/js/sdk/okta-signin-widget/2.6.0/js/okta-sign-in.
min.js" type="text/javascript"></script>
<script src="<%=request.getContextPath() %>/resources/js/jquery-3.3.1.js"></script>
<%
final AWSOperations awsOperations = new AWSOperations();
Map oktaDetails = awsOperations.getOktaMap();
final String oktaBaseURL = (String)oktaDetails.get(LogoutHelper.OKTA_BASE_URL);
final String clientID = (String)oktaDetails.get(LogoutHelper.OIDC_CLIENT_ID);
final String issuer = (String)oktaDetails.get(LogoutHelper.ISSUER);
%>
<script type="text/javascript">

  
  $(document).ready(function(){
	  
	  var oktaSignIn = new OktaSignIn({
		    baseUrl: '<%=oktaBaseURL%>',
		    clientId: '<%=clientID%>',
		    authParams: {
		      issuer: '<%=issuer%>',
		      responseType: ["token", "id_token"],
		      display: "page"
		    }
		  });
	  
	  oktaSignIn.session.get(function (res) {
	      // If we get here, the user is already signed in.
	     var userID = res.userId;	 
	     document.getElementById("userid").value = userID;  
	     var token = '<%= session.getAttribute("csrfToken") %>';
	     document.getElementById("token").value = token; 
	     $("form").submit();   
	    });
	  
	  
		 
		  
  });
 
</script>
<body>

<div class="container">
 <div  class="row justify-content-center align-items-center">
 <div class='centertext'><img class="mb-4" src="<%=request.getContextPath() %>/resources/images/Loading.gif" width="50%"></div>
</div>
</div>
	<form action="user" method="post">
	    <input type="hidden" name="userid" id="userid" />
	    <input type="hidden" name="token" id="token" />	    
	</form>
	
</body>
</html>