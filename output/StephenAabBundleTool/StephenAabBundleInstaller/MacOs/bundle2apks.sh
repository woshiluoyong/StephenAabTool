bundlejar_location="$(readlink -f ../)/libs/bundletool.jar"
apks_folder="$(readlink -f .)/output/"

echo "检查创建输出目录: $apks_folder"
mkdir -p -m 777 $apks_folder

if [ ! -e $apks_folder ]; then
	echo "\033[;31;1m检查创建输出目录出错!\033[0m"
	echo "已退出."
	exit
fi

echo "请输入待安装app bundle(xx.aab)的文件路径："
read app_bundle
echo "请输入签名文件(jks_location)的文件路径："
read ks_location
echo "请输入签名文件(jks_pwd)的签名密码："
read ks_pwd
echo "请输入签名文件(jks_alias)的签名别名："
read ks_alias
echo "请输入签名文件(jks_alias_pwd)的签名别名密码："
read ks_alias_pwd

apks_file_name=$(basename "$app_bundle" ".aab")
echo "准备生成的apks文件名: $apks_file_name"

apks_location="$apks_folder${apks_file_name}.apks"
echo "\033[;35;1m准备生成的apks文件输出路径: $apks_location\033[0m"

[ -e $apks_location ] && rm $apks_location


echo "正在处理app bundle 转 apks..."
java -jar $bundlejar_location build-apks --overwrite --bundle=$app_bundle --output=$apks_location --ks=$ks_location --ks-pass=pass:$ks_pwd --ks-key-alias=$ks_alias --key-pass=pass:$ks_alias_pwd

if [ ! -e $apks_location ]; then
	echo "\033[;31;1m生成apks出错!\033[0m"
	echo "已退出."
	exit
fi

echo "\033[;36;1m生成apks完成\033[0m, 生成路径:$apks_folder"
echo "执行结束"