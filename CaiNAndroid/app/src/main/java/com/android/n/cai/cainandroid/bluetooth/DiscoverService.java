package com.android.n.cai.cainandroid.bluetooth;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.android.n.cai.cainandroid.bluetooth.discovery.AbstractDiscover;
import com.android.n.cai.cainandroid.bluetooth.discovery.BleDevice;
import com.android.n.cai.cainandroid.bluetooth.discovery.Device;
import com.android.n.cai.cainandroid.bluetooth.discovery.DeviceManager;
import com.android.n.cai.cainandroid.bluetooth.discovery.DiscoveryMode;
import com.android.n.cai.cainandroid.event.BluetoothEvent;
import com.android.n.cai.cainandroid.event.DeviceConfigureEvent;
import com.android.n.cai.cainandroid.event.DiscoverErrorEvent;
import com.android.n.cai.cainandroid.event.DiscoverEvent;
import com.android.n.cai.cainandroid.utils.LogUtils;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;

/**
 * Created by hagar on 17/2/11.
 */

public class DiscoverService {
    private static final String TAG = DiscoverService.class.getName();

    private DeviceManager deviceManager;
    public ArrayList<Device> scanDevices = new ArrayList<Device>();
    private boolean scanning = false;


    public DiscoverService(Context context) {

        scanDevices.clear();

        deviceManager = new DeviceManager(context);
        deviceManager.setListener(new AbstractDiscover.DiscoveryListener() {
            @Override
            public void onStartDiscovery(AbstractDiscover abstractDiscover) {
                LogUtils.d(TAG, "on start discovery. mode:" + abstractDiscover.getMode().toString());
                EventBus.getDefault().post(new BluetoothEvent(BluetoothEvent.SCAN));
            }

            @Override
            public void onStopDiscovery(AbstractDiscover abstractDiscover) {
                LogUtils.d(TAG, "on stop discovery. mode:" + abstractDiscover.getMode().toString());
                EventBus.getDefault().post(new BluetoothEvent(BluetoothEvent.STOP));
            }

            @Override
            public void onDiscoveryError(AbstractDiscover abstractDiscover, String s) {
                LogUtils.d(TAG, "on discovery error: " + s);
                if (abstractDiscover.getMode() == DiscoveryMode.BLE) {
                    EventBus.getDefault().post(new DiscoverErrorEvent(s, DiscoverType.BLE, DiscoverError.BLE_POWER_OFF));
                    EventBus.getDefault().post(new BluetoothEvent(BluetoothEvent.FAIL));
                }
            }

            @Override
            public void onDeviceFound(AbstractDiscover abstractDiscover, Device device) {
                LogUtils.d(TAG, "on discovery device ");
//                if (!isScanning()) {
//                    return;
//                }

//                if (!SDPModelUtil.getInstance().isValidManufacturer(device.getManufactureId())) {
//                    return;
//                }

                for (Device d : scanDevices) {
                    if (device.canBeFoundWith(DiscoveryMode.BLE) == d.canBeFoundWith(DiscoveryMode.BLE)
                            && device.getDeviceName().equals(d.getDeviceName())) {
                        return;
                    }
                }

                if (device.canBeFoundWith(DiscoveryMode.BLE)) {
                    scanDevices.add(device);
                    EventBus.getDefault().post(new DiscoverEvent(device));
                    EventBus.getDefault().post(new BluetoothEvent(BluetoothEvent.SUCCESS));
                }

            }
        });


    }

    /**
     * get scan device
     *
     * @return
     */
    public ArrayList<Device> getScanDevices() {
        return scanDevices;
    }


    /**
     * start scan
     */
    public void start() {
        scanning = true;
        deviceManager.start(DiscoveryMode.BLE);


    }

    public boolean isScanning() {
        return scanning;
    }

    /**
     * stop scan
     */
    public void stop() {
        scanning = false;
        deviceManager.stop(DiscoveryMode.BLE);
        scanDevices.clear();

    }

    /**
     * Discover type
     */
    public enum DiscoverType {
        // BLE
        BLE,

        // MDNS
        MDNS,

        // cloud
        CLOUD
    }


    /**
     * Discover error
     */
    public enum DiscoverError {
        // bluetooth low energy not support
        BLE_NOT_SUPPORT,

        // bluetooth disabled
        BLE_POWER_OFF,

        // mdns not start
        MDNS_ERROR
    }


    public void pair(@NonNull BleDevice device, @NonNull String ssid, @Nullable String pwd) {
        deviceManager.pair(device, ssid, pwd, new AbstractDiscover.BleConfigureListener() {
            @Override
            public void onConfigureSuccess() {

                EventBus.getDefault().post(new DeviceConfigureEvent(true));
            }

            @Override
            public void onConfigureFailure(String s) {
                DeviceConfigureEvent event = new DeviceConfigureEvent(false);
                event.setError(s);

                EventBus.getDefault().post(event);
            }
        });
    }

}
