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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.yan.haha.units.BrainRiddle;
import com.yan.haha.OnDataFinishedListener;

/**
 * Created by Leung on 2016/5/20.
 */
public class GetBrainRiddle extends AsyncTask<Void, Void, Void> {
    public static final String DEF_CHATSET = "UTF-8";
    public static final int DEF_CONN_TIMEOUT = 30000;
    public static final int DEF_READ_TIMEOUT = 30000;
    public static final String userAgent = "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) " +
            "Chrome/29.0.1547.66 Safari/537.36";
    public JSONObject myObject = null;
    private int[] id;
    private int randomNum;
    public static String TAG = "leungadd";
    private BrainRiddle brainRiddleObject;
    public static List<BrainRiddle> brainRiddleArray = new ArrayList<BrainRiddle>();
    OnDataFinishedListener onDataFinishedListener;

    public GetBrainRiddle(int[] id) {
        this.id = id;
    }

    public GetBrainRiddle(int randomNum) {
        this.randomNum = randomNum;
    }

    public void setOnDataFinishedListener(
            OnDataFinishedListener onDataFinishedListener) {
        this.onDataFinishedListener = onDataFinishedListener;
    }

    public void clearContent() {
        brainRiddleArray.clear();
    }


    @Override
    protected Void doInBackground(Void... v) {
        try {
            requestBrainRiddle();
        } catch (Exception e) {
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void v) {
        if (brainRiddleObject != null) {
            onDataFinishedListener.onDataSuccessfully(brainRiddleArray);
        } else {
            onDataFinishedListener.onDataFailed();
        }
    }

    public void requestBrainRiddle() {
        String result = null;
        String url = "http://ok511.party:2333/brain";//请求接口地址
        Map params = new HashMap();//请求参数
        params.put("random", randomNum);//随机返回，数量由传入参数决定
        if (id != null)
            params.put("id", id);//id数组，获取指定id的内容，id范围目前是[1, 3600]

        try {
            result = net(url, params, "GET");
            myObject = new JSONObject(result);
            JSONArray jsonArray = myObject.optJSONArray("data");
            if (jsonArray != null) {
                Log.d(TAG, "requestBrainRiddle Succeed");
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    String id = jsonObject.optString("id");
                    String question = jsonObject.optString("question");
                    String answer = jsonObject.optString("answer");
                    brainRiddleObject = new BrainRiddle(id, question, answer);
                    brainRiddleArray.add(brainRiddleObject);
                }
            } else {
                Log.d(TAG, "requestBrainRiddle Failed");
            }
        } catch (Exception e) {
            Log.d(TAG, "requestBrainRiddle exception " + e.toString());
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
            if (i.getKey().equals("id")) {
                sb.append(i.getKey()).append("=");
                int[] tmpParam = (int[]) i.getValue();
                for (int index = 0; index < tmpParam.length - 1; index++) {
                    sb.append(String.valueOf(tmpParam[index])).append(",");
                }
                sb.append(String.valueOf(tmpParam[tmpParam.length - 1])).append("&");
            } else {
                try {
                    sb.append(i.getKey()).append("=").append(URLEncoder.encode(i.getValue() + "", "UTF-8")).append("&");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        }
        //拼接好的url示例：http://ok511.party:2333/brain?id=1,2,3,556&random=10&
        return sb.toString();
    }

}
