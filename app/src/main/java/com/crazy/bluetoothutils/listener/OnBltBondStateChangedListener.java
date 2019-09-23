package com.crazy.bluetoothutils.listener;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;

/**
 * 蓝牙2.0绑定状态
 * Created by feaoes on 2018/4/16.
 */

public interface OnBltBondStateChangedListener extends OnBltListener {

    //正在配对
    void onBondBonding(BluetoothDevice device);

    void onPairingRequest(BluetoothDevice device, BroadcastReceiver receiver);

    //已经配对
    void onBondBonded(BluetoothDevice device);

    //取消配对
    void onBondCancel(BluetoothDevice device, boolean isBonded);
}
