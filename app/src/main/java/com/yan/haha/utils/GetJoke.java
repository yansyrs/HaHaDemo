package com.yan.haha.utils;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.yan.haha.units.Jokes;
import com.yan.haha.OnDataFinishedListener;

/**
 * Created by Leung on 2016/5/20.
 */
public class GetJoke extends AsyncTask<Void, Void, Void> {
    public static final String DEF_CHATSET = "UTF-8";
    public static final int DEF_CONN_TIMEOUT = 30000;
    public static final int DEF_READ_TIMEOUT = 30000;
    public static final String userAgent = "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) " +
            "Chrome/29.0.1547.66 Safari/537.36";
    public static final String APPKEY = "5c6a5e034defb873b5d4971ba36cfdb4";
    public org.json.JSONObject myObject = null;
    public org.json.JSONObject afterReplaceObject = null;
    public org.json.JSONObject finalJsonObject = null;
    private int requestPage;
    public static String TAG = "leungadd";
    private Jokes jokesObject;
    public List<Jokes> jokesArray = new ArrayList<Jokes>();
    OnDataFinishedListener onDataFinishedListener;

    public GetJoke (int requestPage) {
        this.requestPage = requestPage;
    }

    public void setOnDataFinishedListener(
            OnDataFinishedListener onDataFinishedListener) {
        this.onDataFinishedListener = onDataFinishedListener;
    }

    public void clearContent() {
        jokesArray.clear();
    }

    @Override
    protected Void doInBackground(Void... v) {
        try {
            requestJoke();
        } catch (Exception e) {
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void v) {
        String revertReplaceString = "";
        try {
            if (afterReplaceObject != null) {
                //把部分字符或者空格替换成回车换行
                revertReplaceString = afterReplaceObject.toString().replace(".5.5", "\\r\\n");
                revertReplaceString = revertReplaceString.replaceAll("\\s\\s", "\\\\r\\\\n");
                finalJsonObject = new org.json.JSONObject(revertReplaceString);
                JSONArray jsonArray = finalJsonObject.optJSONArray("data");
                for (int i = 0; i < jsonArray.length(); i++) {
                    org.json.JSONObject jsonObject = jsonArray.getJSONObject(i);
                    String content = jsonObject.optString("content");
                    String updateTime = jsonObject.optString("updatetime");
                    jokesObject = new Jokes(content.substring(0, 20), updateTime, content);
                    jokesArray.add(jokesObject);
                }
                onDataFinishedListener.onDataSuccessfully(jokesArray);
            }else{
                onDataFinishedListener.onDataFailed();
            }
        } catch (Exception e) {
            Log.d(TAG, "getrequest2 exception " + e.toString());
        }
    }

    public void requestJoke() {
        String result = null;
        String afterReplaceString = "";
        String url = "http://japi.juhe.cn/joke/content/list.from";//请求接口地址
        Map params = new HashMap();//请求参数
        params.put("sort", "desc");//类型，desc:指定时间之前发布的，asc:指定时间之后发布的
        params.put("page", requestPage);//准备获取的页数，默认是第1页
        params.put("pagesize", "15");//每次返回条数,默认1,最大20
        params.put("time", new Date().getTime() / 1000);//时间戳（10位），如：1418816972
        params.put("key", APPKEY);//您申请的key

        try {
            result = net(url, params, "GET");
            myObject = new org.json.JSONObject(result);
            if (myObject.getInt("error_code") == 0) {
                Log.d(TAG, "requestJoke Succeed");
                afterReplaceString = myObject.get("result").toString().replace("\\r\\n", ".5.5");
                afterReplaceObject = new org.json.JSONObject(afterReplaceString);
            } else {
                Log.d(TAG, "requestJoke Failed" +myObject.get("error_code") + ":" + myObject.get("reason"));
            }
        } catch (Exception e) {
            Log.d(TAG, "requestJoke exception " + e.toString());
        }
    }
    /**
     * @param strUrl 请求地址
     * @param params 请求参数
     * @param method 请求方法
     * @return 网络请求字符串
     * @throws Exception
     */
    public String net(String strUrl, Map params, String method) throws Exception {
        HttpURLConnection conn = null;
        BufferedReader reader = null;
        String rs = null;
        try {
            StringBuffer sb = new StringBuffer();
            if (method == null || method.equals("GET")) {
                strUrl = strUrl + "?" + urlencode(params);
            }
            URL url = new URL(strUrl);
            conn = (HttpURLConnection) url.openConnection();
            if (method == null || method.equals("GET")) {
                conn.setRequestMethod("GET");
            } else {
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
            }
            conn.setRequestProperty("User-agent", userAgent);
            conn.setUseCaches(false);
            conn.setConnectTimeout(DEF_CONN_TIMEOUT);
            conn.setReadTimeout(DEF_READ_TIMEOUT);
            conn.setInstanceFollowRedirects(false);
            conn.connect();
            if (params != null && method.equals("POST")) {
                try {
                    DataOutputStream out = new DataOutputStream(conn.getOutputStream());
                    out.writeBytes(urlencode(params));
                } catch (Exception e) {
                    // TODO: handle exception
                }
            }
            InputStream is = conn.getInputStream();
            reader = new BufferedReader(new InputStreamReader(is, DEF_CHATSET));
            String strRead = null;
            while ((strRead = reader.readLine()) != null) {
                sb.append(strRead);
            }
            rs = sb.toString();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                reader.close();
            }
            if (conn != null) {
                conn.disconnect();
            }
        }
        return rs;
    }

    //将map型转为请求参数型
    public String urlencode(Map<String, Object> data) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry i : data.entrySet()) {
            try {
                sb.append(i.getKey()).append("=").append(URLEncoder.encode(i.getValue() + "", "UTF-8")).append("&");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }

}
