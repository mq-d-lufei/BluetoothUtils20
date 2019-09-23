package com.crazy.bluetoothutils.scan;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;

import com.crazy.bluetoothutils.BltManager;
import com.crazy.bluetoothutils.bond.BltBonder;
import com.crazy.bluetoothutils.callback.BltScanAndBondAndConnectCallback;
import com.crazy.bluetoothutils.callback.BltScanAndBondCallback;
import com.crazy.bluetoothutils.callback.BltScanCallback;
import com.crazy.bluetoothutils.callback.BltScanPresenterImp;
import com.crazy.bluetoothutils.data.BltDevice;
import com.crazy.bluetoothutils.data.BltScanMode;
import com.crazy.bluetoothutils.data.BltScanState;
import com.crazy.bluetoothutils.utils.BltLog;

import java.util.List;

/**
 * Description:
 * Created by Crazy on 2018/7/27.
 */

public class BltScanner {

    @BltScanState.ScanState
    private String mBltScanState = BltScanState.ScanState.STATE_IDLE;

    private BltScanner() {
    }

    private static class BltScannerHolder {
        private static final BltScanner INSTANCE = new BltScanner();
    }

    public static BltScanner getInstance() {
        return BltScannerHolder.INSTANCE;
    }

    private BltScanPresenter mBltScanPresenter = new BltScanPresenter() {
        @Override
        public void onScanStarted(boolean success) {
            BltScanPresenterImp callback = mBltScanPresenter.getBltScanPresenterImp();
            if (callback != null) {
                callback.onScanStarted(success);
            }
        }

        @Override
        public void onScanResult(BltDevice bltDevice) {
            switch (mBltScanPresenter.getBltScanRuleConfig().getScanMode()) {
                case BltScanMode.ScanMode.SCAN: {
                    BltScanCallback callback = (BltScanCallback) mBltScanPresenter.getBltScanPresenterImp();
                    if (callback != null) {
                        callback.onScanResult(bltDevice);
                    }
                    break;
                }
                case BltScanMode.ScanMode.SCAN_BOND: {
                    BltScanAndBondCallback callback = (BltScanAndBondCallback) mBltScanPresenter.getBltScanPresenterImp();
                    if (callback != null) {
                        callback.onScanResult(bltDevice);
                    }
                    break;
                }
                case BltScanMode.ScanMode.SCAN_BOND_CONNECT: {
                    BltScanAndBondAndConnectCallback callback = (BltScanAndBondAndConnectCallback) mBltScanPresenter.getBltScanPresenterImp();
                    if (callback != null) {
                        callback.onScanResult(bltDevice);
                    }
                    break;
                }
                default:
                    break;
            }
        }

        @Override
        public void onScanningFilter(BltDevice bltDevice) {
            BltScanPresenterImp callback = mBltScanPresenter.getBltScanPresenterImp();
            if (callback != null) {
                callback.onScanningFilter(bltDevice);
            }
        }

        @Override
        public void onScanFinished(List<BltDevice> bltDeviceList) {

            switch (mBltScanPresenter.getBltScanRuleConfig().getScanMode()) {
                case BltScanMode.ScanMode.SCAN: {
                    BltScanCallback callback = (BltScanCallback) mBltScanPresenter.getBltScanPresenterImp();
                    if (callback != null) {
                        callback.onScanFinished(bltDeviceList);
                    }
                    break;
                }
                case BltScanMode.ScanMode.SCAN_BOND: {
                    final BltScanAndBondCallback callback = (BltScanAndBondCallback) mBltScanPresenter.getBltScanPresenterImp();
                    if (bltDeviceList == null || bltDeviceList.size() < 1) {
                        if (callback != null) {
                            callback.onScanFinished(null);
                        }
                    } else {
                        if (callback != null) {
                            callback.onScanFinished(bltDeviceList.get(0));
                        }
                        final List<BltDevice> list = bltDeviceList;
                        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                BltBonder.getInstance().autoBond(list.get(0), mBltScanPresenter.getBltScanRuleConfig(), callback);
                            }
                        }, 100);
                    }
                    break;
                }
                case BltScanMode.ScanMode.SCAN_BOND_CONNECT: {
                    final BltScanAndBondAndConnectCallback callback = (BltScanAndBondAndConnectCallback) mBltScanPresenter.getBltScanPresenterImp();
                    if (bltDeviceList == null || bltDeviceList.size() < 1) {
                        if (callback != null) {
                            callback.onScanFinished(null);
                        }
                    } else {
                        if (callback != null) {
                            callback.onScanFinished(bltDeviceList.get(0));
                        }
                        final List<BltDevice> list = bltDeviceList;
                        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                BltBonder.getInstance().autoBond(list.get(0), mBltScanPresenter.getBltScanRuleConfig(), callback);
                            }
                        }, 100);
                    }
                    break;
                }
                default:
                    break;
            }
        }
    };

    public void scan(@NonNull BltScanRuleConfig bltScanRuleConfig, @NonNull final BltScanCallback bltScanCallback) {
        startBltScan(bltScanRuleConfig, bltScanCallback);
    }

    public void scanAndBond(@NonNull BltScanRuleConfig bltScanRuleConfig, @NonNull final BltScanAndBondCallback bltScanAndBondCallback) {

        if (null == bltScanRuleConfig.getBltBondRuleConfig()) {
            throw new IllegalArgumentException("BltBondRuleConfig can not be Null!");
        }

        startBltScan(bltScanRuleConfig, bltScanAndBondCallback);
    }

    public void scanAndBondAndConnect(@NonNull BltScanRuleConfig bltScanRuleConfig, @NonNull final BltScanAndBondAndConnectCallback bltScanAndBondAndConnectCallback) {

        if (null == bltScanRuleConfig.getBltBondRuleConfig()) {
            throw new IllegalArgumentException("BltBondRuleConfig can not be Null!");
        }

        if (null == bltScanRuleConfig.getBltConnectRuleConfig()) {
            throw new IllegalArgumentException("BltConnectRuleConfig can not be Null!");
        }

        startBltScan(bltScanRuleConfig, bltScanAndBondAndConnectCallback);
    }

    private synchronized void startBltScan(BltScanRuleConfig bltScanRuleConfig, BltScanPresenterImp bltScanPresenterImp) {

        if (!mBltScanState.equals(BltScanState.ScanState.STATE_IDLE)) {
            BltLog.w("scan action already exists, complete the previous scan action first");
            if (bltScanPresenterImp != null) {
                bltScanPresenterImp.onScanStarted(false);
            }
            return;
        }

        mBltScanPresenter.prepare(bltScanRuleConfig, bltScanPresenterImp);

        boolean success = BltManager.getInstance().startScan();
        mBltScanState = success ? BltScanState.ScanState.STATE_SCANNING : BltScanState.ScanState.STATE_IDLE;
        mBltScanPresenter.notifyScanStarted(success);
    }

    public synchronized void stopBltScan() {
        BltManager.getInstance().stopScan();
        mBltScanState = BltScanState.ScanState.STATE_IDLE;
        mBltScanPresenter.notifyScanStopped();
    }

}
