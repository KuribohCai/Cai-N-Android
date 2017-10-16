package com.android.n.cai.cainandroid.bluetooth.discovery;

/**
 * Abstract discover interface
 * Created by tony on 4/7/16.
 */
public abstract class AbstractDiscover {
    public static abstract class DiscoveryListener
    {
        public abstract void onStartDiscovery(AbstractDiscover discover);
        public abstract void onStopDiscovery(AbstractDiscover discover);
        public abstract void onDiscoveryError(AbstractDiscover discover, String error);
        public abstract void onDeviceFound(AbstractDiscover discover, Device device);
    }

    public interface BleConfigureListener {
        void onConfigureSuccess();
        void onConfigureFailure(String error);
    }

    public final static String BLE_CONFIG_ERROR_BLE_DISCONNECTED = "Discover: bluetooth connection lost";
    public final static String BLE_CONFIG_ERROR_WRITING_FAIL = "Discover: bluetooth write characteristic failed";
    public final static String BLE_CONFIG_ERROR_SSID_REQUIRED = "Discover: bluetoothRequire ssid";
    public final static String BLE_CONFIG_ERROR_UNABLE_CONNECT_WIFI = "Discover: unable to connect wifi";
    public final static String BLE_CONFIG_ERROR_INVALID_PWD = "Discover: invalid password";

    public enum DiscoverStatus {
        STOPPED,
        WORKING,
        ERROR
    }

    protected DiscoveryMode mode;

    public abstract DiscoveryMode getMode();

    public abstract void setListener(DiscoveryListener listener);
    public abstract DiscoverStatus getStatus(DiscoveryMode mode);
    public abstract void start(DiscoveryMode mode);
    public abstract void stop(DiscoveryMode mode);
}
