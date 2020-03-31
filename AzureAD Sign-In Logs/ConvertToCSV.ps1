#Replace {Path_to_dir} with the actual Path
$dirpath='{Path_to_dir}'
$completedpath='{Path_to_dir}\BACKUP'
$FINALOUT='{Path_to_dir}\MERGED'
$logdate=Get-Date -Format "_MM_dd_yyyy" 
$tmpfolder='{Path_to_dir}\TEMP'
$folderdate="Logs"+$logdate

$EXPORT_PATH=$dirpath+$folderdate
$READPATH='{Path_to_dir}\READ'

$properties=@('id','createdDateTime','userPrincipalName','appDisplayName','clientAppUsed','resourceDisplayName','status')
foreach($file in Get-ChildItem $READPATH)
{
$pathToOutputFile=$tmpfolder+"\"+$file.BaseName+".csv"
Write-host $file.FullName
$json = (Get-Content $file.FullName -Raw) | ConvertFrom-Json
$json.psobject.properties.Where({$_.name -eq 'value'}).value | Select-Object -Property $properties | ConvertTo-Csv -NoTypeInformation | Set-Content $pathToOutputFile
Move-Item $file.FullName $completedpath
}


(Get-Content $tmpfolder\signinlogs_full_*.csv | Select-Object -Skip 1) | Set-Content $FINALOUT\final_log$logdate.csv

Move-Item $tmpfolder\signinlogs_full_*.csv $EXPORT_PATH