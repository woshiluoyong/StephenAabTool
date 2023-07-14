#coding=utf-8
#coding:utf-8
#!/usr/bin/python
# -*- coding: UTF-8 -*-

import pip
import os
import sys
import platform
import subprocess
import time
import itertools
import threading
import urllib
import json
import random

try:
    import requests
except ImportError as e:
    #pip.main(['install', pkg])
    os.system('pip install requests')# 注意 pip 和 pip3 在使用时的区别
    import requests

isDebug = False
useJdkPath = ''
useAdbPath = ''
useBundleToolPath = ''
useAabPath = ''

useJksPath = ''
useJksPwd = ''
useJksAlias = ''
useJksAliasPwd = ''

selectDevice = None
isLoadingDone = False

curScriptFolder = '../'

#执行命令核心方法
def execute_shell_command(cmd):
  inputCmd = str(cmd)
  returnContentAry = []
  try:
    return_info = subprocess.Popen(inputCmd, shell=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
    while True:
        next_line = return_info.stdout.readline()
        return_line = next_line.decode("utf-8", "ignore")
        returnContentAry.append(return_line)
        if return_line == '' and return_info.poll() != None:
          break
        if True == isDebug:
          print('=====Stephen==googleAabPkgInstaller======>执行命令【'+cmd+' 】实时输出:', return_line)
    returncode = return_info.wait()
    print('=====Stephen==googleAabPkgInstaller======>执行命令返回code:', returncode)
    if returncode:
      showOrHideLoadingAnim(False)
      print('=====Stephen==googleAabPkgInstaller======>错误执行命令【'+cmd+'】,请手动执行命令确定具体错误原因:', return_info.stderr.read().decode())
      sys.exit(0)
  except Exception as err:
    showOrHideLoadingAnim(False)
    print('=====Stephen==googleAabPkgInstaller======>异常执行命令【'+cmd+'】,请手动执行命令确定具体错误原因:', err)
    sys.exit(0)
  return returnContentAry

#执行loading
def execLoadingAnim():
  global isLoadingDone
  for c in itertools.cycle(['|', '/', '-', '\\']):
    if isLoadingDone:
        break
    sys.stdout.write('\rLoading Data...' + c)
    sys.stdout.flush()
    time.sleep(0.1)

#显示/隐藏loading
def showOrHideLoadingAnim(isShow):
  global isLoadingDone
  if True == isShow:
    isLoadingDone = False
    t = threading.Thread(target=execLoadingAnim)
    t.start()
  else:
    isLoadingDone = True

def execuAabPkgOperation(isUseJks, isDelJks):
  print('=====Stephen==googleAabPkgInstaller======>执行AAB包解压到APKS开始...')#, useJksPath, useJksPwd, useJksAlias, useJksAliasPwd)
  showOrHideLoadingAnim(True)
  print('\n')
  outputTmpApks = (curScriptFolder + os.sep + os.path.basename(useAabPath).replace('.aab','.apks'))
  if os.path.exists(outputTmpApks):
    os.remove(outputTmpApks)
  returnContentAry = execute_shell_command(((useJdkPath + ' -jar ' + useBundleToolPath) if useBundleToolPath.lower().endswith('.jar') else useBundleToolPath)
  +' build-apks --bundle='+useAabPath+' --output='+outputTmpApks
  +((' --ks='+useJksPath+' --ks-pass=pass:'+useJksPwd+' --ks-key-alias='+useJksAlias+' --key-pass=pass:'+useJksAliasPwd) if True == isUseJks else ''))
  showOrHideLoadingAnim(False)
  if True == isDelJks and os.path.exists(useJksPath):
    os.remove(useJksPath)
  print('=====Stephen==googleAabPkgInstaller======>执行APKS包安装到设备开始,请注意查看手机上是否提示安装确认弹框,如有,请确认...')
  showOrHideLoadingAnim(True)
  print('\n')
  returnContentAry = execute_shell_command(((useJdkPath + ' -jar ' + useBundleToolPath) if useBundleToolPath.lower().endswith('.jar') else useBundleToolPath)
  +' install-apks --apks='+outputTmpApks+((' --adb='+useAdbPath) if (useAdbPath != 'adb') else '')+' --device-id='+selectDevice)
  showOrHideLoadingAnim(False)
  if os.path.exists(outputTmpApks):
    os.remove(outputTmpApks)
  print('=====Stephen==googleAabPkgInstaller======>恭喜💐,全部安装操作完成!!')

#初始化方法
def initExecuteFun():
  global curScriptFolder, isDebug, useJdkPath, useAdbPath, useBundleToolPath, useAabPath, useJksPath, useJksPwd, useJksAlias, useJksAliasPwd, selectDevice
  osName = platform.system()
  curScriptFolder = sys.path[0]
  print('=====Stephen==googleAabPkgInstaller======>初始化入参:', sys.argv, osName, curScriptFolder)
  if len(sys.argv) > 1:
    for param in sys.argv:
      if param is not None:
        if param == 'debug':
          isDebug = True
        elif 'jdk=' in param:
          useJdkPath = param.split('=')[1]
        elif 'adb=' in param:
          useAdbPath = param.split('=')[1]
        elif 'bundletool=' in param:
          useBundleToolPath = param.split('=')[1]
        elif 'aab=' in param:
          useAabPath = param.split('=')[1]
  if useJdkPath is not None and len(useJdkPath) <= 0:
    useJdkPath = 'java'
  if useAdbPath is not None and len(useAdbPath) <= 0:
    useAdbPath = 'adb'
  if useBundleToolPath is not None and len(useBundleToolPath) <= 0:
    useBundleToolPath = 'bundletool'
  print('=====Stephen==googleAabPkgInstaller======>初始化操作:isDebug='+str(isDebug)+';useJdkPath='+useJdkPath+';useAdbPath='+useAdbPath+';useBundleToolPath='+useBundleToolPath+';useAabPath='+useAabPath)

  print('=====Stephen==googleAabPkgInstaller======>开始检查java命令是否可以执行...\n(快捷下载Jdk源:\nhttps://www.injdk.cn/\nhttp://www.codebaoku.com/jdk/jdk-index.html)')
  returnContentAry = execute_shell_command(useJdkPath+' -version')
  print('=====Stephen==googleAabPkgInstaller======>恭喜💐,可以执行java命令!')

  print('=====Stephen==googleAabPkgInstaller======>开始检查adb命令是否可以执行...\n(快捷下载Adb源:\nhttps://www.androiddevtools.cn/)')
  returnContentAry = execute_shell_command(useAdbPath+' version')
  print('=====Stephen==googleAabPkgInstaller======>恭喜💐,可以执行adb命令!')

  print('=====Stephen==googleAabPkgInstaller======>开始检查bundleTool命令是否可以执行...\n(快捷下载BundleTool源:\nhttps://github.com/google/bundletool/releases)')
  returnContentAry = execute_shell_command(((useJdkPath + ' -jar ' + useBundleToolPath) if useBundleToolPath.lower().endswith('.jar') else useBundleToolPath)+' version')
  print('=====Stephen==googleAabPkgInstaller======>恭喜💐,可以执行bundleTool命令!')

  if useAabPath is not None and len(useAabPath) <= 0:
    useAabPath = input('=====Stephen==googleAabPkgInstaller======>检测到执行时没有带上待安装的AAB包绝对路径,请补充输入:\n')
    useAabPath = useAabPath.strip()
    if useAabPath is not None and len(useAabPath) <= 0:
      print('=====Stephen==googleAabPkgInstaller======>补充输入的待安装AAB包绝对路径仍然为空,请重新执行输入!')
      sys.exit(0)

  useAabPath = useAabPath.strip()
  if not useAabPath.lower().endswith('.aab'):
      print('=====Stephen==googleAabPkgInstaller======>输入的待安装AAB包文件必须为aab文件,请重新执行输入!')
      sys.exit(0)
  if not os.path.exists(useAabPath):
      print('=====Stephen==googleAabPkgInstaller======>输入的待安装AAB包文件不存在,请重新执行输入!')
      sys.exit(0)
  print('=====Stephen==googleAabPkgInstaller======>待安装AAB包文件:',os.path.basename(useAabPath))

  print('=====Stephen==googleAabPkgInstaller======>开始执行连接设备数检查...')
  returnContentAry = execute_shell_command(useAdbPath+' devices')
  print('=====Stephen==googleAabPkgInstaller======>执行连接设备数检查:', returnContentAry)
  onlineDeviceAry = []
  for info in returnContentAry:
    if '\t' in info:
      onlineDeviceAry.append(info.split('\t')[0])
  onlineDeviceNum = len(onlineDeviceAry)
  if onlineDeviceNum <= 0:
    print('=====Stephen==googleAabPkgInstaller======>连接设备数为0,请先连接一个Android设备再试')
    sys.exit(0)
  elif onlineDeviceNum > 1:
    waitSelectInfo = ''
    for index, onlineDevice in enumerate(onlineDeviceAry):
      waitSelectInfo += str(index)+'.'+onlineDevice+'\n'

    selectDevIndex = input('=====Stephen==googleAabPkgInstaller======>连接设备数为'+str(onlineDeviceNum)+',请输入编号选择一个待安装的Android设备\n'+waitSelectInfo)
    try:
      selectDevIndex = int(selectDevIndex)
      if selectDevIndex < 0 or selectDevIndex >= onlineDeviceNum:
        raise ValueError("输入编号范围错误")
    except Exception as err:
      print('=====Stephen==googleAabPkgInstaller======>输入选择的编号【'+str(selectDevIndex)+' 】不正确,输入编号数字范围0~'+str(onlineDeviceNum-1))
      sys.exit(0)
    selectDevice = onlineDeviceAry[selectDevIndex]
    print('=====Stephen==googleAabPkgInstaller======>选择待安装的Android设备:',selectDevice)
  else:
    selectDevIndex = 0
    selectDevice = onlineDeviceAry[selectDevIndex]
    print('=====Stephen==googleAabPkgInstaller======>连接设备数为1,默认选择待安装的Android设备:',selectDevice)
  
  selectSignTypeIndex = input('=====Stephen==googleAabPkgInstaller======>请输入编号选择是否需要使用对应签名文件解包AAB文件,如未选择正确可能解包失败或安装错误!\n0.不使用签名\n1.使用公司签名文件\n2.使用本地签名文件\n')
  try:
    selectSignTypeIndex = int(selectSignTypeIndex)
    if selectDevIndex < 0 or selectDevIndex >= 2:
      raise ValueError("输入编号范围错误")
  except Exception as err:
    print('=====Stephen==googleAabPkgInstaller======>输入选择的编号【'+str(selectSignTypeIndex)+' 】不正确,输入编号数字范围0~2')
    sys.exit(0)
  print('=====Stephen==googleAabPkgInstaller======>选择解包AAB文件对应的签名类型编号:'+str(selectSignTypeIndex))
  if 1 == selectSignTypeIndex:
    getSignUrl = input('=====Stephen==googleAabPkgInstaller======>请输入获取公司签名文件的网络地址(返回的签名文件Json格式需按文档定义,否则读取失败!):')
    getSignUrl = getSignUrl.strip()
    if getSignUrl is None:
      print('=====Stephen==googleAabPkgInstaller======>输入获取公司签名文件的网络地址为空,请输入!')
      sys.exit(0)
    else:
      print("=====Stephen==googleAabPkgInstaller======>获取公司签名文件信息中...")
      showOrHideLoadingAnim(True)
      print('\n')
      try:
        loadSignResult = requests.get(getSignUrl)
        showOrHideLoadingAnim(False)
      except Exception as err:
        showOrHideLoadingAnim(False)
        print("=====Stephen==googleAabPkgInstaller======>获取公司签名文件信息异常,", err)
        sys.exit(0)
      if True == isDebug:
        print("=====Stephen==googleAabPkgInstaller======>获取公司签名文件信息:", loadSignResult.text)
      try:
        if loadSignResult.text is not None:
          jsonObj = json.loads(loadSignResult.text)
          if jsonObj is not None and True == jsonObj['isSuccess'] and jsonObj['dataInfo'] is not None:
              jksDataInfo = jsonObj['dataInfo']
              jksDataInfoNum = len(jksDataInfo)
              if jksDataInfoNum > 0:
                if True == isDebug:
                  print("=====Stephen==googleAabPkgInstaller======>获取公司签名文件信息成功:", jksDataInfo)
                waitSelectInfo = ''
                for index, jksData in enumerate(jksDataInfo):
                  waitSelectInfo += str(index)+'.'+jksData['jksName']+'\n'
                selectJksIndex = input('=====Stephen==googleAabPkgInstaller======>获取公司签名文件数为'+str(jksDataInfoNum)+',请输入编号选择使用一个签名文件,如未选择正确可能解包失败或安装错误!\n'+waitSelectInfo)
                try:
                  selectJksIndex = int(selectJksIndex)
                  if selectJksIndex < 0 or selectJksIndex >= jksDataInfoNum:
                    raise ValueError("输入编号范围错误")
                except Exception as err:
                  print('=====Stephen==googleAabPkgInstaller======>输入选择的编号【'+str(selectJksIndex)+' 】不正确,输入编号数字范围0~'+str(jksDataInfoNum-1))
                  sys.exit(0)
                print('=====Stephen==googleAabPkgInstaller======>选择使用的公司签名文件:'+jksDataInfo[selectJksIndex]['jksName'])
                downloadJksUrl = jksDataInfo[selectJksIndex]['jksFilePath']
                if downloadJksUrl is None:
                  print('=====Stephen==googleAabPkgInstaller======>选择使用的公司签名文件下载路径为空,请检查接口返回数据!')
                  sys.exit(0)
                outputJksPath = curScriptFolder + os.sep + os.path.basename(urllib.parse.urlparse(downloadJksUrl).path)
                print('=====Stephen==googleAabPkgInstaller======>选择使用的公司签名文件内容数据输出路径：'+outputJksPath+'=====>下载链接:'+downloadJksUrl)
                downloadJksResult = requests.get(downloadJksUrl, stream=True, timeout=20)
                with open(outputJksPath, "wb") as code:
                    code.write(downloadJksResult.content)
                if os.path.exists(outputJksPath):
                    useJksPath = outputJksPath
                    useJksPwd = jksDataInfo[selectJksIndex]['jksFilePwd']
                    useJksAlias = jksDataInfo[selectJksIndex]['jksAlias']
                    useJksAliasPwd = jksDataInfo[selectJksIndex]['jksAliasPwd']
                    execuAabPkgOperation(True, True)
                else:
                    print("=====Stephen==googleAabPkgInstaller======>下载公司签名文件信息失败,请检查接口返回数据")
                    sys.exit(0)
              else:
                print("=====Stephen==googleAabPkgInstaller======>获取公司签名文件信息为空,请检查接口返回数据")
                sys.exit(0)
          else:
              print("=====Stephen==googleAabPkgInstaller======>获取公司签名文件信息失败,请检查接口返回数据")
              sys.exit(0)
        else:
            print("=====Stephen==googleAabPkgInstaller======>获取公司签名文件信息异常,请检查接口是否可用")
            sys.exit(0)
      except Exception as err:
        showOrHideLoadingAnim(False)
        print("=====Stephen==googleAabPkgInstaller======>解码公司签名文件返回信息异常,", err)
        sys.exit(0)
  elif 2 == selectSignTypeIndex:
    useJksPath = input('=====Stephen==googleAabPkgInstaller======>请输入本地签名jks文件的绝对路径:')
    useJksPath = useJksPath.strip()
    if useJksPath is None:
      print('=====Stephen==googleAabPkgInstaller======>输入本地签名jks文件的绝对路径为空,请输入!')
      sys.exit(0)
    if not os.path.exists(useJksPath):
      print('=====Stephen==googleAabPkgInstaller======>输入的本地签名jks文件不存在,请重新执行输入!')
      sys.exit(0)
    useJksPwd = input('=====Stephen==googleAabPkgInstaller======>请输入本地签名jks文件的密码:')
    useJksPwd = useJksPwd.strip()
    if useJksPwd is None:
      print('=====Stephen==googleAabPkgInstaller======>输入本地签名jks文件的密码为空,请输入!')
      sys.exit(0)
    useJksAlias = input('=====Stephen==googleAabPkgInstaller======>请输入本地签名jks文件的别名:')
    useJksAlias = useJksAlias.strip()
    if useJksAlias is None:
      print('=====Stephen==googleAabPkgInstaller======>输入本地签名jks文件的别名为空,请输入!')
      sys.exit(0)
    useJksAliasPwd = input('=====Stephen==googleAabPkgInstaller======>请输入本地签名jks文件的别名密码:')
    useJksAliasPwd = useJksAliasPwd.strip()
    if useJksAliasPwd is None:
      print('=====Stephen==googleAabPkgInstaller======>输入本地签名jks文件的别名密码为空,请输入!')
      sys.exit(0)
    execuAabPkgOperation(True, False)
  else:
    execuAabPkgOperation(False, False)
      
#init
initExecuteFun()
#execute_shell_command("java -jar d:\GameDownload\\bundletool-all-1.15.1.jar install-apks --apks=d:\GameDownload\\app-googleplay2023-05-31_092354.apks --device-id=8CLX1QYTK")
