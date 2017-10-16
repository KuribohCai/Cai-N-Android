package com.android.n.cai.cainandroid.event;

/**
 * Created by Alan on 4/20/16.
 */
public class DeviceConfigureEvent {

    private boolean success;

    private String error;

    public DeviceConfigureEvent(boolean success) {
        this.success = success;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
