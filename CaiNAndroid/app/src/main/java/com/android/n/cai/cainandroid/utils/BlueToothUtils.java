package com.android.n.cai.cainandroid.utils;

import android.annotation.TargetApi;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Set;

import static android.app.Activity.RESULT_FIRST_USER;

/**
 * Created by Kuriboh on 2017/9/20.
 * E-Mail Address: cai_android@163.com
 */

public class BlueToothUtils {

    private static BlueToothUtils sInstance;
    private static BluetoothAdapter sBluetoothAdapter;
    private static boolean hasRegister = false;

    private static BlueToothUtils getUtils() {
        if (sInstance == null) {
            synchronized (BlueToothUtils.class) {
                if (sInstance == null) {
                    sInstance = new BlueToothUtils();
                }
            }
        }
        return sInstance;
    }

    public BlueToothUtils() {
        sBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    private static BluetoothAdapter getInstance() {
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
        if (!getInstance().isEnabled()) {
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
        return getInstance().isEnabled();
    }

    //关闭蓝牙
    public static boolean closeBluetooth(Activity activity) {
        ShowToast.s("已经关闭蓝牙");
        return getInstance().disable();
    }

    /**
     * 扫描已经配对的设备
     *
     * @return
     */
    public static ArrayList<BluetoothDevice> scanPairs() {
        ShowToast.s("扫描已经配对的蓝牙设备");
        ArrayList<BluetoothDevice> list = null;
        Set<BluetoothDevice> deviceSet = getInstance().getBondedDevices();
        if (deviceSet.size() > 0) {
            //存在已经配对过的蓝牙设备
            list = new ArrayList<>();
            list.addAll(deviceSet);
            ShowToast.s("添加已经配对的设备");
        }
        return list;
    }

    //开始扫描
    public static void scan() {
        ShowToast.s("扫描开始");
        getInstance().startDiscovery();
    }

    //取消扫描
    public static void cancelScan() {
        ShowToast.s("扫描取消");
        if (getInstance().isDiscovering())
            ShowToast.s("取消成功");
        getInstance().cancelDiscovery();

    }

    //蓝牙配对 api19
    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static boolean createBond(BluetoothDevice device) {
        return bond(device, "createBond");
        /*if (device.createBond()) {
            return device.setPairingConfirmation(true);
        }
        return false;*/
    }

    //解除配对
    public static boolean removeBond(BluetoothDevice device) {
        return bond(device, "removeBond");
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private static boolean bond(BluetoothDevice device, String methodName) {
        ShowToast.s("api19 蓝牙配对或解除");
        Boolean returnValue = false;
        if (device != null && device.getBondState() == BluetoothDevice.BOND_NONE) {
            try {
                device.setPairingConfirmation(false);
                cancelPairingUserInput(device);
                Method removeBondMethod = BluetoothDevice.class.getMethod(methodName);
                returnValue = (Boolean) removeBondMethod.invoke(device);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return returnValue;
    }

    //取消配对
    public static boolean cancelBondProcess(BluetoothDevice device) {
        ShowToast.s("配对取消");
        try {
            Method cancelBondMethod = BluetoothDevice.class.getMethod("cancelBondProcess");
            Boolean returnValue = (Boolean) cancelBondMethod.invoke(device);
            return returnValue.booleanValue();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    //取消用户输入
    public static boolean cancelPairingUserInput(BluetoothDevice device) {
        ShowToast.s("输入取消");
        try {
            Method cancelPairingUserInputMethod = BluetoothDevice.class.getMethod("cancelPairingUserInput");
            Boolean returnValue = (Boolean) cancelPairingUserInputMethod.invoke(device);
            return returnValue.booleanValue();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    //注册蓝牙接收广播
    public static void registerBT(Activity activity, BroadcastReceiver mMyReceiver) {
        ShowToast.s("注册蓝牙接收 广播");
        if (!hasRegister) {
            hasRegister = true;
            //扫描结束广播
            IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
            //找到设备广播
            filter.addAction(BluetoothDevice.ACTION_FOUND);//搜索蓝压设备，每搜到一个设备发送一条广播
            filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);//配对开始时，配对成功时
            filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED); //配对时，发起连接
            filter.addAction(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);//搜索模式改变
            filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);//连接蓝牙，断开蓝牙
            filter.addAction(BluetoothDevice.ACTION_PAIRING_REQUEST); //配对请求
            activity.registerReceiver(mMyReceiver, filter);
        }
    }


}
