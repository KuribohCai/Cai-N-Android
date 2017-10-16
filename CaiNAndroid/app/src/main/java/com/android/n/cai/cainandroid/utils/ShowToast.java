package com.android.n.cai.cainandroid.utils;

import android.widget.Toast;

import com.android.n.cai.cainandroid.BuildConfig;
import com.android.n.cai.cainandroid.CaiNApplication;

/**
 * Created by Kuriboh on 2017/9/20.
 * E-Mail Address: cai_android@163.com
 */

public class ShowToast {

    public static String TAG = "Cai";

    public static void s(String string) {
        if (BuildConfig.DEBUG) {
            Toast.makeText(CaiNApplication.getAppContext(), TAG + ":" + string, Toast.LENGTH_SHORT).show();
        }
    }

    public static void l( String string) {
        if (BuildConfig.DEBUG) {
            Toast.makeText(CaiNApplication.getAppContext(), TAG + ":" + string, Toast.LENGTH_LONG).show();
        }
    }
}
