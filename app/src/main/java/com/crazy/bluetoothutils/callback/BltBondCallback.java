package com.crazy.bluetoothutils.callback;

import android.bluetooth.BluetoothDevice;

/**
 * Description:
 * Created by Crazy on 2018/7/30.
 */
public abstract class BltBondCallback {

    //正在配对
    public abstract void onBondBonding(BluetoothDevice device);

    //已经配对
    public abstract void onBondBonded(BluetoothDevice device);

    //取消配对
    public abstract void onBondCancel(BluetoothDevice device, boolean isBonded);
}
