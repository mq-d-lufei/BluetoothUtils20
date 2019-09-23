package com.crazy.bluetoothutils.callback;

import com.crazy.bluetoothutils.data.BltDevice;

/**
 * Description:
 * Created by Crazy on 2018/7/27.
 */
public abstract class BltScanAndBondCallback extends BltBondCallback implements BltScanPresenterImp {

    public abstract void onScanFinished(BltDevice scanResult);

    public void onScanResult(BltDevice bleDevice) {
    }
}
