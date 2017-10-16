package com.android.n.cai.cainandroid.bluetooth.discovery;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Context;

import com.android.n.cai.cainandroid.utils.BlueToothBleUtils;
import com.android.n.cai.cainandroid.utils.LogUtils;

import java.lang.ref.WeakReference;
import java.nio.charset.Charset;
import java.util.UUID;

import static com.android.n.cai.cainandroid.utils.BlueToothBleUtils.CONNECTIONSTATE;
import static com.android.n.cai.cainandroid.utils.BlueToothBleUtils.sBindList;

/**
 * Auto discovery based on BLE
 * Created by tony on 4/7/16.
 */
class BleDiscover extends AbstractDiscover implements BleDiscoverManager.IBleDiscoverManagerListener {

    private WeakReference<Context> context;

    private boolean configing;

    private DiscoverStatus status;

    private DiscoveryListener listener;
    private BleConfigureListener configureListener;

    private String ssid;
    private String password;

    private BleDiscoverManager discoverManager;

    final static UUID UUID_WRITING = UUID.fromString("00002aea-0000-1000-8000-00805f9b34fb");
    final static UUID UUID_NOTIFY = UUID.fromString("00002aeb-0000-1000-8000-00805f9b34fb");
    final static UUID UUID_SERVICE = UUID.fromString("00001848-0000-1000-8000-00805f9b34fb");
    final static UUID UUID_INFO = UUID.fromString("00002aec-0000-1000-8000-00805f9b34fb");
    final static UUID UUID_MODEL = UUID.fromString("00002aed-0000-1000-8000-00805f9b34fb");
    private String mAddress;

    // 驱动
    public synchronized void connect() {
        // 判断是否空闲状态-1
        if (CONNECTIONSTATE == -1) {
            // 非空闲状态
            CONNECTIONSTATE = -2;
            // 添加蓝牙设备
            Context context = this.context.get();
            if (context != null && mAddress != null && !mAddress.isEmpty() && !sBindList.contains(mAddress)) {
                LogUtils.e("device-connect", "address:" + mAddress);
                discoverManager.addBluetoothDevice(context, mAddress);
            } else {
                CONNECTIONSTATE = -1;
            }
        }
    }

    // gatt callback for configuration
    private BluetoothGattCallback gattConfigureCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);

            if (configing && newState == BluetoothProfile.STATE_CONNECTED) {
                gatt.discoverServices();
            } else if (configing && newState == BluetoothProfile.STATE_DISCONNECTED) {
                configing = false;
                configureListener.onConfigureFailure(BLE_CONFIG_ERROR_BLE_DISCONNECTED);
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);

            enableNotify(gatt);
            writeConfig(gatt);
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);

            if (configing && status != BluetoothGatt.GATT_SUCCESS) {
                configing = false;
                configureListener.onConfigureFailure(BLE_CONFIG_ERROR_WRITING_FAIL);

                gatt.disconnect();
                gatt.close();
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);

            parseConfig(gatt, characteristic);
        }
    };

    public BleDiscover(WeakReference<Context> context) {
        this.context = context;
        this.status = DiscoverStatus.STOPPED;
        this.discoverManager = new BleDiscoverManager(this);
        BlueToothBleUtils.getUtils().setBleDiscoverListener(new BlueToothBleUtils.BleDiscoverListener() {
            @Override
            public void drive(String address) {
                if (mAddress != null && address.equals(mAddress)) {
                    return;
                }
                mAddress = address;
                connect();
            }
        });
        BlueToothBleUtils.getUtils().setLeScanCallback("slamware");
    }

    @Override
    public void setListener(DiscoveryListener listener) {
        this.listener = listener;
    }

    @Override
    public DiscoverStatus getStatus(DiscoveryMode m) {
        return status;
    }

    @Override
    public void start(DiscoveryMode mode) {
        if (this.status != DiscoverStatus.WORKING) {
            // 开始扫描
            if (BlueToothBleUtils.getAdapter() != null && BlueToothBleUtils.getAdapter().isEnabled()) {
                this.status = DiscoverStatus.WORKING;
                BlueToothBleUtils.getUtils().startSearch();
                this.listener.onStartDiscovery(this);
            } else {
                this.status = DiscoverStatus.ERROR;
                this.listener.onDiscoveryError(this, "Discover: bluetooth is shut down");
            }
        }
    }

    @Override
    public void stop(DiscoveryMode mode) {
        if (status == DiscoverStatus.WORKING) {
            // 停止扫描
            BlueToothBleUtils.getUtils().stopSearch();
            listener.onStopDiscovery(this);
            status = DiscoverStatus.STOPPED;
            discoverManager.clearDevices();
        }
    }

    @Override
    public DiscoveryMode getMode() {
        return DiscoveryMode.BLE;
    }

    // config device
    public void configDevice(Device device, String ssid, String password, BleConfigureListener listener) {
        if (ssid == null || ssid.isEmpty()) {
            listener.onConfigureFailure("Discover: bluetoothRequire ssid");
            return;
        }

        if (!(device instanceof BleDevice)) {
            listener.onConfigureFailure("Discover: bluetoothInvalid device");
            return;
        }

        this.configureListener = listener;

        configing = true;
        this.ssid = ssid;
        this.password = password;
        final BluetoothDevice bluetoothDevice = ((BleDevice) device).getDevice();
        bluetoothDevice.connectGatt(context.get(), false, gattConfigureCallback);
    }

    // register notify service
    private void enableNotify(BluetoothGatt gatt) {
        BluetoothGattService service = gatt.getService(UUID_SERVICE);
        BluetoothGattCharacteristic characteristic = service.getCharacteristic(UUID_NOTIFY);
        gatt.setCharacteristicNotification(characteristic, true);
    }

    // write config
    private void writeConfig(BluetoothGatt gatt) {
        BluetoothGattService service = gatt.getService(UUID_SERVICE);

        BluetoothGattCharacteristic charac = service.getCharacteristic(UUID_WRITING);
        byte[] data;
        if ((ssid != null && !ssid.isEmpty()) && (password == null || password.isEmpty())) {
            byte[] ssid = this.ssid.getBytes(Charset.forName("UTF-8"));
            data = new byte[ssid.length + 2];
            data[0] = (byte) 1;
            data[1] = (byte) ssid.length;
            System.arraycopy(ssid, 0, data, 2, ssid.length);
        } else if ((ssid != null && !ssid.isEmpty())) {
            byte[] ssid = this.ssid.getBytes(Charset.forName("UTF-8"));
            byte[] pwd = this.password.getBytes(Charset.forName("UTF-8"));
            data = new byte[ssid.length + pwd.length + 4];
            data[0] = (byte) 1;
            data[1] = (byte) ssid.length;
            System.arraycopy(ssid, 0, data, 2, ssid.length);
            data[ssid.length + 2] = (byte) 2;
            data[ssid.length + 3] = (byte) pwd.length;
            System.arraycopy(pwd, 0, data, ssid.length + 4, pwd.length);
        } else {
            configureListener.onConfigureFailure(BLE_CONFIG_ERROR_SSID_REQUIRED);
            return;
        }

        byte[] cData = new byte[data.length + 1];
        cData[0] = (byte) (data.length + 1);
        System.arraycopy(data, 0, cData, 1, data.length);

        charac.setValue(cData);

        gatt.writeCharacteristic(charac);
    }

    // parse config
    private void parseConfig(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        if (!characteristic.getUuid().equals(UUID_NOTIFY)) {
            gatt.disconnect();
            gatt.close();
            return;
        }

        byte[] data = characteristic.getValue();

        switch (data[0]) {
            case (byte) 0:
                configureListener.onConfigureSuccess();
                break;
            case (byte) 1:
                configureListener.onConfigureFailure(BLE_CONFIG_ERROR_UNABLE_CONNECT_WIFI);
                break;
            case (byte) 2:
                configureListener.onConfigureFailure(BLE_CONFIG_ERROR_INVALID_PWD);
                break;
            default:
                // other data, return.
                return;
        }
        configing = false;
        ssid = null;
        password = null;
        configureListener = null;
        gatt.disconnect();
        gatt.close();
    }

    //
    // implement IBleDiscoverManagerListener
    //

    @Override
    public void foundDevice(BleDevice device) {
        listener.onDeviceFound(this, device);
    }

}
