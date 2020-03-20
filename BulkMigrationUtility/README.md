# Bulk Migration Utility

## Utility Usage

The utility helps in migration of users from any external system to Okta. For example, this utility can be used to migrate users from Novell eDirectory to Okta. The utility jar can be placed on any server on the external system (Ex: Novell eDirectory) and its methods can be invoked to perform specific operations in Okta.

The Utility contains 3 main classes:

1. OktaMigrator – This class takes the data from external source, formulates it if needed and calls the respective method in CloudUser class.
2. CloudUser – This class is responsible for posting data to Okta. It makes call to Okta api and sends back response to the OktaMigrator class.
3. OktaApi – Customized api class which connects to Okta and sends the response back to CloudUser class.

The utility also contains bean classes like User, Factor and RestResponse. It also includes a test class called TestUtility which contains methods for testing of create user operations.

## Bulk Migration

The method createUser from OktaMigrator class will be used to create users in Okta. This method can be called in loop so that bulk users can be migrated to Okta.

The method takes 4 arguments
* apiUrl – Url of the Okta tenant
* apiKey – Admin token for the Okta tenant
* json – User details as Json object
* loginDisabled – This is a Boolean field. If it is true, the user will be created in Staged mode. If false, the user will be created in Active mode.
