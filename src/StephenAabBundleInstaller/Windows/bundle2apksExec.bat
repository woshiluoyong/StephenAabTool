@echo off
set argSize=0
for %%x in (%*) do Set /A argSize+=1
echo "=====Stephen========>EnterParam:(%*);argSize:%argSize%"
if %argSize% lss 7 (
    echo "=====Stephen========>The parameter is missing"
    exit
)
echo "=====Stephen========>The parameter: %1 ===> %2 ===> %3 ===> %4 ===> %5 ===> %6 ===> %7"

set app_bundle=%1
if "%app_bundle%" == "" (
	echo "=====Stephen========>The param:app_bundle is missing"
	exit
)

if not exist %app_bundle% (
	echo "=====Stephen========>To aab file: %app_bundle% does not exist!"
	exit
)

set apks_folder=%2
if "%apks_folder%" == "" (
	echo "=====Stephen========>The param:apks_folder is missing"
	exit
)
echo "=====Stephen========>Check to create the output directory: %apks_folder%"
md %apks_folder%

if not exist %apks_folder% (
	echo "=====Stephen========>Check to create the output directory Error"
	echo "=====Stephen========>Exit."
	exit
)

set ks_location=%3
if "%ks_location%" == "" (
	echo "=====Stephen========>The param:jks_location is missing"
	exit
)

if not exist %ks_location% (
	echo "=====Stephen========>To jks file: %ks_location% does not exist!"
	exit
)

set ks_pwd=%4
if "%ks_pwd%" == "" (
	echo "=====Stephen========>The param:jks_pwd is missing"
	exit
)

set ks_alias=%5
if "%ks_alias%" == "" (
	echo "=====Stephen========>The param:jks_alias is missing"
	exit
)

set ks_alias_pwd=%6
if "%ks_alias_pwd%" == "" (
	echo "=====Stephen========>The param:jks_alias_pwd is missing"
	exit
)

set bundlejar_location=%7
if "%bundlejar_location%" == "" (
	echo "=====Stephen========>The param:bundlejar_location is missing"
	exit
)

if not exist %bundlejar_location% (
	echo "=====Stephen========>To param: %bundlejar_location% does not exist!"
	exit
)

set apks_file_name
set appSuffix
for %%I in (%app_bundle%) do set apks_file_name=%%~nI
for %%I in (%app_bundle%) do set appSuffix=%%~xI
echo "=====Stephen========>Ready to generate apks file name: %apks_file_name%"

set apks_location="%apks_folder%\%apks_file_name%.apks"
echo "=====Stephen========>Ready to generate apks file Path: %apks_location%"

if exist %apks_location% (
  del /Q/F %apks_location%
)

echo "=====Stephen========>Is dealing with app bundle apks..."
java -jar %bundlejar_location% build-apks --overwrite --bundle=%app_bundle% --output=%apks_location% --ks=%ks_location% --ks-pass=pass:%ks_pwd% --ks-key-alias=%ks_alias% --key-pass=pass:%ks_alias_pwd%

if not exist %apks_location% (
	echo "=====Stephen========>generated apk error, Please Retry"
	echo "=====Stephen========>Finish."
	exit
)

echo "=====Stephen========>Generate apks completed, generated path:%apks_folder%"
echo "=====Stephen========>execute Finish"