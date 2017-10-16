package com.android.n.cai.cainandroid.utils;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import static android.app.Activity.RESULT_FIRST_USER;

/**
 * Created by Kuriboh on 2017/9/28.
 * E-Mail Address: cai_android@163.com
 */

public class BlueToothBleUtils {
    // 连接状态 -1 空闲
    public static int CONNECTIONSTATE = -1;
    private static BlueToothBleUtils sInstance;
    private static BluetoothAdapter sBluetoothAdapter;
    private boolean isScaning = false;

    // 已绑定设备地址列表
    public static List<String> sBindList = new ArrayList<>();

    private WeakReference<Context> mContext;
    private BluetoothAdapter.LeScanCallback leScanCallback;

    public void setBleDiscoverListener(BleDiscoverListener bleDiscoverListener) {
        mBleDiscoverListener = bleDiscoverListener;
    }

    private BleDiscoverListener mBleDiscoverListener;

    public interface BleDiscoverListener {
        void drive(String address);
    }

    ;

    Handler mHandler = new Handler(Looper.myLooper());

    Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            if (CONNECTIONSTATE == -1) {

            } else {
                mHandler.postDelayed(mRunnable,500);
            }
        }
    };

    public void setLeScanCallback(final String flag) {
        this.leScanCallback = new BluetoothAdapter.LeScanCallback() {
            @Override
            public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
                String name = device.getName();
                if (name != null && name.toLowerCase().contains(flag)) {
                    String address = device.getAddress();
                    if (address != null && !sBindList.contains(address)) {
                        LogUtils.e("device-LeScan", "name:" + name + "   address:" + address);
                        connectDevice(address);
                    }
                }
            }
        };
    }

    // 连接设备
    public synchronized void connectDevice(String address) {
        mBleDiscoverListener.drive(address);
    }

    public static BlueToothBleUtils getUtils() {
        if (sInstance == null) {
            synchronized (BlueToothUtils.class) {
                if (sInstance == null) {
                    sInstance = new BlueToothBleUtils();
                }
            }
        }
        return sInstance;
    }

    public BlueToothBleUtils() {
        sBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    // 蓝牙本地适配器
    public static BluetoothAdapter getAdapter() {
        if (sBluetoothAdapter == null) {
            sBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        }
        return sBluetoothAdapter;
    }

    /**
     * 开启蓝牙
     *
     * @param activity 上下文
     * @return 是否开启成功
     */
    public static boolean openBluetooth(Activity activity) {
        //确认开启蓝牙
        boolean enabled = getAdapter().isEnabled();
        if (!enabled) {
            //=默认120秒==============================================================
            //使蓝牙设备可见，方便配对
            //Intent in = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            //in.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            //activity.startActivityForResult(in,Activity.RESULT_OK);
            //=1=============================================================
            //请求用户开启，需要提示
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            activity.startActivityForResult(intent, RESULT_FIRST_USER);
            //=2=============================================================
            //程序直接开启，不经过提示
//            getInstance().enable();
        }
        ShowToast.s("已经开启蓝牙");
        return enabled;
    }

    //关闭蓝牙
    public static boolean closeBluetooth(Activity activity) {
        ShowToast.s("已经关闭蓝牙");
        return getAdapter().disable();
    }

    // 开始搜索
    public void startSearch() {
        if (!isScaning) {
            isScaning = true;
            getAdapter().startLeScan(leScanCallback);
        }
    }

    // 停止搜索
    public void stopSearch() {
        if (isScaning) {
            isScaning = false;
            getAdapter().stopLeScan(leScanCallback);
        }
    }

}
