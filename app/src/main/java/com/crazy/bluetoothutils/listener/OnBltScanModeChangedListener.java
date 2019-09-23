package com.crazy.bluetoothutils.listener;

/**
 * 蓝牙状态
 * Created by feaoes on 2018/4/16.
 */

public interface OnBltScanModeChangedListener extends OnBltListener {

    //允许周围设备检测到
    void onScanModeConnectableDiscoverable(String describe);

    //仅让已配对设备检测到
    void onScanModeConnectable(String describe);

    //未处于可检测到模式并且无法接收连接
    void onScanModeNone(String describe);
}
