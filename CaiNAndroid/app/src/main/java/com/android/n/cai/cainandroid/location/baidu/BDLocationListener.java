package com.android.n.cai.cainandroid.location.baidu;

import android.widget.TextView;

import com.android.n.cai.cainandroid.utils.LogUtils;
import com.android.n.cai.cainandroid.R;
import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.Poi;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;

/**
 * Created by Kuriboh on 2017/9/14.
 * E-Mail Address: cai_android@163.com
 *
 * 定位结果回调，重写onReceiveLocation方法
 */

public class BDLocationListener extends BDAbstractLocationListener {

    private TextView mTextView;
    private BaiduMap mBaiduMap;

    private BitmapDescriptor mCurrentMarker;
    /**
     * 定位模式
     */
    private MyLocationConfiguration.LocationMode mCurrentMode;

    public BDLocationListener(TextView textview, BaiduMap baiduMap) {
        mTextView = textview;
        mBaiduMap = baiduMap;
    }

    @Override
    public void onReceiveLocation(BDLocation location) {
        //定义地图状态  zoom(18)【地图缩放级别 3~20】
        MapStatus mMapStatus = new MapStatus.Builder().zoom(18).build();
        // 地图状态更新
        MapStatusUpdate mMapStatusUpdate = MapStatusUpdateFactory.newMapStatus(mMapStatus);
        //改变地图状态
        mBaiduMap.setMapStatus(mMapStatusUpdate);
        //定位图层显示方式
        mCurrentMode = MyLocationConfiguration.LocationMode.FOLLOWING;
        // 开启定位图层
        mBaiduMap.setMyLocationEnabled(true);
        // 用户自定义定位图标
        mCurrentMarker = BitmapDescriptorFactory.fromResource(R.mipmap.icon_baidu_location);
        /**
         * 设置定位图层配置信息，只有先允许定位图层后设置定位图层配置信息才会生效
         * customMarker用户自定义定位图标
         * enableDirection是否允许显示方向信息
         * locationMode定位图层显示方式
         */
        MyLocationConfiguration config = new MyLocationConfiguration(mCurrentMode, true, mCurrentMarker);
        // 设置定位配置信息
        mBaiduMap.setMyLocationConfiguration(config);
        // 构造定位数据
        MyLocationData locData = new MyLocationData.Builder()
                //定位精度bdLocation.getRadius()
                .accuracy(location.getRadius())
                // 此处设置开发者获取到的方向信息，顺时针0-360
                .direction(location.getDirection())
                //经度
                .latitude(location.getLatitude())
                //纬度
                .longitude(location.getLongitude())
                .build();
        // 设置定位数据
        mBaiduMap.setMyLocationData(locData);

        // TODO Auto-generated method stub
        if (null != location && location.getLocType() != BDLocation.TypeServerError) {
            StringBuffer sb = new StringBuffer(256);
            sb.append("time : ");
            /**
             * 时间也可以使用systemClock.elapsedRealtime()方法 获取的是自从开机以来，每次回调的时间；
             * location.getTime() 是指服务端出本次结果的时间，如果位置不发生变化，则时间不变
             */
            sb.append(location.getTime());
            sb.append("\nlocType : ");// 定位类型
            sb.append(location.getLocType());
            sb.append("\nlocType description : ");// *****对应的定位类型说明*****
            sb.append(location.getLocTypeDescription());
            sb.append("\nlatitude : ");// 纬度
            sb.append(location.getLatitude());
            sb.append("\nlontitude : ");// 经度
            sb.append(location.getLongitude());
            sb.append("\nradius : ");// 半径
            sb.append(location.getRadius());
            sb.append("\nCountryCode : ");// 国家码
            sb.append(location.getCountryCode());
            sb.append("\nCountry : ");// 国家名称
            sb.append(location.getCountry());
            sb.append("\ncitycode : ");// 城市编码
            sb.append(location.getCityCode());
            sb.append("\ncity : ");// 城市
            sb.append(location.getCity());
            sb.append("\nDistrict : ");// 区
            sb.append(location.getDistrict());
            sb.append("\nStreet : ");// 街道
            sb.append(location.getStreet());
            sb.append("\naddr : ");// 地址信息
            sb.append(location.getAddrStr());
            sb.append("\nUserIndoorState: ");// *****返回用户室内外判断结果*****
            sb.append(location.getUserIndoorState());
            sb.append("\nDirection(not all devices have value): ");
            sb.append(location.getDirection());// 方向
            sb.append("\nlocationdescribe: ");
            sb.append(location.getLocationDescribe());// 位置语义化信息
            sb.append("\nPoi: ");// POI信息
            if (location.getPoiList() != null && !location.getPoiList().isEmpty()) {
                for (int i = 0; i < location.getPoiList().size(); i++) {
                    Poi poi = (Poi) location.getPoiList().get(i);
                    sb.append(poi.getName() + ";");
                }
            }
            if (location.getLocType() == BDLocation.TypeGpsLocation) {// GPS定位结果
                sb.append("\nspeed : ");
                sb.append(location.getSpeed());// 速度 单位：km/h
                sb.append("\nsatellite : ");
                sb.append(location.getSatelliteNumber());// 卫星数目
                sb.append("\nheight : ");
                sb.append(location.getAltitude());// 海拔高度 单位：米
                sb.append("\ngps status : ");
                sb.append(location.getGpsAccuracyStatus());// *****gps质量判断*****
                sb.append("\ndescribe : ");
                sb.append("gps定位成功");
            } else if (location.getLocType() == BDLocation.TypeNetWorkLocation) {// 网络定位结果
                // 运营商信息
                if (location.hasAltitude()) {// *****如果有海拔高度*****
                    sb.append("\nheight : ");
                    sb.append(location.getAltitude());// 单位：米
                }
                sb.append("\noperationers : ");// 运营商信息
                sb.append(location.getOperators());
                sb.append("\ndescribe : ");
                sb.append("网络定位成功");
            } else if (location.getLocType() == BDLocation.TypeOffLineLocation) {// 离线定位结果
                sb.append("\ndescribe : ");
                sb.append("离线定位成功，离线定位结果也是有效的");
            } else if (location.getLocType() == BDLocation.TypeServerError) {
                sb.append("\ndescribe : ");
                sb.append("服务端网络定位失败，可以反馈IMEI号和大体定位时间到loc-bugs@baidu.com，会有人追查原因");
            } else if (location.getLocType() == BDLocation.TypeNetWorkException) {
                sb.append("\ndescribe : ");
                sb.append("网络不同导致定位失败，请检查网络是否通畅");
            } else if (location.getLocType() == BDLocation.TypeCriteriaException) {
                sb.append("\ndescribe : ");
                sb.append("无法获取有效定位依据导致定位失败，一般是由于手机的原因，处于飞行模式下一般会造成这种结果，可以试着重启手机");
            }
            logMsg(sb.toString());
        }
    }

//    @Override
//    public void onReceiveLocation(BDLocation location) {
//
//        //获取定位结果
//        location.getTime();    //获取定位时间
//        location.getLocationID();    //获取定位唯一ID，v7.2版本新增，用于排查定位问题
//        location.getLocType();    //获取定位类型
//        location.getLatitude();    //获取纬度信息
//        location.getLongitude();    //获取经度信息
//        location.getRadius();    //获取定位精准度
//        location.getAddrStr();    //获取地址信息
//        location.getCountry();    //获取国家信息
//        location.getCountryCode();    //获取国家码
//        location.getCity();    //获取城市信息
//        location.getCityCode();    //获取城市码
//        location.getDistrict();    //获取区县信息
//        location.getStreet();    //获取街道信息
//        location.getStreetNumber();    //获取街道码
//        location.getLocationDescribe();    //获取当前位置描述信息
//        location.getPoiList();    //获取当前位置周边POI信息
//
//        location.getBuildingID();    //室内精准定位下，获取楼宇ID
//        location.getBuildingName();    //室内精准定位下，获取楼宇名称
//        location.getFloor();    //室内精准定位下，获取当前位置所处的楼层信息
//
//        if (location.getLocType() == BDLocation.TypeGpsLocation){
//
//            //当前为GPS定位结果，可获取以下信息
//            location.getSpeed();    //获取当前速度，单位：公里每小时
//            location.getSatelliteNumber();    //获取当前卫星数
//            location.getAltitude();    //获取海拔高度信息，单位米
//            location.getDirection();    //获取方向信息，单位度
//            location.
//
//        } else if (location.getLocType() == BDLocation.TypeNetWorkLocation){
//
//            //当前为网络定位结果，可获取以下信息
//            location.getOperators();    //获取运营商信息
//
//        } else if (location.getLocType() == BDLocation.TypeOffLineLocation) {
//
//            //当前为网络定位结果
//
//        } else if (location.getLocType() == BDLocation.TypeServerError) {
//
//            //当前网络定位失败
//            //可将定位唯一ID、IMEI、定位失败时间反馈至loc-bugs@baidu.com
//
//        } else if (location.getLocType() == BDLocation.TypeNetWorkException) {
//
//            //当前网络不通
//
//        } else if (location.getLocType() == BDLocation.TypeCriteriaException) {
//
//            //当前缺少定位依据，可能是用户没有授权，建议弹出提示框让用户开启权限
//            //可进一步参考onLocDiagnosticMessage中的错误返回码
//
//        }
//    }

    /**
     * 回调定位诊断信息，开发者可以根据相关信息解决定位遇到的一些问题
     * 自动回调，相同的diagnosticType只会回调一次
     *
     * @param locType           当前定位类型
     * @param diagnosticType    诊断类型（1~9）
     * @param diagnosticMessage 具体的诊断信息释义
     */
    public void onLocDiagnosticMessage(int locType, int diagnosticType, String diagnosticMessage) {

        if (diagnosticType == LocationClient.LOC_DIAGNOSTIC_TYPE_BETTER_OPEN_GPS) {

            //建议打开GPS

        } else if (diagnosticType == LocationClient.LOC_DIAGNOSTIC_TYPE_BETTER_OPEN_WIFI) {

            //建议打开wifi，不必连接，这样有助于提高网络定位精度！

        } else if (diagnosticType == LocationClient.LOC_DIAGNOSTIC_TYPE_NEED_CHECK_LOC_PERMISSION) {

            //定位权限受限，建议提示用户授予APP定位权限！

        } else if (diagnosticType == LocationClient.LOC_DIAGNOSTIC_TYPE_NEED_CHECK_NET) {

            //网络异常造成定位失败，建议用户确认网络状态是否异常！

        } else if (diagnosticType == LocationClient.LOC_DIAGNOSTIC_TYPE_NEED_CLOSE_FLYMODE) {

            //手机飞行模式造成定位失败，建议用户关闭飞行模式后再重试定位！

        } else if (diagnosticType == LocationClient.LOC_DIAGNOSTIC_TYPE_NEED_INSERT_SIMCARD_OR_OPEN_WIFI) {

            //无法获取任何定位依据，建议用户打开wifi或者插入sim卡重试！

        } else if (diagnosticType == LocationClient.LOC_DIAGNOSTIC_TYPE_NEED_OPEN_PHONE_LOC_SWITCH) {

            //无法获取有效定位依据，建议用户打开手机设置里的定位开关后重试！

        } else if (diagnosticType == LocationClient.LOC_DIAGNOSTIC_TYPE_SERVER_FAIL) {

            //百度定位服务端定位失败
            //建议反馈location.getLocationID()和大体定位时间到loc-bugs@baidu.com

        } else if (diagnosticType == LocationClient.LOC_DIAGNOSTIC_TYPE_FAIL_UNKNOWN) {

            //无法获取有效定位依据，但无法确定具体原因
            //建议检查是否有安全软件屏蔽相关定位权限
            //或调用LocationClient.restart()重新启动后重试！

        }
    }

    /**
     * 显示请求字符串
     *
     * @param str
     */
    public void logMsg(String str) {
        final String s = str;
        try {
            if (mTextView != null) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        mTextView.post(new Runnable() {
                            @Override
                            public void run() {
                                mTextView.setText(s);
                                LogUtils.e("百度",s);
                            }
                        });

                    }
                }).start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
