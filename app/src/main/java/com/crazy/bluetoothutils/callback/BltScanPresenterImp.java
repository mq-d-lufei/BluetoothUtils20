package com.crazy.bluetoothutils.callback;

import com.crazy.bluetoothutils.data.BltDevice;

/**
 * Description:
 * Created by Crazy on 2018/7/27.
 */
public interface BltScanPresenterImp {

    void onScanStarted(boolean success);

    void onScanningFilter(BltDevice bltDevice);
}
