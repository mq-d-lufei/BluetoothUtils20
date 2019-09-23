package com.crazy.bluetoothutils.connect;

import android.bluetooth.BluetoothDevice;
import android.text.TextUtils;

import com.crazy.bluetoothutils.connect.bltclient.BltSocketClient;
import com.crazy.bluetoothutils.data.BltClientSource;
import com.crazy.bluetoothutils.data.BltDevice;
import com.crazy.bluetoothutils.exception.OtherException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 蓝牙设备连接管理器
 * Created by Crazy on 2018/8/3.
 */
public class MultipleBluetoothController {

    //包含直连或反连的BltSocketClient
    private final LinkedHashMap<String, BltSocketClient> bltHashMap;
    //临时连接中的BltSocketClient
    private final HashMap<String, BltSocketClient> bltTempHashMap;

    public MultipleBluetoothController() {
        bltHashMap = new LinkedHashMap<>();
        bltTempHashMap = new HashMap<>();
    }

    /**
     * Client端连接中的BltSocketClient
     */
    public synchronized BltSocketClient buildConnectingBlt(BltDevice bltDevice, BltConnectRuleConfig bltConnectRuleConfig) {
        BltSocketClient bltSocketClient = new BltSocketClient(bltDevice, bltConnectRuleConfig);
        if (bltTempHashMap.containsKey(bltSocketClient.getDeviceKey())) {
            bltTempHashMap.remove(bltSocketClient.getDeviceKey());
        }
        bltTempHashMap.put(bltSocketClient.getDeviceKey(), bltSocketClient);
        return bltSocketClient;
    }

    public synchronized void removeConnectingBlt(BltSocketClient bltSocketClient) {
        if (null != bltSocketClient) {
            if (bltTempHashMap.containsKey(bltSocketClient.getDeviceKey())) {
                bltTempHashMap.remove(bltSocketClient.getDeviceKey());
            }
        }
    }

    /**
     * 已连接的BltSocketClient
     */
    public synchronized void addBltSocketClient(BltSocketClient bltSocketClient) {
        if (null != bltSocketClient) {
            if (bltHashMap.containsKey(bltSocketClient.getDeviceKey())) {
                bltHashMap.remove(bltSocketClient.getDeviceKey());
            }
            bltHashMap.put(bltSocketClient.getDeviceKey(), bltSocketClient);
        }
    }

    public synchronized void removeBltSocket(BltSocketClient bltSocketClient) {
        if (null != bltSocketClient) {
            if (bltHashMap.containsKey(bltSocketClient.getDeviceKey())) {
                bltHashMap.remove(bltSocketClient.getDeviceKey());
            }
        }
    }

    public synchronized boolean isContainDevice(BltDevice bltDevice) {
        return null != bltDevice && bltHashMap.containsKey(bltDevice.getKey());
    }

    public synchronized boolean isContainDevice(String deviceKey) {
        return !TextUtils.isEmpty(deviceKey) && bltHashMap.containsKey(deviceKey);
    }

    public synchronized boolean isContainDevice(BluetoothDevice bluetoothDevice) {
        return null != bluetoothDevice && bltHashMap.containsKey(bluetoothDevice.getName() + "&" + bluetoothDevice.getAddress());
    }

    public synchronized BltSocketClient getBltSocketClient(String deviceKey) {
        if (!TextUtils.isEmpty(deviceKey)) {
            if (bltHashMap.containsKey(deviceKey)) {
                return bltHashMap.get(deviceKey);
            }
        }
        return null;
    }

    public synchronized BltSocketClient getBltSocketClient(BltDevice bltDevice) {
        if (null != bltDevice) {
            if (bltHashMap.containsKey(bltDevice.getKey())) {
                return bltHashMap.get(bltDevice.getKey());
            }
        }
        return null;
    }

    public synchronized void disconnect(String deviceKey, boolean isReconnect) {
        if (isContainDevice(deviceKey)) {
            getBltSocketClient(deviceKey).disconnect(isReconnect, new OtherException("手动调用断开:" + deviceKey + "设备"));
        }
    }

    public synchronized void disconnect(BltDevice bltDevice, boolean isReconnect) {
        if (isContainDevice(bltDevice)) {
            getBltSocketClient(bltDevice).disconnect(isReconnect, new OtherException("手动调用断开:" + bltDevice.getKey() + "设备"));
        }
    }

    public synchronized void disconnectAllServerDevice() {
        Iterator<Map.Entry<String, BltSocketClient>> iterator = bltHashMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, BltSocketClient> next = iterator.next();
            BltSocketClient bltSocketClient = next.getValue();
            if (bltSocketClient.getClientSource().equals(BltClientSource.ClientSource.SERVER)) {
                bltSocketClient.disconnect(false, new OtherException("手动调用断开所有服务端连接设备"));
                iterator.remove();
            }
        }
    }

    public synchronized void disconnectAllDevice() {
        for (Map.Entry<String, BltSocketClient> bltSocketClientSet : bltHashMap.entrySet()) {
            bltSocketClientSet.getValue().disconnect(false, new OtherException("手动调用断开所有设备"));
        }
        bltHashMap.clear();
    }

    public void destory() {
        for (Map.Entry<String, BltSocketClient> bltSocketClientSet : bltHashMap.entrySet()) {
            bltSocketClientSet.getValue().destory();
        }
        bltHashMap.clear();
        for (Map.Entry<String, BltSocketClient> bltTempSocketClientSet : bltTempHashMap.entrySet()) {
            bltTempSocketClientSet.getValue().destory();
        }
        bltTempHashMap.clear();
    }

    public synchronized List<BltSocketClient> getBltSocketClientList() {
        ArrayList<BltSocketClient> bltSocketClientList = new ArrayList<>(bltHashMap.values());
        Collections.sort(bltSocketClientList, new Comparator<BltSocketClient>() {
            @Override
            public int compare(BltSocketClient lhs, BltSocketClient rhs) {
                return lhs.getDeviceKey().compareToIgnoreCase(rhs.getDeviceKey());
            }
        });
        return bltSocketClientList;
    }

    public synchronized List<BltDevice> getDeviceList() {
        refreshConnectedDevice();
        List<BltDevice> deviceList = new ArrayList<>();
        for (BltSocketClient bltSocketClient : getBltSocketClientList()) {
            if (bltSocketClient != null) {
                deviceList.add(bltSocketClient.getBltDevice());
            }
        }
        return deviceList;
    }

    /**
     * 移除未连接设备
     */
    private void refreshConnectedDevice() {
        List<BltSocketClient> bluetoothList = getBltSocketClientList();
        for (int i = 0; bluetoothList != null && i < bluetoothList.size(); i++) {
            BltSocketClient bltSocketClient = bluetoothList.get(i);
            if (!bltSocketClient.isConnected()) {
                removeBltSocket(bltSocketClient);
            }
        }
    }
}