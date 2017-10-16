package com.android.n.cai.cainandroid.bluetooth.discovery;

/**
 * Created by Alan on 4/8/16.
 */
public class MdnsDevice extends Device {

    private String addr;
    private int port;

    public MdnsDevice(String addr, int port) {
        this.addr = addr;
        this.port = port;
    }

    public String getAddr() {
        return addr;
    }

    public int getPort() {
        return port;
    }

    @Override
    public boolean canBeFoundWith(DiscoveryMode mode) {
        return mode == DiscoveryMode.MDNS;
    }
}
