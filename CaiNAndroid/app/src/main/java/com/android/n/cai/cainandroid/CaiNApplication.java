package com.android.n.cai.cainandroid;

import android.app.Application;
import android.app.Service;
import android.os.Vibrator;

import com.android.n.cai.cainandroid.location.baidu.LocationService;
import com.baidu.mapapi.SDKInitializer;

/**
 * Created by Kuriboh on 2017/9/14.
 * E-Mail Address: cai_android@163.com
 */

public class CaiNApplication extends Application {

    public LocationService locationService;
    public Vibrator mVibrator;

    @Override
    public void onCreate() {
        super.onCreate();
        /***
         * 初始化定位sdk，建议在Application中创建
         */
        locationService = new LocationService(getApplicationContext());
        mVibrator =(Vibrator)getApplicationContext().getSystemService(Service.VIBRATOR_SERVICE);
        //在使用SDK各组件之前初始化context信息，传入ApplicationContext
        //注意该方法要再setContentView方法之前实现
        SDKInitializer.initialize(getApplicationContext());
    }
}
