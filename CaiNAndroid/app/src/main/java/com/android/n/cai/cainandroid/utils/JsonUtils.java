package com.android.n.cai.cainandroid.utils;

import com.android.n.cai.cainandroid.moshi.BaseMoshi;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;

/**
 * Created by Kuriboh on 2017/10/16.
 * E-Mail Address: cai_android@163.com
 */

public class JsonUtils<T extends BaseMoshi> {

    public static String sString = "{\"name\":\"kuriboh\",\"phone\":\"15107187437\",\"id\":{\"idNumber\":\"42262319940805****\",\"birthday\":\"1994-08-05\"}}";
    private static JsonUtils sJsonUtils;
    private static Moshi sMoshi;

    private JsonUtils() {
        sMoshi = new Moshi.Builder().build();
    }

    private static JsonUtils getUtils() {
        if (sJsonUtils == null) {
            synchronized (JsonUtils.class) {
                if (sJsonUtils == null) {
                    sJsonUtils = new JsonUtils();
                }
            }
        }
        return sJsonUtils;
    }

    public String moshiToJson(T moshi,Class<T> tClass) {
        String json = "";
        try {
            JsonAdapter<T> adapter = sMoshi.adapter(tClass);
            json = adapter.toJson(moshi);
            ShowToast.s(json);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return json;
    }

    public T jsonToMoshi(String json,Class<T> tClass) {
        BaseMoshi moshi = null;
        try {
            JsonAdapter<T> adapter = sMoshi.adapter(tClass);
            moshi = adapter.fromJson(json);
            ShowToast.s(moshi.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (moshi == null) {
            moshi = new BaseMoshi() {};
        }
        return (T) moshi;
    }
}
