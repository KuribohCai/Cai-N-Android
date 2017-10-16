package com.android.n.cai.cainandroid.bluetooth.discovery;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;

/**
 * BleOperation
 * Created by Alan on 10/08/2017.
 */

final class BleReadOperation implements IBleOperation {
    private BluetoothGatt gatt;

    private BluetoothGattCharacteristic charac;

    BleReadOperation(BluetoothGatt gatt, BluetoothGattCharacteristic charac) {
        this.gatt = gatt;
        this.charac = charac;
    }

    BluetoothGatt getGatt() {
        return gatt;
    }

    BluetoothGattCharacteristic getCharac() {
        return charac;
    }

    @Override
    public BleOperationType getOperationType() {
        return BleOperationType.Read;
    }
}
