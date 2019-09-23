package com.crazy.bluetoothutils.listener;

/**
 * 蓝牙状态
 * Created by feaoes on 2018/4/16.
 */

public interface OnBltStateChangedListener extends OnBltListener {

    //打开中
    void onStateTurningOn();

    //已打开
    void onStateOn();

    //关闭中
    void onStateTurningoff();

    //已关闭
    void onStateOff();

}
