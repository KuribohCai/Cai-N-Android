package com.android.n.cai.cainandroid.event;

/**
 * Created by Kuriboh on 2017/9/20.
 * E-Mail Address: cai_android@163.com
 */

public class BluetoothEvent {

    public static String SCAN = "发现新设备...";
    public static String CONNECT = "设备连接中...";
    public static String SUCCESS = "设备连接成功";
    public static String FAIL = "设备连接失败";
    public static String STOP = "暂未发现新设备";

    private String mString;

    public BluetoothEvent(String string) {

        mString = string;
    }

    public String getString() {
        return mString;
    }

    public void setString(String string) {
        mString = string;
    }
}
