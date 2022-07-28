import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

//aab包安装器主界面;author:Stephen
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
        JLabel labelForAab = new JLabel("选择安装 Aab 包路径:");
        JButton btnForAab = new JButton("选择");
        JTextField labelForAabResult = new JTextField("请选择", 40);
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

        FileDialog fdForApks = new FileDialog(this, "选择输出Apks包目录", FileDialog.SAVE);
        JLabel labelForApks = new JLabel("选择输出Apks包目录:");
        JButton btnForApks = new JButton("选择");
        JTextField labelForApksResult = new JTextField("请选择", 40);
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

        FileDialog fdForJks = new FileDialog(this, "选择签名文件Jks路径", FileDialog.LOAD);
        fdForJks.setFilenameFilter((dir, name) -> name.endsWith(".jks"));
        JLabel labelForJks = new JLabel("选择签名文件Jks路径:");
        JButton btnForJks = new JButton("选择");
        JTextField labelForJksResult = new JTextField("请选择", 40);
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

        /**********************************************adb**************************************************************/
        FileDialog fdForAdb = new FileDialog(this, "选择精确的Adb命令文件路径", FileDialog.LOAD);
        fdForAdb.setFilenameFilter((dir, name) -> name.equals("adb"));
        JLabel labelForAdb = new JLabel("选择或输入Adb命令路径(可选,默认adb环境不可用需设置):");
        JButton btnForAdb = new JButton("选择");
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
        inputForJksAdb = new JTextField(40);
        Box boxForAdb = Box.createHorizontalBox();
        boxForAdb.add(labelForAdb);
        boxForAdb.add(Box.createHorizontalStrut(8));
        boxForAdb.add(btnForAdb);
        boxForAdb.add(Box.createHorizontalStrut(8));
        boxForAdb.add(inputForJksAdb);

        JLabel labelForAdbHint = new JLabel("可点击右边的测试按钮检测你现在环境的adb(或你上面输入框adb路径)是否可用");
        JButton btnForAdbTest = new JButton("测试");
        btnForAdbTest.addActionListener(e -> {
            boolean adbTestResult = execProcessBuilder("adb", "version");
            textAreaForLog.append("==========adb测试结果=======>"+adbTestResult);
            JOptionPane.showMessageDialog(null, adbTestResult ? "恭喜,adb检测可用" : "抱歉,adb检测不可用,请设置一个精确路径", "提示",
                    adbTestResult ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE);
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

        JLabel boxForAdbHint2 = new JLabel("adb文件路径是在AndroidSdk的platform-tools目录下,举例:/Users/stephen/Library/Android/sdk/platform-tools/adb");
        JPanel adbHint2ForPanel = new JPanel();
        adbHint2ForPanel.setBackground(Color.pink);
        adbHint2ForPanel.add(boxForAdbHint2);
        bindJLabelCopyFun(boxForAdbHint2);
        /********************************************adb**************************************************************/

        Box boxForExecute = Box.createHorizontalBox();
        JButton btnForClear = new JButton("清除所有内容");
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
            //adbPathStr = null;
            inputForJksAliasPwd.setText("");
            textAreaForLog.setText("等待操作中...\n");
            JOptionPane.showMessageDialog(null, "清除完成!", "提示", JOptionPane.INFORMATION_MESSAGE);
        });
        boxForExecute.add(btnForClear);
        boxForExecute.add(Box.createHorizontalStrut(8));
        JButton btnForStart = new JButton("开始执行安装");
        btnForStart.addActionListener(e -> {
            /*textAreaForLog.append("=======request=====content==>"+httpGetRequest("http://172.30.2.27:5000/getChannelContent", new HashMap<>(){{
                put("appType", "Ljb");
                put("channelType", "channel");
            }}));*/
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
        textPanelForLog.setAutoscrolls(true);
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
        baseBox.add(adbForPanel);
        baseBox.add(adbHintForPanel);
        baseBox.add(adbHint2ForPanel);
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
        final String folderName = isWinOs ? "Windows" : "MacLinux";
        final String rootFolderPathStr = rootPathStr+(rootPathStr.endsWith(File.separator) ? "" : File.separator)+"src"+File.separator+"StephenAppBundleInstaller"+File.separator;
        adbPathStr = rootFolderPathStr+folderName+File.separator+"adb-tool"+File.separator+(isWinOs ? "adb.exe" : "adb"); //inputForJksAdb.getText();
        textAreaForLog.append(isStrNotEmpty(adbPathStr) ? "设置了adb自定义路径,本次使用自定义adb路径:"+adbPathStr+"中\n" : "未设置adb自定义路径,本次使用系统adb路径中,如果报错,请设置精确的adb路径\n");
        (new Timer()).schedule(new TimerTask() {
            @Override
            public void run() {
                sureStartExecuteCore(rootFolderPathStr, folderName);
            }
        }, 500);
    }

    private void sureStartExecuteCore(String rootFolderPathStr, String folderName){
        boolean execResult = execProcessBuilder(rootFolderPathStr+folderName+File.separator+"bundle2apksExec.sh", aabPathStr,
                apksPathStr, jksPathStr, inputForJksPwd.getText(), inputForJksAlias.getText(), inputForJksAliasPwd.getText(),
                rootFolderPathStr+"libs"+File.separator+"bundletool.jar");
        textAreaForLog.append("======bundle2apksExec====execResult=>"+execResult);
        if(execResult)execProcessBuilder(rootFolderPathStr+folderName+File.separator+"installapksExec.sh",
                apksPathStr+(apksPathStr.endsWith(File.separator) ? "" : File.separator)+aabName.replace(".aab", ".apks"),
                rootFolderPathStr+"libs"+File.separator+"bundletool.jar", adbPathStr);
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
        processBuilder.command(commandStr);//带参数需要用可变参数方式设置
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
        textAreaForLog.append("=====doGet===>请求Url:" + urlString);
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
                return "{\"isSuccess\": true, \"errCode\": "+resCode+",\"dataInfo\": "+infoStr+"}";
            } else {
                return "{\"isSuccess\": false, \"errCode\": "+resCode+",\"dataInfo\": "+infoStr+"}";
            }
        } catch (Exception e) {
            return "{\"isSuccess\": false, \"errCode\": -1,\"dataInfo\": "+e.getMessage()+"}";
        } finally {
            if (httpURLConnection != null) httpURLConnection.disconnect();
        }

        /*try {
            String paramStr = URLEncoder.encode("key1", "UTF-8") + "=" + URLEncoder.encode("value1", "UTF-8");
            paramStr += "&" + URLEncoder.encode("key2", "UTF-8") + "=" + URLEncoder.encode("value2", "UTF-8");
            URL url = new URL("http://192.168.180.4/NewCS/queryfee/queryFeeIndex.action");
            URLConnection conn = url.openConnection();
            conn.setDoOutput(true);
            OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
            wr.write(paramStr);
            wr.flush();
            conn.getco
            BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = rd.readLine()) != null) {
            }
            wr.close();
            rd.close();
            return "{\"isSuccess\": true, \"errMsg\": "+e.getMessage()+"}";
        } catch (Exception e) {
            e.printStackTrace();
            return "{\"isSuccess\": false, \"errMsg\": "+e.getMessage()+"}";
        }*/
    }

    public static void main(String[] args) {
        new StephenAabMain();
    }
}
