@echo off
set argSize=0
for %%x in (%*) do Set /A argSize+=1
echo "=====Stephen========>EnterParam:(%*);argSize:%argSize%"
if %argSize% lss 2 (
    echo "=====Stephen========>The parameter is missing"
    exit
)
echo "=====Stephen========>The parameter: %1 ===> %2 ===> %3"

set apks_location=%1
if "%apks_location%" == "" (
	echo "=====Stephen========>The param:apks_location is missing"
	exit
)

if not exist %apks_location% (
	echo "=====Stephen========>To install apks file: %apks_location% does not exist!"
	exit
)

set bundlejar_location=%2
if "%bundlejar_location%" == "" (
	echo "=====Stephen========>The param:bundlejar_location is missing"
	exit
)

if not exist %bundlejar_location% (
	echo "=====Stephen========>To param: %bundlejar_location% does not exist!"
	exit
)

set adb_location=%3
if "%adb_location%" == "" (
    echo "=====Stephen========>Install apks..."
    java -jar %bundlejar_location% install-apks --allow-downgrade --allow-test-only --apks=%apks_location%
) else (
    echo "=====Stephen========>Install apks(custom adb path:%adb_location%)..."
    java -jar %bundlejar_location% install-apks --allow-downgrade --allow-test-only --apks=%apks_location% --adb=%adb_location%
)

echo "=====Stephen========>Install Operation Completed!"
echo "=====Stephen========>execute Finish"
pause
exit