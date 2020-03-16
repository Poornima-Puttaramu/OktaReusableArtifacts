# Live Sync and Bulk Migration Utility

## Utility Usage

The utility helps in migration of users from any external system to Okta.This utility further helps in synchronization between the external system and Okta.  For example, this utility can be used to migrate/synchronize from Novell eDirectory to Okta. The utility jar can be placed on any server on the external system (Ex: Novell eDirectory) and its methods can be invoked to perform specific operations in Okta.

The Utility contains 3 main classes:

1. OktaMigrator – This class takes the data from external source, formulates it if needed and calls the respective method in CloudUser class.
2. CloudUser – This class is responsible for posting data to Okta. It makes call to Okta api and sends back response to the OktaMigrator class.
3. OktaApi – Customized api class which connects to Okta and sends the response back to CloudUser class.

The utility also contains bean classes like User, Group, Factor and RestResponse. It also includes a test class called TestUtility which contains methods for testing each of the operations.

## Bulk Migration

The method createUser from OktaMigrator class will be used to create users in Okta. This method can be called in loop so that bulk users can be migrated to Okta.

The method takes 4 arguments
* apiUrl – Url of the Okta tenant
* apiKey – Admin token for the Okta tenant
* json – User details as Json object
* loginDisabled – This is a Boolean field. If it is true, the user will be created in Staged mode. If false, the user will be created in Active mode.

## Live Synchronization

Once the initial load is migrated to Okta from an external system, the utility jar can be used to synchronize the external system with Okta.

Basic Parameters used in Live synchronization operations:

* apiUrl – Url of the Okta tenant
* apiKey – Admin token for the Okta tenant
* json – User details as Json object
* loginDisabled –Boolean field, if it is true, the user will be created in Staged mode. If false, the user will be created in Active mode.
* oktaId – ID of the user in Okta 
* login – Login of the user in Okta
* grpJson – Json containing Group details
* groupId – ID of the group in Okta

**Table below articulates the operations and their respective parameters**


| Sl No | Operation | Parameters |
| ----- | --------- | ---------- |
| 1 | Create User | apiUrl, apiKey, json,loginDisabled |
| 2 | Update User | apiUrl, apiKey, json, oktaId |
| 3 | Assign Application to User | apiUrl, apiKey, appId, oktaId |
| 4 | Delete User by OktaID | apiUrl, apiKey, oktaId |
| 5 | Delete User by Login | apiUrl, apiKey, login |
| 6 | Activate User | apiUrl, apiKey, oktaId |
| 7 | DeActivate User | apiUrl, apiKey, oktaId |
| 8 | Suspend User | apiUrl, apiKey, oktaId |
| 9 | Unsuspend User | apiUrl, apiKey, oktaId |
| 10 | Create Group | apiUrl, apiKey, grpJson |
| 11 | Add User to Group | apiUrl, apiKey, groupId, oktaId |
| 12 | Remove User from Group | apiUrl, apiKey, groupId, oktaId |
| 13 | Delete Group | apiUrl, apiKey, groupId |
