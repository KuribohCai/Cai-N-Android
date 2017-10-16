package com.android.n.cai.cainandroid.bluetooth.discovery;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.android.n.cai.cainandroid.event.BluetoothEvent;
import com.android.n.cai.cainandroid.utils.BlueToothBleUtils;
import com.android.n.cai.cainandroid.utils.LogUtils;

import org.greenrobot.eventbus.EventBus;

import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.util.UUID;

import static com.android.n.cai.cainandroid.bluetooth.discovery.BleDiscover.UUID_INFO;
import static com.android.n.cai.cainandroid.bluetooth.discovery.BleDiscover.UUID_MODEL;

/**
 * BleDiscoverHelper
 * Handle ble discovering messages.
 * Created by Alan on 09/08/2017.
 */

public final class BleDiscoverHelper extends BluetoothGattCallback {
    private final static int AUTO_DISCONNECT_INTERVAL = 1000 * 10;

    public BluetoothGatt bluetoothGatt;

    private BleDevice bleDevice;

    private Context mContext;

    public String getDeviceAddress() {
        return mDeviceAddress;
    }

    private String mDeviceAddress;
    private IBleDiscoverHelperListener listener;

    private Handler disconnectHandler = new Handler(Looper.getMainLooper());

    private Runnable disconnectRunnable = new Runnable() {
        @Override
        public void run() {
            gattClosed();
        }
    };

    private boolean isNeedConnect = true;
    private BluetoothDevice mDeviceForAdddress;
    private int count = 0;

    BleDiscoverHelper(Context context, String deviceAddress, IBleDiscoverHelperListener listener) {
        mContext = context;
        mDeviceAddress = deviceAddress;
        this.listener = listener;
        // 连接
        connect();
    }

    private synchronized void connect() {
        // 官方demo推荐使用adapter通过address创建device
        mDeviceForAdddress = BlueToothBleUtils.getAdapter().getRemoteDevice(mDeviceAddress);
        String name = mDeviceForAdddress.getName();
        bleDevice = new BleDevice(mDeviceForAdddress);
        EventBus.getDefault().post(new BluetoothEvent(name + " " + BluetoothEvent.CONNECT));
        // connect ble device
        this.bluetoothGatt = createGatt();
        // 当设备服务更新需要刷新操作
        try {
            Method refreshMethod = bluetoothGatt.getClass().getMethod("refresh", new Class[0]);
            if (refreshMethod != null) {
                refreshMethod.invoke(bluetoothGatt, new Object[0]);
            }
        } catch (Exception e) {
            // do nothing
        }
        // 超时断开
        disconnectHandler.postDelayed(disconnectRunnable, 1000 * 8);
        LogUtils.e("gatt-connect", "001");
    }

    public synchronized BluetoothGatt createGatt() {
        return mDeviceForAdddress.connectGatt(mContext, false, this);
    }

    //
    // implement BluetoothGattCallback
    //

    @Override
    public synchronized void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
        // 移除超时断开
        disconnectHandler.removeCallbacks(disconnectRunnable);
        LogUtils.e("gatt-connect", "002");
        // 操作成功
        if (status == BluetoothGatt.GATT_SUCCESS) {
            // 连接成功状态
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                bluetoothGatt.discoverServices();
                // 超时断开
                disconnectHandler.postDelayed(disconnectRunnable, 1000 * 4);
            }
            // 连接失败状态
            else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                gattClosed();
                EventBus.getDefault().post(new BluetoothEvent(BluetoothEvent.FAIL));
            }
        } else {
            gattClosed();
            count++;
            if (count < 2) {
                // 重连
                connect();
            } else {
                listener.deviceFailed(BleDiscoverHelper.this);
                LogUtils.e("gatt-close", "close");
                EventBus.getDefault().post(new BluetoothEvent(BluetoothEvent.FAIL));
                count = 0;
            }
        }
    }

    @Override
    public synchronized void onServicesDiscovered(BluetoothGatt gatt, int status) {
        // 移除超时断开
        disconnectHandler.removeCallbacks(disconnectRunnable);
        LogUtils.e("gatt-connect", "003");
        if (status == BluetoothGatt.GATT_SUCCESS) {
            BluetoothGattService service = bluetoothGatt.getService(BleDiscover.UUID_SERVICE);
            if (service != null) {
                listener.appendOperation(new BleReadOperation(bluetoothGatt, service.getCharacteristic(BleDiscover.UUID_INFO)));
                listener.appendOperation(new BleReadOperation(bluetoothGatt, service.getCharacteristic(BleDiscover.UUID_MODEL)));
                // 超时断开
                disconnectHandler.postDelayed(disconnectRunnable, 1000 * 4);
            } else {
                gattClosed();
                EventBus.getDefault().post(new BluetoothEvent(BluetoothEvent.FAIL));
            }
        } else {
            gattClosed();
            EventBus.getDefault().post(new BluetoothEvent(BluetoothEvent.FAIL));
        }
    }

    @Override
    public synchronized void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        disconnectHandler.removeCallbacks(disconnectRunnable);
        LogUtils.e("gatt-connect", "004");
        listener.proceedOperation();

        if (characteristic != null) {
            if (characteristic.getUuid().compareTo(UUID_INFO) == 0) {
                byte[] data = characteristic.getValue();
                bleDevice.setDeviceId(getUUID(data));
                bleDevice.setDeviceName(gatt.getDevice().getName());
            } else if (characteristic.getUuid().compareTo(UUID_MODEL) == 0) {
                byte[] data = characteristic.getValue();
                String str = new String(data);
                LogUtils.e("bleDevice::" + bleDevice.getDeviceName(), str);
                String[] split = str.split(",");
                if (split.length == 6) {
                    int manufacturerId = Integer.parseInt(split[0]);
                    String manufacturerName = split[1];
                    int modelId = Integer.parseInt(split[2]);
                    String modelName = split[3];
                    String hardwareVersion = split[4];
                    String softwareVersion = split[5];

                    bleDevice.setManufactureId(manufacturerId);
                    bleDevice.setManufactureName(manufacturerName);
                    bleDevice.setModelId(modelId);
                    bleDevice.setModelName(modelName);
                    bleDevice.setHardwareVersion(hardwareVersion);
                    bleDevice.setSoftwareVersion(softwareVersion);
                }
                if (shouldEmitListener()) {
                    listener.deviceQualified(this, bleDevice);
                    LogUtils.e("gatt-connect", "005");
                } else {
                    EventBus.getDefault().post(new BluetoothEvent(BluetoothEvent.FAIL));
                }
                gattClosed();
            }

        } else {
            gattClosed();
            EventBus.getDefault().post(new BluetoothEvent(BluetoothEvent.FAIL));
        }
    }

    private void gattClosed() {
        try {
            if (bluetoothGatt != null) {
                bluetoothGatt.disconnect();
                bluetoothGatt.close();
                bluetoothGatt = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        listener.deviceFailed(BleDiscoverHelper.this);
        LogUtils.e("gatt-close", "close");
    }

    private boolean shouldEmitListener() {
        return bleDevice.getDeviceId() != null && bleDevice.getDeviceName() != null &&
                bleDevice.getManufactureId() != 0 && bleDevice.getManufactureName() != null
                && bleDevice.getModelId() != 0 && bleDevice.getModelName() != null &
                bleDevice.getHardwareVersion() != null && bleDevice.getSoftwareVersion() != null;
    }

    private UUID getUUID(byte[] data) {
        UUID uuid = null;
        try {
            ByteBuffer bb = ByteBuffer.wrap(data);
            long high = bb.getLong();
            long low = bb.getLong();
            uuid = new UUID(high, low);
            LogUtils.e("device-uuid", uuid.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return uuid;
    }


    interface IBleDiscoverHelperListener {
        void deviceQualified(BleDiscoverHelper helper, BleDevice device);

        void deviceFailed(BleDiscoverHelper helper);

        void appendOperation(IBleOperation operation);

        void proceedOperation();
    }
}
