package com.android.n.cai.cainandroid.event;


import com.android.n.cai.cainandroid.bluetooth.DiscoverService.DiscoverError;
import com.android.n.cai.cainandroid.bluetooth.DiscoverService.DiscoverType;

/**
 * Created by hagar on 17/2/10.
 */

public class DiscoverErrorEvent {
    private String message;

    private DiscoverType type;

    private DiscoverError error;

    public DiscoverErrorEvent(String message, DiscoverType type, DiscoverError error) {
        this.message = message;
        this.type = type;
        this.error = error;
    }

    public String getMessage() {
        return message;
    }

    public DiscoverType getType() {
        return type;
    }

    public DiscoverError getError() {
        return error;
    }

}
