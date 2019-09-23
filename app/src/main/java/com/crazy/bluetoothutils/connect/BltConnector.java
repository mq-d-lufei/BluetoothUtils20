package com.crazy.bluetoothutils.connect;

import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.support.annotation.NonNull;
import android.util.Log;

import com.crazy.bluetoothutils.BltManager;
import com.crazy.bluetoothutils.connect.bltclient.BltSocketClient;
import com.crazy.bluetoothutils.connect.delegate.BltSocketClientDelegate;
import com.crazy.bluetoothutils.connect.delegate.BltSocketClientReceivingDelegate;
import com.crazy.bluetoothutils.connect.delegate.BltSocketClientSendingDelegate;
import com.crazy.bluetoothutils.data.BltDevice;
import com.crazy.bluetoothutils.data.BluetoothDeviceType;

/**
 * Description:
 * Created by Crazy on 2018/8/2.
 */
public class BltConnector {

    public static final String TAG = BltConnector.class.getSimpleName();

    private BltConnector() {
    }

    private static class BltConnectorHolder {
        private static final BltConnector INSTANCE = new BltConnector();
    }

    public static BltConnector getInstance() {
        return BltConnector.BltConnectorHolder.INSTANCE;
    }

    /**
     * 作为客户端连接
     */
    public void connect(@NonNull String bltMacStr, @NonNull BltConnectRuleConfig bltConnectRuleConfig, BltSocketClientDelegate bltSocketClientDelegate) {
        judgeIllegal(bltConnectRuleConfig);
        BluetoothDevice remoteDevice = BltManager.getInstance().getBluetoothAdapter().getRemoteDevice(bltMacStr);
        startConnect(getBltDevice(remoteDevice), bltConnectRuleConfig, bltSocketClientDelegate);
    }

    public void connect(@NonNull BluetoothDevice bluetoothDevice, @NonNull BltConnectRuleConfig bltConnectRuleConfig, BltSocketClientDelegate bltSocketClientDelegate) {
        judgeIllegal(bltConnectRuleConfig);
        startConnect(getBltDevice(bluetoothDevice), bltConnectRuleConfig, bltSocketClientDelegate);
    }

    public void connect(@NonNull BltDevice bltDevice, @NonNull BltConnectRuleConfig bltConnectRuleConfig, BltSocketClientDelegate bltSocketClientDelegate) {
        judgeIllegal(bltConnectRuleConfig);
        startConnect(bltDevice, bltConnectRuleConfig, bltSocketClientDelegate);
    }

    public void connect(@NonNull BltDevice bltDevice, @NonNull BltConnectRuleConfig bltConnectRuleConfig, BltSocketClientDelegate bltSocketClientDelegate, BltSocketClientSendingDelegate bltSocketClientSendingDelegate, BltSocketClientReceivingDelegate bltSocketClientReceivingDelegate) {
        judgeIllegal(bltConnectRuleConfig);
        startConnect(bltDevice, bltConnectRuleConfig, bltSocketClientDelegate, bltSocketClientSendingDelegate, bltSocketClientReceivingDelegate);
    }

    private void judgeIllegal(BltConnectRuleConfig bltConnectRuleConfig) {
        if (null == bltConnectRuleConfig.getSecureType()) {
            throw new IllegalArgumentException("bltConnectRuleConfig.getSecureType() can not be Null!");
        }

        if (null == bltConnectRuleConfig.getUuid()) {
            throw new IllegalArgumentException("bltConnectRuleConfig.getUuid() can not be Null!");
        }
    }

    private void startConnect(@NonNull BltDevice bltDevice, @NonNull BltConnectRuleConfig bltConnectRuleConfig, BltSocketClientDelegate bltSocketClientDelegate) {
        BltSocketClient bltSocketClient = BltManager.getInstance().getMultipleBluetoothController().buildConnectingBlt(bltDevice, bltConnectRuleConfig);
        bltSocketClient.registerBltSocketClientDelegate(bltSocketClientDelegate);
        bltSocketClient.connect();
    }

    private void startConnect(@NonNull BltDevice bltDevice, @NonNull BltConnectRuleConfig bltConnectRuleConfig, BltSocketClientDelegate bltSocketClientDelegate, BltSocketClientSendingDelegate bltSocketClientSendingDelegate, BltSocketClientReceivingDelegate bltSocketClientReceivingDelegate) {
        BltSocketClient bltSocketClient = BltManager.getInstance().getMultipleBluetoothController().buildConnectingBlt(bltDevice, bltConnectRuleConfig);
        bltSocketClient.registerBltSocketClientDelegate(bltSocketClientDelegate);
        bltSocketClient.registerBltSocketClientSendingDelegate(bltSocketClientSendingDelegate);
        bltSocketClient.registerBltSocketClientReceiveDelegate(bltSocketClientReceivingDelegate);
        bltSocketClient.connect();
    }

    public void registerBltSocketClientSendingDelegate(@NonNull BltDevice bltDevice, BltSocketClientSendingDelegate bltSocketClientSendingDelegate) {
        BltSocketClient bltSocketClient = BltManager.getInstance().getMultipleBluetoothController().getBltSocketClient(bltDevice);
        if (null != bltSocketClient) {
            bltSocketClient.registerBltSocketClientSendingDelegate(bltSocketClientSendingDelegate);
        }
    }

    public void registerBltSocketClientReceiveDelegate(@NonNull BltDevice bltDevice, BltSocketClientReceivingDelegate bltSocketClientReceivingDelegate) {
        BltSocketClient bltSocketClient = BltManager.getInstance().getMultipleBluetoothController().getBltSocketClient(bltDevice);
        if (null != bltSocketClient) {
            bltSocketClient.registerBltSocketClientReceiveDelegate(bltSocketClientReceivingDelegate);
        }
    }

    public void disconnect(@NonNull BltDevice bltDevice, boolean isReconnect) {
        BltManager.getInstance().getMultipleBluetoothController().disconnect(bltDevice, isReconnect);
    }

    private BltDevice getBltDevice(BluetoothDevice bluetoothDevice) {
        BltDevice bltDevice = new BltDevice(bluetoothDevice, (BluetoothSocket) null);

        BluetoothClass bluetoothClass = bluetoothDevice.getBluetoothClass();

        BluetoothDeviceType deviceType = BltManager.getInstance().getBluetoothDeviceType(bluetoothClass);

        Log.d(TAG, "deviceType name: " + bluetoothDevice.getName() + " -getDeviceClass： " + bluetoothClass.getDeviceClass() + " -getMajorDeviceClass: " + bluetoothClass.getMajorDeviceClass() + "-&: " + (bluetoothClass.hashCode() & 0xFFE000) + " |address:  " + bluetoothDevice.getAddress());
        Log.e(TAG, "deviceType address: " + bluetoothDevice.getAddress() + " deviceType：" + deviceType + " class: " + bluetoothClass);

        return bltDevice;
    }
}
