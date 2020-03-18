# On Premises Provisioning Utility - Okta to Ldap

## Utility Usage

This utility built on Okta Provisioning Connector SDK, is used to provision and update users from Okta to any on-premise application like LDAP which doesn’t support SCIM. When a user is assigned to an on-prem application in Okta, it creates a provisioning event. The OPP agent installed will poll Okta for the provisioning agent which is translated to a SCIM request by the agent. This is received by our utility which acts as a SCIM server to process the SCIM messages and return response back to the agent.
This utility is built using maven as a war file. This war is deployed on any webserver(Ex: Tomcat)

This Utility contains 3 main classes:
1. SCIMServiceImpl – This is the main class which implements SCIMService interface. It provides various methods 
    * afterCreation – This method initializes the variables used for provisioning along with setting the context used to create connection with LDAP.
    * createUser – This method creates the user in LDAP. It is invoked when a POST is made to /Users with a SCIM payload representing a user to be created. 
    * updateUser – This method updates the user in LDAP. It is invoked when a PUT is made to /Users/{id} with the SCIM payload representing a user to be updated. This method also compares the values between Okta and LDAP and calls update only if the user attributes are changed. This was mainly needed to avoid infinite loop of update in our scenario where LDAP in turn was triggering events to Okta.
    * getUsers – This method is invoked when Okta sends an instruction to get a set of users from LDAP. It supports querying based on a start index and the maximum number of results expected by the client. 
    * getUser – This method is invoked when Okta sends an instruction to get a particular user from LDAP.
    * createGroup – This method creates a group in LDAP. It is invoked when a POST is made to /Groups with a SCIM payload representing a group to be created.
    * updateGroup – This method updates a group in LDAP. It invoked when a PUT is made to /Groups/{id} with the SCIM payload representing a group to be updated.
    * getGroups – This method is invoked when Okta sends an instruction to get a set of groups. It supports querying based on a start index and the maximum number of results expected by the client.
    * getGroup – This method is used to get a group from LDAP. It is invoked when a GET is made to /Groups/{id}.
    * deleteGroup – This method is used to delete a group from LDAP. It is invoked when a DELETE is made to /Groups/{id}.
    * getImplementedUserManagementCapabilities – This method is used to get all the Okta User Management capabilities that this SCIM Service has implemented. It is invoked when a GET is made to /ServiceProviderConfigs. It is called only when you are testing or modifying your connector configuration from the Okta Application instance UM UI.
2. SCIMServiceHelper – This is the helper class which holds the constants used in the code base.
3. SCIMServiceUtil – This class contains all utility methods used for the implementation of the connector.

It also contains a properties file called ‘connector.properties’ placed on the webserver. It contains ldap connection details, Okta to LDAP user core and custom attribute mapping, group core attribute mapping, Okta application name, Okta schema name, etc., 

## How To Enable SSL

Supporting SSL for Https connections would involve
1. Generating a key for the SCIM Server and exporting a certificate.
2. Importing the certificate exported above into the trust store of the Okta Provisioning Agent.

You can follow the simple steps below to enable SSL using self-signed certificates. If you wish to have better security and use certificates signed by trusted third-parties, you can follow below steps to import such a certificate into the trust store of the Okta Provisioning Agent. 
1. Generate a key

    ```
    keytool -genkey -alias scim_tom -keyalg RSA -keystore /root/scim_tomcat_keystore
    Enter keystore password:
    Re-enter new password:
    What is your first and last name?
      [Unknown]:  localhost
    What is the name of your organizational unit?
      [Unknown]:  IT
    What is the name of your organization?
      [Unknown]:  MyCompany
    What is the name of your City or Locality?
      [Unknown]:  sf
    What is the name of your State or Province?
      [Unknown]:  ca
    What is the two-letter country code for this unit?
      [Unknown]:  us
    Is CN=K0208, OU=eng, O=okta, L=sf, ST=ca, C=us correct?
      [no]:  yes

    Enter key password for <scim_tom>
    (RETURN if same as keystore password):
    ```

      NOTE : The answer to the first question "What is your first and last name?" should be "localhost" if your Tomcat server is going         to be accessed through the localhost URL.        If your Tomcat Server will be accessed through an IP (For example :                     https://10.11.12.13:8443/), you should execute the following command in the place of the above command to generate the key. 
      (Note that the command below should be executed from a Java 7 installation. The option "-ext san" to specify IPs in the                  SubjectAltNames is available only in Java 7) 
  
      *$JAVA_7_HOME/bin/keytool -genkey -alias scim_tom -ext san=ip:10.11.12.13 -keyalg RSA -keystore /root/scim_tomcat_keystore*

2. Go to $TOMCAT_HOME/conf/server.xml and enable SSL - Use the configuration below which asks Tomcat to use the keystore /root/scim_tomcat_keystore (Generated above)
   ```
   <Connector port="8443" protocol="HTTP/1.1" SSLEnabled="true"
               maxThreads="150" scheme="https" secure="true"
               clientAuth="false" sslProtocol="TLS"
               keystoreFile="/root/scim_tomcat_keystore"
               keystorePass="changeit" />
               ```
3. Start tomcat and check you can reach the server over https
4. Export the public certificate out of the keystore generated in step 1
   *keytool -export -keystore /root/scim_tomcat_keystore -alias scim_tom -file /root/scim_tomcat.cert*
   Enter keystore password:
   Certificate stored in file </root/scim_tomcat.cert>
5. Import this certificate into the trust store of the Okta Provisioning Agent so that it can trust Tomcat server and the connection is secure.

    Note that you need to execute this command on the machine where the Okta Provisioning Agent is installed
    
    */opt/OktaProvisioningAgent/jre/bin/keytool -import -file /root/scim_tomcat.cert -alias scim_tom -keystore /opt/OktaProvisioningAgent/jre/lib/security/cacerts* 
   


