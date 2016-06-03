package com.yan.haha.units;

import java.util.HashMap;

public class HoroscopeInfo {
    private String mName;
    private String mDate;

    private static HashMap<String, String> mNameMap = new HashMap<String, String>(){
        {
            put("水瓶座", "aquarius");
            put("白羊座", "aries");
            put("巨蟹座", "cancer");
            put("摩羯座", "capricorn");
            put("双子座", "gemini");
            put("狮子座", "leo");
            put("天秤座", "libra");
            put("双鱼座", "pisces");
            put("射手座", "sagittarius");
            put("天蝎座", "scorpius");
            put("金牛座", "taurus");
            put("处女座", "virgo");
        }
    };

    public HoroscopeInfo(String name, String date) {
        mName = name;
        mDate = date;
    }

    public String getName() {
        return mName;
    }

    public String getLatinName() {
        return mNameMap.get(mName);
    }

    public static String getLatinName(String chsName) {
        return mNameMap.get(chsName);
    }

    public String getDate() {
        return mDate;
    }

    public String getDescription() {
        return mName + " (" + mDate + ")";
    }
}
