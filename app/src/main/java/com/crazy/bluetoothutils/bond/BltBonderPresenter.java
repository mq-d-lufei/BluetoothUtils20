package com.crazy.bluetoothutils.bond;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;

import com.crazy.bluetoothutils.BltManager;
import com.crazy.bluetoothutils.BltMonitorManager;
import com.crazy.bluetoothutils.callback.BltBondCallback;
import com.crazy.bluetoothutils.data.BltDevice;
import com.crazy.bluetoothutils.listener.OnBltBondStateChangedListener;
import com.crazy.bluetoothutils.scan.BltScanRuleConfig;
import com.crazy.bluetoothutils.utils.BltLog;
import com.crazy.bluetoothutils.utils.ClsUtils;


/**
 * Description:
 * Created by Crazy on 2018/7/31.
 */
public abstract class BltBonderPresenter implements OnBltBondStateChangedListener {

    public static final String bondMonitorName = "BondMonitor";

    private BltBondCallback mBltBondCallback;
    private BltScanRuleConfig mBltScanRuleConfig;
    private BltDevice mBltDevice;

    public BltBondCallback getBltBondCallback() {
        return mBltBondCallback;
    }

    public BltScanRuleConfig getBltScanRuleConfig() {
        return mBltScanRuleConfig;
    }

    public BltBondRuleConfig getBltBondRuleConfig() {
        return mBltScanRuleConfig.getBltBondRuleConfig();
    }

    public BltDevice getBltDevice() {
        return mBltDevice;
    }

    public void prepare(BltBondCallback bltBondCallback) {
        this.mBltBondCallback = bltBondCallback;
        registerBltScanReceiver();
    }

    public void prepare(BltDevice bltDevice, BltScanRuleConfig bltScanRuleConfig, BltBondCallback bltBondCallback) {
        this.mBltDevice = bltDevice;
        this.mBltBondCallback = bltBondCallback;
        this.mBltScanRuleConfig = bltScanRuleConfig;
        registerBltScanReceiver();
    }

    private void registerBltScanReceiver() {
        Context context = BltManager.getInstance().getContext();
        BltMonitorManager.getInstance().registerBltMonitor(context, bondMonitorName, (OnBltBondStateChangedListener) BltBonderPresenter.this);
    }

    private void unregisterBltScanReceiver() {
        Context context = BltManager.getInstance().getContext();
        BltMonitorManager.getInstance().unregisterBltMonitor(context, bondMonitorName);
    }

    @Override
    public void onPairingRequest(BluetoothDevice bluetoothDevice, BroadcastReceiver receiver) {

        if (null != getBltScanRuleConfig() && getBltBondRuleConfig().isAutoPaired()) {
            try {

                receiver.abortBroadcast();//如果没有将广播终止，则会出现一个一闪而过的配对框。

                /**
                 * 密码根据已知设备判断
                 */
                ClsUtils.setPin(BluetoothDevice.class, bluetoothDevice, getBltBondRuleConfig().getPairPassword());
            } catch (Exception e) {
                BltLog.e("onPairingRequest", "0 Password 0000 PairingRequest: error: " + e.toString());
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onBondBonded(BluetoothDevice device) {
        unregisterBltScanReceiver();
        onBonded(device);
    }

    @Override
    public void onBondCancel(BluetoothDevice device, boolean isBonded) {
        unregisterBltScanReceiver();
        onCancel(device, isBonded);
    }

    public abstract void onBonded(BluetoothDevice device);

    public abstract void onCancel(BluetoothDevice device, boolean isBonded);
}
