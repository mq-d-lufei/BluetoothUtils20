package com.crazy.bluetoothutils.listener;

import android.bluetooth.BluetoothDevice;

import java.util.List;

/**
 * 蓝牙设备扫描
 * Created by feaoes on 2018/4/16.
 */

public interface OnScanListener {
    //扫描结果(单个设备/已搜索到的所有设备)
    void onScanResult(BluetoothDevice bluetoothDevice, List<BluetoothDevice> foundDevices);

    //开始扫描
    void onScanStarted();

    //扫描完成
    void onScanFinished();
}
