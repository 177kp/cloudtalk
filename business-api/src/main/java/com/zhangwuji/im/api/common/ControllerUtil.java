package com.zhangwuji.im.api.common;


import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zhangwuji.im.api.entity.IMOnline;
import com.zhangwuji.im.api.entity.IMUser;
import com.zhangwuji.im.api.service.IIMOnlineService;
import com.zhangwuji.im.api.service.IIMUserService;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Component
public class ControllerUtil {

    @Resource
    @Qualifier(value = "imUserService")
    private IIMUserService iOnImuserService;


    @Value("${cloudtalk.api.url}")
    public  String cloudTalkHttpApi;

    private static String url = "http://v.juhe.cn/sms/send";
    private static String appKey = "de86918924b570cda1a194f944e9556d";
    private static String modelId = "153475";



    public static String sendMsg(String phone) throws UnsupportedEncodingException {

        //验证码4个随机数字
        String msg = String.valueOf(Math.random() * 100000).substring(0, 4);

        String code = "#code#=" + msg + "&#company#=伙聊";
        code = URLEncoder.encode(code, "utf-8");
        String param = "mobile=" + phone + "&tpl_id=" + modelId + "&tpl_value=" + code + "&key=" + appKey;
        JSONObject json;
        //这里换成你们的get请求工具 就可以了
        String val = postApi(url + "?" + param,"");
        if(val!=null && val.length()>0)
        {
            json=JSONObject.parseObject(val);
        }
        else
        {
            return null;
        }
        if (json != null && json.getInteger("error_code") == 0) {
            //成功 (返回发送的验证码)
            return msg;
        } else {
            //失败
            return null;
        }

    }

    public void sendRoBotMessage(int cmd,int fromuid,int touid,String msg)
    {
        Map<String, Object> p= new LinkedHashMap<>();
        p.put("app_key","asdfsdf");
        p.put("req_user_id",fromuid);
        p.put("to_session_id",touid);
        p.put("msg_type",cmd);
        p.put("msg_data",msg);
        p.put("from_user_id",fromuid);
        String poststr=new JSONObject(p).toJSONString();

        httpPostWithJson(poststr);
    }

    public void sendIMSystemMessage(int cmd,int to_session_id,String msg,int session_type,int from_user_id)
    {
        Map<String, Object> p= new LinkedHashMap<>();
        p.put("app_key","asdfsdf");
        p.put("req_user_id",1);
        p.put("to_session_id",to_session_id);
        p.put("session_type",session_type);
        p.put("msg_type",cmd);
        p.put("msg_data",msg);
        p.put("from_user_id",from_user_id);
        String poststr=new JSONObject(p).toJSONString();

        httpPostWithJson(poststr);
    }


    public  boolean httpPostWithJson(String msg){
        boolean isSuccess = false;
        HttpPost post = null;
        try {
            HttpClient httpClient = new DefaultHttpClient();

            // 设置超时时间
            httpClient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 2000);
            httpClient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 2000);

            post = new HttpPost(cloudTalkHttpApi);
            // 构造消息头
            post.setHeader("Content-type", "application/json; charset=utf-8");
            post.setHeader("Connection", "Close");
            String sessionId = getSessionId();
            post.setHeader("SessionId", sessionId);

            // 构建消息实体
            StringEntity entity = new StringEntity(msg, Charset.forName("UTF-8"));
            entity.setContentEncoding("UTF-8");
            // 发送Json格式的数据请求
            entity.setContentType("application/json");
            post.setEntity(entity);

            HttpResponse response = httpClient.execute(post);

            // 检验返回码
            int statusCode = response.getStatusLine().getStatusCode();
            if(statusCode != HttpStatus.SC_OK){
                isSuccess = false;
            }else{
                int retCode = 0;
                String sessendId = "";
                // 返回码中包含retCode及会话Id
                for(Header header : response.getAllHeaders()){
                    if(header.getName().equals("retcode")){
                        retCode = Integer.parseInt(header.getValue());
                    }
                    if(header.getName().equals("SessionId")){
                        sessendId = header.getValue();
                    }
                }
                isSuccess=true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            isSuccess = false;
        }finally{
            if(post != null){
                try {
                    post.releaseConnection();
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        return isSuccess;
    }

    public static String postApi(String urls,String msg)
    {
        try {
            //建立连接
            URL url = new URL(urls);
            HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();

            //设置参数
            httpConn.setDoOutput(true);     //需要输出
            httpConn.setDoInput(true);      //需要输入
            httpConn.setUseCaches(false);   //不允许缓存
            httpConn.setRequestMethod("POST");      //设置POST方式连接

            //设置请求属性
            httpConn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            httpConn.setRequestProperty("Connection", "Keep-Alive");// 维持长连接
            httpConn.setRequestProperty("Charset", "UTF-8");

            //连接,也可以不用明文connect，使用下面的httpConn.getOutputStream()会自动connect
            httpConn.connect();

            //建立输入流，向指向的URL传入参数
            DataOutputStream dos = new DataOutputStream(httpConn.getOutputStream());
            dos.writeBytes(msg);
            dos.flush();
            dos.close();

            //获得响应状态
            int resultCode = httpConn.getResponseCode();
            if (HttpURLConnection.HTTP_OK == resultCode) {
                StringBuffer sb = new StringBuffer();
                String readLine = new String();
                BufferedReader responseReader = new BufferedReader(new InputStreamReader(httpConn.getInputStream(), "UTF-8"));
                while ((readLine = responseReader.readLine()) != null) {
                    sb.append(readLine).append("\n");
                }
                responseReader.close();
                System.out.println(sb.toString());
                return sb.toString();
            }
        }catch (Exception e){}
        return "";
    }
    public static String getApi(String urls)
    {
        try {
            //建立连接
            URL url = new URL(urls);
            HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();

            //设置参数
            httpConn.setDoOutput(true);     //需要输出
            httpConn.setDoInput(true);      //需要输入
            httpConn.setUseCaches(false);   //不允许缓存
            httpConn.setRequestMethod("GET");      //设置POST方式连接

            //设置请求属性
            httpConn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            httpConn.setRequestProperty("Connection", "Keep-Alive");// 维持长连接
            httpConn.setRequestProperty("Charset", "UTF-8");

            //连接,也可以不用明文connect，使用下面的httpConn.getOutputStream()会自动connect
            httpConn.connect();

            //获得响应状态
            int resultCode = httpConn.getResponseCode();
            if (HttpURLConnection.HTTP_OK == resultCode) {
                StringBuffer sb = new StringBuffer();
                String readLine = new String();
                BufferedReader responseReader = new BufferedReader(new InputStreamReader(httpConn.getInputStream(), "UTF-8"));
                while ((readLine = responseReader.readLine()) != null) {
                    sb.append(readLine).append("\n");
                }
                responseReader.close();
                System.out.println(sb.toString());
                return sb.toString();
            }
        }catch (Exception e){}
        return "";
    }

    public int getRandom(int count) {
        return (int) Math.round(Math.random() * (count));
    }

    public String string = "abcdefghijklmnopqrstuvwxyzABCDEFG123456789";

    public String getRandomString(int length){
        StringBuffer sb = new StringBuffer();
        int len = string.length();
        for (int i = 0; i < length; i++) {
            sb.append(string.charAt(getRandom(len-1)));
        }
        return sb.toString();
    }

    // 构建唯一会话Id
    public static String getSessionId(){
        UUID uuid = UUID.randomUUID();
        String str = uuid.toString();
        return str.substring(0, 8) + str.substring(9, 13) + str.substring(14, 18) + str.substring(19, 23) + str.substring(24);
    }

    public int getIntParameter(HttpServletRequest req,String key,int def)
    {
        String value=req.getParameter(key);
        if(value!=null && value!="")
        {
            return Integer.parseInt(value);
        }
        else
        {
            return def;
        }
    }
    public double getDoubleParameter(HttpServletRequest req,String key,double def)
    {
        String value=req.getParameter(key);
        if(value!=null && value!="")
        {
            return Double.parseDouble(value);
        }
        else
        {
            return def;
        }
    }
    public String getStringParameter(HttpServletRequest req,String key,String def)
    {
        String value=req.getParameter(key);
        if(value!=null && value!="")
        {
            return value;
        }
        else
        {
            return def;
        }
    }

    public IMUser checkToken(HttpServletRequest req)
    {
        String appId=req.getHeader("appid");
        String token=req.getHeader("token");
        if(appId==null ||appId=="")
        {
            appId= req.getParameter("appid");
            token= req.getParameter("token");
        }
        if(token==null|| token=="")return null;

        IMUser user=iOnImuserService.getOne(new QueryWrapper<IMUser>().eq("appId",appId).eq("api_token",token));
        return user;
    }

    public  Long timestamp() {
        long timeStampSec = System.currentTimeMillis()/1000;
        String timestamp = String.format("%010d", timeStampSec);
        return Long.parseLong(timestamp);
    }
    public  Integer timestamp2() {
        long timeStampSec = System.currentTimeMillis()/1000;
        String timestamp = String.format("%010d", timeStampSec);
        return Integer.parseInt(timestamp);
    }
}
