package com.android.n.cai.cainandroid.bluetooth.discovery;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.android.n.cai.cainandroid.utils.BlueToothBleUtils;
import com.android.n.cai.cainandroid.utils.LogUtils;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * BleDiscoverManager
 * Manage ble characteristic read operations.
 * Created by Alan on 09/08/2017.
 */

final class BleDiscoverManager implements BleDiscoverHelper.IBleDiscoverHelperListener {
    private ConcurrentHashMap<String, BleDiscoverHelper> helpers = new ConcurrentHashMap<>();

    private ConcurrentLinkedQueue<IBleOperation> operationQueue = new ConcurrentLinkedQueue<>();

    private IBleDiscoverManagerListener listener;

    private Handler operationHandler = new Handler(Looper.getMainLooper());

    private Runnable operationRunnable = new Runnable() {
        @Override
        public void run() {
            hasFinishedCurrentOperation = true;
            drive();
        }
    };

    private final static int INTERVAL_OPERATION_TIMEOUT = 500;

    private boolean hasFinishedCurrentOperation = true;

    BleDiscoverManager(IBleDiscoverManagerListener listener) {
        this.listener = listener;
    }

    void addBluetoothDevice(Context context, String address) {
        if (!helpers.containsKey(address)) {
            LogUtils.e("manager","addsuccess  "+"address:" + address);
            helpers.put(address, new BleDiscoverHelper(context,address ,this));
        }
    }

    void clearDevices() {
        helpers.clear();
    }

    // run an operation
    private synchronized void drive() {
        if (operationQueue.isEmpty()) {
            hasFinishedCurrentOperation = true;
            return;
        }

        if (!hasFinishedCurrentOperation) {
            return;
        }

        IBleOperation operation = operationQueue.poll();
        if (operation == null) {
            hasFinishedCurrentOperation = true;
            return;
        }

        hasFinishedCurrentOperation = false;

        operationHandler.postDelayed(operationRunnable, INTERVAL_OPERATION_TIMEOUT);

        if (operation instanceof BleReadOperation) {
            BluetoothGatt gatt = ((BleReadOperation) operation).getGatt();
            BluetoothGattCharacteristic charac = ((BleReadOperation) operation).getCharac();
            gatt.readCharacteristic(charac);
        }
    }

    //
    // implement IBleDiscoverHelperListener
    //

    @Override
    public void deviceQualified(BleDiscoverHelper helper, BleDevice device) {
        listener.foundDevice(device);
        if (helpers.containsValue(helper)) {
            helpers.remove(helper.getDeviceAddress());
        }
        // 添加至已接连设备名列表
        BlueToothBleUtils.sBindList.add(device.getDeviceName());
    }

    @Override
    public void deviceFailed(BleDiscoverHelper helper) {
        if (helpers.containsValue(helper)) {
            helpers.remove(helper.getDeviceAddress());
        }
        BlueToothBleUtils.CONNECTIONSTATE = -1;
    }

    @Override
    public void appendOperation(IBleOperation operation) {
        operationQueue.add(operation);
        drive();
    }

    @Override
    public synchronized void proceedOperation() {
        hasFinishedCurrentOperation = true;
        operationHandler.removeCallbacks(operationRunnable);
        drive();
    }

    /**
     * IBleDiscoverManagerListener:
     * Notify BleDiscover that found our devices.
     */
    interface IBleDiscoverManagerListener {
        void foundDevice(BleDevice device);
    }
}
