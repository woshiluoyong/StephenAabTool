#!/bin/bash
argSize=$#
echo "=====Stephen========>EnterParam:$*;argSize:$argSize"
if (( $argSize < 7 )); then
    echo "=====Stephen========>The parameter is missing"
	exit
fi
echo "=====Stephen========>The parameter: $0 ===> $1 ===> $2 ===> $3 ===> $4 ===> $5 ===> $6 ===> $7"

app_bundle=$1
if [ -z "$app_bundle" ]; then
	echo "=====Stephen========>The param:app_bundle is missing"
	exit
fi

if [ ! -e $app_bundle ]; then
	echo "=====Stephen========>To aab file: $app_bundle does not exist!"
	exit
fi

apks_folder=$2
if [ -z "$apks_folder" ]; then
	echo "=====Stephen========>The param:apks_folder is missing"
	exit
fi
echo "=====Stephen========>Check to create the output directory: $apks_folder"
mkdir -p -m 777 $apks_folder

if [ ! -e $apks_folder ]; then
	echo "=====Stephen========>Check to create the output directory Error"
	echo "=====Stephen========>Exit."
	exit
fi

ks_location=$3
if [ -z "$ks_location" ]; then
	echo "=====Stephen========>The param:jks_location is missing"
	exit
fi

if [ ! -e $ks_location ]; then
	echo "=====Stephen========>To jks file: $ks_location does not exist!"
	exit
fi

ks_pwd=$4
if [ -z "$ks_pwd" ]; then
	echo "=====Stephen========>The param:jks_pwd is missing"
	exit
fi

ks_alias=$5
if [ -z "$ks_alias" ]; then
	echo "=====Stephen========>The param:jks_alias is missing"
	exit
fi

ks_alias_pwd=$6
if [ -z "$ks_alias_pwd" ]; then
	echo "=====Stephen========>The param:jks_alias_pwd is missing"
	exit
fi

bundlejar_location=$7
if [ -z "$bundlejar_location" ]; then
	echo "=====Stephen========>The param:bundlejar_location is missing"
	exit
fi

if [ ! -e $bundlejar_location ]; then
	echo "=====Stephen========>To param: $bundlejar_location does not exist!"
	exit
fi

apks_file_name=$(basename "$app_bundle" ".aab")
echo "=====Stephen========>Ready to generate apks file name: $apks_file_name"

apks_location="$apks_folder/${apks_file_name}.apks"
echo "=====Stephen========>Ready to generate apks file Path: $apks_location"

[ -e $apks_location ] && rm $apks_location

echo "=====Stephen========>Is dealing with app bundle apks..."
java -jar $bundlejar_location build-apks --overwrite --bundle=$app_bundle --output=$apks_location --ks=$ks_location --ks-pass=pass:$ks_pwd --ks-key-alias=$ks_alias --key-pass=pass:$ks_alias_pwd

if [ ! -e $apks_location ]; then
	echo "=====Stephen========>generated apk error, Please Retry"
	echo "=====Stephen========>Finish."
	exit
fi

echo "=====Stephen========>Generate apks completed, generated path:$apks_folder"
echo "=====Stephen========>execute Finish"