package com.android.n.cai.cainandroid.bluetooth;

import android.Manifest;
import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.android.n.cai.cainandroid.R;
import com.android.n.cai.cainandroid.bluetooth.discovery.BleDevice;
import com.android.n.cai.cainandroid.bluetooth.discovery.Device;
import com.android.n.cai.cainandroid.common.BaseActivity;
import com.android.n.cai.cainandroid.event.BluetoothEvent;
import com.android.n.cai.cainandroid.event.DiscoverEvent;
import com.android.n.cai.cainandroid.utils.BlueToothBleUtils;
import com.android.n.cai.cainandroid.utils.LogUtils;
import com.android.n.cai.cainandroid.utils.MIUIUtils;
import com.android.n.cai.cainandroid.utils.ShowToast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Kuriboh on 2017/9/19.
 * E-Mail Address: cai_android@163.com
 */

public class BluetoothBleActivity extends BaseActivity {

    private static final String TAG = "BluetoothActivity";
    @BindView(R.id.listview)
    ListView mListview;
    @BindView(R.id.textview)
    TextView mTextview;
    private DiscoverService discoverService;
    private int requestCount = 0;
    private BluetoothAdapter mBluetoothAdapter;
    private final static int REQUEST_PERMISSION_LOCATION = 10;
    private ArrayList<BluetoothDevice> mListData = new ArrayList<>();
    private MyAdapter dataAdapter;
    private boolean mOpenBluetooth = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetoothble);
        ButterKnife.bind(this);

        mTextview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        discoverService = new DiscoverService(BluetoothBleActivity.this);
        dataAdapter = new MyAdapter(this, mListData);
        mListview.setAdapter(dataAdapter);

        // 注册eventbus
        EventBus.getDefault().register(this);
    }

    public static void startActivity(Context context, Bundle bundle) {
        Intent intent = new Intent(context, BluetoothBleActivity.class);
        intent.putExtras(bundle);
        context.startActivity(intent);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDiscoverDeviceEvent(DiscoverEvent discoverEvent) {
        Device device = discoverEvent.getDevice();
        mListData.add(((BleDevice) device).getDevice());
        dataAdapter.refreshData(mListData);
        LogUtils.e("bluetoothactivity", "device:" + device.getDeviceName());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDiscoverStateEvent(BluetoothEvent event) {
        mTextview.setText(event.getString());
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mOpenBluetooth = BlueToothBleUtils.openBluetooth(this);
        if (mOpenBluetooth) {
            checkLocationPermission(Manifest.permission.ACCESS_COARSE_LOCATION, R.string.message_permission_explanation_location, REQUEST_PERMISSION_LOCATION);
        }


    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopScan();
        // 取消eventbus
        EventBus.getDefault().unregister(this);
    }

    /**
     * start scan
     */
    private void startScan() {
        if (!discoverService.isScanning()) {
            if (!mOpenBluetooth) {
                return;
            }
            BlueToothBleUtils.openBluetooth(this);
            if (mListData != null) {
                mListData.clear();
                // 重置已接连设备名列表
                BlueToothBleUtils.sBindList.clear();
                dataAdapter.refreshData(mListData);
            }
            discoverService.start();
            LogUtils.e("bluetoothactivity", "开始");
            mTextview.setText(BluetoothEvent.SCAN);
        }
    }

    /**
     * stop scan
     */
    private void stopScan() {
        mListData.clear();
        // 重置已接连设备名列表
        BlueToothBleUtils.sBindList.clear();
        discoverService.stop();
        mTextview.setText(BluetoothEvent.STOP);
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void checkLocationPermission(@NonNull final String permission, @StringRes int message, final int requestId) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            startScan();
            return;
        }
        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                requestCount += 1;
                new android.app.AlertDialog.Builder(this)
                        .setMessage(message)
                        .setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                ActivityCompat.requestPermissions(BluetoothBleActivity.this, new String[]{permission}, requestId);
                            }
                        })
                        .setNegativeButton(R.string.button_cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                finish();
                            }
                        })
                        .setCancelable(false)
                        .show();
            } else {
                ActivityCompat.requestPermissions(this, new String[]{permission}, requestId);
            }
        } else {
            startScan();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_PERMISSION_LOCATION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    requestCount = 0;
                    startScan();
                } else {
                    if (requestCount < 1) {
                        if (MIUIUtils.isMIUI()) {//针对MIUI系统的设置，如果用户拒绝一次，就不会在弹出请求权限的对话框
                            final AlertDialog.Builder builder = new AlertDialog.Builder(this);

                            builder.setCancelable(false);
                            builder.setMessage(R.string.setting_open_location_permission);
                            builder.setPositiveButton(R.string.menu_setting, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    //start app settting page
                                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                    intent.setData(Uri.parse("package:" + getPackageName()));
                                    startActivity(intent);
                                }
                            });
                            builder.setNegativeButton(R.string.button_cancel, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                    finish();
                                }
                            });
                            builder.show();
                        } else {
                            checkLocationPermission(Manifest.permission.ACCESS_COARSE_LOCATION, R.string.message_permission_explanation_location, REQUEST_PERMISSION_LOCATION);
                        }
                    } else {
                        finish();
                    }
                }
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT_FIRST_USER) {
            if (resultCode == RESULT_OK) {
                mOpenBluetooth = true;
                checkLocationPermission(Manifest.permission.ACCESS_COARSE_LOCATION, R.string.message_permission_explanation_location, REQUEST_PERMISSION_LOCATION);
            } else if (resultCode == RESULT_CANCELED) {
                mOpenBluetooth = false;
                ShowToast.s("不允许蓝牙开启");
            }

        }
    }
}
