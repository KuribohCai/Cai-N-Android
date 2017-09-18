package com.android.n.cai.cainandroid.location.baidu;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.Button;
import android.widget.TextView;

import com.android.n.cai.cainandroid.common.BaseActivity;
import com.android.n.cai.cainandroid.CaiNApplication;
import com.android.n.cai.cainandroid.R;
import com.baidu.location.BDAbstractLocationListener;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 百度SDK
 * Created by Kuriboh on 2017/9/12.
 * E-Mail Address: cai_android@163.com
 */

public class BaiDuSDKActivity extends BaseActivity {

    @BindView(R.id.mapView)
    MapView mMapView;
    @BindView(R.id.button)
    Button mButton;
    @BindView(R.id.textView)
    TextView mTextView;
    private LocationService locationService;
    private BDAbstractLocationListener mListener;
    private BaiduMap mBaiduMap;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_baidusdk);
        ButterKnife.bind(this);
        initView();
    }

    private void initView() {
        // 获得地图
        mBaiduMap = mMapView.getMap();
        // 创建百度定位监听
        mListener = new BDLocationListener(mTextView, mBaiduMap);

    }

    public void closeMap() {
        if (mBaiduMap != null) {
            // 当不需要定位图层时关闭定位图层
            mBaiduMap.setMyLocationEnabled(false);
        }
    }

    public static void startActivity(Context context, Bundle bundle) {
        Intent intent = new Intent(context, BaiDuSDKActivity.class);
        intent.putExtras(bundle);
        context.startActivity(intent);
    }

    @OnClick(R.id.button)
    public void onViewClicked() {
        if (mButton.getText().toString().equals(getString(R.string.startlocation))) {
            locationService.start();// 定位SDK
            // start之后会默认发起一次定位请求，开发者无须判断isstart并主动调用request
            mButton.setText(getString(R.string.stoplocation));
        } else {
            locationService.stop();
            mButton.setText(getString(R.string.startlocation));
        }
    }

    /***
     * Stop location service
     */
    @Override
    protected void onStop() {
        // TODO Auto-generated method stub
        locationService.unregisterListener(mListener); //注销掉监听
        locationService.stop(); //停止定位服务
        super.onStop();
    }

    @Override
    protected void onStart() {
        // TODO Auto-generated method stub
        super.onStart();
        // -----------location config ------------
        locationService = ((CaiNApplication) getApplication()).locationService;
        //获取locationservice实例，建议应用中只初始化1个location实例，然后使用，可以参考其他示例的activity，都是通过此种方式获取locationservice实例的
        locationService.registerListener(mListener);
        //注册监听
        locationService.setLocationOption(locationService.getDefaultLocationClientOption());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
        mMapView.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView. onResume ()，实现地图生命周期管理
        mMapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView. onPause ()，实现地图生命周期管理
        mMapView.onPause();
    }
}
