package com.crazy.bluetoothutils.scan;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;

import com.crazy.bluetoothutils.BltManager;
import com.crazy.bluetoothutils.BltMonitorManager;
import com.crazy.bluetoothutils.callback.BltScanPresenterImp;
import com.crazy.bluetoothutils.data.BltDevice;
import com.crazy.bluetoothutils.data.BltScanMode;
import com.crazy.bluetoothutils.data.BluetoothDeviceType;
import com.crazy.bluetoothutils.listener.OnBltScanListener;
import com.crazy.bluetoothutils.utils.BltLog;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Description:
 * Created by Crazy on 2018/7/27.
 */

public abstract class BltScanPresenter implements OnBltScanListener {

    public static final String scanMonitorName = "ScanMonitor";

    private static final int MSG_SCAN_DEVICE = 0X00;

    private BltScanRuleConfig mBltScanRuleConfig;

    private BltScanPresenterImp mBltScanPresenterImp;

    private List<BltDevice> mBltDeviceList = new ArrayList<>();

    private Handler mMainHandler = new Handler(Looper.getMainLooper());
    private HandlerThread mHandlerThread;
    private Handler mHandler;
    private boolean mHandling;

    public BltScanPresenterImp getBltScanPresenterImp() {
        return mBltScanPresenterImp;
    }

    public BltScanRuleConfig getBltScanRuleConfig() {
        return mBltScanRuleConfig;
    }

    private static final class ScanHandler extends Handler {

        private final WeakReference<BltScanPresenter> mBleScanPresenter;

        ScanHandler(Looper looper, BltScanPresenter bltScanPresenter) {
            super(looper);
            mBleScanPresenter = new WeakReference<>(bltScanPresenter);
        }

        @Override
        public void handleMessage(Message msg) {
            BltScanPresenter bleScanPresenter = mBleScanPresenter.get();
            if (bleScanPresenter != null) {
                if (msg.what == MSG_SCAN_DEVICE) {
                    final BltDevice bleDevice = (BltDevice) msg.obj;
                    if (bleDevice != null) {
                        bleScanPresenter.handleResult(bleDevice);
                    }
                }
            }
        }
    }

    private void handleResult(final BltDevice bltDevice) {
        mMainHandler.post(new Runnable() {
            @Override
            public void run() {
                onScanResult(bltDevice);
            }
        });
        checkDevice(bltDevice);
    }

    /**
     * 根据扫描条件判断
     */
    private void checkDevice(BltDevice bltDevice) {
        //Step1、优先根据设备类型判断
        if (null != mBltScanRuleConfig.getBluetoothDeviceTypeList()) {
            BluetoothDeviceType willJudegDevice = BltManager.getInstance().getBluetoothDeviceType(bltDevice.getDevice().getBluetoothClass());
            for (BluetoothDeviceType bluetoothDeviceType : mBltScanRuleConfig.getBluetoothDeviceTypeList()) {
                if (willJudegDevice.equals(bluetoothDeviceType)) {
                    correctDeviceAndNextStep(bltDevice);
                    return;
                }
            }
            return;
        }
        //Step2、MAC地址、Name都为空不判断
        if (TextUtils.isEmpty(mBltScanRuleConfig.getDeviceMac()) && (mBltScanRuleConfig.getDeviceNames() == null || mBltScanRuleConfig.getDeviceNames().length < 1)) {
            correctDeviceAndNextStep(bltDevice);
            return;
        }
        //Step3、根据MAC地址判断
        if (!TextUtils.isEmpty(mBltScanRuleConfig.getDeviceMac())) {
            if (!mBltScanRuleConfig.getDeviceMac().equalsIgnoreCase(bltDevice.getMac()))
                return;
        }
        //Step4、Names不为空模糊匹配
        if (mBltScanRuleConfig.getDeviceNames() != null && mBltScanRuleConfig.getDeviceNames().length > 0) {
            AtomicBoolean equal = new AtomicBoolean(false);
            for (String name : mBltScanRuleConfig.getDeviceNames()) {
                String remoteName = bltDevice.getName();
                if (remoteName == null)
                    remoteName = "";
                if (mBltScanRuleConfig.isFuzzyQuery() ? remoteName.contains(name) : remoteName.equals(name)) {
                    equal.set(true);
                }
            }
            if (!equal.get()) {
                return;
            }
        }
        //最终匹配结果
        correctDeviceAndNextStep(bltDevice);
    }

    private void correctDeviceAndNextStep(final BltDevice bltDevice) {

        BltLog.i("devices detected  ------"
                + "  name: " + bltDevice.getName()
                + "  mac: " + bltDevice.getMac()
                + "  deviceType: " + bltDevice.getDeviceType());

        switch (mBltScanRuleConfig.getScanMode()) {
            case BltScanMode.ScanMode.SCAN: {
                AtomicBoolean hasFound = new AtomicBoolean(false);
                for (BltDevice result : mBltDeviceList) {
                    if (result.getDevice().equals(bltDevice.getDevice())) {
                        hasFound.set(true);
                    }
                }
                if (!hasFound.get()) {
                    mBltDeviceList.add(bltDevice);
                    mMainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            onScanningFilter(bltDevice);
                        }
                    });
                }
                break;
            }
            case BltScanMode.ScanMode.SCAN_BOND:
            case BltScanMode.ScanMode.SCAN_BOND_CONNECT: {
                mBltDeviceList.add(bltDevice);
                mMainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        BltScanner.getInstance().stopBltScan();
                    }
                });
                break;
            }
            default:
                break;
        }
    }


    public void prepare(BltScanRuleConfig bltScanRuleConfig, BltScanPresenterImp bltScanPresenterImp) {
        this.mBltScanRuleConfig = bltScanRuleConfig;
        this.mBltScanPresenterImp = bltScanPresenterImp;

        mHandlerThread = new HandlerThread(BltScanPresenter.class.getSimpleName());
        mHandlerThread.start();
        mHandler = new ScanHandler(mHandlerThread.getLooper(), this);
        mHandling = true;

        registerBltScanReceiver();
    }

    private void registerBltScanReceiver() {
        Context context = BltManager.getInstance().getContext();
        BltMonitorManager.getInstance().registerBltMonitor(context, scanMonitorName, (OnBltScanListener) BltScanPresenter.this);
    }

    private void unregisterBltScanReceiver() {
        Context context = BltManager.getInstance().getContext();
        BltMonitorManager.getInstance().unregisterBltMonitor(context, scanMonitorName);
    }

    /**
     * 扫描
     */
    @Override
    public void onScanResult(BluetoothDevice bluetoothDevice, List<BluetoothDevice> foundDevices) {
        if (bluetoothDevice == null)
            return;

        if (!mHandling)
            return;

        Message message = mHandler.obtainMessage();
        message.what = MSG_SCAN_DEVICE;

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

        message.obj = new BltDevice(bluetoothDevice, format.format(new Date()));
        mHandler.sendMessage(message);
    }

    @Override
    public void onScanStarted() {
    }

    @Override
    public void onScanFinished() {
    }


    public final void notifyScanStarted(final boolean success) {
        mBltDeviceList.clear();

        removeHandlerMsg();

        if (success && mBltScanRuleConfig.getScanTimeout() > 0) {
            mMainHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    BltScanner.getInstance().stopBltScan();
                }
            }, mBltScanRuleConfig.getScanTimeout());
        }

        mMainHandler.post(new Runnable() {
            @Override
            public void run() {
                onScanStarted(success);
            }
        });
    }

    public final void notifyScanStopped() {
        mHandling = false;
        mHandlerThread.quit();
        removeHandlerMsg();
        mMainHandler.post(new Runnable() {
            @Override
            public void run() {
                onScanFinished(mBltDeviceList);
            }
        });
        unregisterBltScanReceiver();
    }

    public final void removeHandlerMsg() {
        mMainHandler.removeCallbacksAndMessages(null);
        mHandler.removeCallbacksAndMessages(null);
    }

    public abstract void onScanStarted(boolean success);

    public abstract void onScanResult(BltDevice bltDevice);

    public abstract void onScanningFilter(BltDevice bltDevice);

    public abstract void onScanFinished(List<BltDevice> bltDevice);
}
