package com.crazy.bluetoothutils.bond;

import android.bluetooth.BluetoothDevice;
import android.support.annotation.NonNull;

import com.crazy.bluetoothutils.BltManager;
import com.crazy.bluetoothutils.callback.BltBondCallback;
import com.crazy.bluetoothutils.callback.BltScanAndBondCallback;
import com.crazy.bluetoothutils.connect.BltConnector;
import com.crazy.bluetoothutils.connect.delegate.BltSocketClientDelegate;
import com.crazy.bluetoothutils.data.BltDevice;
import com.crazy.bluetoothutils.data.BltScanMode;
import com.crazy.bluetoothutils.scan.BltScanRuleConfig;

/**
 * 蓝牙配对管理
 * Created by Crazy on 2018/7/31.
 */
public class BltBonder {

    private BltBonder() {
    }

    private static final class BltBonderHolder {
        private static final BltBonder sBLtBonder = new BltBonder();
    }

    public static BltBonder getInstance() {
        return BltBonderHolder.sBLtBonder;
    }

    private BltBonderPresenter mBltBonderPresenter = new BltBonderPresenter() {

        @Override
        public void onBondBonding(BluetoothDevice device) {
            BltBondCallback callback = mBltBonderPresenter.getBltBondCallback();
            if (callback != null) {
                callback.onBondBonding(device);
            }
        }

        @Override
        public void onBonded(BluetoothDevice device) {

            BltBondCallback callback = mBltBonderPresenter.getBltBondCallback();
            if (callback != null) {
                callback.onBondBonded(device);
            }

            if (null != mBltBonderPresenter.getBltScanRuleConfig() && (mBltBonderPresenter.getBltScanRuleConfig().getScanMode()).equals(BltScanMode.ScanMode.SCAN_BOND_CONNECT)) {
                //TODO connect
                BltConnector.getInstance().connect(device, mBltBonderPresenter.getBltScanRuleConfig().getBltConnectRuleConfig(), (BltSocketClientDelegate) callback);
            }
        }

        @Override
        public void onCancel(BluetoothDevice device, boolean isBonded) {
            BltBondCallback callback = mBltBonderPresenter.getBltBondCallback();
            if (callback != null) {
                callback.onBondCancel(device, isBonded);
            }
            if (isBonded && null != mBltBonderPresenter.getBltScanRuleConfig() && (mBltBonderPresenter.getBltScanRuleConfig().getScanMode()).equals(BltScanMode.ScanMode.SCAN_BOND_CONNECT)) {
                //TODO connect
                BltConnector.getInstance().connect(device, mBltBonderPresenter.getBltScanRuleConfig().getBltConnectRuleConfig(), (BltSocketClientDelegate) callback);
            }
        }
    };


    public void bond(@NonNull String bltMacStr, @NonNull BltBondCallback bltBondCallback) {
        BluetoothDevice remoteDevice = BltManager.getInstance().getBluetoothAdapter().getRemoteDevice(bltMacStr);
        startBltBond(remoteDevice, bltBondCallback);
    }

    public void bond(@NonNull BluetoothDevice bluetoothDevice, @NonNull BltBondCallback bltBondCallback) {
        startBltBond(bluetoothDevice, bltBondCallback);
    }

    public void bond(@NonNull BltDevice bltDevice, @NonNull BltBondCallback bltBondCallback) {
        startBltBond(bltDevice.getDevice(), bltBondCallback);
    }

    public void autoBond(@NonNull BltDevice bltDevice, @NonNull BltScanRuleConfig bltScanRuleConfig, @NonNull BltScanAndBondCallback bltScanAndBondCallback) {
        if (null == bltScanRuleConfig.getBltBondRuleConfig()) {
            throw new IllegalArgumentException("BltBondRuleConfig can not be Null!");
        }

        startBltBond(bltDevice, bltScanRuleConfig, bltScanAndBondCallback);
    }


    private synchronized void startBltBond(BluetoothDevice bluetoothDevice, BltBondCallback bltBondCallback) {
        if (bluetoothDevice.getBondState() == BluetoothDevice.BOND_BONDED) {
            bltBondCallback.onBondBonded(bluetoothDevice);
            return;
        }
        mBltBonderPresenter.prepare(bltBondCallback);
        BltManager.getInstance().bondDeviceWithReflectPair(bluetoothDevice);
        // BltManager.getInstance().bondDevice(bluetoothDevice);
    }

    private synchronized void startBltBond(BltDevice bltDevice, BltScanRuleConfig bltScanRuleConfig, BltBondCallback bltBondCallback) {
        mBltBonderPresenter.prepare(bltDevice, bltScanRuleConfig, bltBondCallback);
        if (bltDevice.getDevice().getBondState() == BluetoothDevice.BOND_BONDED) {
            mBltBonderPresenter.onBondBonded(bltDevice.getDevice());
            return;
        }
        BltManager.getInstance().bondDeviceWithReflectPair(bltDevice.getDevice());
    }
}
