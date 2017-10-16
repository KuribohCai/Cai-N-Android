package com.android.n.cai.cainandroid.bluetooth;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.android.n.cai.cainandroid.R;
import com.android.n.cai.cainandroid.common.BaseActivity;
import com.android.n.cai.cainandroid.event.BluetoothEvent;
import com.android.n.cai.cainandroid.utils.BlueToothUtils;
import com.android.n.cai.cainandroid.utils.LogUtils;
import com.android.n.cai.cainandroid.utils.ShowToast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Kuriboh on 2017/9/19.
 * E-Mail Address: cai_android@163.com
 */

public class BluetoothActivity extends BaseActivity implements AdapterView.OnItemClickListener, DialogInterface.OnCancelListener {
    private static final int REQUEST_ENABLE_BT = 10;
    private static final long INTERVAL_TIME = 1000 * 5;
    private static final String TAG = "BluetoothActivity";
    @BindView(R.id.listview)
    ListView mListview;
    @BindView(R.id.scan)
    Button mScan;
    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private List<BluetoothClass.Device> devices = new ArrayList<>();
    private ProgressDialog progressDialog;
    private MyAdapter dataAdapter;
    private ArrayList<BluetoothDevice> mListData = new ArrayList<>();
    private int position = -1;
    private boolean hasRegister = false;
    private BroadcastReceiver mMyReceiver = new BlueToothReceiver();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);
        ButterKnife.bind(this);

        initView();
//        init();
        // 注册eventbus
        EventBus.getDefault().register(this);
    }

    private void initView() {

        dataAdapter = new MyAdapter(this, mListData);
        mListview.setAdapter(dataAdapter);
        mListview.setOnItemClickListener(this);
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("提示:");
        progressDialog.setMessage("正在扫描...\n此过程大约需要12秒.");
        progressDialog.setOnCancelListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        BlueToothUtils.cancelScan();
        progressDialog.dismiss();
        if (hasRegister) {
            hasRegister = false;
            unregisterReceiver(mMyReceiver);
        }

        // 取消eventbus
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(BluetoothEvent bluetoothEvent) {
        //接收到发布者发布的事件后，进行相应的处理操作

    }

    private void init() {

//        // 获取蓝牙管理器  BluetoothManager在Android4.3以上支持(API level 18)
//        mBluetoothManager = ((BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE));
//        // 获取蓝牙适配器 方式一
//        mBluetoothAdapter = mBluetoothManager.getAdapter();
        // 获取蓝牙适配器 方式二
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // 检查设备上是否支持蓝牙
        if (mBluetoothAdapter == null) {
            Toast.makeText(BluetoothActivity.this, "没有发现蓝牙模块", Toast.LENGTH_SHORT).show();
            return;
        }

        // 开启蓝牙  开启并且有弹框进行提示，隐式启动Intent
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, REQUEST_ENABLE_BT);
        }
        // 开启蓝牙设备 不给用户进行提示
//        if (!mBluetoothAdapter.isEnabled()) {
//            mBluetoothAdapter.enable();
//        }

        // 获取本地蓝牙信息
        getBlueToothIhfo();
        // 搜索设备
        mBluetoothAdapter.startDiscovery();
        // 注册广播,监听搜索


        // 停止搜索
        mBluetoothAdapter.cancelDiscovery();
    }

    private void getBlueToothIhfo() {
        //获取本机蓝牙名称
        String name = mBluetoothAdapter.getName();
        //获取本机蓝牙地址
        String address = mBluetoothAdapter.getAddress();
        LogUtils.d(TAG, "bluetooth name =" + name + " address =" + address);
        //获取已配对蓝牙设备
        Set<BluetoothDevice> devices = mBluetoothAdapter.getBondedDevices();
        LogUtils.d(TAG, "bonded device size =" + devices.size());
        for (BluetoothDevice bonddevice : devices) {
            LogUtils.d(TAG, "bonded device name =" + bonddevice.getName() + " address" + bonddevice.getAddress());
        }
    }

    private void startScan() {
        progressDialog.show();
        if (BlueToothUtils.openBluetooth(this)) {
            if (mListData != null) {
                mListData.clear();
                dataAdapter.refreshData(mListData);
            }
            BlueToothUtils.scan();
            mScan.setText("正在扫描...");
        }
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    protected void onStart() {
        if (mBluetoothAdapter == null) {
            BlueToothUtils.openBluetooth(this);
        }
        //注册蓝牙接收广播
        if (!hasRegister) {
            hasRegister = true;
            //扫描结束广播
            IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
            //找到设备广播
            filter.addAction(BluetoothDevice.ACTION_FOUND);
            filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
            filter.addAction(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
            filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
            filter.addAction(BluetoothDevice.ACTION_PAIRING_REQUEST);
            registerReceiver(mMyReceiver, filter);
        }
        super.onStart();
    }

    @OnClick(R.id.scan)
    public void onViewClicked() {
        startScan();
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        final BluetoothDevice device = mListData.get(position);
        this.position = position;
        final AlertDialog.Builder dialog = new AlertDialog.Builder(this);// 定义一个弹出框对象
        dialog.setTitle("确认配对此设备吗?");
        dialog.setMessage(device.getName() + ":" + device.getAddress());
        dialog.setPositiveButton("配对",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        BlueToothUtils.cancelScan();
                        BluetoothActivity.this.progressDialog.dismiss();
                        // 连接建立之前的先配对
                        boolean bl = BlueToothUtils.createBond(device);
                        dialog.dismiss();
                        ShowToast.s("配对完成，请返回已配对列表界面");
                    }
                });
        dialog.setNegativeButton("取消",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
        dialog.show();
    }

    @Override
    public void onCancel(DialogInterface dialogInterface) {
        BlueToothUtils.cancelScan();
        mScan.setText("重新扫描");
    }

    public static void startActivity(Context context, Bundle bundle) {
        Intent intent = new Intent(context,BluetoothActivity.class);
        intent.putExtras(bundle);
        context.startActivity(intent);
    }

    public class BlueToothReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            ShowToast.s("收到广播");
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                //搜索到新设备
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                //搜索没有配过对的蓝牙设备
                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    mListData.add(device);
                    dataAdapter.refreshData(mListData);
                } else {
                    ShowToast.s(device.getName() + '\n' + device.getAddress() + " > 已发现");
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {   //搜索结束
                if (mListData.size() == 0) {
                    ShowToast.s("没有发现任何蓝牙设备");
                }
                progressDialog.dismiss();
                mScan.setText("重新扫描");
            } else if (BluetoothDevice.ACTION_PAIRING_REQUEST.equals(action)) {
                if (BluetoothActivity.this.position != -1) {
                    final BluetoothDevice device = mListData.get(BluetoothActivity.this.position);
                    ShowToast.s(device + "  配对成功");
                    EventBus.getDefault().post(new BluetoothEvent(BluetoothEvent.SUCCESS));
                }
            }
        }
    }
}
