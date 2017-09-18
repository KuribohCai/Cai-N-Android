package com.android.n.cai.cainandroid.utils;

import android.os.SystemClock;
import android.util.Log;

import com.android.n.cai.cainandroid.BuildConfig;

/**
 * 日志打印
 * Created by Kuriboh on 2017/9/12.
 * E-Mail Address: cai_android@163.com
 */

public class LogUtils {

    // 获取毫秒值
    private static long getCurrentTime() {
        return SystemClock.currentThreadTimeMillis();
    }

    public static void i(String tag, String msg) {
        if (BuildConfig.DEBUG) {
            Log.i(tag, "*Time:" + getCurrentTime() + "/n*Msg:" + msg);
        }
    }

    public static void i(String tag, String msg, Throwable t) {
        if (BuildConfig.DEBUG) {
            Log.i(tag, "*Time:" + getCurrentTime() + "/n*Msg:" + msg, t);
        }
    }

    public static void d(String tag, String msg) {
        if (BuildConfig.DEBUG) {
            Log.d(tag, "*Time:" + getCurrentTime() + "/n*Msg:" + msg);
        }
    }

    public static void d(String tag, String msg, Throwable t) {
        if (BuildConfig.DEBUG) {
            Log.d(tag, "*Time:" + getCurrentTime() + "/n*Msg:" + msg, t);
        }
    }

    public static void e(String tag, String msg) {
        if (BuildConfig.DEBUG) {
            Log.d(tag, "*Time:" + getCurrentTime() + "/n*Msg:" + msg);
        }
    }

    public static void e(String tag, String msg, Throwable t) {
        if (BuildConfig.DEBUG) {
            Log.e(tag, "*Time:" + getCurrentTime() + "/n*Msg:" + msg, t);
        }
    }

    public static void w(String tag, String msg) {
        if (BuildConfig.DEBUG) {
            Log.w(tag, "*Time:" + getCurrentTime() + "/n*Msg:" + msg);
        }
    }

    public static void w(String tag, String msg, Throwable t) {
        if (BuildConfig.DEBUG) {
            Log.w(tag, "*Time:" + getCurrentTime() + "/n*Msg:" + msg, t);
        }
    }

    public static void v(String tag, String msg) {
        if (BuildConfig.DEBUG) {
            Log.v(tag, "*Time:" + getCurrentTime() + "/n*Msg:" + msg);
        }
    }

    public static void v(String tag, String msg, Throwable t) {
        if (BuildConfig.DEBUG) {
            Log.v(tag, "*Time:" + getCurrentTime() + "/n*Msg:" + msg, t);
        }
    }

}
