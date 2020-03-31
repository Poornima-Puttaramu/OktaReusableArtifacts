#File path for JSON and CSV



$thtimespan =  (Get-Date).AddMinutes(50)
#Replace {Path_to_dir} with the actual Path
$dirpath='{Path_to_dir}'
 
# Converting from zulu to CST (5)
#$APIStarttime=(Get-Date).AddHours(4).ToString('yyyy-MM-ddTHH:mm:ss')
#$APIEndtime=(Get-Date).AddHours(6).ToString('yyyy-MM-ddTHH:mm:ss')

		#Hardcode start and stop time
		#Enter Dallas Time (start and stop)


$APIStarttime=(New-Object DateTime 2020,2,21,05,00,0, ([DateTimeKind]::UTC)).AddHours(6).ToString('yyyy-MM-ddTHH:mm:ss')
$APIEndtime=(New-Object DateTime 2020,2,21,06,00,0, ([DateTimeKind]::UTC)).AddHours(6).ToString('yyyy-MM-ddTHH:mm:ss')

Write-Host "DALLAS  Time : " (Get-Date).AddHours(0).ToString('yyyy-MM-ddTHH:mm:ss')
Write-Host " "
Write-Host "Start Time : " $APIStarttime
Write-Host " "
Write-Host "End Time : " $APIEndtime
Write-Host " "
$logdate=Get-Date -Format "_MM_dd_yyyy" 

$folderdate="Logs"+$logdate

$EXPORT_PATH=$dirpath+$folderdate

 if(!(Test-Path -Path $EXPORT_PATH)) { 
   New-Item $folderdate -type directory
}

Write-Host $thtimespan

#SignIn logs API URI


$loguri='https://graph.microsoft.com/v1.0/auditLogs/signIns?$top=5000&$filter=createdDateTime gt '+$APIStarttime+'.0Z and createdDateTime le '+$APIEndtime+'.0Z'
 
# Tim's request ----  Lax updated - 
#Change start time and end time.  to custom run.
#comment the above #loguri and uncomment the below.
#$loguri='https://graph.microsoft.com/v1.0/auditLogs/signIns?$top=5000&$filter=createdDateTime gt '+$APIStarttime+'.0Z and createdDateTime le '+$APIEndtime+'.0Z'
 

# Create app of type Web app / API in Azure AD, generate a Client Secret, and update the client id and client secret here
$ClientID = "******"
 
$ClientSecret ="********" 
 
$loginURL = "https://login.microsoftonline.com"
#Replace {microsoft_tenant} with the actual tenant name
$tenantdomain = "{microsoft_tenant}"
 
# Replace {tenantGUID} with the tenant GUID from Properties | Directory ID under the Azure Active Directory section
$TenantGUID = "{tenantGUID}"
 
$resource = "https://manage.office.com"
 
$body = @{grant_type="client_credentials";client_id=$ClientID;client_secret=$ClientSecret;scope="https://graph.microsoft.com/.default"}
$oauth="";

#Counter
$counter=1

function GetSigninlogs($headerParams,$loguri)
{

try{
$signinjsonlogs = Invoke-WebRequest -Method GET -Headers $headerParams -Uri $loguri 

Write-Host $counter
$fileformat=Get-Date -Format "dd_MM_yy_HH_mm_ss"
$pathToJsonFile=$dirpath+"\READ\signinlogs_full_"+$fileformat+".json"

$signinjsonlogs | Set-Content $pathToJsonFile


$json = (Get-Content $pathToJsonFile -Raw) | ConvertFrom-Json

$nextlink=$json.psobject.properties.Where({$_.name -eq '@odata.nextLink'}).value
if($nextlink -ne $null)
{
$counter++

 
Write-Host "inside loop-"$counter
 

if((get-date) -gt $thtimespan)
{
Write-host "Token going to expire so refershing Token"
$thtimespan =  (Get-Date).AddMinutes(45)
runscript $nextlink
}

GetSigninlogs $headerParams $nextlink
}

}
 catch [Exception]
    {
      Write-Host $_.Exception.GetType().FullName, $_.Exception.Message
	  Write-Host "inside loop exception block-"$counter
	  Start-Sleep -s 360
	  GetSigninlogs $headerParams $nextlink
    }

}
 
function runscript($loguri)
{

$oauth = Invoke-RestMethod -Method Post -Uri $loginURL/$tenantdomain/oauth2/v2.0/token -Body $body
 
$headerParams = @{'Authorization'="$($oauth.token_type) $($oauth.access_token)"}

GetSigninlogs $headerParams $loguri

#$json.psobject.properties.Where({$_.name -eq 'value'}).value | ConvertTo-Csv -NoTypeInformation | Set-Content $pathToOutputFile
}

runscript $loguri

 

 




 
