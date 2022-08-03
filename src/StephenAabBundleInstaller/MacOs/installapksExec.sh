#!/bin/bash
argSize=$#
echo "=====Stephen========>EnterParam:$*;argSize:$argSize"
if (( $argSize < 2 )); then
    echo "=====Stephen========>The parameter is missing"
	exit
fi
echo "=====Stephen========>The parameter: $0 ===> $1 ===> $2 ===> $3"

apks_location=$1
if [ -z "$apks_location" ]; then
	echo "=====Stephen========>The param:apks_location is missing"
	exit
fi

if [ ! -e $apks_location ]; then
	echo "=====Stephen========>To install apks file: $apks_location does not exist!"
	exit
fi

bundlejar_location=$2
if [ -z "$bundlejar_location" ]; then
	echo "=====Stephen========>The param:bundlejar_location is missing"
	exit
fi

if [ ! -e $bundlejar_location ]; then
	echo "=====Stephen========>To param: $bundlejar_location does not exist!"
	exit
fi

adb_location=$3
if [ -z "$adb_location" ]; then
    echo "=====Stephen========>Install apks..."
    java -jar $bundlejar_location install-apks --allow-downgrade --allow-test-only --apks=$apks_location
else
    echo "=====Stephen========>Install apks(custom adb path:$adb_location)..."
    java -jar $bundlejar_location install-apks --allow-downgrade --allow-test-only --apks=$apks_location --adb=$adb_location
fi

echo "=====Stephen========>Install Operation Completed!"
echo "=====Stephen========>execute Finish"