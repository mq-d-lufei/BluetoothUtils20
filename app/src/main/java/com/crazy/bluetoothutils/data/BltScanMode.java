package com.crazy.bluetoothutils.data;

import android.support.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Description:
 * Created by Crazy on 2018/7/27.
 */
public interface BltScanMode {

    @Retention(RetentionPolicy.SOURCE)
    @StringDef({ScanMode.SCAN, ScanMode.SCAN_BOND, ScanMode.SCAN_BOND_CONNECT})
    @interface ScanMode {
        String SCAN = "scan";
        String SCAN_BOND = "scan_bond";
        String SCAN_BOND_CONNECT = "scan_bond_connect";
    }
}
