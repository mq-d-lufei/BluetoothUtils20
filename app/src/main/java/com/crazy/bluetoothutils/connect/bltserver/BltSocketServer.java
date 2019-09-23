package com.crazy.bluetoothutils.connect.bltserver;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.WorkerThread;

import com.crazy.bluetoothutils.BltManager;
import com.crazy.bluetoothutils.connect.BltConnectRuleConfig;
import com.crazy.bluetoothutils.connect.bltclient.BltServerSocketClient;
import com.crazy.bluetoothutils.connect.bltclient.BltSocketClient;
import com.crazy.bluetoothutils.connect.delegate.BltSocketClientDelegate;
import com.crazy.bluetoothutils.connect.delegate.BltSocketServerDelegate;
import com.crazy.bluetoothutils.connect.packet.BltSocketResponsePacket;
import com.crazy.bluetoothutils.data.BltDevice;
import com.crazy.bluetoothutils.data.BltSeruceType;
import com.crazy.bluetoothutils.data.BluetoothDeviceType;
import com.crazy.bluetoothutils.exception.BltException;
import com.crazy.bluetoothutils.utils.BltLog;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;


/**
 * 服务端
 */

public class BltSocketServer implements BltSocketClientDelegate {

    public static final String TAG = "BltSocketServer";
    final BltSocketServer self = this;

    public BltSocketServer(BltConnectRuleConfig bltConnectRuleConfig) {
        this.secureType = bltConnectRuleConfig.getSecureType();
        this.bltConnectRuleConfig = bltConnectRuleConfig;
    }

    /**
     * 加密类型
     */
    @BltSeruceType.SecureType
    public String secureType;

    public String getSecureType() {
        return secureType;
    }

    public void setSecureType(@BltSeruceType.SecureType String secureType) {
        this.secureType = secureType;
    }

    private BltConnectRuleConfig bltConnectRuleConfig;

    public BltConnectRuleConfig getBltConnectRuleConfig() {
        return bltConnectRuleConfig;
    }

    public void setBltConnectRuleConfig(BltConnectRuleConfig bltConnectRuleConfig) {
        this.bltConnectRuleConfig = bltConnectRuleConfig;
    }

    /**
     * 监听状态
     */
    private boolean listening;

    protected BltSocketServer setListening(boolean listening) {
        this.listening = listening;
        return this;
    }

    public boolean isListening() {
        return this.listening;
    }

    private ListenThread listenThread;

    protected BltSocketServer setListenThread(ListenThread listenThread) {
        this.listenThread = listenThread;
        return this;
    }

    public ListenThread getListenThread() {
        if (this.listenThread == null) {
            this.listenThread = new ListenThread();
        }
        return this.listenThread;
    }

    /**
     * 开始监听
     */
    public boolean beginListen() {
        if (isListening()) {
            BltLog.e("isListenSuccess： " + false);
            return false;
        }

        if (getRunningBltServerSocket() == null) {
            BltLog.e("isListenSuccess： " + false);
            return false;
        }

        setListening(true);
        __i__onSocketServerBeginListen();

        BltLog.e("isListenSuccess： " + true);
        return true;
    }

    /**
     * 断开监听
     */
    public void stopListen() {
        stopListen(true);
    }

    /**
     * 断开监听
     *
     * @param isDisconnectAllServerClients 是否断开所有客户端
     */
    public void stopListen(boolean isDisconnectAllServerClients) {
        BltLog.e(TAG, "stopListen： " + isListening());
        if (isListening()) {
            BltLog.e(TAG, "stopListen： " + isListening());

            getListenThread().setDisconnectAllServerClients(isDisconnectAllServerClients);
            getListenThread().interrupt();
            try {
                getRunningBltServerSocket().close();
                BltLog.e(TAG, "stopListen： close success");
            } catch (IOException e) {
                e.printStackTrace();
                BltLog.e(TAG, "stopListen： close failure");
            }
        }
    }

    /**
     * 断开某个客户端
     */
    public void disconnectClient(final BltServerSocketClient client) {
        __i__disconnectClient(client);
    }


    /**
     * 注册监听回调
     *
     * @param delegate 回调接收者
     */
    public BltSocketServer registerBltSocketServerDelegate(BltSocketServerDelegate delegate) {
        if (!getBltSocketServerDelegates().contains(delegate)) {
            getBltSocketServerDelegates().add(delegate);
        }
        return this;
    }

    /**
     * 取消注册监听回调
     *
     * @param delegate 回调接收者
     */
    public BltSocketServer removeSocketServerDelegate(BltSocketServerDelegate delegate) {
        getBltSocketServerDelegates().remove(delegate);
        return this;
    }

    /**
     * BluetoothServerSocket
     */
    private BluetoothServerSocket runningBltServerSocket;

    protected BltSocketServer setRunningBltServerSocket(BluetoothServerSocket runningBltServerSocket) {
        this.runningBltServerSocket = runningBltServerSocket;
        return this;
    }

    protected BluetoothServerSocket getRunningBltServerSocket() {
        if (this.runningBltServerSocket == null) {
            try {
                if (secureType.equals(BltSeruceType.SecureType.SECURE)) {
                    runningBltServerSocket = BltManager.getInstance().getBluetoothAdapter().listenUsingRfcommWithServiceRecord(bltConnectRuleConfig.getName(), bltConnectRuleConfig.getUuid());
                } else if (secureType.equals(BltSeruceType.SecureType.INSECURE)) {
                    runningBltServerSocket = BltManager.getInstance().getBluetoothAdapter().listenUsingInsecureRfcommWithServiceRecord(bltConnectRuleConfig.getName(), bltConnectRuleConfig.getUuid());
                }
            } catch (IOException e) {
                BltLog.e(TAG, "Socket Type: " + secureType + "listen() failed, " + e);
            }
        }
        return this.runningBltServerSocket;
    }

    @Override
    public void onConnected(BltSocketClient client) {

    }

    @Override
    public void onConnectFail(BltSocketClient client, BltException bltException) {

    }


    @Override
    public void onDisconnected(BltSocketClient client, boolean isReconnect, BltException bltException) {
        BltManager.getInstance().getMultipleBluetoothController().removeBltSocket(client);
        // getRunningBltServerSocketClients().remove((BltServerSocketClient) client);
        __i__onSocketServerClientDisconnected((BltServerSocketClient) client);
    }

    @Override
    public void onResponse(BltSocketClient client, @NonNull BltSocketResponsePacket responsePacket, boolean isCallbackInMainThread) {

    }

   /* private ArrayList<BltServerSocketClient> runningBltServerSocketClients;

    public ArrayList<BltServerSocketClient> getRunningBltServerSocketClients() {

        if (this.runningBltServerSocketClients == null) {
            this.runningBltServerSocketClients = new ArrayList<BltServerSocketClient>();
        }
        return this.runningBltServerSocketClients;
    }*/

    private ArrayList<BltSocketServerDelegate> socketServerDelegates;

    public ArrayList<BltSocketServerDelegate> getBltSocketServerDelegates() {
        if (this.socketServerDelegates == null) {
            this.socketServerDelegates = new ArrayList<BltSocketServerDelegate>();
        }
        return this.socketServerDelegates;
    }

    private UIHandler uiHandler;

    private UIHandler getUiHandler() {
        if (this.uiHandler == null) {
            this.uiHandler = new UIHandler(this);
        }
        return this.uiHandler;
    }

    private static class UIHandler extends Handler {
        private WeakReference<BltSocketServer> referenceSocketServer;

        private UIHandler(@NonNull BltSocketServer referenceSocketServer) {
            super(Looper.getMainLooper());

            this.referenceSocketServer = new WeakReference<BltSocketServer>(referenceSocketServer);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
        }
    }


    /* Protected Methods */
    @WorkerThread
    protected BltServerSocketClient internalGetSocketServerClient(BltDevice mBltDevice, BltConnectRuleConfig bltConnectRuleConfig) {
        return new BltServerSocketClient(mBltDevice, bltConnectRuleConfig);
    }

    /* Private Methods */
    private boolean __i__checkServerSocketAvailable() {
        return getRunningBltServerSocket() != null;
    }

    private void __i__disconnectClient(final BltServerSocketClient client) {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            getUiHandler().post(new Runnable() {
                @Override
                public void run() {
                    self.__i__disconnectClient(client);
                }
            });
            return;
        }

        BltManager.getInstance().getMultipleBluetoothController().disconnect(client.getBltDevice(), false);
    }

    private void __i__disconnectAllClients() {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            getUiHandler().post(new Runnable() {
                @Override
                public void run() {
                    self.__i__disconnectAllClients();
                }
            });
            return;
        }

        BltLog.e(TAG, "__i__disconnectAllClients");

        BltManager.getInstance().getMultipleBluetoothController().disconnectAllServerDevice();
    }

    private void __i__onSocketServerBeginListen() {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            getUiHandler().post(new Runnable() {
                @Override
                public void run() {
                    self.__i__onSocketServerBeginListen();
                }
            });
            return;
        }

        ArrayList<BltSocketServerDelegate> copyList =
                (ArrayList<BltSocketServerDelegate>) getBltSocketServerDelegates().clone();
        int count = copyList.size();
        for (int i = 0; i < count; ++i) {
            copyList.get(i).onServerBeginListen(this);
        }

        if (!getListenThread().isAlive()) {
            getListenThread().start();
        }

    }

    private void __i__onSocketServerStopListen(final boolean isDisconnectAllServerClients) {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            getUiHandler().post(new Runnable() {
                @Override
                public void run() {
                    self.__i__onSocketServerStopListen(isDisconnectAllServerClients);
                }
            });
            return;
        }

        ArrayList<BltSocketServerDelegate> copyList =
                (ArrayList<BltSocketServerDelegate>) getBltSocketServerDelegates().clone();
        int count = copyList.size();
        for (int i = 0; i < count; ++i) {
            copyList.get(i).onServerStopListen(this, isDisconnectAllServerClients);
        }
    }

    private void __i__onSocketServerClientConnected(final BltServerSocketClient socketServerClient) {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            getUiHandler().post(new Runnable() {
                @Override
                public void run() {
                    self.__i__onSocketServerClientConnected(socketServerClient);
                }
            });
            return;
        }

        ArrayList<BltSocketServerDelegate> copyList =
                (ArrayList<BltSocketServerDelegate>) getBltSocketServerDelegates().clone();
        int count = copyList.size();
        for (int i = 0; i < count; ++i) {
            copyList.get(i).onClientConnected(this, socketServerClient);
        }
    }

    private void __i__onSocketServerClientDisconnected(final BltServerSocketClient socketServerClient) {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            getUiHandler().post(new Runnable() {
                @Override
                public void run() {
                    self.__i__onSocketServerClientDisconnected(socketServerClient);
                }
            });
            return;
        }

        ArrayList<BltSocketServerDelegate> copyList =
                (ArrayList<BltSocketServerDelegate>) getBltSocketServerDelegates().clone();
        int count = copyList.size();
        for (int i = 0; i < count; ++i) {
            copyList.get(i).onClientDisconnected(this, socketServerClient);
        }
    }

    /* Inner Classes */
    public class ListenThread extends Thread {

        private boolean running;
        private boolean isDisconnectAllServerClients = true;

        protected ListenThread setRunning(boolean running) {
            this.running = running;
            return this;
        }

        protected boolean isRunning() {
            return this.running;
        }

        public ListenThread setDisconnectAllServerClients(boolean disconnectAllServerClients) {
            isDisconnectAllServerClients = disconnectAllServerClients;
            return this;
        }

        public boolean isDisconnectAllServerClients() {
            return isDisconnectAllServerClients;
        }

        @Override
        public void run() {
            super.run();

            BltLog.e(TAG, "ListenThread...run");

            setRunning(true);

            while (!Thread.interrupted()
                    && self.__i__checkServerSocketAvailable()) {

                BluetoothSocket socket = null;
                BluetoothDevice device = null;
                BltDevice bltDevice = null;

                try {

                    if (Thread.interrupted()) {
                        break;
                    }

                    socket = self.getRunningBltServerSocket().accept();

                    BltLog.e(TAG, "Thread.interrupted() 2 ");
                    if (Thread.interrupted()) {
                        break;
                    }
                    BltLog.e(TAG, "Thread.interrupted() 3 ");

                    device = socket.getRemoteDevice();
                    bltDevice = new BltDevice(device, socket);


                    /**
                     * 判断指定类型的设备才可以连接
                     */
                    List<BluetoothDeviceType> bqfjBluetoothDeviceList = BltManager.getInstance().getBluetoothDeviceTypeList();
                    if (null != bqfjBluetoothDeviceList && bqfjBluetoothDeviceList.size() > 0) {
                        AtomicBoolean equal = new AtomicBoolean(false);
                        for (BluetoothDeviceType judegDevice : bqfjBluetoothDeviceList) {
                            if (judegDevice.equals(BltManager.getInstance().getBluetoothDeviceType(device.getBluetoothClass()))) {
                                equal.set(true);
                                break;
                            }
                        }
                        if (!equal.get()) {
                            socket.close();
                            continue;
                        }
                    }

                    BltServerSocketClient socketServerClient = self.internalGetSocketServerClient(bltDevice, getBltConnectRuleConfig());
                    BltManager.getInstance().getMultipleBluetoothController().addBltSocketClient(socketServerClient);

                    socketServerClient.registerBltSocketClientDelegate(self);
                    self.__i__onSocketServerClientConnected(socketServerClient);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            setRunning(false);

            self.setListening(false);
            self.setListenThread(null);
            self.setRunningBltServerSocket(null);

            if (isDisconnectAllServerClients) {
                self.__i__disconnectAllClients();
            }
            self.__i__onSocketServerStopListen(isDisconnectAllServerClients);

            BltLog.e(TAG, "stop end ...  ");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BltSocketServer that = (BltSocketServer) o;

        return bltConnectRuleConfig.equals(that.bltConnectRuleConfig);
    }

    @Override
    public int hashCode() {
        return bltConnectRuleConfig.hashCode();
    }
}
