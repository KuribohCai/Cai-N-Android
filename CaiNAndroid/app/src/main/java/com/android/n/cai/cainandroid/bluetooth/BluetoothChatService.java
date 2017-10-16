package com.android.n.cai.cainandroid.bluetooth;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.text.TextUtils;

import com.android.n.cai.cainandroid.utils.LogUtils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.UUID;

/**
 * Created by Kuriboh on 2017/9/21.
 * E-Mail Address: cai_android@163.com
 */

@SuppressLint("NewApi")
public class BluetoothChatService {
    private static final String TAG = "BluetoothChatService";
    private static boolean DEBUG = true;

    // 此处，必须使用Android的SSP（协议栈默认）的UUID：
    //00001101-0000-1000-8000-00805F9B34FB
    //才能正常和外部的，也是SSP串口的蓝牙设备去连接。
    private static final UUID MY_UUID_SECURE = UUID.fromString("a81dc3ff-bee3-4df1-9553-9d08e1b5b9d6");
    private static final UUID MY_UUID_INSECURE = UUID.fromString("a81dc3ff-bee3-4df1-9553-9d08e1b5b9d6");

    // Member fields
    private final BluetoothAdapter mAdapter;
    private final Handler mHandler;// UI线程的Handler
    private ServerConnectThread mServerConnectThread;// 服务器监听线程
    private ClientConnectThread mClientConnectThread;// 客户端监听线程
    private ReadWriteThread mReadWriteThread;// 数据流读写线程

    private int mState = STATE_NONE;// 当前连接状态
    // 表示当前连接状态的常量
    public static final int STATE_NONE = 0x0; // 初始状态
    public static final int STATE_LISTEN = 0x1; // 正在监听
    public static final int STATE_CONNECTING = 0x2; // 正在连接
    public static final int STATE_CONNECTED = 0x3; // 已连接

    //handler message类型
    // 表示当前连接状态的常量
    public static final int MESSAGE_TOAST_ERROR = 0x0; // 错误
    public static final int MESSAGE_TOAST_STATE_CHANGE = 0x1; // 状态改变
    public static final int MESSAGE_TOAST_SEND = 0x2; // 发送消息
    public static final int MESSAGE_RECEIVE = 0x3; // 收到消息

    private SecurityType mSecurityType = SecurityType.SECURE;
    private ServerOrClient mServerOrClient = ServerOrClient.SERVER;
    private BluetoothDevice mBluetoothDevice;// 如果是客户端，客户端连接的服务器设备对象保存在这儿

    public enum SecurityType {
        SECURE("SECURE"), // 配对后的连接(安全)
        INSECURE("INSECURE");// 未配对的连接(不安全)
        private final String value;

        SecurityType(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    public enum ServerOrClient {
        SERVER, // 服务器的监听连接
        CLIENT// 客户端的监听连接
    }

    /**
     * 初始化及启动蓝牙socket
     *
     * @param handler         UI消息传递对象
     * @param securityType    连接的安全模式
     * @param serverOrClient  客户端或服务端
     * @param bluetoothDevice 服务器端设备
     */
    public BluetoothChatService(Handler handler, SecurityType securityType, ServerOrClient serverOrClient,
                                BluetoothDevice bluetoothDevice) {
        if (securityType != null)
            this.mSecurityType = securityType;
        if (serverOrClient != null)
            this.mServerOrClient = serverOrClient;
        if (bluetoothDevice != null)
            this.mBluetoothDevice = bluetoothDevice;
        mAdapter = BluetoothAdapter.getDefaultAdapter();
        mHandler = handler;
        start();
    }

    /**
     * 多线程同步修改状态标识
     *
     * @param state
     */
    private synchronized void setState(int state) {
        mState = state;
        mHandler.obtainMessage(MESSAGE_TOAST_STATE_CHANGE, state, -1, null).sendToTarget();
    }

    /**
     * 多线程同步读取状态标识
     */
    public synchronized int getState() {
        return mState;
    }

    /**
     * 启动服务
     */
    public void start() {
        start(null, null, null);
    }

    /**
     * 启动服务
     *
     * @param securityType    连接的安全模式
     * @param serverOrClient  客户端或服务端
     * @param bluetoothDevice 服务器端设备
     */
    public void start(SecurityType securityType, ServerOrClient serverOrClient, BluetoothDevice bluetoothDevice) {
        if (securityType != null)
            this.mSecurityType = securityType;
        if (this.mSecurityType == null) {
            LogUtils.e(TAG, "mSecurityType cannot be null");
            mHandler.obtainMessage(MESSAGE_TOAST_ERROR, -1, -1, "mSecurityType cannot be null").sendToTarget();
            return;
        }
        if (serverOrClient != null)
            this.mServerOrClient = serverOrClient;
        if (this.mServerOrClient == null) {
            LogUtils.e(TAG, "mServerOrClient cannot be null");
            mHandler.obtainMessage(MESSAGE_TOAST_ERROR, -1, -1, "mServerOrClient cannot be null").sendToTarget();
            return;
        }
        if (bluetoothDevice != null)
            this.mBluetoothDevice = bluetoothDevice;
        if (this.mBluetoothDevice == null) {
            LogUtils.e(TAG, "mBluetoothDevice cannot be null");
            mHandler.obtainMessage(MESSAGE_TOAST_ERROR, -1, -1, "mBluetoothDevice cannot be null").sendToTarget();
            return;
        }
        if (mState == STATE_NONE) {
            stop();
            if (this.mServerOrClient == ServerOrClient.SERVER) {
                if (mServerConnectThread == null) {
                    mServerConnectThread = new ServerConnectThread(this.mSecurityType);
                    mServerConnectThread.start();
                }
            } else if (this.mServerOrClient == ServerOrClient.CLIENT) {
                if (mClientConnectThread == null) {
                    mClientConnectThread = new ClientConnectThread(this.mSecurityType);
                    mClientConnectThread.start();
                }
            }
            setState(STATE_LISTEN);
        }
    }

    /**
     * 停止服务
     */
    public synchronized void stop() {
        try {
            if (mReadWriteThread != null) {
                mReadWriteThread.cancel();
                mReadWriteThread = null;
            }
            if (mServerConnectThread != null) {
                mServerConnectThread.cancel();
                mServerConnectThread = null;
            }
            if (mClientConnectThread != null) {
                mClientConnectThread.cancel();
                mClientConnectThread = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            LogUtils.e(TAG, "BluetoothChatService -> stop() -> :failed " + e.getMessage());
            mHandler.obtainMessage(MESSAGE_TOAST_ERROR, -1, -1, "BluetoothChatService -> stop() -> :failed").sendToTarget();
            mReadWriteThread = null;
            mServerConnectThread = null;
            mClientConnectThread = null;
        } finally {
            setState(STATE_NONE);
            System.gc();
        }
    }

    /**
     * 发送消息
     *
     * @param out 数据参数
     */
    public void write(String out) {
        if (TextUtils.isEmpty(out)) {
            LogUtils.e(TAG, "please write something now");
            mHandler.obtainMessage(MESSAGE_TOAST_ERROR, -1, -1, "BluetoothChatService -> write() -> :failed").sendToTarget();
            return;
        }
        ReadWriteThread r;
        synchronized (this) {
            if (mState != STATE_CONNECTED)
                return;
            r = mReadWriteThread;
        }
        r.write(out);
    }

    /**
     * 服务器端连接线程
     */
    @SuppressLint("NewApi")
    private class ServerConnectThread extends Thread {
        private BluetoothServerSocket mmServerSocket;
        private BluetoothSocket mmSocket = null;

        public ServerConnectThread(SecurityType securityType) {
            setName("ServerConnectionThread:" + securityType.getValue());
            BluetoothServerSocket tmp = null;
            try {
                if (securityType == SecurityType.SECURE) {
                    tmp = mAdapter.listenUsingRfcommWithServiceRecord(SecurityType.SECURE.getValue(), MY_UUID_SECURE);
                } else if (securityType == SecurityType.INSECURE) {
                    tmp = mAdapter.listenUsingInsecureRfcommWithServiceRecord(SecurityType.INSECURE.getValue(),
                            MY_UUID_INSECURE);
                }
                if (tmp != null)
                    mmServerSocket = tmp;
            } catch (IOException e) {
                e.printStackTrace();
               LogUtils.e(TAG, "ServerConnectThread -> ServerConnectThread() -> :failed " + e.getMessage());
                mHandler.obtainMessage(MESSAGE_TOAST_ERROR, -1, -1, "ServerConnectThread -> ServerConnectThread() -> :failed").sendToTarget();
                mmServerSocket = null;
                BluetoothChatService.this.stop();
            }
        }

        public void run() {
            try {
                // 正在连接
                setState(STATE_CONNECTING);
                //accept() 阻塞式的方法，群聊时，需要循环accept接收客户端
                mmSocket = mmServerSocket.accept();
                connected(mmSocket);
            } catch (Exception e) {
                e.printStackTrace();
                LogUtils.e(TAG, "ServerConnectThread -> run() -> :failed " + e.getMessage());
                mHandler.obtainMessage(MESSAGE_TOAST_ERROR, -1, -1, "ServerConnectThread -> run() -> :failed").sendToTarget();
                BluetoothChatService.this.stop();
            }
        }

        public void cancel() {
            try {
                if (mmSocket != null) {
                    mmSocket.close();
                    mmSocket = null;
                }
                if (mmServerSocket != null) {
                    mmServerSocket.close();
                    mmServerSocket = null;
                }
            } catch (IOException e) {
                e.printStackTrace();
                LogUtils.e(TAG, "ServerConnectThread -> cancel() -> :failed " + e.getMessage());
                mHandler.obtainMessage(MESSAGE_TOAST_ERROR, -1, -1, "ServerConnectThread -> cancel() -> :failed").sendToTarget();
                mmSocket = null;
                mmServerSocket = null;
                BluetoothChatService.this.stop();
            }
        }
    }

    // 客户端连接线程
    private class ClientConnectThread extends Thread {
        private BluetoothSocket mmSocket;

        public ClientConnectThread(SecurityType securityType) {
            setName("ClientConnectThread:" + securityType.getValue());
            BluetoothSocket tmp = null;
            try {
                if (securityType == SecurityType.SECURE) {
                    tmp = mBluetoothDevice.createRfcommSocketToServiceRecord(MY_UUID_SECURE);
                    //Method m = mBluetoothDevice.getClass().getMethod("createRfcommSocket", int.class);
                    //tmp = (BluetoothSocket) m.invoke(mBluetoothDevice, 1);
                } else if (securityType == SecurityType.INSECURE) {
                    tmp = mBluetoothDevice.createInsecureRfcommSocketToServiceRecord(MY_UUID_INSECURE);
                    //Method m = mBluetoothDevice.getClass().getMethod("createRfcommSocket", int.class);
                    //tmp = (BluetoothSocket) m.invoke(mBluetoothDevice, 1);
                }
                if (tmp != null)
                    mmSocket = tmp;
            } catch (Exception e) {
                e.printStackTrace();
                LogUtils.e(TAG, "ClientConnectThread -> ClientConnectThread() -> :failed " + e.getMessage());
                mHandler.obtainMessage(MESSAGE_TOAST_ERROR, -1, -1, "ClientConnectThread -> ClientConnectThread() -> :failed").sendToTarget();
                mmSocket = null;
                BluetoothChatService.this.stop();
            }
        }

        public void run() {
            try {
                setState(STATE_CONNECTING);
                mmSocket.connect();
                connected(mmSocket);
            } catch (IOException e) {
                e.printStackTrace();
                LogUtils.e(TAG, "ClientConnectThread -> run() -> :failed " + e.getMessage());
                mHandler.obtainMessage(MESSAGE_TOAST_ERROR, -1, -1, "ClientConnectThread -> run() -> :failed").sendToTarget();
                BluetoothChatService.this.stop();
            }
        }

        public void cancel() {
            try {
                if (mmSocket != null && mmSocket.isConnected()) {
                    mmSocket.close();
                }
                mmSocket = null;
            } catch (IOException e) {
                e.printStackTrace();
                LogUtils.e(TAG, "ClientConnectThread -> cancel() -> :failed " + e.getMessage());
                mHandler.obtainMessage(MESSAGE_TOAST_ERROR, -1, -1, "ClientConnectThread -> cancel() -> :failed").sendToTarget();
                mmSocket = null;
                BluetoothChatService.this.stop();
            }
        }
    }

    /**
     * 以获取socket，建立数据流线程
     *
     * @param socket
     */
    private synchronized void connected(BluetoothSocket socket) {
        if (mReadWriteThread != null) {
            mReadWriteThread.cancel();
            mReadWriteThread = null;
        }
        mReadWriteThread = new ReadWriteThread(socket);
        mReadWriteThread.start();
    }

    /**
     * 连接成功线程，可进行读写操作
     */
    private class ReadWriteThread extends Thread {
        private BluetoothSocket mmSocket;
        private DataInputStream mmInStream;
        private DataOutputStream mmOutStream;
        private boolean isRunning = true;

        public ReadWriteThread(BluetoothSocket socket) {
            mmSocket = socket;
            try {
                mmInStream = new DataInputStream(mmSocket.getInputStream());
                mmOutStream = new DataOutputStream(mmSocket.getOutputStream());
                // 连接建立成功
                setState(STATE_CONNECTED);
            } catch (IOException e) {
                e.printStackTrace();
                LogUtils.e(TAG, "ReadWriteThread -> ReadWriteThread() -> :failed " + e.getMessage());
                mHandler.obtainMessage(MESSAGE_TOAST_ERROR, -1, -1, "ReadWriteThread -> ReadWriteThread() -> :failed").sendToTarget();
                mmOutStream = null;
                mmInStream = null;
                BluetoothChatService.this.stop();
            }
        }

        public void run() {
            byte[] buffer = new byte[1024];
            int len;
            while (isRunning) {
                try {
                    //readUTF()，read(buffer) 都是阻塞式的方法
                    //如果这儿用readUTF，那么写的地方得用writeUTF。对应
                    String receive_str = mmInStream.readUTF();
                    if (!TextUtils.isEmpty(receive_str))
                        mHandler.obtainMessage(MESSAGE_RECEIVE, -1, -1, receive_str).sendToTarget();
//                    len = mmInStream.read(buffer);
//                    if(len > 0){
//                        String receive_str = new String(buffer,0,len);
//                        if (!TextUtils.isEmpty(receive_str))
//                            mHandler.obtainMessage(MESSAGE_RECEIVE, -1, -1, receive_str).sendToTarget();
//                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    LogUtils.e(TAG, "ReadWriteThread -> run() -> :failed " + e.getMessage());
                    mHandler.obtainMessage(MESSAGE_TOAST_ERROR, -1, -1, "ReadWriteThread -> run() -> :failed").sendToTarget();
                    BluetoothChatService.this.stop();
                }
            }
        }

        public void write(String str) {
            try {
                mmOutStream.writeUTF(str);
                mmOutStream.flush();
                mHandler.obtainMessage(MESSAGE_TOAST_SEND, -1, -1, str).sendToTarget();
            } catch (IOException e) {
                e.printStackTrace();
                    LogUtils.e(TAG, "ReadWriteThread -> write() -> :failed " + e.getMessage());
                mHandler.obtainMessage(MESSAGE_TOAST_ERROR, -1, -1, "ReadWriteThread -> write() -> :failed").sendToTarget();
                BluetoothChatService.this.stop();
            }
        }

        public void cancel() {
            try {
                isRunning = false;
                if (mmInStream != null) {
                    mmInStream.close();
                    mmInStream = null;
                }
                if (mmOutStream != null) {
                    mmOutStream.close();
                    mmOutStream = null;
                }
            } catch (IOException e) {
                e.printStackTrace();
                    LogUtils.e(TAG, "ReadWriteThread -> cancel() -> :failed " + e.getMessage());
                mHandler.obtainMessage(MESSAGE_TOAST_ERROR, -1, -1, "ReadWriteThread -> cancel() -> :failed").sendToTarget();
                mmInStream = null;
                mmOutStream = null;
                BluetoothChatService.this.stop();
            }
        }
    }
}

