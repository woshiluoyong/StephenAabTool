import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.Timer;
import java.util.TimerTask;

public class StephenAabMain extends JFrame {
    private boolean isWinOs = false;
    private String rootPathStr;
    private String aabPathStr;
    private String apksPathStr;
    private String jksPathStr;
    private JTextField inputForJksPwd, inputForJksAlias, inputForJksAliasPwd, inputForJksAdb;
    private String adbPathStr;
    private JTextArea textAreaForLog;

    private String aabName = null;

    public StephenAabMain() {
        rootPathStr = System.getProperty("user.dir");
        String os = System.getProperty("os.name");
        isWinOs = (os != null && os.toLowerCase().startsWith("windows"));
        System.out.println("====rootPathStr====>"+rootPathStr+"===os==>"+os);

        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();

        float width = (float)screen.width/2.5f;
        float height = (float)screen.height/2f;

        setLayout(new FlowLayout());

        FileDialog fdForAab = new FileDialog(this, "选择待安装Aab包路径", FileDialog.LOAD);
        fdForAab.setFilenameFilter((dir, name) -> name.endsWith(".aab"));
        JLabel labelForAab = new JLabel("选择待安装Aab包路径:");
        JButton btnForAab = new JButton("选择...");
        JLabel labelForAabResult = new JLabel("请选择");
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

        FileDialog fdForApks = new FileDialog(this, "选择输出Apks包目录", FileDialog.SAVE);
        JLabel labelForApks = new JLabel("选择输出Apks包目录:");
        JButton btnForApks = new JButton("选择...");
        JLabel labelForApksResult = new JLabel("请选择");
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

        FileDialog fdForJks = new FileDialog(this, "选择签名文件Jks路径", FileDialog.LOAD);
        fdForJks.setFilenameFilter((dir, name) -> name.endsWith(".jks"));
        JLabel labelForJks = new JLabel("选择包签名文件Jks路径:");
        JButton btnForJks = new JButton("选择...");
        JLabel labelForJksResult = new JLabel("请选择");
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

        JLabel labelForJksAliasPwd = new JLabel("输入签名文件Jks别名密码:");
        inputForJksAliasPwd = new JTextField(40);
        Box boxForJksAliasPwd = Box.createHorizontalBox();
        boxForJksAliasPwd.add(labelForJksAliasPwd);
        boxForJksAliasPwd.add(Box.createHorizontalStrut(8));
        boxForJksAliasPwd.add(inputForJksAliasPwd);

        FileDialog fdForAdb = new FileDialog(this, "选择精确的Adb命令文件路径", FileDialog.LOAD);
        fdForAdb.setFilenameFilter((dir, name) -> name.equals("adb"));
        JLabel labelForAdb = new JLabel("选择或输入Adb命令路径(adb环境报错才设置):");
        JButton btnForAdb = new JButton("选择...");
        inputForJksAdb = new JTextField(40);
        btnForAdb.addActionListener(e -> {
            fdForAdb.setVisible(true);
            if(isStrNotEmpty(fdForAdb.getDirectory()) && isStrNotEmpty(fdForAdb.getFile())){
                adbPathStr = fdForAdb.getDirectory()+fdForAdb.getFile();
                textAreaForLog.append("==============选择的Adb执行路径===>"+adbPathStr+"\n");
                inputForJksAdb.setText(adbPathStr);
            }else{
                inputForJksAdb.setText(isStrNotEmpty(adbPathStr) ? adbPathStr : "");
            }
        });
        Box boxForAdb = Box.createHorizontalBox();
        boxForAdb.add(labelForAdb);
        boxForAdb.add(Box.createHorizontalStrut(8));
        boxForAdb.add(btnForAdb);
        boxForAdb.add(Box.createHorizontalStrut(8));
        boxForAdb.add(inputForJksAdb);

        Box boxForExecute = Box.createHorizontalBox();
        JButton btnForClear = new JButton("清除");
        btnForClear.addActionListener(e -> {
            aabPathStr = null;
            apksPathStr = null;
            jksPathStr = null;
            labelForAabResult.setText("请选择");
            labelForApksResult.setText("请选择");
            labelForJksResult.setText("请选择");
            inputForJksPwd.setText("");
            inputForJksAlias.setText("");
            inputForJksAliasPwd.setText("");
            adbPathStr = null;
            inputForJksAliasPwd.setText("");
            textAreaForLog.setText("等待操作中...\n");
            JOptionPane.showMessageDialog(null, "清除完成!", "提示", JOptionPane.INFORMATION_MESSAGE);
        });
        boxForExecute.add(btnForClear);
        boxForExecute.add(Box.createHorizontalStrut(8));
        JButton btnForStart = new JButton("开始");
        btnForStart.addActionListener(e -> {
            //sureStartExecute();//test
            if(isStrEmpty(aabPathStr)){
                JOptionPane.showMessageDialog(null, "请选择待安装Aab包路径", "提示", JOptionPane.WARNING_MESSAGE);
                return;
            }//end of if
            if(isStrEmpty(apksPathStr)){
                JOptionPane.showMessageDialog(null, "请选择输出Apks包目录", "提示", JOptionPane.WARNING_MESSAGE);
                return;
            }//end of if
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
            sureStartExecute();
        });
        boxForExecute.add(btnForStart);

        Box boxForLog = Box.createHorizontalBox();
        textAreaForLog = new JTextArea("等待操作中...\n", 20, 10);
        textAreaForLog.setBackground(Color.BLACK);
        textAreaForLog.setLineWrap(true);
        textAreaForLog.setForeground(Color.WHITE);
        JScrollPane textPanelForLog = new JScrollPane(textAreaForLog);
        textPanelForLog.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);//取消显示水平滚动条
        textPanelForLog.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);//显示垂直滚动条
        boxForLog.add(textPanelForLog);

        Box baseBox = Box.createVerticalBox();
        baseBox.add(boxForAab);
        baseBox.add(Box.createVerticalStrut(5));
        baseBox.add(boxForApks);
        baseBox.add(Box.createVerticalStrut(5));
        baseBox.add(boxForJks);
        baseBox.add(Box.createVerticalStrut(5));
        baseBox.add(boxForJksPwd);
        baseBox.add(Box.createVerticalStrut(5));
        baseBox.add(boxForJksAlias);
        baseBox.add(Box.createVerticalStrut(5));
        baseBox.add(boxForJksAliasPwd);
        baseBox.add(Box.createVerticalStrut(10));
        baseBox.add(boxForAdb);
        baseBox.add(Box.createVerticalStrut(5));
        baseBox.add(boxForExecute);
        baseBox.add(Box.createVerticalStrut(5));
        baseBox.add(boxForLog);
        baseBox.add(Box.createVerticalStrut(5));
        add(baseBox);

        setTitle("奇游GoogleAab包安装器(Running on "+os+")");
        setSize((int)width, (int)height);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setVisible(true);

        //init test
        aabName = "sixMill_google.aab";
        aabPathStr = "/Users/stephen/Downloads/sixMill_google.aab";
        apksPathStr = "/Users/stephen/Downloads/Vicky/Untitled";
        jksPathStr = "/Users/stephen/Documents/AndroidProjects/qeeyou_accelerator_android_overseas/app/overseas_key.jks";
        inputForJksPwd.setText("123456");
        inputForJksAlias.setText("mobile");
        inputForJksAliasPwd.setText("123456");
    }

    private void sureStartExecute(){
        textAreaForLog.setText("开始执行中...\n");
        adbPathStr = inputForJksAdb.getText();
        textAreaForLog.append(isStrNotEmpty(adbPathStr) ? "设置了adb自定义路径,本次使用自定义adb路径:"+adbPathStr+"中\n" : "未设置adb自定义路径,本次使用系统adb路径中,如果报错,请设置精确的adb路径\n");
        (new Timer()).schedule(new TimerTask() {
            @Override
            public void run() {
                sureStartExecuteCore();
            }
        }, 500);
    }

    private void sureStartExecuteCore(){
        String folderName = isWinOs ? "Windows" : "MacLinux";
        boolean execResult = execProcessBuilder(rootPathStr+(rootPathStr.endsWith(File.separator) ? "" : File.separator)+"src"+File.separator+"StephenAppBundleInstaller"+File.separator+folderName
                        +File.separator+"bundle2apksExec.sh", aabPathStr, apksPathStr, jksPathStr, inputForJksPwd.getText(), inputForJksAlias.getText(), inputForJksAliasPwd.getText(),
                rootPathStr+(rootPathStr.endsWith(File.separator) ? "" : File.separator)+"src"+File.separator+"StephenAppBundleInstaller"+File.separator+"libs"+File.separator+"bundletool.jar");
        System.out.print("======bundle2apksExec====execResult=>"+execResult);
        if(execResult)execProcessBuilder(rootPathStr+(rootPathStr.endsWith(File.separator) ? "" : File.separator)+"src"+File.separator+"StephenAppBundleInstaller"+File.separator+folderName
                        +File.separator+"installapksExec.sh", apksPathStr+(apksPathStr.endsWith(File.separator) ? "" : File.separator)+aabName.replace(".aab", ".apks"),
                rootPathStr+(rootPathStr.endsWith(File.separator) ? "" : File.separator)+"src"+File.separator+"StephenAppBundleInstaller"+File.separator+"libs"+File.separator+"bundletool.jar", adbPathStr);
    }

    private boolean execProcessBuilder(String... commandStr) {
        String commandStrInfo = "";
        if(null != commandStr && commandStr.length > 0){
            for (int i = 0; i < commandStr.length; i++) {
                commandStrInfo += " " + commandStr[i];
            }//end of for
        }//end of if
        textAreaForLog.append("==execProcess===commandStrInfo=>"+commandStrInfo+"\n");
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command(commandStr);
        processBuilder.redirectErrorStream(true);
        try {
            Process start = processBuilder.start();
            InputStream inputStream = start.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "gbk");
            int len = -1;
            char[] c = new char[1024];
            while ((len = inputStreamReader.read(c)) != -1) {
                String str = new String(c, 0, len);
                textAreaForLog.append(str);
                System.out.print("======execProcessResult====line=>"+str);
            }
            inputStream.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            textAreaForLog.append("==execProcess===exception=>"+e.getMessage()+"\n");
            return false;
        }
    }

    private boolean isStrEmpty(String str){
        return null == str || str.length() <= 0;
    }

    private boolean isStrNotEmpty(String str){
        return null != str && str.length() > 0;
    }

    public static void main(String[] args) {
        new StephenAabMain();
    }
}
