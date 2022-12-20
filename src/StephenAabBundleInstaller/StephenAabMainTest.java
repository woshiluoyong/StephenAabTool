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
import java.util.Timer;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

//aab包安装器主界面;author:Stephen
public class StephenAabMainTest {

    private static boolean isStrEmpty(String str){
        return null == str || str.length() <= 0;
    }

    private static boolean isStrNotEmpty(String str){
        return null != str && str.length() > 0;
    }

    private static String httpGetRequest(final String urlOnlyStr, final Map<String, String> paramMap, String jsonParam){
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
        System.out.println("=====doGet===>请求Url:" + urlString+"\n");
        URL url;
        HttpURLConnection httpURLConnection = null;
        int resCode = -1;
        try {
            url = new URL(urlString);
            httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestMethod("GET");
            httpURLConnection.setConnectTimeout(5000);
            httpURLConnection.setReadTimeout(8000);
            httpURLConnection.setRequestProperty("Charset", "UTF-8");
            httpURLConnection.setRequestProperty("accept", "*/*");
            httpURLConnection.setRequestProperty("connection", "Keep-Alive");
            httpURLConnection.setRequestProperty("Content-Type", "application/json"); // 设置发送数据的格式

            httpURLConnection.setConnectTimeout(5000);
            httpURLConnection.setReadTimeout(8000);
            httpURLConnection.setDoInput(true);
            httpURLConnection.setDoOutput(true);

            DataOutputStream dataOutputStream = new DataOutputStream(httpURLConnection.getOutputStream());
            dataOutputStream.write(jsonParam.getBytes(StandardCharsets.UTF_8));

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

    public static void main(String[] args) {
        String httpRetInfo = httpGetRequest("http://106.13.19.65:8760/wlw/api/queryLine", null, "{\n" +
                "   \"deviceId\": \"10506B8E\",\n" +
                "   \"startDate\": \"2022-08-07 01:00:00\",\n" +
                "   \"endDate\": \"2022-08-07 23:00:00\"\n" +
                "}");
        System.out.println("==============接口返回===>"+httpRetInfo+"\n");
    }
}
