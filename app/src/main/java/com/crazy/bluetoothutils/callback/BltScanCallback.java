package com.crazy.bluetoothutils.callback;

import com.crazy.bluetoothutils.data.BltDevice;

import java.util.List;

/**
 * Description:
 * Created by Crazy on 2018/7/27.
 */
public abstract class BltScanCallback implements BltScanPresenterImp {

    public abstract void onScanFinished(List<BltDevice> scanResultList);

    public void onScanResult(BltDevice bltDevice) {
    }
}
