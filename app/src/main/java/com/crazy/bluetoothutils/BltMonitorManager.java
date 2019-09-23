package com.crazy.bluetoothutils;

import android.content.Context;

import com.crazy.bluetoothutils.listener.OnBltBondStateChangedListener;
import com.crazy.bluetoothutils.listener.OnBltListener;
import com.crazy.bluetoothutils.listener.OnBltScanListener;
import com.crazy.bluetoothutils.listener.OnBltScanModeChangedListener;
import com.crazy.bluetoothutils.listener.OnBltStateChangedListener;
import com.crazy.bluetoothutils.utils.BltLog;

import java.util.HashMap;
import java.util.Map;

/**
 * 经典蓝牙监听管理器
 */

public class BltMonitorManager {

    private Map<String, BltMonitor> mBltMonitorMap;

    private BltMonitorManager() {
        mBltMonitorMap = new HashMap<>();
    }

    private final static class HolderClass {
        private final static BltMonitorManager INSTANCE = new BltMonitorManager();
    }

    public static BltMonitorManager getInstance() {
        return HolderClass.INSTANCE;
    }

    /**
     * 一个Context可以有多个BltMonitor
     *
     * @param context        上下文
     * @param monitorName    名字标识
     * @param onBltListeners 不同监听
     */
    public void registerBltMonitor(Context context, final String monitorName, OnBltListener... onBltListeners) {
        if (null == context) {
            BltLog.e("registerBltMonitor failure");
            return;
        }

        BltMonitor existBltMonitor = mBltMonitorMap.get(monitorName);

        BltMonitor bltMonitor = null != existBltMonitor ? existBltMonitor : BltMonitor.obtainBltManager();

        for (OnBltListener onBltListener : onBltListeners) {

           /* OnBltStateChangedListener onBltStateChangedListener1  = (OnBltStateChangedListener)  onBltListener;
            OnBltScanListener onBltStateChangedListener2  = (OnBltScanListener)  onBltListener;
            OnBltBondStateChangedListener onBltStateChangedListener3  = (OnBltBondStateChangedListener)  onBltListener;*/

            try {
                OnBltStateChangedListener onBltStateChangedListenerTest = (OnBltStateChangedListener) onBltListener;
                //已注册就不重复注册
                OnBltStateChangedListener onBltStateChangedListener = bltMonitor.getOnBltStateChangedListener();
                if (null != onBltStateChangedListener && onBltStateChangedListener.equals(onBltListener)) {
                    continue;
                }
                bltMonitor.setOnBltStateChangedListener((OnBltStateChangedListener) onBltListener);
                bltMonitor.registerBltStateChanageReceiver(context);
            } catch (ClassCastException e) {

            }

            try {
                OnBltScanListener onBltScanListenerTest = (OnBltScanListener) onBltListener;
                //已注册就不重复注册
                OnBltScanListener onBltScanListener = bltMonitor.getOnBltScanListener();
                if (null != onBltScanListener && onBltScanListener.equals(onBltListener)) {
                    continue;
                }

                bltMonitor.setOnBltScanListener((OnBltScanListener) onBltListener);
                bltMonitor.registerBltDiscoveryReceiver(context);
            } catch (ClassCastException e) {

            }

            try {
                OnBltBondStateChangedListener onBltBondStateChangedListenerTest = (OnBltBondStateChangedListener) onBltListener;
                //已注册就不重复注册
                OnBltBondStateChangedListener onBltBondStateChangedListener = bltMonitor.getOnBltBondStateChangedListener();
                if (null != onBltBondStateChangedListener && onBltBondStateChangedListener.equals(onBltListener)) {
                    continue;
                }

                bltMonitor.setOnBltBondStateChangedListener((OnBltBondStateChangedListener) onBltListener);
                bltMonitor.registerBltBondStateReceiver(context);
            } catch (ClassCastException e) {

            }


            try {
                OnBltScanModeChangedListener onBltScanModeChangedListenerTest = (OnBltScanModeChangedListener) onBltListener;
                //已注册就不重复注册
                OnBltScanModeChangedListener onBltScanModeChangedListener = bltMonitor.getOnBltScanModeChangedListener();
                if (null != onBltScanModeChangedListener && onBltScanModeChangedListener.equals(onBltListener)) {
                    continue;
                }

                bltMonitor.setOnBltScanModeChangedListener((OnBltScanModeChangedListener) onBltListener);
                bltMonitor.registerBltScanModeReceiver(context);
            } catch (ClassCastException e) {

            }
        }

        mBltMonitorMap.put(monitorName, bltMonitor);
    }

    /**
     * 解绑监听，一个Context可以有多个BltMonitor
     *
     * @param context     上下文
     * @param monitorName 名字标识
     */
    public void unregisterBltMonitor(Context context, final String... monitorName) {
        if (null != context) {
            for (String name : monitorName) {
                BltMonitor bltMonitor = mBltMonitorMap.get(name);
                if (null != bltMonitor) {
                    bltMonitor.unregisterReceiver(context);
                    mBltMonitorMap.remove(name);
                }
            }
        }
    }

}
