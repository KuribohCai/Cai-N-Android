package com.android.n.cai.cainandroid.bluetooth.discovery;

import android.content.Context;

import java.lang.ref.WeakReference;

/**
 * The manager to manage devices
 * Created by tony on 4/7/16.
 */
public class DeviceManager extends AbstractDiscover {
    private BleDiscover bleDiscover;
    private MdnsDiscover mdnsDiscover;


    public DeviceManager(Context context)
    {
        bleDiscover = new BleDiscover(new WeakReference<>(context));
        mdnsDiscover = new MdnsDiscover(new WeakReference<>(context));
    }
//
//    // Connection
//    /**
//     * Connect to Slamware Core directly (usually used in Android devices directly connected to Slamware Core via the High Speed Bus)
//     * @param host The device host (usually 192.168.11.1)
//     * @param port The port
//     * @return The connected platform
//     */
//    public static AbstractSlamwarePlatform connect(String host, int port)
//    {
//        return SlamwareCorePlatform.connect(host, port);
//    }
//
//    /**
//     * Connect to a specific Slamware-based device
//     * @param device The device to connect to
//     * @return The connected platform
//     */
//    public AbstractSlamwarePlatform connect(Device device)
//    {
//        // TODO
//        return null;
//    }

    /**
     * Pair Slamware device with SSID and password
     * @param device The device to pair
     * @param wifiSSID The WiFi SSID
     * @param wifiPassword The WiFi password
     * @param listener The configuration listener
     */
    public void pair(Device device, String wifiSSID, String wifiPassword, BleConfigureListener listener)
    {
        bleDiscover.configDevice(device, wifiSSID, wifiPassword, listener);
    }

    // Auto discovery
    @Override
    public void setListener(DiscoveryListener listener) {
        bleDiscover.setListener(listener);
        mdnsDiscover.setListener(listener);
    }

    @Override
    public DiscoverStatus getStatus(DiscoveryMode mode) {
        return mode == DiscoveryMode.BLE ? this.bleDiscover.getStatus(mode) : this.mdnsDiscover.getStatus(mode);
    }

    @Override
    public void start(DiscoveryMode mode) {
        if (mode == DiscoveryMode.MDNS) {
            mdnsDiscover.start(mode);
        } else if (mode == DiscoveryMode.BLE) {
            bleDiscover.start(mode);
        }
    }

    @Override
    public void stop(DiscoveryMode mode) {
        if (mode == DiscoveryMode.MDNS) {
            mdnsDiscover.stop(mode);
        } else if (mode == DiscoveryMode.BLE) {
            bleDiscover.stop(mode);
        }
    }

    @Override
    public DiscoveryMode getMode() {
        return null;
    }
}
