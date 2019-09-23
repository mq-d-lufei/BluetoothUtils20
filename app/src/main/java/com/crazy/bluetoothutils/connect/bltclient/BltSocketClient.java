package com.crazy.bluetoothutils.connect.bltclient;

import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;

import com.crazy.bluetoothutils.BltManager;
import com.crazy.bluetoothutils.connect.BltConnectRuleConfig;
import com.crazy.bluetoothutils.connect.delegate.BltSocketClientDelegate;
import com.crazy.bluetoothutils.connect.delegate.BltSocketClientReceivingDelegate;
import com.crazy.bluetoothutils.connect.delegate.BltSocketClientSendingDelegate;
import com.crazy.bluetoothutils.connect.packet.BltSocketResponsePacket;
import com.crazy.bluetoothutils.connect.packet.BltSocketSendPacket;
import com.crazy.bluetoothutils.connect.reader.BltSocketInputReader;
import com.crazy.bluetoothutils.data.BltClientSource;
import com.crazy.bluetoothutils.data.BltDevice;
import com.crazy.bluetoothutils.data.BltSeruceType;
import com.crazy.bluetoothutils.data.BluetoothDeviceType;
import com.crazy.bluetoothutils.exception.BltConnectException;
import com.crazy.bluetoothutils.exception.BltException;
import com.crazy.bluetoothutils.exception.BltReceiveException;
import com.crazy.bluetoothutils.exception.BltSendException;
import com.crazy.bluetoothutils.exception.BltSocketException;
import com.crazy.bluetoothutils.exception.OtherException;
import com.crazy.bluetoothutils.utils.BltLog;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by feaoes on 2018/6/29.
 */

public class BltSocketClient {

    public final static String TAG = "BltSocketClient";

    public BltSocketClient self = this;

    /**
     * 蓝牙设备，根据对应的蓝牙设备创建Socket连接
     */
    private BltDevice mBltDevice;

    private BltConnectRuleConfig mBltConnectRuleConfig;

    public BltSocketClient(BltDevice mBltDevice, BltConnectRuleConfig bltConnectRuleConfig) {
        this.mBltDevice = mBltDevice;
        this.mBltConnectRuleConfig = bltConnectRuleConfig;
        this.secureType = bltConnectRuleConfig.getSecureType();
    }

    public void setBltDevice(BltDevice mBltDevice) {
        this.mBltDevice = mBltDevice;
    }

    public BltDevice getBltDevice() {
        return mBltDevice;
    }

    public BltConnectRuleConfig getBltConnectRuleConfig() {
        return mBltConnectRuleConfig;
    }

    public String getDeviceKey() {
        if (getBltDevice() != null)
            return getBltDevice().getKey();
        return "";
    }

    /**
     * 加密类型
     */
    @BltSeruceType.SecureType
    public String secureType;

    public String getSecureType() {
        return secureType;
    }

    /**
     * 客户端来源
     */
    @BltClientSource.ClientSource
    public String clientSource;

    public String getClientSource() {
        if (null == clientSource) {
            clientSource = BltClientSource.ClientSource.CLIENT;
        }
        return clientSource;
    }

    public void setClientSource(@BltClientSource.ClientSource String clientSource) {
        this.clientSource = clientSource;
    }


    public String clientConnectedTime;

    public String getClientConnectedTime() {
        return clientConnectedTime;
    }

    public void setClientConnectedTime(String clientConnectedTime) {
        this.clientConnectedTime = clientConnectedTime;
    }

    /**
     * 主线程Handler
     */
    private UIHandler uiHandler;

    protected UIHandler getUiHandler() {
        if (this.uiHandler == null) {
            this.uiHandler = new UIHandler(this);
        }
        return this.uiHandler;
    }

    private static class UIHandler extends Handler {

        private WeakReference<BltSocketClient> referenceSocketClient;

        public UIHandler(@NonNull BltSocketClient referenceSocketClient) {
            super(Looper.getMainLooper());

            this.referenceSocketClient = new WeakReference<BltSocketClient>(referenceSocketClient);
        }

        @Override
        public void handleMessage(Message msg) {
            BltSocketClient socketClient = referenceSocketClient.get();
            if (null == socketClient) {
                return;
            }
            super.handleMessage(msg);
        }
    }

    /**
     * 返回null为正常加入发送队列，返回BltSocketSendPacket为发送失败
     */
    public synchronized BltSocketSendPacket sendPacket(final BltSocketSendPacket packet) {
        //TODO 检测本地连接状态，并检测Socket,inputStream状态
        if (Looper.myLooper() != Looper.getMainLooper()) {
            getUiHandler().post(new Runnable() {
                @Override
                public void run() {
                    self.sendPacket(packet);
                }
            });
            return null;
        }

        if (packet == null) {
            throw new IllegalArgumentException("In sendPacket, SocketSendPacket cannot be empty");
        }

        //连接状态： Disconnected -> Connecting -> Connected -> disconnecting

        //如果是未连接状态，并且当前状态是已经完全断开连接，则重新连接
        if (!isConnected() && isDisconnected()) {
            BltLog.e(TAG, ",!isConnected() && isDisconnected()");
            connect();
            self.__i__enqueueNewPacket(packet);
            return null;
        }
        //已连接    检查发送线程，如果未处于活跃状态，则开启，并将数据包加入发送队列, return null;
        if (isConnected() && !getSendThread().isAlive()) {
            BltLog.e(TAG, ",isConnected() && !getSendThread().isAlive()");
            getSendThread().start();
        }
        //已连接    检查接收线程，如果未处于活跃状态，则开启，并将数据包加入发送队列, return null;
        if (isConnected() && !getReceiveThread().isAlive()) {
            BltLog.e(TAG, ",isConnected() && !getReceiveThread2().isAlive()");
            getReceiveThread().start();
        }
        //正在连接  可以将数据包加入发送队列，等待连接成功后发送,如果连接失败，则会断开连接，并将发送队列数据包返回给发送者
        else if (isConnecting()) {
        }
        //正在断开  说明此时不可以发送数据包，则不加入发送队列，直接 return  packet
        else if (isDisconnecting()) {
            return packet;
        }

        //TODO 本地连接状态正常，但远程连接状态未知，所以可发送检测包检测，检测成功后才发送数据包

        self.__i__enqueueNewPacket(packet);

        return null;
    }

    /**
     * 在子线程中,按顺序将发送包加入发送队列
     */
    private void __i__enqueueNewPacket(final BltSocketSendPacket packet) {

        getPacketEnqueueHandler().post(new Runnable() {
            @Override
            public void run() {
                try {
                    getSendingPacketQueue().put(packet);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * 数据包加入消息队列子线程
     */
    private Handler packetEnqueueHandler;

    private HandlerThread handlerThread;

    private Handler getPacketEnqueueHandler() {

        if (this.packetEnqueueHandler == null) {
            if (this.handlerThread == null) {
                handlerThread = new HandlerThread("packet_enqueue_thread");
            }
            if (!this.handlerThread.isAlive()) {
                handlerThread.start();
            }
            this.packetEnqueueHandler = new Handler(handlerThread.getLooper());
        }
        return this.packetEnqueueHandler;
    }

    /**
     * **************************************************************
     * *********************当前Socket连接状态************************
     * **************************************************************
     */
    public enum State {
        Disconnected, Connecting, Connected;
    }

    public State state;

    public State getState() {
        if (this.state == null) {
            return State.Disconnected;
        }
        return this.state;
    }

    public void setState(State state) {
        this.state = state;
    }

    /**
     * 是否未连接,已连接，连接中
     *
     * @return
     */
    public boolean isDisconnected() {
        return getState() == State.Disconnected;
    }

    public boolean isConnected() {
        return getState() == State.Connected;
    }

    public boolean isConnecting() {
        return getState() == State.Connecting;
    }

    /**
     * 正在断开连接状态
     */
    private boolean disconnecting;

    protected BltSocketClient setDisconnecting(boolean disconnecting) {
        this.disconnecting = disconnecting;
        return this;
    }

    public boolean isDisconnecting() {
        return this.disconnecting;
    }

    /**
     * BluetoothSocket对象
     */
    private BluetoothSocket runningBltSocket;

    public BluetoothSocket getRunningBltSocket() {
        if (null == runningBltSocket) {
            if (getClientSource().equals(BltClientSource.ClientSource.CLIENT)) {
                try {
                    if (secureType.equals(BltSeruceType.SecureType.SECURE)) {
                        runningBltSocket = mBltDevice.getDevice().createRfcommSocketToServiceRecord(getBltConnectRuleConfig().getUuid());

                       /* Method method = mBltDevice.getClass().getMethod("createRfcommSocket", new Class[]{int.class});
                        runningBltSocket = (BluetoothSocket) method.invoke(mBltDevice, 1);*/

                    } else if (secureType.equals(BltSeruceType.SecureType.INSECURE)) {
                        runningBltSocket = mBltDevice.getDevice().createInsecureRfcommSocketToServiceRecord(getBltConnectRuleConfig().getUuid());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    BltLog.e(TAG, "Socket Type: " + secureType + "create() failed, " + e);
                }/* catch (NoSuchMethodException e) {
                    e.printStackTrace();
                    BltLog.e(TAG, "Socket Type: " + secureType + "create() failed", e);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }*/
            }
        }
        return runningBltSocket;
    }

    public void setRunningBltSocket(BluetoothSocket runningBltSocket) {
        this.runningBltSocket = runningBltSocket;
    }

    /**
     * **************************************************************
     * **************************连接Socket**************************
     * **************************************************************
     */
    public void connect() {

        //1、判断Socket是否已经断开连接，已经是未连接状态才去连接
        if (!isDisconnected()) {
            self.__i__onConnectFailure(new BltConnectException("Socket是尚未断开连接"));
            return;
        }
        //2、检查蓝牙设备信息
        if (null == mBltDevice.getDevice()) {
            throw new IllegalArgumentException("we need a BluetoothDevice to connect !!!");
        }
        if (null == secureType) {
            throw new IllegalArgumentException("we need a SecureType to connect !!!");
        }
        //3、设置当前Socket状态
        setState(State.Connecting);
        //4、开启连接线程
        if (!getConnectionThread().isAlive()) {
            getConnectionThread().start();
        }
    }


    /**
     * 未连接
     */
    public void disconnect(boolean isReconnect, BltException bltException) {
        if (isDisconnected() || isDisconnecting()) {
            return;
        }
        //正在断开连接
        setDisconnecting(true);

        getDisconnectionThread(isReconnect, bltException).start();
    }

    /**
     * **************************************************************
     * **************************连接线程****************************
     * **************************************************************
     */
    private ConnectionThread connectionThread;

    public ConnectionThread getConnectionThread() {
        if (null == connectionThread) {
            connectionThread = new ConnectionThread();
        }
        return connectionThread;
    }

    public void setConnectionThread(ConnectionThread connectionThread) {
        this.connectionThread = connectionThread;
    }

    private class ConnectionThread extends Thread {

        @Override
        public void run() {
            super.run();


            try {
                if (Thread.interrupted()) {
                    return;
                }

                BltLog.e(TAG, "connect....startTime: " + System.currentTimeMillis());

                if (null == getRunningBltSocket()) {
                    BltLog.e(TAG, "error : getRunningBltSocket is null ");
                    disconnect(false, new BltSocketException("Socket Type: " + secureType + "create() failed"));
                    return;
                }

                getRunningBltSocket().connect();

                if (Thread.interrupted()) {
                    return;
                }

                BluetoothDevice remoteDevice = getRunningBltSocket().getRemoteDevice();
                BluetoothClass bluetoothClass = remoteDevice.getBluetoothClass();

               BluetoothDeviceType deviceType = BltManager.getInstance().getBluetoothDeviceType(bluetoothClass);

                BltLog.d(TAG, "deviceType123 name: " + remoteDevice.getName() + " -getDeviceClass： " + bluetoothClass.getDeviceClass() + " -getMajorDeviceClass: " + bluetoothClass.getMajorDeviceClass() + "-&: " + (bluetoothClass.hashCode() & 0xFFE000) + " |address:  " + remoteDevice.getAddress());
                BltLog.e(TAG, "deviceTyoe address: " + remoteDevice.getAddress() + " deviceTyoe：" + deviceType + " class: " + bluetoothClass);

                BltLog.e(TAG, "connect....endTime: " + System.currentTimeMillis());

                self.setState(BltSocketClient.State.Connected);

                //重置连接线程
                self.setConnectionThread(null);

                //开始已连接操作
                self.__i__onConnected();

            } catch (IOException e) {
                BltLog.e(TAG, ",ConnectionThread(), e: " + e.toString());

                e.printStackTrace();

                self.__i__onConnectFailure(new BltConnectException());

                //TODO 断开连接
                self.disconnect(false, new BltConnectException());
            }
        }
    }

    /**
     * Socket已连接通知
     */
    private void __i__onConnected() {
        /**
         * 切换到主线程通知连接状态
         */
        if (Looper.myLooper() != Looper.getMainLooper()) {
            getUiHandler().post(new Runnable() {
                @Override
                public void run() {
                    self.__i__onConnected();
                }
            });
            return;
        }

        //更新连接
        BltManager.getInstance().getMultipleBluetoothController().removeConnectingBlt(this);
        BltManager.getInstance().getMultipleBluetoothController().addBltSocketClient(this);

        //开启发送线程
        if (!getSendThread().isAlive())
            getSendThread().start();

        //接收线程
        if (!getReceiveThread().isAlive())
            getReceiveThread().start();

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Date data = new Date();
        setClientConnectedTime(format.format(data));
        getBltDevice().setConnectedTimeString(format.format(data));

        //通知已连接
        ArrayList<BltSocketClientDelegate> delegatesCopy = (ArrayList<BltSocketClientDelegate>) getBltSocketClientDelegates().clone();
        int count = delegatesCopy.size();
        for (int i = 0; i < count; i++) {
            delegatesCopy.get(i).onConnected(self);
        }
    }

    /**
     * Socket已连接通知
     */
    private void __i__onConnectFailure(BltException bltException) {
        /**
         * 切换到主线程通知连接状态
         */
        if (Looper.myLooper() != Looper.getMainLooper()) {
            getUiHandler().post(new Runnable() {
                @Override
                public void run() {
                    self.__i__onConnected();
                }
            });
            return;
        }

        //通知连接失败
        ArrayList<BltSocketClientDelegate> delegatesCopy = (ArrayList<BltSocketClientDelegate>) getBltSocketClientDelegates().clone();
        int count = delegatesCopy.size();
        for (int i = 0; i < count; i++) {
            delegatesCopy.get(i).onConnectFail(self, bltException);
        }
    }

    /**
     * **************************************************************
     * **************************发送线程****************************
     * **************************************************************
     */

    /**
     * 正在发送中的数据包
     */
    private BltSocketSendPacket sendingPacket;

    public BltSocketSendPacket getSendingPacket() {
        return sendingPacket;
    }

    public BltSocketClient setSendingPacket(BltSocketSendPacket sendingPacket) {
        this.sendingPacket = sendingPacket;
        return this;
    }

    /**
     * 发送包阻塞队列
     */
    private LinkedBlockingQueue<BltSocketSendPacket> sendingPacketQueue;

    public LinkedBlockingQueue<BltSocketSendPacket> getSendingPacketQueue() {
        if (null == sendingPacketQueue) {
            sendingPacketQueue = new LinkedBlockingQueue<>();
        }
        return sendingPacketQueue;
    }

    /**
     * 记录上次发送数据片段的时间
     * 仅在每个发送包开始发送时计时，结束后重置计时
     * NoSendingTime 表示当前没有在发送数据
     */
    private final static long NoSendingTime = -1;
    private long lastSendMessageTime = NoSendingTime;

    protected BltSocketClient setLastSendMessageTime(long lastSendMessageTime) {
        this.lastSendMessageTime = lastSendMessageTime;
        return this;
    }

    protected long getLastSendMessageTime() {
        return this.lastSendMessageTime;
    }


    /**
     * 发送线程
     */
    private SendThread sendThread;

    public SendThread getSendThread() {
        if (null == sendThread) {
            this.sendThread = new SendThread();
        }
        return sendThread;
    }

    public void setSendThread(SendThread sendThread) {
        this.sendThread = sendThread;
    }

    private class SendThread extends Thread {
        @Override
        public void run() {
            super.run();
            BltLog.e(TAG, "SendThread is running start ");

            BltSocketSendPacket packet;

            try {
                while (self.isConnected()      //已连接状态
                        && !self.isDisconnecting()   //正在断开连接时不再发送数据
                        && !Thread.interrupted()    //线程未终断
                        && null != (packet = self.getSendingPacketQueue().take())) {

                    BltLog.e(TAG, "SendThread is running take a packet ");

                    //设置发送包
                    self.setSendingPacket(packet);
                    self.setLastSendMessageTime(System.currentTimeMillis());

                    try {   //防止异常后结束while循环

                        //数据包
                        byte[] sendPacket = packet.getSendPacket();

                        //通知数据发送失败
                        if (null == sendPacket || sendPacket.length == 0) {
                            self.__i__onSendPacketCancel(packet, new BltSendException("SendThread(),取消发送,数据包不能为空或长度不能为0"));
                            self.setSendingPacket(null);
                            continue;
                        }

                        self.__i__onSendPacketBegin(packet);

                        int sendPaketLength = sendPacket.length;

                        BltLog.e(TAG, "sendPaketLength: " + sendPaketLength);

                        self.getRunningBltSocket().getOutputStream().write(sendPacket);
                        self.getRunningBltSocket().getOutputStream().flush();
                        self.setLastSendMessageTime(System.currentTimeMillis());

                        self.__i__onSendPacketEnd(packet);

                        self.setSendingPacket(null);

                        //发送时计时，结束后重置计时
                        self.setLastSendMessageTime(NoSendingTime);

                    } catch (Exception e) {
                        BltLog.e(TAG, ",SendThread->while内部：" + e.toString());
                        e.printStackTrace();

                        if (self.getSendingPacket() != null) {
                            self.__i__onSendPacketCancel(self.getSendingPacket(), new BltSendException("SendThread(),while内部,取消发送,发送数据时出现异常,正在发送中的数据被取消发送"));
                            self.setSendingPacket(null);
                        }

                        //TODO Socket状态异常，发送失败则断开连接
                        if (e instanceof IOException) {
                            //断开连接,断开连接后，心跳链路重连，数据链路检测是否有发送失败数据，若有则重连发送，重复n次后停止,直到下次主动发送时重连,
                            disconnect(true, new BltSocketException("Send Packet Failure !!!"));
                            return;
                        }

                    } // try end

                }//while end
            } catch (Exception e) {
                BltLog.e(TAG, ",SendThread->while外部：" + e.toString());
                e.printStackTrace();

                if (self.getSendingPacket() != null) {
                    self.__i__onSendPacketCancel(self.getSendingPacket(), new BltSendException("SendThread(),while外部,取消发送,发送数据时出现异常,正在发送中的数据被取消发送"));
                    self.setSendingPacket(null);
                }
            }

            BltLog.e(TAG, "SendThread is running end");


        }
    }

    /**
     * **************************************************************
     * **************************接收线程****************************
     * **************************************************************
     */

    /**
     * Socket输入流读取类
     */
    private BltSocketInputReader socketInputReader;

    protected BltSocketClient setSocketInputReader(BltSocketInputReader socketInputReader) {
        this.socketInputReader = socketInputReader;
        return this;
    }

    protected BltSocketInputReader getSocketInputReader() throws IOException {
        if (this.socketInputReader == null) {
            this.socketInputReader = new BltSocketInputReader(getRunningBltSocket().getInputStream());
        }
        return this.socketInputReader;
    }

    /**
     * 正在接收的响应包
     */
    private BltSocketResponsePacket receivingResponsePacket;

    protected BltSocketClient setReceivingResponsePacket(BltSocketResponsePacket receivingResponsePacket) {
        this.receivingResponsePacket = receivingResponsePacket;
        return this;
    }

    protected BltSocketResponsePacket getReceivingResponsePacket() {
        return this.receivingResponsePacket;
    }

    /**
     * 记录上次接收到消息的时间
     */
    private long lastReceiveMessageTime;

    protected BltSocketClient setLastReceiveMessageTime(long lastReceiveMessageTime) {
        this.lastReceiveMessageTime = lastReceiveMessageTime;
        return this;
    }

    protected long getLastReceiveMessageTime() {
        return this.lastReceiveMessageTime;
    }


    private ReceiveThread receiveThread;

    public ReceiveThread getReceiveThread() {
        if (null == receiveThread) {
            this.receiveThread = new ReceiveThread();
        }
        return receiveThread;
    }

    public void setReceiveThread(ReceiveThread receiveThread) {
        this.receiveThread = receiveThread;
    }

    private class ReceiveThread extends Thread {
        @Override
        public void run() {
            super.run();
            BltLog.e(TAG, "ReceiveThread is running...");

            long bytesRead = 0;
            long count = 0, remain = 0;

            try {
                while (self.isConnected()
                        && self.getSocketInputReader() != null
                        && !Thread.interrupted()) {

                    try {

                        BltSocketResponsePacket packet = new BltSocketResponsePacket();

                        self.__i__onReceivePacketBegin(packet);

                        /*byte[] receiveBufferArray = new byte[1024];

                        int readNumber = self.getRunningBltSocket().getInputStream().read(receiveBufferArray, 0, receiveBufferArray.length);*/

                        long stime = SystemClock.elapsedRealtime();

                        // byte[] data = self.getSocketInputReader().readDataAndDelayTime(4096, 200);
                        byte[] data;

                        if (getBltConnectRuleConfig().getDelayReceiveTime() > 0) {
                            data = self.getSocketInputReader().readData(getBltConnectRuleConfig().getReceiveBufferSize(), getBltConnectRuleConfig().getDelayReceiveTime());
                        } else {
                            data = self.getSocketInputReader().readData(getBltConnectRuleConfig().getReceiveBufferSize());
                        }

                        if (null == data) {
                            continue;
                        }

                       /* bytesRead += data.length;
                        count = bytesRead / 1495;
                        remain = bytesRead % 1495;

                        BltLog.e(TAG, "receive " + count + " x 1495 + " + remain
                                + " bytes(+" + data.length
                                + ") data from device(elapse "
                                + (SystemClock.elapsedRealtime() - stime)
                                + " ms).");*/

                        packet.setResponsePacket(data);

                        self.__i__onReceivePacketEnd(packet, false);
                        self.__i__onReceiveResponse(packet, false);
                        self.setReceivingResponsePacket(null);

                    } catch (Exception e) {
                        BltLog.e(TAG, ",ReceiveThread(),Inner Exception: " + e.toString());

                        if (self.getReceivingResponsePacket() != null) {
                            self.__i__onReceivePacketCancel(self.getReceivingResponsePacket(), new BltReceiveException(" e: " + e.toString()));
                            self.setReceivingResponsePacket(null);
                        }
                        if (e instanceof IOException) {
                            //断开连接
                            disconnect(true, new BltSocketException("Receive Packet Failure !!!"));
                            return;
                        }
                    }
                }

            } catch (Exception e) {
                BltLog.e(TAG, ",ReceiveThread(),External Exception: " + e.toString());
                e.printStackTrace();

                if (self.getReceivingResponsePacket() != null) {
                    self.__i__onReceivePacketCancel(self.getReceivingResponsePacket(), new BltReceiveException(" e: " + e.toString()));
                    self.setReceivingResponsePacket(null);
                }
                if (e instanceof IOException) {
                    //断开连接,断开连接后，心跳链路重连，数据链路检测是否有发送失败数据，若有则重连发送，重复n次后停止,直到下次主动发送时重连,
                    disconnect(true, new BltSocketException("Receive Packet Failure !!!"));
                }
            }
        }
    }

    /**
     * **************************************************************
     * *************************未连接线程***************************
     * **************************************************************
     */
    private DisconnectionThread disconnectionThread;

    public DisconnectionThread getDisconnectionThread(boolean isReconnect, BltException bltException) {
        if (null == disconnectionThread)
            this.disconnectionThread = new DisconnectionThread();
        return disconnectionThread.setReconnect(isReconnect).setBltException(bltException);
    }

    public void setDisconnectionThread(DisconnectionThread disconnectionThread) {
        this.disconnectionThread = disconnectionThread;
    }

    private class DisconnectionThread extends Thread {

        private BltException bltException;
        private boolean isReconnect;

        public DisconnectionThread setReconnect(boolean reconnect) {
            isReconnect = reconnect;
            return this;
        }

        public DisconnectionThread setBltException(BltException bltException) {
            this.bltException = bltException;
            return this;
        }

        @Override
        public void run() {
            super.run();

            BltLog.e(TAG, "DisconnectionThread  run()...");

            //关闭连接线程
            if (null != self.connectionThread) {
                self.getConnectionThread().interrupt();
                self.setConnectionThread(null);
            }
            //关闭Socket以及输入输出流
            if (null != self.getRunningBltSocket() && self.isConnected()) {
                try {
                    self.getRunningBltSocket().getOutputStream().close();
                    self.getRunningBltSocket().getInputStream().close();
                } catch (IOException e) {
                    BltLog.e(TAG, ",DisconnectionThread(),e:" + e.toString());
                    e.printStackTrace();
                } finally {
                    try {
                        self.getRunningBltSocket().close();
                        BltLog.e(TAG, ",DisconnectionThread(),getRunningBltSocket.close()...");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    self.setRunningBltSocket(null);
                }
            }

            if (self.sendThread != null) {
                self.getSendThread().interrupt();
                self.setSendThread(null);
            }

            if (self.receiveThread != null) {
                self.getReceiveThread().interrupt();
                self.setReceiveThread(null);
            }

            self.setDisconnecting(false);
            self.setState(BltSocketClient.State.Disconnected);


            if (null != self.getSendingPacket()) {
                self.__i__onSendPacketCancel(self.getSendingPacket(), new OtherException("DisconnectionThread(),正在断开连接,正在发送中的数据包被取消发送"));
                self.setSendingPacket(null);
            }

            BltSocketSendPacket packet;
            while (null != (packet = self.getSendingPacketQueue().poll())) {
                self.__i__onSendPacketCancel(packet, new OtherException("DisconnectionThread(),正在断开连接,发送队列中存在的所有数据包被取消发送"));
            }

            if (null != self.getReceivingResponsePacket()) {
                self.__i__onReceivePacketCancel(self.getReceivingResponsePacket(), new OtherException("连接断开，取消接收"));
                self.setReceivingResponsePacket(null);
            }
            self.setDisconnectionThread(null);

            setClientConnectedTime("");
            getBltDevice().setConnectedTimeString("");

            self.__i__onDisconnected(this.isReconnect, bltException);

            BltLog.e(TAG, "DisconnectionThread  run()... end");
        }
    }

    /**
     * 通知
     */
    /**
     * 开始发送数据通知
     */
    private void __i__onSendPacketBegin(final BltSocketSendPacket packet) {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            getUiHandler().post(new Runnable() {
                @Override
                public void run() {
                    self.__i__onSendPacketBegin(packet);
                }
            });
            return;
        }

        if (getBltSocketClientDelegates().size() > 0) {
            ArrayList<BltSocketClientSendingDelegate> delegatesCopy =
                    (ArrayList<BltSocketClientSendingDelegate>) geBltSocketClientSendingDelegates().clone();
            int count = delegatesCopy.size();
            for (int i = 0; i < count; ++i) {
                BltSocketClientSendingDelegate bltSocketClientSendingDelegate = delegatesCopy.get(i);
                if (null != bltSocketClientSendingDelegate) {
                    bltSocketClientSendingDelegate.onSendPacketBegin(this, packet);
                }
            }
        }
    }

    /**
     * 发送数据失败通知
     */
    private void __i__onSendPacketCancel(final BltSocketSendPacket packet, final BltException bltException) {
        /**
         * 切换到主线程通知连接状态
         */
        if (Looper.myLooper() != Looper.getMainLooper()) {
            getUiHandler().post(new Runnable() {
                @Override
                public void run() {
                    self.__i__onSendPacketCancel(packet, bltException);
                }
            });
        }

        if (getBltSocketClientDelegates().size() > 0) {
            ArrayList<BltSocketClientSendingDelegate> delegatesCopy =
                    (ArrayList<BltSocketClientSendingDelegate>) geBltSocketClientSendingDelegates().clone();
            int count = delegatesCopy.size();
            for (int i = 0; i < count; ++i) {
                BltSocketClientSendingDelegate bltSocketClientSendingDelegate = delegatesCopy.get(i);
                if (null != bltSocketClientSendingDelegate) {
                    bltSocketClientSendingDelegate.onSendPacketCancel(this, packet, bltException);
                }
                //TODO 数据包被取消发送后，统一处理，给出取消原因，
            }
        }
    }

    /**
     * 数据发送完成通知
     */

    /**
     * SocketClientDelegate
     */
    private ArrayList<BltSocketClientDelegate> bltSocketClientDelegates;

    protected ArrayList<BltSocketClientDelegate> getBltSocketClientDelegates() {
        if (this.bltSocketClientDelegates == null) {
            this.bltSocketClientDelegates = new ArrayList<BltSocketClientDelegate>();
        }
        return this.bltSocketClientDelegates;
    }

    /**
     * SocketClientReceivingDelegate
     */
    private ArrayList<BltSocketClientReceivingDelegate> bltSocketClientReceivingDelegates;

    protected ArrayList<BltSocketClientReceivingDelegate> getBLtSocketClientReceivingDelegates() {
        if (this.bltSocketClientReceivingDelegates == null) {
            this.bltSocketClientReceivingDelegates = new ArrayList<BltSocketClientReceivingDelegate>();
        }
        return this.bltSocketClientReceivingDelegates;
    }

    /**
     * SocketClientSendingDelegate
     */
    private ArrayList<BltSocketClientSendingDelegate> bltSocketClientSendingDelegates;

    public ArrayList<BltSocketClientSendingDelegate> geBltSocketClientSendingDelegates() {
        if (this.bltSocketClientSendingDelegates == null) {
            this.bltSocketClientSendingDelegates = new ArrayList<BltSocketClientSendingDelegate>();
        }
        return this.bltSocketClientSendingDelegates;
    }

    private void __i__onSendPacketEnd(final BltSocketSendPacket packet) {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            getUiHandler().post(new Runnable() {
                @Override
                public void run() {
                    self.__i__onSendPacketEnd(packet);
                }
            });
            return;
        }

        if (geBltSocketClientSendingDelegates().size() > 0) {
            ArrayList<BltSocketClientSendingDelegate> delegatesCopy =
                    (ArrayList<BltSocketClientSendingDelegate>) geBltSocketClientSendingDelegates().clone();
            int count = delegatesCopy.size();
            for (int i = 0; i < count; ++i) {
                BltSocketClientSendingDelegate bltSocketClientSendingDelegate = delegatesCopy.get(i);
                if (null != bltSocketClientSendingDelegate) {
                    bltSocketClientSendingDelegate.onSendPacketEnd(this, packet);
                }
            }
        }
    }


    private void __i__onDisconnected(final boolean isReconnect, final BltException bltException) {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            getUiHandler().post(new Runnable() {
                @Override
                public void run() {
                    self.__i__onDisconnected(isReconnect, bltException);
                }
            });
            return;
        }

        ArrayList<BltSocketClientDelegate> delegatesCopy =
                (ArrayList<BltSocketClientDelegate>) getBltSocketClientDelegates().clone();
        int count = delegatesCopy.size();
        for (int i = 0; i < count; ++i) {
            BltSocketClientDelegate bltSocketClientDelegate = delegatesCopy.get(i);
            if (null != bltSocketClientDelegate) {
                bltSocketClientDelegate.onDisconnected(this, isReconnect, bltException);
            }
        }
    }

    private void __i__onReceivePacketBegin(final BltSocketResponsePacket packet) {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            getUiHandler().post(new Runnable() {
                @Override
                public void run() {
                    self.__i__onReceivePacketBegin(packet);
                }
            });
            return;
        }

        if (getBLtSocketClientReceivingDelegates().size() > 0) {
            ArrayList<BltSocketClientReceivingDelegate> delegatesCopy =
                    (ArrayList<BltSocketClientReceivingDelegate>) getBLtSocketClientReceivingDelegates().clone();
            int count = delegatesCopy.size();
            for (int i = 0; i < count; ++i) {
                BltSocketClientReceivingDelegate bltSocketClientReceivingDelegate = delegatesCopy.get(i);
                if (null != bltSocketClientReceivingDelegate) {
                    bltSocketClientReceivingDelegate.onReceivePacketBegin(this, packet);
                }
            }
        }
    }

    private void __i__onReceivePacketEnd(final BltSocketResponsePacket packet, final boolean isCallbackInMainThread) {

        if (isCallbackInMainThread) {
            if (Looper.myLooper() != Looper.getMainLooper()) {
                getUiHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        self.__i__onReceivePacketEnd(packet, isCallbackInMainThread);
                    }
                });
                return;
            }
        }

        if (getBLtSocketClientReceivingDelegates().size() > 0) {
            ArrayList<BltSocketClientReceivingDelegate> delegatesCopy =
                    (ArrayList<BltSocketClientReceivingDelegate>) getBLtSocketClientReceivingDelegates().clone();
            int count = delegatesCopy.size();
            for (int i = 0; i < count; ++i) {
                BltSocketClientReceivingDelegate bltSocketClientReceivingDelegate = delegatesCopy.get(i);
                if (null != bltSocketClientReceivingDelegate) {
                    bltSocketClientReceivingDelegate.onReceivePacketEnd(this, packet, isCallbackInMainThread);
                }
            }
        }
    }

    private void __i__onReceivePacketCancel(final BltSocketResponsePacket packet, final BltException bltException) {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            getUiHandler().post(new Runnable() {
                @Override
                public void run() {
                    self.__i__onReceivePacketCancel(packet, bltException);
                }
            });
            return;
        }

        if (getBLtSocketClientReceivingDelegates().size() > 0) {
            ArrayList<BltSocketClientReceivingDelegate> delegatesCopy =
                    (ArrayList<BltSocketClientReceivingDelegate>) getBLtSocketClientReceivingDelegates().clone();
            int count = delegatesCopy.size();
            for (int i = 0; i < count; ++i) {
                BltSocketClientReceivingDelegate bltSocketClientReceivingDelegate = delegatesCopy.get(i);
                if (null != bltSocketClientReceivingDelegate) {
                    bltSocketClientReceivingDelegate.onReceivePacketCancel(this, packet, bltException);
                }
            }
        }
    }

    /**
     * 接收数据通知回调
     *
     * @param responsePacket
     * @param isCallbackInMainThread 是否在主线程回调
     */
    private void __i__onReceiveResponse(@NonNull final BltSocketResponsePacket responsePacket, final boolean isCallbackInMainThread) {

        if (isCallbackInMainThread) {
            if (Looper.myLooper() != Looper.getMainLooper()) {
                getUiHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        self.__i__onReceiveResponse(responsePacket, isCallbackInMainThread);
                    }
                });
                return;
            }
        }

        setLastReceiveMessageTime(System.currentTimeMillis());

        if (getBltSocketClientDelegates().size() > 0) {
            ArrayList<BltSocketClientDelegate> delegatesCopy =
                    (ArrayList<BltSocketClientDelegate>) getBltSocketClientDelegates().clone();
            int count = delegatesCopy.size();
            for (int i = 0; i < count; ++i) {
                BltSocketClientDelegate bltSocketClientDelegate = delegatesCopy.get(i);
                if (null != bltSocketClientDelegate) {
                    bltSocketClientDelegate.onResponse(this, responsePacket, isCallbackInMainThread);
                }
            }
        }
    }

    /**
     * 注册监听回调
     *
     * @param delegate 回调接收者
     */
    public BltSocketClient registerBltSocketClientDelegate(BltSocketClientDelegate delegate) {
        if (!getBltSocketClientDelegates().contains(delegate)) {
            getBltSocketClientDelegates().add(delegate);
        }
        return this;
    }

    /**
     * 取消注册监听回调
     *
     * @param delegate 回调接收者
     */
    public BltSocketClient removeSocketClientDelegate(BltSocketClientDelegate delegate) {
        getBltSocketClientDelegates().remove(delegate);
        return this;
    }

    public BltSocketClient removeAllSocketClientDelegate() {
        getBltSocketClientDelegates().clear();
        return this;
    }


    /**
     * 注册信息发送回调
     *
     * @param delegate 回调接收者
     */
    public BltSocketClient registerBltSocketClientSendingDelegate(BltSocketClientSendingDelegate delegate) {
        if (!geBltSocketClientSendingDelegates().contains(delegate)) {
            geBltSocketClientSendingDelegates().add(delegate);
        }
        return this;
    }

    /**
     * 取消注册信息发送回调
     *
     * @param delegate 回调接收者
     */
    public BltSocketClient removeSocketClientSendingDelegate(BltSocketClientSendingDelegate delegate) {
        geBltSocketClientSendingDelegates().remove(delegate);
        return this;
    }

    public BltSocketClient removeAllSocketClientSendingDelegate() {
        geBltSocketClientSendingDelegates().clear();
        return this;
    }

    /**
     * 注册信息接收回调
     *
     * @param delegate 回调接收者
     */
    public BltSocketClient registerBltSocketClientReceiveDelegate(BltSocketClientReceivingDelegate delegate) {
        if (!getBLtSocketClientReceivingDelegates().contains(delegate)) {
            getBLtSocketClientReceivingDelegates().add(delegate);
        }
        return this;
    }

    /**
     * 取消注册信息接收回调
     *
     * @param delegate 回调接收者
     */
    public BltSocketClient removeSocketClientReceiveDelegate(BltSocketClientReceivingDelegate delegate) {
        getBLtSocketClientReceivingDelegates().remove(delegate);
        return this;
    }

    public BltSocketClient removeAllSocketClientReceiveDelegate() {
        getBLtSocketClientReceivingDelegates().clear();
        return this;
    }

    public void destory() {
        disconnect(false, new OtherException("It's Over !!!"));
        removeAllSocketClientDelegate();
        removeAllSocketClientSendingDelegate();
        removeAllSocketClientReceiveDelegate();
    }

    /**
     * server端调用
     */
    @CallSuper
    protected void internalOnConnected() {
        setState(State.Connected);

        setLastReceiveMessageTime(System.currentTimeMillis());
        setLastSendMessageTime(NoSendingTime);

        setSendingPacket(null);
        setReceivingResponsePacket(null);

        __i__onConnected();
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BltSocketClient that = (BltSocketClient) o;

        return mBltDevice.equals(that.mBltDevice);
    }

    @Override
    public int hashCode() {
        return mBltDevice.hashCode();
    }
}
