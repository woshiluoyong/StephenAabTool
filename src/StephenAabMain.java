import org.json.JSONArray;
import org.json.JSONObject;

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

//aab包安装器主界面;author:Stephen
public class StephenAabMain extends JFrame {
    private String GetJksContentUrl = "http://172.30.2.27:5000/getJksContent";
    private boolean isWinOs = false;
    private String osFolderName;
    private String rootSrcPathStr;
    private String rootAssetsPathStr;
    private String rootFolderPathStr;
    private String builtInAdbPathStr;
    private String aabPathStr;
    private String apksPathStr;
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

    public StephenAabMain() {
        String rootPathStr = System.getProperty("user.dir");
        String os = System.getProperty("os.name");
        isWinOs = (os != null && os.toLowerCase().contains("windows"));
        System.out.println("====rootPathStr=======>"+rootPathStr+"===os==>"+os);
        osFolderName = isWinOs ? "Windows" : "MacLinux";
        rootSrcPathStr = rootPathStr+(rootPathStr.endsWith(File.separator) ? "" : File.separator)+"src"+File.separator;
        rootAssetsPathStr = rootSrcPathStr+"assets"+File.separator;
        rootFolderPathStr = rootSrcPathStr+"StephenAppBundleInstaller"+File.separator;
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

        FileDialog fdForApks = new FileDialog(this, "选择中间临时文件输出目录", FileDialog.SAVE);
        JLabel labelForApks = new JLabel("选择临时文件Tmp目录:");
        JButton btnForApks = new JButton("选择");
        JTextField labelForApksResult = new JTextField("", 40);
        labelForApksResult.setEnabled(false);
        btnForApks.addActionListener(e -> {
            fdForApks.setVisible(true);
            if(isStrNotEmpty(fdForApks.getDirectory()) && isStrNotEmpty(fdForApks.getFile())){
                apksPathStr = fdForApks.getDirectory()+fdForApks.getFile();
                textAreaForLog.append("==============选择的Apks包目录===>"+apksPathStr+"\n");
                labelForApksResult.setText(apksPathStr);
            }else{
                labelForApksResult.setText(isStrNotEmpty(apksPathStr) ? apksPathStr : "未选择");
            }
        });
        Box boxForApks = Box.createHorizontalBox();
        boxForApks.add(labelForApks);
        boxForApks.add(Box.createHorizontalStrut(8));
        boxForApks.add(btnForApks);
        boxForApks.add(Box.createHorizontalStrut(8));
        boxForApks.add(labelForApksResult);
        JPanel apksForPanel = new JPanel();
        apksForPanel.setBackground(Color.ORANGE);
        apksForPanel.add(boxForApks);

        tabBarPane = new JTabbedPane();
        tabBarPane.setForeground(Color.BLACK);
        tabBarPane.addTab("自定义解包签名文件", new ImageIcon(rootAssetsPathStr+"icon_custom.png"), createJksComponent());
        tabBarPane.addTab("俊云快速解包签名文件", new ImageIcon(rootAssetsPathStr+"icon_junyun.png"), createJunYunComponent());
        tabBarPane.setSelectedIndex(0);

        Box boxForExecute = Box.createHorizontalBox();
        JButton btnForClear = new JButton("重置所有内容", new ImageIcon(rootAssetsPathStr+"icon_clear.png"));
        btnForClear.addActionListener(e -> {
            aabPathStr = null;
            apksPathStr = null;
            jksPathStr = null;
            labelForAabResult.setText("");
            labelForApksResult.setText("");
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
                JOptionPane.showMessageDialog(null, "你选择的adb经测试不可用,请先确认或切换", "提示", JOptionPane.WARNING_MESSAGE);
                return;
            }//end of if
            if(isStrEmpty(aabPathStr)){
                JOptionPane.showMessageDialog(null, "请选择待安装Aab包路径", "提示", JOptionPane.WARNING_MESSAGE);
                return;
            }//end of if
            if(isStrEmpty(apksPathStr)){
                JOptionPane.showMessageDialog(null, "请选择输出Apks包目录", "提示", JOptionPane.WARNING_MESSAGE);
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
                String saveJksPath = apksPathStr+(apksPathStr.endsWith(File.separator) ? "" : File.separator)+"selectJks.jks";
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
        baseBox.add(apksForPanel);
        baseBox.add(Box.createVerticalStrut(5));
        baseBox.add(tabBarPane);
        baseBox.add(Box.createVerticalStrut(5));
        baseBox.add(boxForExecute);
        baseBox.add(Box.createVerticalStrut(5));
        baseBox.add(boxForLog);
        baseBox.add(Box.createVerticalStrut(5));
        add(baseBox);

        setTitle("GoogleAab包安装器(Running on "+os+")");
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
            checkAdbSourceAvailable(true);
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
        JLabel labelForJunYun = new JLabel("请在后面输入认证码获取俊云产品签名:");
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
                JOptionPane.showMessageDialog(null, "请先输入俊云认证码", "提示", JOptionPane.WARNING_MESSAGE);
                return;
            }//end of if
            btnGroupForJunYun = null;
            panelForJunYun.removeAll();
            panelForJunYun.setVisible(false);
            entityForJksBeanArrayList.clear();
            labelForJunYunHint.setText("获取俊云产品签名数据中,请稍后");
            String httpRetInfo = httpGetRequest(GetJksContentUrl, new HashMap<>(){{
                put("authPwdCode", labelForJunYunRwd.getText());
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
        boolean adbTestResult = execProcessBuilder(curAdbCommand, "version");
        textAreaForLog.append("==========adb可用性测试结果=======>"+adbTestResult+"\n");
        if(isHintDialog)JOptionPane.showMessageDialog(null, adbTestResult ? "恭喜,adb检测可用" : "抱歉,adb检测不可用,请确认adb", "提示",
                adbTestResult ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE);
        return adbTestResult;
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
                apksPathStr, useJksBean.jksFilePath, useJksBean.jksFilePwd, useJksBean.jksAlias, useJksBean.jksAliasPwd,
                rootFolderPathStr+"libs"+File.separator+"bundletool.jar");
        textAreaForLog.append("======bundle2apksExec====execResult=>"+execResult+"\n");
        if(execResult) {
            execResult = execProcessBuilder(rootFolderPathStr + osFolderName + File.separator + "installapksExec.sh",
                    apksPathStr + (apksPathStr.endsWith(File.separator) ? "" : File.separator) + aabName.replace(".aab", ".apks"),
                    rootFolderPathStr + "libs" + File.separator + "bundletool.jar", curAdbCommand);
            if(!execResult)JOptionPane.showMessageDialog(null, "抱歉,安装apks到手机失败,请查看日志信息解决再试!", "提示", JOptionPane.ERROR_MESSAGE);
        }else{
            JOptionPane.showMessageDialog(null, "抱歉,解压aab到apks失败,请查看日志信息解决再试!", "提示", JOptionPane.ERROR_MESSAGE);
        }
    }

    private boolean execProcessBuilder(String... commandStr) {
        String commandStrInfo = "";
        if(null != commandStr && commandStr.length > 0){
            if(isStrEmpty(commandStr[0]))return false;
            for (int i = 0; i < commandStr.length; i++) {
                commandStrInfo += " " + commandStr[i];
            }//end of for
        }//end of if
        textAreaForLog.append("==execProcess===commandStrInfo=>"+commandStrInfo+"\n");
        if(isStrEmpty(commandStrInfo))return false;
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command(commandStr);//带参数需要用可变参数方式设置
        processBuilder.redirectErrorStream(true);
        InputStream inputStream = null;
        try {
            Process start = processBuilder.start();
            inputStream = start.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "gbk");
            boolean isNotError = true;
            int len = -1;
            char[] c = new char[1024];
            while ((len = inputStreamReader.read(c)) != -1) {
                String lineStr = new String(c, 0, len);
                if(isStrNotEmpty(lineStr) && lineStr.toLowerCase().contains("error"))isNotError = false;
                textAreaForLog.append("======execProcessResult====>isNotError:"+isNotError+"===lineStr:"+lineStr);
            }
            return isNotError;
        } catch (Exception e) {
            e.printStackTrace();
            textAreaForLog.append("==execProcess===exception=>"+e.getMessage()+"\n");
            return false;
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

    public static void main(String[] args) {
        new StephenAabMain();
    }
}
