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
import com.yan.haha.units.Horoscope;
import com.yan.haha.OnDataFinishedListener;

/**
 * Created by Leung on 2016/5/20.
 */
public class GetHoroscope extends AsyncTask<Void, Void, Void> {
    public static final String DEF_CHATSET = "UTF-8";
    public static final int DEF_CONN_TIMEOUT = 30000;
    public static final int DEF_READ_TIMEOUT = 30000;
    public static final String userAgent = "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) " +
            "Chrome/29.0.1547.66 Safari/537.36";
    public static final String APPKEY = "4daea466b6a8bed984cad42c4e468cd8";
    public JSONObject myObject = null;
    public static String TAG = "GetHoroscope";
    private Horoscope horoscopeObject;
    public List<Horoscope> horoscopeArray = new ArrayList<Horoscope>();
    OnDataFinishedListener onDataFinishedListener;
    private String type;
    private String consName;
    private String dateTime;
    private String complexPoints;
    private String QFriend;
    private String color;
    private String health;
    private String healthPoints;
    private String love;
    private String lovePoints;
    private String money;
    private String moneyPoints;
    private int luckyNum;
    private String summary;
    private String summaryTitle;
    private String work;
    private String workPoints;
    private String job;
    private int weekth;
    private String luckyStone;

    public GetHoroscope(String type, String consName) {
        this.type = type;
        this.consName = consName;
    }

    public void setOnDataFinishedListener(
            OnDataFinishedListener onDataFinishedListener) {
        this.onDataFinishedListener = onDataFinishedListener;
    }

    public void clearContent() {
        horoscopeArray.clear();
    }

    @Override
    protected Void doInBackground(Void... v) {
        try {
            requestHoroscope();
        } catch (Exception e) {
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void v) {
        String revertReplaceString = "";
            if (horoscopeObject != null) {
                onDataFinishedListener.onDataSuccessfully(horoscopeArray);
            }else{
                onDataFinishedListener.onDataFailed();
            }
    }

    public void requestHoroscope() {
        String result = null;
        String afterReplaceString = "";
        String url = "http://web.juhe.cn:8080/constellation/getAll";//接口地址
        Map params = new HashMap();//请求参数
        params.put("consName", consName);//星座名称
        params.put("type", type);//查询类型，支持六种[today,tomorrow,week,nextweek,month,year]
        params.put("key", APPKEY);//您申请的key

        try {
            result = net(url, params, "GET");
            myObject = new JSONObject(result);
            if (myObject.getInt("error_code") == 0) {
                Log.d(TAG, "requestHoroscope Succeed");
                switch (type) {
                    case "today":
                    case "tomorrow":
                        dateTime = myObject.getString("datetime");
                        complexPoints = myObject.getString("all");
                        QFriend = myObject.getString("QFriend");
                        color = myObject.getString("color");
                        healthPoints = myObject.getString("health");
                        lovePoints = myObject.getString("love");
                        moneyPoints = myObject.getString("money");
                        luckyNum = myObject.getInt("number");
                        summary = myObject.getString("summary");
                        workPoints = myObject.getString("work");
                        horoscopeObject = new Horoscope(type, consName, dateTime, complexPoints, QFriend,
                                color, healthPoints, lovePoints, moneyPoints, luckyNum, summary, workPoints);
                        horoscopeArray.add(horoscopeObject);
                        break;
                    case "week":
                    case "nextweek":
                        dateTime = myObject.getString("date");
                        health = myObject.getString("health");
                        job = myObject.getString("job");
                        love = myObject.getString("love");
                        money = myObject.getString("money");
                        weekth = myObject.getInt("weekth");
                        work = myObject.getString("work");
                        horoscopeObject = new Horoscope(type, consName, dateTime, health, job,
                                love, money, weekth, work);
                        horoscopeArray.add(horoscopeObject);
                        break;
                    case "month":
                        dateTime = myObject.getString("date");
                        summary = myObject.getString("all");
                        health = myObject.getString("health");
                        love = myObject.getString("love");
                        money = myObject.getString("money");
                        work = myObject.getString("work");
                        horoscopeObject = new Horoscope(type, consName, dateTime, summary, health,
                                love, money, work);
                        horoscopeArray.add(horoscopeObject);
                        break;
                    case "year":
                        dateTime = myObject.getString("date");
                        summary = myObject.getJSONObject("mima").getJSONArray("text").getString(0);
                        summaryTitle = myObject.getJSONObject("mima").getString("info");
                        work = myObject.getJSONArray("career").getString(0);
                        love = myObject.getJSONArray("love").getString(0);
                        health = myObject.getJSONArray("health").getString(0);
                        money = myObject.getJSONArray("finance").getString(0);
                        luckyStone = myObject.getString("luckyStone");
                        horoscopeObject = new Horoscope(type, consName, dateTime, summary, summaryTitle,
                                work, love, health, money, luckyStone);
                        horoscopeArray.add(horoscopeObject);
                        break;
                }
            } else {
                Log.d(TAG, "requestHoroscope Failed" +myObject.get("error_code") + ":" + myObject.get("reason"));
            }
        } catch (Exception e) {
            Log.d(TAG, "requestHoroscope exception " + e.toString());
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
