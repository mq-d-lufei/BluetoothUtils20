package com.crazy.bluetoothutils.data;

import android.support.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Description:
 * Created by Crazy on 2018/7/30.
 */
public interface BltScanState {

    @Retention(RetentionPolicy.SOURCE)
    @StringDef({ScanState.STATE_IDLE, ScanState.STATE_SCANNING, ScanState.STATE_SCAN_FINISHED})
    @interface ScanState {
        String STATE_IDLE = "STATE_IDLE";
        String STATE_SCANNING = "STATE_SCANNING";
        String STATE_SCAN_FINISHED = "STATE_SCAN_FINISHED";
    }
}
