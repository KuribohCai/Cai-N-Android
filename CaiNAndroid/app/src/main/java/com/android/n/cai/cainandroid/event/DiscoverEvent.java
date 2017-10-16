package com.android.n.cai.cainandroid.event;

import android.bluetooth.BluetoothDevice;

import com.android.n.cai.cainandroid.bluetooth.discovery.Device ;

/**
 * Created by hagar on 17/2/10.
 */

public class DiscoverEvent {
    private Device device;

    public DiscoverEvent(Device devices) {
        this.device = devices;
    }

    public Device getDevice() {
        return device;
    }

    public void setDevice(BluetoothDevice device) {
        mDevice = device;
    }

    private BluetoothDevice mDevice;



    public BluetoothDevice getBluetoothDevice() {
        return mDevice;
    }
}
