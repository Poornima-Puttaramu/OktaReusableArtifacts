# SLO Utility

## AWS Resource Details
The below table contains the details of the AWS resources involved in SLO module.
These resources needs to exported as CFT templates and migrated to the other environment(s).
| AWS Resources | Name of the Resources |
| ------------- | --------------------- |
| DynamoDB      | <ul><li>Application_Details</li><li>OktaDetails</li></ul> |
| Elastic Beanstalk | CustomLogoutApp |
| SecretsManager | OKTA_DEV_TOKEN  (The name can be changed as per the environment) |
| IAM Roles | <ul><li>access_secret_manager_role</li><li>aws-elasticbeanstalk-ec2-role</li><li>aws-elasticbeanstalk-service-role</li><li>AWSServiceRoleForAutoScaling</li><li>AWSServiceRoleForElasticLoadBalancing</li></ul> |

## OpenID setup in Okta
A “dummy” Single Page OpenID application has to be created in Okta.Set up an Open ID Application in by choosing the platform as “Single Page App”

By creating this application in Okta, we will use this application’s Client ID which is needed to determine userid of current user’s session.
Okta JS library provides the logic to do the sign in process and get the current user’s session and this needs a Client ID of an application configured in Okta..
Using current user’s session, we can determine userid from Okta for current user session and use i in further processing with SysLogs API to get all applications user has signed into.

### Detailed Steps to the above task

1. Log into Okta as an Administrator.
2. Click on the Applications tab in the Okta Dashboard
