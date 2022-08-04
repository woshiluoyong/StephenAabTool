package StephenAabBundleInstaller;

import StephenAabBundleInstaller.org.json.JSONArray;
import StephenAabBundleInstaller.org.json.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.Timer;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

//aab包安装器主界面;author:Stephen
public class StephenAabMain extends JFrame {
    private String osFolderName;
    private String tempFolderPathStr;
    private String rootAssetsPathStr;
    private String rootFolderPathStr;
    private String builtInAdbPathStr;
    private String aabPathStr;
    private String jksPathStr;
    private JRadioButton radioForBuiltInAdb, radioForSysAdb, radioForCustomizeAdb;
    private JTabbedPane tabBarPane;
    private JTextField labelForJksResult;
    private JTextField inputForJksPwd, inputForJksAlias, inputForJksAliasPwd, inputForJksAdb;
    private JTextArea textAreaForLog;
    private ButtonGroup btnGroupForJunYun;
    private String aabName = null;
    private ArrayList<EntityForJksBean> entityForJksBeanArrayList = new ArrayList<>();
    private EntityForJksBean useJksBean = new EntityForJksBean();

    public StephenAabMain(String rootPathStr, boolean isWinOs, String curOsFolderName) {
        System.out.println("==StephenAabMain==rootPathStr=======>"+rootPathStr+"===curOsFolderName==>"+curOsFolderName);
        osFolderName = curOsFolderName;
        rootFolderPathStr = rootPathStr+(rootPathStr.endsWith(File.separator) ? "" : File.separator)+ "StephenAabBundleInstaller" +File.separator;
        tempFolderPathStr = rootFolderPathStr+"temp"+File.separator;
        if(!(new File(tempFolderPathStr).exists()))(new File(tempFolderPathStr)).mkdirs();
        rootAssetsPathStr = rootFolderPathStr+"assets"+File.separator;
        builtInAdbPathStr = rootFolderPathStr+osFolderName+File.separator+"adb-tool"+File.separator+(isWinOs ? "adb.exe" : "adb");

        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();

        float width = (float)screen.width/2.5f;
        float height = (float)screen.height/1.5f;

        setLayout(new FlowLayout());

        FileDialog fdForAab = new FileDialog(this, "选择待安装Aab包路径", FileDialog.LOAD);
        fdForAab.setFilenameFilter((dir, name) -> name.endsWith(".aab"));
        JLabel labelForAab = new JLabel("选择安装 Aab 包路径:");
        JButton btnForAab = new JButton("选择");
        JTextField labelForAabResult = new JTextField("", 40);
        labelForAabResult.setEnabled(false);
        btnForAab.addActionListener(e -> {
            fdForAab.setVisible(true);
            if(isStrNotEmpty(fdForAab.getDirectory()) && isStrNotEmpty(fdForAab.getFile())){
                aabName = fdForAab.getFile();
                aabPathStr = fdForAab.getDirectory()+aabName;
                textAreaForLog.append("==============选择的Aab包===aabName:"+aabName+"===>aabPathStr:"+aabPathStr+"\n");
                labelForAabResult.setText(aabPathStr);
            }else{
                labelForAabResult.setText(isStrNotEmpty(aabPathStr) ? aabPathStr : "未选择");
            }
        });
        Box boxForAab = Box.createHorizontalBox();
        boxForAab.add(labelForAab);
        boxForAab.add(Box.createHorizontalStrut(8));
        boxForAab.add(btnForAab);
        boxForAab.add(Box.createHorizontalStrut(8));
        boxForAab.add(labelForAabResult);
        JPanel aabForPanel = new JPanel();
        aabForPanel.setBackground(Color.ORANGE);
        aabForPanel.add(boxForAab);

        tabBarPane = new JTabbedPane();
        tabBarPane.setForeground(Color.BLACK);
        tabBarPane.addTab("自定义解包签名文件", new ImageIcon(rootAssetsPathStr+"icon_custom.png"), createJksComponent());
        tabBarPane.addTab("俊云快速解包签名文件", new ImageIcon(rootAssetsPathStr+"icon_junyun.png"), createJunYunComponent());
        tabBarPane.setSelectedIndex(0);

        Box boxForExecute = Box.createHorizontalBox();
        JButton btnForClear = new JButton("重置所有内容", new ImageIcon(rootAssetsPathStr+"icon_clear.png"));
        btnForClear.addActionListener(e -> {
            aabPathStr = null;
            jksPathStr = null;
            labelForAabResult.setText("");
            labelForJksResult.setText("");
            inputForJksPwd.setText("");
            inputForJksAlias.setText("");
            inputForJksAliasPwd.setText("");
            inputForJksAdb.setText("");
            textAreaForLog.setText("打印日志处,等待操作中...\n");
            JOptionPane.showMessageDialog(null, "重置完成!", "提示", JOptionPane.INFORMATION_MESSAGE);
        });
        boxForExecute.add(btnForClear);
        boxForExecute.add(Box.createHorizontalStrut(8));
        JButton btnForStart = new JButton("开始执行安装", new ImageIcon(rootAssetsPathStr+"icon_install.png"));
        btnForStart.addActionListener(e -> {
            textAreaForLog.setText("开始执行中...\n");
            if(radioForCustomizeAdb.isSelected() && isStrEmpty(inputForJksAdb.getText())){
                JOptionPane.showMessageDialog(null, "请选择精确的可用的adb路径", "提示", JOptionPane.WARNING_MESSAGE);
                return;
            }//end of if
            if(!checkAdbSourceAvailable(false)){
                JOptionPane.showMessageDialog(null, "你选择的adb经测试不可用,请先确认或切换adb源", "提示", JOptionPane.WARNING_MESSAGE);
                return;
            }//end of if
            if(checkAdbDevicesAvailable(false) <= 0){
                JOptionPane.showMessageDialog(null, "adb没有成功连接上设备,请先连接", "提示", JOptionPane.WARNING_MESSAGE);
                return;
            }//end of if
            if(checkAdbDevicesAvailable(false) > 1){
                JOptionPane.showMessageDialog(null, "adb连接超过了1台设备,请先断开多余的设备", "提示", JOptionPane.WARNING_MESSAGE);
                return;
            }//end of if
            if(isStrEmpty(aabPathStr)){
                JOptionPane.showMessageDialog(null, "请选择待安装Aab包路径", "提示", JOptionPane.WARNING_MESSAGE);
                return;
            }//end of if
            if(0 == tabBarPane.getSelectedIndex()){
                if(isStrEmpty(jksPathStr)){
                    JOptionPane.showMessageDialog(null, "请选择签名文件Jks路径", "提示", JOptionPane.WARNING_MESSAGE);
                    return;
                }//end of if
                if(isStrEmpty(inputForJksPwd.getText())){
                    JOptionPane.showMessageDialog(null, "请输入签名文件Jks密码", "提示", JOptionPane.WARNING_MESSAGE);
                    return;
                }//end of if
                if(isStrEmpty(inputForJksAlias.getText())){
                    JOptionPane.showMessageDialog(null, "请输入签名文件Jks别名", "提示", JOptionPane.WARNING_MESSAGE);
                    return;
                }//end of if
                if(isStrEmpty(inputForJksAliasPwd.getText())){
                    JOptionPane.showMessageDialog(null, "请输入签名文件Jks别名密码", "提示", JOptionPane.WARNING_MESSAGE);
                    return;
                }//end of if
                useJksBean.jksFilePath = jksPathStr;
                useJksBean.jksFilePwd = inputForJksPwd.getText();
                useJksBean.jksAlias = inputForJksAlias.getText();
                useJksBean.jksAliasPwd = inputForJksAliasPwd.getText();
            }else if(1 == tabBarPane.getSelectedIndex()){
                int curSelectJksIndex = getCustomizeJksSelectIndex();
                if(curSelectJksIndex < 0){
                    JOptionPane.showMessageDialog(null, "请先成功获取签名文件然后选择一个签名文件", "提示", JOptionPane.WARNING_MESSAGE);
                    return;
                }//end of if
                String saveJksPath = tempFolderPathStr+(tempFolderPathStr.endsWith(File.separator) ? "" : File.separator)+"selectJks.jks";
                (new File(saveJksPath)).delete();
                EntityForJksBean originUseJksBean = entityForJksBeanArrayList.get(curSelectJksIndex);
                downloadForUrl(originUseJksBean.jksFilePath, saveJksPath);
                if(!(new File(saveJksPath)).exists()){
                    JOptionPane.showMessageDialog(null, "签名文件不存在,请重试!", "提示", JOptionPane.WARNING_MESSAGE);
                    return;
                }//end of if
                useJksBean.jksFilePath = saveJksPath;
                useJksBean.jksFilePwd = originUseJksBean.jksFilePwd;
                useJksBean.jksAlias = originUseJksBean.jksAlias;
                useJksBean.jksAliasPwd = originUseJksBean.jksAliasPwd;
            }//end of if
            sureStartExecute();
        });
        boxForExecute.add(btnForStart);

        Box boxForLog = Box.createHorizontalBox();
        textAreaForLog = new JTextArea("打印日志处,等待操作中...\n", 20, 10);
        textAreaForLog.setBackground(Color.BLACK);
        textAreaForLog.setLineWrap(true);
        textAreaForLog.setForeground(Color.WHITE);
        JScrollPane textPanelForLog = new JScrollPane(textAreaForLog);
        textPanelForLog.setAutoscrolls(true);
        textPanelForLog.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);//取消显示水平滚动条
        textPanelForLog.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);//显示垂直滚动条
        boxForLog.add(textPanelForLog);

        Box baseBox = Box.createVerticalBox();
        baseBox.add(createTextPanel("Qy Google Aab Package Quick Installer", 30));
        baseBox.add(Box.createVerticalStrut(2));
        baseBox.add(createTextPanel("请先确保手机已经打开[开发者模式]->[调试模式],并且已经连接电脑成功!", 12));
        baseBox.add(Box.createVerticalStrut(15));
        baseBox.add(createAdbComponent());
        baseBox.add(Box.createVerticalStrut(10));
        baseBox.add(aabForPanel);
        baseBox.add(Box.createVerticalStrut(5));
        baseBox.add(tabBarPane);
        baseBox.add(Box.createVerticalStrut(5));
        baseBox.add(boxForExecute);
        baseBox.add(Box.createVerticalStrut(5));
        baseBox.add(boxForLog);
        baseBox.add(Box.createVerticalStrut(5));
        add(baseBox);

        textAreaForLog.append("====rootPathStr=======>"+rootPathStr+"===curOsFolderName==>"+curOsFolderName);
        setTitle("GoogleAab包安装器(Running on "+System.getProperty("os.name")+")");
        setSize((int)width, (int)height);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setVisible(true);
    }

    private JComponent createTextPanel(String text, int textSize) {
        JPanel panel = new JPanel(new GridLayout(1, 1));//使用一个1行1列的网格布局,为了让标签的宽高自动撑满面板
        JLabel label = new JLabel(text);
        label.setFont(new Font(null, Font.PLAIN, textSize));
        label.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(label);
        return panel;
    }

    private JComponent createAdbComponent(){
        FileDialog fdForAdb = new FileDialog(this, "选择精确的Adb命令文件路径", FileDialog.LOAD);
        fdForAdb.setFilenameFilter((dir, name) -> name.equals("adb"));
        JLabel labelForAdb = new JLabel("选择或输入Adb命令路径:");
        JButton btnForAdb = new JButton("选择");
        btnForAdb.addActionListener(e -> {
            fdForAdb.setVisible(true);
            if(isStrNotEmpty(fdForAdb.getDirectory()) && isStrNotEmpty(fdForAdb.getFile())){
                String adbPathStr = fdForAdb.getDirectory()+fdForAdb.getFile();
                textAreaForLog.append("==============选择的Adb执行路径===>"+adbPathStr+"\n");
                inputForJksAdb.setText(adbPathStr);
            }else{
                String adbPathStr = inputForJksAdb.getText();
                textAreaForLog.append("==============复用的上传选择的Adb执行路径===>"+adbPathStr+"\n");
                inputForJksAdb.setText(isStrNotEmpty(adbPathStr) ? adbPathStr : "");
            }
        });
        inputForJksAdb = new JTextField(40);
        Box boxForAdb = Box.createHorizontalBox();
        boxForAdb.add(labelForAdb);
        boxForAdb.add(Box.createHorizontalStrut(8));
        boxForAdb.add(btnForAdb);
        boxForAdb.add(Box.createHorizontalStrut(8));
        boxForAdb.add(inputForJksAdb);

        JLabel labelForAdbHint = new JLabel("为避免后续流程安装中断,可点击右边的测试按钮检测你现在选择来源的adb是否可用");
        JButton btnForAdbTest = new JButton("点我可测试Adb可用性");
        btnForAdbTest.addActionListener(e -> {
            if(radioForCustomizeAdb.isSelected() && isStrEmpty(inputForJksAdb.getText())){
                JOptionPane.showMessageDialog(null, "请选择精确的adb路径", "提示", JOptionPane.WARNING_MESSAGE);
                return;
            }//end of if
            if(checkAdbSourceAvailable(true))checkAdbDevicesAvailable(true);
        });
        Box boxForAdbHint = Box.createHorizontalBox();
        boxForAdbHint.add(labelForAdbHint);
        boxForAdbHint.add(Box.createHorizontalStrut(8));
        boxForAdbHint.add(btnForAdbTest);

        JPanel adbForPanel = new JPanel();
        adbForPanel.setBackground(Color.pink);
        adbForPanel.add(boxForAdb);
        JPanel adbHintForPanel = new JPanel();
        adbHintForPanel.setBackground(Color.pink);
        adbHintForPanel.add(boxForAdbHint);

        JLabel boxForAdbHint2 = new JLabel("注:adb文件路径是在AndroidSdk的platform-tools目录下,e.g.:/Users/stephen/Library/Android/sdk/platform-tools/adb");
        JPanel adbHint2ForPanel = new JPanel();
        adbHint2ForPanel.setBackground(Color.pink);
        adbHint2ForPanel.add(boxForAdbHint2);
        bindJLabelCopyFun(boxForAdbHint2);

        JPanel panelForAdbSource = new JPanel();
        panelForAdbSource.setBackground(Color.pink);
        JLabel labelForHint = new JLabel("请选择安装adb使用来源:");
        radioForBuiltInAdb = new JRadioButton("本软件内建Adb");
        radioForSysAdb = new JRadioButton("本机系统环境Adb");
        radioForCustomizeAdb = new JRadioButton("自定义设置Adb");
        ButtonGroup adbSourceGroup = new ButtonGroup();
        adbSourceGroup.add(radioForBuiltInAdb);
        adbSourceGroup.add(radioForSysAdb);
        adbSourceGroup.add(radioForCustomizeAdb);
        radioForBuiltInAdb.setSelected(true);
        panelForAdbSource.add(labelForHint);
        panelForAdbSource.add(radioForBuiltInAdb);
        panelForAdbSource.add(radioForSysAdb);
        panelForAdbSource.add(radioForCustomizeAdb);
        ActionListener actionListener = e -> {
            adbForPanel.setVisible(e.getSource() == radioForCustomizeAdb);
            adbHint2ForPanel.setVisible(e.getSource() == radioForCustomizeAdb);
        };
        radioForBuiltInAdb.addActionListener(actionListener);
        radioForSysAdb.addActionListener(actionListener);
        radioForCustomizeAdb.addActionListener(actionListener);

        Box baseBox = Box.createVerticalBox();
        baseBox.add(panelForAdbSource);
        baseBox.add(adbForPanel);
        baseBox.add(adbHint2ForPanel);
        baseBox.add(adbHintForPanel);

        adbForPanel.setVisible(false);
        adbHint2ForPanel.setVisible(false);
        return baseBox;
    }
    private JComponent createJksComponent() {
        FileDialog fdForJks = new FileDialog(this, "选择签名文件Jks路径", FileDialog.LOAD);
        fdForJks.setFilenameFilter((dir, name) -> name.endsWith(".jks"));
        JLabel labelForJks = new JLabel("选择签名文件Jks路径:");
        JButton btnForJks = new JButton("选择");
        labelForJksResult = new JTextField("", 40);
        labelForJksResult.setEnabled(false);
        btnForJks.addActionListener(e -> {
            fdForJks.setVisible(true);
            if(isStrNotEmpty(fdForJks.getDirectory()) && isStrNotEmpty(fdForJks.getFile())){
                jksPathStr = fdForJks.getDirectory()+fdForJks.getFile();
                textAreaForLog.append("==============选择的Jks路径===>"+jksPathStr+"\n");
                labelForJksResult.setText(jksPathStr);
            }else{
                labelForJksResult.setText(isStrNotEmpty(jksPathStr) ? jksPathStr : "未选择");
            }
        });
        Box boxForJks = Box.createHorizontalBox();
        boxForJks.add(labelForJks);
        boxForJks.add(Box.createHorizontalStrut(8));
        boxForJks.add(btnForJks);
        boxForJks.add(Box.createHorizontalStrut(8));
        boxForJks.add(labelForJksResult);

        JLabel labelForJksPwd = new JLabel("输入签名文件Jks密码:");
        inputForJksPwd = new JTextField(40);
        Box boxForJksPwd = Box.createHorizontalBox();
        boxForJksPwd.add(labelForJksPwd);
        boxForJksPwd.add(Box.createHorizontalStrut(8));
        boxForJksPwd.add(inputForJksPwd);

        JLabel labelForJksAlias = new JLabel("输入签名文件Jks别名:");
        inputForJksAlias = new JTextField(40);
        Box boxForJksAlias = Box.createHorizontalBox();
        boxForJksAlias.add(labelForJksAlias);
        boxForJksAlias.add(Box.createHorizontalStrut(8));
        boxForJksAlias.add(inputForJksAlias);

        JLabel labelForJksAliasPwd = new JLabel("输入Jks文件别名密码:");
        inputForJksAliasPwd = new JTextField(40);
        Box boxForJksAliasPwd = Box.createHorizontalBox();
        boxForJksAliasPwd.add(labelForJksAliasPwd);
        boxForJksAliasPwd.add(Box.createHorizontalStrut(8));
        boxForJksAliasPwd.add(inputForJksAliasPwd);

        Box baseBox = Box.createVerticalBox();
        baseBox.add(boxForJks);
        baseBox.add(Box.createVerticalStrut(5));
        baseBox.add(boxForJksPwd);
        baseBox.add(Box.createVerticalStrut(5));
        baseBox.add(boxForJksAlias);
        baseBox.add(Box.createVerticalStrut(5));
        baseBox.add(boxForJksAliasPwd);
        baseBox.add(Box.createVerticalStrut(5));
        return baseBox;
    }

    private JComponent createJunYunComponent() {
        JLabel labelForJunYun = new JLabel("请在后面输入链接信息获取俊云产品签名:");
        JButton btnForJunYun = new JButton("获取");
        JTextField labelForJunYunRwd = new JTextField(40);
        labelForJunYunRwd.setEnabled(true);

        Box boxForJunYun = Box.createHorizontalBox();
        boxForJunYun.add(labelForJunYun);
        boxForJunYun.add(Box.createHorizontalStrut(8));
        boxForJunYun.add(btnForJunYun);
        boxForJunYun.add(Box.createHorizontalStrut(8));
        boxForJunYun.add(labelForJunYunRwd);

        JLabel labelForJunYunHint = new JLabel("请点击按钮获取,获取成功下面区域将展示俊云产品签名数据,然后就可以选择啦!");
        Box boxForJunYunHint = Box.createHorizontalBox();
        boxForJunYunHint.add(labelForJunYunHint);

        JPanel panelForJunYun = new JPanel();
        btnForJunYun.addActionListener(e -> {
            if(isStrEmpty(labelForJunYunRwd.getText())){
                JOptionPane.showMessageDialog(null, "请先输入俊云链接信息", "提示", JOptionPane.WARNING_MESSAGE);
                return;
            }//end of if
            btnGroupForJunYun = null;
            panelForJunYun.removeAll();
            panelForJunYun.setVisible(false);
            entityForJksBeanArrayList.clear();
            labelForJunYunHint.setText("获取俊云产品签名数据中,请稍后");
            String httpRetInfo = httpGetRequest(labelForJunYunRwd.getText(), new HashMap<>(){{
                put("otherParam", "stephen");
            }});
            textAreaForLog.append("==============获取俊云产品签名数据===>"+httpRetInfo+"\n");
            if(isStrEmpty(httpRetInfo)){
                labelForJunYunHint.setText("获取俊云产品签名数据为空,请重试");
            }else{
                JSONObject jsonObject = new JSONObject(httpRetInfo);
                if(null != jsonObject){
                    if(jsonObject.has("isSuccess")){
                        JSONArray jsonArray = jsonObject.has("dataInfo") ? jsonObject.getJSONArray("dataInfo") : null;
                        if(null != jsonArray && jsonArray.length() > 0){
                            btnGroupForJunYun = new ButtonGroup();
                            int addOkNum = 0;
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject jsonObjectForJsk = jsonArray.getJSONObject(i);
                                if(null != jsonObjectForJsk){
                                    JRadioButton radioBtnForJks = new JRadioButton(jsonObjectForJsk.getString("jksName"));
                                    btnGroupForJunYun.add(radioBtnForJks);
                                    radioBtnForJks.setSelected(0 == i);
                                    panelForJunYun.add(radioBtnForJks);
                                    entityForJksBeanArrayList.add(new EntityForJksBean(jsonObjectForJsk.getString("jksName"),
                                            jsonObjectForJsk.getString("jksFilePath") ,jsonObjectForJsk.getString("jksFilePwd"),
                                            jsonObjectForJsk.getString("jksAlias"), jsonObjectForJsk.getString("jksAliasPwd")));
                                    addOkNum++;
                                }//end of if
                            }
                            if(addOkNum > 0){
                                labelForJunYunHint.setText("获取俊云产品签名成功,共"+addOkNum+"款,请选择!");
                                panelForJunYun.add(new JLabel("请选择产品签名:"), 0);
                                panelForJunYun.setVisible(true);
                            }else{
                                labelForJunYunHint.setText("解析返回的俊云产品签名为空!");
                            }
                        }else{
                            labelForJunYunHint.setText("获取返回的俊云产品签名为空!");
                        }
                    }else{
                        labelForJunYunHint.setText("获取俊云产品签名返回数据失败,"+(jsonObject.has("dataInfo") ? jsonObject.getString("dataInfo") : "请重试"));
                    }
                }else{
                    labelForJunYunHint.setText("获取俊云产品签名返回数据为空,请重试");
                }
            }
        });

        Box baseBox = Box.createVerticalBox();
        baseBox.add(Box.createVerticalStrut(10));
        baseBox.add(boxForJunYun);
        baseBox.add(Box.createVerticalStrut(10));
        baseBox.add(boxForJunYunHint);
        baseBox.add(Box.createVerticalStrut(10));
        baseBox.add(panelForJunYun);
        baseBox.add(Box.createVerticalStrut(10));

        panelForJunYun.setVisible(false);
        return baseBox;
    }

    private int getCustomizeJksSelectIndex(){
        int tmpIndex = -1, curIndex = -1;
        if(null == btnGroupForJunYun)return curIndex;
        Enumeration<AbstractButton> enumeration = btnGroupForJunYun.getElements();
        while (enumeration.hasMoreElements()){
            if(tmpIndex < 0)tmpIndex = 0;
            JRadioButton radioBtnForJks = (JRadioButton)enumeration.nextElement();
            if(radioBtnForJks.isSelected())curIndex = tmpIndex;
            textAreaForLog.append("=====radioBtnForJks===tmpIndex:"+tmpIndex+"===curIndex:"+curIndex+"==>"+radioBtnForJks.isSelected()+"\n");
            tmpIndex++;
        }
        return curIndex;
    }

    private String getCurAdbSourceCommand(){
        String curAdbCommand = "";
        if(radioForBuiltInAdb.isSelected()){
            curAdbCommand = builtInAdbPathStr;
        }else if(radioForSysAdb.isSelected()){
            curAdbCommand = "adb";
        }else if(radioForCustomizeAdb.isSelected()){
            curAdbCommand = inputForJksAdb.getText();
        }
        return curAdbCommand;
    }
    private boolean checkAdbSourceAvailable(boolean isHintDialog){
        String curAdbCommand = getCurAdbSourceCommand();
        boolean adbTestResult = execProcessBuilder(curAdbCommand, "version").isExecResult;
        textAreaForLog.append("==========adb可用性测试结果=======>"+adbTestResult+"\n");
        if(isHintDialog)JOptionPane.showMessageDialog(null, adbTestResult ? "恭喜,adb检测可用" : "抱歉,adb检测不可用,请确认adb", "提示",
                adbTestResult ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE);
        return adbTestResult;
    }

    private int checkAdbDevicesAvailable(boolean isHintDialog){
        String curAdbCommand = getCurAdbSourceCommand();
        EntityForAdbRes adbDeviceResult = execProcessBuilder(curAdbCommand, "devices");
        textAreaForLog.append("==========adb设备连接测试结果=======>"+adbDeviceResult.isExecResult+"=====>"+adbDeviceResult.execResultStr+"\n");
        int connectNum = 0;
        if(adbDeviceResult.isExecResult && isStrNotEmpty(adbDeviceResult.execResultStr)){
            String[] execResultStrAry = adbDeviceResult.execResultStr.split("\n");
            if(null != execResultStrAry && execResultStrAry.length > 0){
                if(isStrNotEmpty(execResultStrAry[0]) && execResultStrAry[0].contains("List of devices attached"))connectNum = execResultStrAry.length - 1;
            }//end of if
        }//end of if
        if(isHintDialog)JOptionPane.showMessageDialog(null, connectNum > 0 ? "恭喜,adb有"+connectNum+"台设备连接" : "抱歉,adb无设备连接,请连接设备", "提示",
                connectNum > 0 ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE);
        return connectNum;
    }

    private void sureStartExecute(){
        String curAdbCommand = getCurAdbSourceCommand();
        textAreaForLog.append("当前使用adb路径:"+curAdbCommand+"\n");
        (new Timer()).schedule(new TimerTask() {
            @Override
            public void run() {
                sureStartExecuteCore(curAdbCommand);
            }
        }, 500);
    }

    private void sureStartExecuteCore(String curAdbCommand){
        boolean execResult = execProcessBuilder(rootFolderPathStr+osFolderName+File.separator+"bundle2apksExec.sh", aabPathStr,
                tempFolderPathStr, useJksBean.jksFilePath, useJksBean.jksFilePwd, useJksBean.jksAlias, useJksBean.jksAliasPwd,
                rootFolderPathStr+"libs"+File.separator+"bundletool.jar").isExecResult;
        textAreaForLog.append("======bundle2apksExec====execResult=>"+execResult+"\n");
        if(execResult) {
            execResult = execProcessBuilder(rootFolderPathStr + osFolderName + File.separator + "installapksExec.sh",
                    tempFolderPathStr + (tempFolderPathStr.endsWith(File.separator) ? "" : File.separator) + aabName.replace(".aab", ".apks"),
                    rootFolderPathStr + "libs" + File.separator + "bundletool.jar", curAdbCommand).isExecResult;
            if(!execResult)JOptionPane.showMessageDialog(null, "抱歉,安装apks到手机失败,请查看日志信息解决再试!", "提示", JOptionPane.ERROR_MESSAGE);
        }else{
            JOptionPane.showMessageDialog(null, "抱歉,解压aab到apks失败,请查看日志信息解决再试!", "提示", JOptionPane.ERROR_MESSAGE);
        }
    }

    private EntityForAdbRes execProcessBuilder(String... commandStr) {
        String commandStrInfo = "";
        if(null != commandStr && commandStr.length > 0){
            if(isStrEmpty(commandStr[0]))return new EntityForAdbRes(false);
            for (int i = 0; i < commandStr.length; i++) {
                commandStrInfo += " " + commandStr[i];
            }//end of for
        }//end of if
        textAreaForLog.append("==execProcess===commandStrInfo=>"+commandStrInfo+"\n");
        if(isStrEmpty(commandStrInfo))return new EntityForAdbRes(false);
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command(commandStr);//带参数需要用可变参数方式设置
        processBuilder.redirectErrorStream(true);
        InputStream inputStream = null;
        try {
            Process start = processBuilder.start();
            inputStream = start.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "utf-8");
            boolean isNotError = true;
            int len = -1;
            char[] c = new char[1024];
            String commandResStr = "";
            while ((len = inputStreamReader.read(c)) != -1) {
                String lineStr = new String(c, 0, len);
                if(isStrNotEmpty(lineStr) && lineStr.toLowerCase().contains("error"))isNotError = false;
                commandResStr += lineStr;
                textAreaForLog.append("======execProcessResult====>isNotError:"+isNotError+"===lineStr:"+lineStr);
            }
            return new EntityForAdbRes(isNotError, commandResStr);
        } catch (Exception e) {
            e.printStackTrace();
            textAreaForLog.append("==execProcess===exception=>"+e.getMessage()+"\n");
            return new EntityForAdbRes(false);
        } finally {
            try {
                if(null != inputStream)inputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void bindJLabelCopyFun(JLabel label){
        label.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                textAreaForLog.append("=====提示=>双击可复制内容\n");
                if(e.getClickCount() == 2) {//双击选中
                    String clipboardStr = label.getText();
                    setClipboardString(clipboardStr);
                    label.setOpaque(true);
                    label.setForeground(Color.BLUE);
                    label.setFont(new Font("Helvetica", Font.BOLD, 14));
                    textAreaForLog.append("=====提示=>已经复制内容,粘贴可使用内容:"+clipboardStr+"\n");
                }//end of if
            }

            @Override
            public void mouseExited(MouseEvent e) {
                label.setOpaque(false);
                label.setForeground(Color.BLACK);
                label.setFont(new Font("Helvetica", Font.PLAIN, 14));
            }
        });
    }

    private void setClipboardString(String str) {
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();//获取协同剪贴板，单例
        Transferable trans = new StringSelection(str);//封装文本内容
        clipboard.setContents(trans, null);//把文本内容设置到系统剪贴板上
    }

    private boolean isStrEmpty(String str){
        return null == str || str.length() <= 0;
    }

    private boolean isStrNotEmpty(String str){
        return null != str && str.length() > 0;
    }

    private String httpGetRequest(final String urlOnlyStr, final Map<String, String> paramMap){
        if(isStrEmpty(urlOnlyStr)) {
            return "{\"isSuccess\": false, \"dataInfo\": \"request url is empty\"}";
        }//end of if
        StringBuffer paramSb = new StringBuffer();
        if (null != paramMap && paramMap.size() > 0) {
            for (String key : paramMap.keySet()) {
                if (isStrNotEmpty(key) && null != paramMap.get(key) && paramMap.get(key) instanceof String && isStrNotEmpty(paramMap.get(key).toString())) {
                    paramSb.append((0 == paramSb.length() ? "" : "&")+key+"="+paramMap.get(key).toString());
                }//end of if
            }//end of for
        }//end of if
        String urlString = urlOnlyStr + (urlOnlyStr.contains("?") ? "&" : "?") + paramSb.toString();
        textAreaForLog.append("=====doGet===>请求Url:" + urlString+"\n");
        URL url;
        HttpURLConnection httpURLConnection = null;
        int resCode = -1;
        try {
            url = new URL(urlString);
            httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestMethod("GET");
            httpURLConnection.setConnectTimeout(5000);
            httpURLConnection.setReadTimeout(8000);
            httpURLConnection.setRequestProperty("Accept", "application/json"); // 设置接收数据的格式
            httpURLConnection.setRequestProperty("Content-Type", "application/json"); // 设置发送数据的格式
            InputStream is = null;
            resCode = httpURLConnection.getResponseCode();
            if (resCode == HttpURLConnection.HTTP_OK) {
                is = httpURLConnection.getInputStream();
            } else {
                is = httpURLConnection.getErrorStream();
            }
            BufferedReader bf = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
            StringBuffer buffer = new StringBuffer();
            String line = "";
            while ((line = bf.readLine()) != null) {
                buffer.append(line);
            }
            bf.close();
            is.close();
            String infoStr = buffer.toString();
            if (resCode == HttpURLConnection.HTTP_OK) {
                return infoStr;
            } else {
                return "{\"isSuccess\": false, \"errCode\": "+resCode+",\"dataInfo\": "+infoStr+"}";
            }
        } catch (Exception e) {
            return "{\"isSuccess\": false, \"errCode\": -1,\"dataInfo\": "+e.getMessage()+"}";
        } finally {
            if (httpURLConnection != null) httpURLConnection.disconnect();
        }
    }

    private File downloadForUrl(String downloadUrl, String saveFilePath){
        textAreaForLog.append("======Stephen============>下载url："+downloadUrl+"===>存储位置："+saveFilePath+"\n");
        File file = new File(saveFilePath);
        if (!file.getParentFile().exists())file.getParentFile().mkdirs();
        FileOutputStream fileOut = null;
        HttpURLConnection conn = null;
        InputStream inputStream = null;
        try {
            textAreaForLog.append("======Stephen============>下载中..."+"\n");
            URL httpUrl=new URL(downloadUrl);
            conn=(HttpURLConnection) httpUrl.openConnection();
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setUseCaches(false);
            conn.connect();
            inputStream=conn.getInputStream();
            BufferedInputStream bis = new BufferedInputStream(inputStream);
            fileOut = new FileOutputStream(saveFilePath);
            BufferedOutputStream bos = new BufferedOutputStream(fileOut);
            byte[] buf = new byte[4096];
            int length = bis.read(buf);
            while(length != -1) {
                bos.write(buf, 0, length);
                length = bis.read(buf);
            }//end of while
            bos.close();
            bis.close();
            conn.disconnect();
        } catch (Exception e) {
            file = null;
            textAreaForLog.append("======Stephen============>下载异常："+e.toString()+"\n");
        }
        return file;
    }

    public class EntityForAdbRes{
        boolean isExecResult;
        String execResultStr;

        public EntityForAdbRes(boolean isExecResult) {
            this.isExecResult = isExecResult;
        }

        public EntityForAdbRes(boolean isExecResult, String execResultStr) {
            this.isExecResult = isExecResult;
            this.execResultStr = execResultStr;
        }
    }
    public class EntityForJksBean{
        String jksName;
        String jksFilePath;
        String jksFilePwd;
        String jksAlias;
        String jksAliasPwd;

        public EntityForJksBean() {}

        public EntityForJksBean(String jksName, String jksFilePath, String jksFilePwd, String jksAlias, String jksAliasPwd) {
            this.jksName = jksName;
            this.jksFilePath = jksFilePath;
            this.jksFilePwd = jksFilePwd;
            this.jksAlias = jksAlias;
            this.jksAliasPwd = jksAliasPwd;
        }
    }

    //提取当前运行jar中的指定目录/文件到jar外的指定目录(targetFolderInJarAry 需要从jar中提取的文件目录  这里的地址请参考打成jar之后的目录结构来,destDirStr 提取到jar包外的路径地址)
    private static boolean extractFilesFromJar(String jarFile, String destDirStr, String... targetFolderInJarAry) {
        if(null == jarFile || jarFile.length() <= 0 || null == destDirStr || destDirStr.length() <= 0 || null == targetFolderInJarAry || targetFolderInJarAry.length <= 0){
            System.out.println("====extractFilesFromJar============>关键数据有空值,执行失败");
            return false;
        }//end of if
        if (!jarFile.endsWith(".jar")) {
            System.out.println("====extractFilesFromJar============>获取执行jar失败,请直接执行jar文件");
            return false;
        }//end of if
        boolean isIntegrityOk = true;
        for (int i = 0; i < targetFolderInJarAry.length; i++) {
            if(null != targetFolderInJarAry[i] && !(new File(destDirStr+(destDirStr.endsWith(File.separator) ? "" : File.separator)+targetFolderInJarAry[i]).exists())) {
                isIntegrityOk = false;
                break;
            }//end of if
        }//end of for
        if(isIntegrityOk){
            System.out.println("====extractFilesFromJar============>校验必备运行环境完整,跳过释放文件步骤!");
            return true;
        }//end of if
        System.out.println("====extractFilesFromJar============>校验必备运行环境不完整,执行释放文件步骤...");
        try (JarFile jar = new JarFile(jarFile)) {
            Enumeration enumEntries = jar.entries();
            while (enumEntries.hasMoreElements()) {
                JarEntry jarEntry = (JarEntry) enumEntries.nextElement();
                String name = null != jarEntry ? jarEntry.getName() : null;
                System.out.println("====extractFilesFromJar============>name:"+name);
                String needCopyFolderStr = null;// 根据路径定位目标文件
                for (int i = 0; i < targetFolderInJarAry.length; i++) {
                    if(null != name && null != targetFolderInJarAry[i] && name.startsWith(targetFolderInJarAry[i])) {
                        needCopyFolderStr = targetFolderInJarAry[i];
                        break;
                    }//end of if
                }//end of for
                if(null == needCopyFolderStr) continue;
                File file = new File(destDirStr + (destDirStr.endsWith(File.separator) ? "" : File.separator) + jarEntry.getName());
                if (jarEntry.isDirectory()) {
                    System.out.println("====extractFilesFromJar============>提取文件夹:"+file.getAbsolutePath());
                    file.mkdirs();
                    continue;
                }//end of if
                try (InputStream is = jar.getInputStream(jarEntry);
                     FileOutputStream fos = new FileOutputStream(file)) {
                    System.out.println("====extractFilesFromJar============>提取文件:"+file.getAbsolutePath());
                    while (is.available() > 0) fos.write(is.read());
                }
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("====extractFilesFromJar============>提取文件异常:"+e.getMessage());
            return false;
        }
    }

    public static void main(String[] args) {
        System.out.println("====StephenAabBundleInstaller====Main===>注:运行会检查必备运行环境是否完整,如果不完整,会释放运行必备文件到本Jar运行的目录[StephenAabBundleInstaller]文件夹下,释放会占用一点时间,请耐心等待!");
        String jarFile = StephenAabMain.class.getProtectionDomain().getCodeSource().getLocation().getFile();//System.getProperty("java.class.path");//获取当前执行的jar路径
        String destDirStr = ((new File(jarFile)).getParent());
        String os = System.getProperty("os.name");
        System.out.println("====StephenAabBundleInstaller====Main===run===jar==>"+jarFile+"====>destDirStr:"+destDirStr+"=====>os:"+os);
        boolean isWinOs = (os != null && os.toLowerCase().contains("windows"));
        boolean isMacOs = (os != null && os.toLowerCase().contains("mac"));
        String curOsFolderName = (isWinOs ? "Windows" : (isMacOs ? "MacOs" : "Linux"));
        if(extractFilesFromJar(jarFile, destDirStr, "StephenAabBundleInstaller"+File.separator+curOsFolderName,
                "StephenAabBundleInstaller"+File.separator+"libs", "StephenAabBundleInstaller"+File.separator+"assets"))new StephenAabMain(destDirStr, isWinOs, curOsFolderName);
        //new StephenAabMain(destDirStr, isWinOs, curOsFolderName);//开发时看界面用
    }
}
