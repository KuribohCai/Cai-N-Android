package com.android.n.cai.cainandroid.bluetooth.discovery;

import android.bluetooth.BluetoothDevice;

/**
 * Created by Alan on 4/8/16.
 */
public class BleDevice extends Device {

    private BluetoothDevice device;

    public BleDevice(BluetoothDevice device) {
        this.device = device;
    }

    public BluetoothDevice getDevice() {
        return device;
    }

    @Override
    public boolean canBeFoundWith(DiscoveryMode mode) {
        return mode == DiscoveryMode.BLE;
    }
}
