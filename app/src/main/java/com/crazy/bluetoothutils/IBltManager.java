package com.crazy.bluetoothutils;

import android.content.Context;

/**
 * Created by feaoes on 2018/4/16.
 */

public interface IBltManager {

    //是否支持蓝牙
    boolean isSupportBluetooth();

    //是否支持蓝牙4.0
    boolean isSupportBLE();

    //蓝牙是否打开
    boolean isBluetoothOpen();

    //跳转开启蓝牙
    int enableBluetooth(Context context);

    //跳转开启蓝牙
    int enableBluetooth(Context context, int requestCode);

    // 关闭蓝牙
    boolean disableBluetooth();

    //开始扫描蓝牙
    boolean startScan();

    //取消扫描蓝牙
    boolean stopScan();

}
