bundlejar_location="$(readlink -f ../)/libs/bundletool.jar"

echo "请输入待安装apks的文件路径："
read apks_location

if [ ! -e $apks_location ]; then
	echo "\033[;31;1m待安装apks文件不存在!\033[0m"
	echo "已退出."
	exit
fi

echo "正在安装apks..."
java -jar $bundlejar_location install-apks --allow-downgrade --allow-test-only --apks=$apks_location

echo "\033[;36;1m安装操作结束\033[0m"
echo "执行结束"