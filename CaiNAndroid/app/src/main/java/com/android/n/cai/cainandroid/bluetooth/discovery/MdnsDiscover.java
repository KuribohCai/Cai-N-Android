package com.android.n.cai.cainandroid.bluetooth.discovery;

import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;

import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Discover based on mDNS
 * Created by tony on 4/7/16.
 */
class MdnsDiscover extends AbstractDiscover {

    private WeakReference<Context> context;

    // mdns service type
    private final static String SERVICE_TYPE = "_slamware._tcp.";

    private DeviceInfoHandler deviceInfoHandler;

    private NsdManager nsdManager;

    private DiscoveryListener listener;

    private DiscoverStatus status = DiscoverStatus.STOPPED;

    private NsdManager.DiscoveryListener discoveryListener = new NsdManager.DiscoveryListener() {
        @Override
        public void onStartDiscoveryFailed(String serviceType, int errorCode) {
            if (serviceType.equals(SERVICE_TYPE)) {
                listener.onDiscoveryError(MdnsDiscover.this, String.format("MdnDiscover: Failed to start discover. Code: %d", errorCode));
            }
        }

        @Override
        public void onStopDiscoveryFailed(String serviceType, int errorCode) {
            if (serviceType.equals(SERVICE_TYPE)) {
                listener.onDiscoveryError(MdnsDiscover.this, String.format("MdnsDiscover: Failed to stop discover. Code: %d", errorCode));
            }
        }

        @Override
        public void onDiscoveryStarted(String serviceType) {
            listener.onStartDiscovery(MdnsDiscover.this);
            status = DiscoverStatus.WORKING;
        }

        @Override
        public void onDiscoveryStopped(String serviceType) {
            listener.onStopDiscovery(MdnsDiscover.this);
            status = DiscoverStatus.STOPPED;
        }

        @Override
        public void onServiceFound(NsdServiceInfo serviceInfo) {
            if (status == DiscoverStatus.WORKING && serviceInfo.getServiceType().equals(SERVICE_TYPE)) {
                resolveService(serviceInfo);
            }
        }

        @Override
        public void onServiceLost(NsdServiceInfo serviceInfo) {
            // intentionally empty.
        }
    };

    public MdnsDiscover(WeakReference<Context> context) {
        this.context = context;
        deviceInfoHandler = new DeviceInfoHandler();
        nsdManager = (NsdManager) context.get().getSystemService(Context.NSD_SERVICE);
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
        if (status == DiscoverStatus.WORKING) {
            return;
        }

        nsdManager.discoverServices(SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, discoveryListener);
    }

    @Override
    public void stop(DiscoveryMode mode) {
        if (status == DiscoverStatus.WORKING) {
            nsdManager.stopServiceDiscovery(discoveryListener);
        }
    }

    @Override
    public DiscoveryMode getMode() {
        return DiscoveryMode.MDNS;
    }

    private void resolveService(NsdServiceInfo serviceInfo) {
        nsdManager.resolveService(serviceInfo, new NsdManager.ResolveListener() {
            @Override
            public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {
                // intentionally empty
            }

            @Override
            public void onServiceResolved(NsdServiceInfo serviceInfo) {
                MdnsDevice device = new MdnsDevice(serviceInfo.getHost().getHostAddress(), serviceInfo.getPort());
                device.setDeviceName(serviceInfo.getServiceName());

                deviceInfoHandler.appendDevice(device);
            }
        });
    }

    private UUID getUUID(byte[] data) {
        ByteBuffer bb = ByteBuffer.wrap(data);
        long high = bb.getLong();
        long low = bb.getLong();
        return new UUID(high, low);
    }

    private class DeviceInfoHandler {
        private ExecutorService worker;

        private final class Work implements Runnable {
            private MdnsDevice device;

            public Work(MdnsDevice device) {
                this.device = device;
            }

            @Override
            public void run() {
//                SlamwareCorePlatform platform = null;
//                try {
//                    platform = SlamwareCorePlatform.connect(device.getAddr(), device.getPort());
//                    device.setManufactureId(platform.getManufacturerId());
//                    device.setManufactureName(platform.getManuFacturerName());
//                    device.setModelId(platform.getModelId());
//                    device.setModelName(platform.getModelName());
//                    device.setHardwareVersion(platform.getHardwareVersion());
//                    device.setSoftwareVersion(platform.getSoftwareVersion());
//                    device.setDeviceId(UUID.fromString(getFormattedUUIDString(platform.getDeviceId())));
//                    platform.disconnect();
//
//                    if (listener != null) {
//                        listener.onDeviceFound(MdnsDiscover.this, device);
//                    }
//                } catch (Exception e) {
//                    if (platform != null) {
//                        platform.disconnect();
//                    }
//                }
            }

            private String getFormattedUUIDString(String uuid) {
                return String.format("%s-%s-%s-%s-%s", uuid.substring(0, 8), uuid.substring(8, 12),
                        uuid.substring(12, 16), uuid.substring(16, 20), uuid.substring(20, 32));
            }
        }

        public DeviceInfoHandler() {
            worker = Executors.newSingleThreadExecutor();
        }

        public void appendDevice(MdnsDevice device) {
            worker.submit(new Work(device));
        }
    }
}
