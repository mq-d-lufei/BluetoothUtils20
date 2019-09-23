package com.crazy.bluetoothutils;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.text.TextUtils;

import com.crazy.bluetoothutils.listener.OnBltBondStateChangedListener;
import com.crazy.bluetoothutils.listener.OnBltScanListener;
import com.crazy.bluetoothutils.listener.OnBltScanModeChangedListener;
import com.crazy.bluetoothutils.listener.OnBltStateChangedListener;
import com.crazy.bluetoothutils.utils.BltLog;

import java.util.ArrayList;
import java.util.Set;

/***
 *  经典蓝牙监听器
 */
public class BltMonitor {

    private static final String TAG = "BltMonitor";

    private OnBltStateChangedListener onBltStateChangedListener;
    private OnBltBondStateChangedListener onBltBondStateChangedListener;
    private OnBltScanListener onBltScanListener;
    private OnBltScanModeChangedListener onBltScanModeChangedListener;

    public OnBltStateChangedListener getOnBltStateChangedListener() {
        return onBltStateChangedListener;
    }

    public void setOnBltStateChangedListener(OnBltStateChangedListener onBltStateChangedListener) {
        this.onBltStateChangedListener = onBltStateChangedListener;
    }

    public OnBltBondStateChangedListener getOnBltBondStateChangedListener() {
        return onBltBondStateChangedListener;
    }

    public void setOnBltBondStateChangedListener(OnBltBondStateChangedListener onBltBondStateChangedListener) {
        this.onBltBondStateChangedListener = onBltBondStateChangedListener;
    }

    public OnBltScanListener getOnBltScanListener() {
        return onBltScanListener;
    }

    public void setOnBltScanListener(OnBltScanListener onBltScanListener) {
        this.onBltScanListener = onBltScanListener;
    }

    public OnBltScanModeChangedListener getOnBltScanModeChangedListener() {
        return onBltScanModeChangedListener;
    }

    public void setOnBltScanModeChangedListener(OnBltScanModeChangedListener onBltScanModeChangedListener) {
        this.onBltScanModeChangedListener = onBltScanModeChangedListener;
    }

    public static BltMonitor obtainBltManager() {
        return new BltMonitor();
    }

    /**
     * 注册蓝牙状态广播
     */
    public void registerBltStateChanageReceiver(Context context) {
        IntentFilter intentFilter = new IntentFilter();
        //BluetoothAdapter:本设备的蓝牙适配器对象
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);//蓝牙状态值发生改变
        context.registerReceiver(getBltReceiver(), intentFilter);
    }

    /**
     * 注册蓝牙可检测状态广播
     */
    public void registerBltScanModeReceiver(Context context) {
        IntentFilter intentFilter = new IntentFilter();
        //BluetoothAdapter:本设备的蓝牙适配器对象
        //蓝牙设备可检测性广播，蓝牙扫描模式(SCAN_MODE)发生改变,
        intentFilter.addAction(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
        context.registerReceiver(getBltReceiver(), intentFilter);
    }

    /**
     * 注册蓝牙搜索广播
     */
    public void registerBltDiscoveryReceiver(Context context) {
        IntentFilter intentFilter = new IntentFilter();
        //BluetoothAdapter:本设备的蓝牙适配器对象
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);//蓝牙扫描过程开始
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);//蓝牙扫描过程结束
        intentFilter.addAction(BluetoothDevice.ACTION_FOUND);//搜索发现设备,扫描到任一远程蓝牙设备时，会发送此广播
        context.registerReceiver(getBltReceiver(), intentFilter);
    }

    /**
     * 注册蓝牙绑定状态广播
     */
    public void registerBltBondStateReceiver(Context context) {
        IntentFilter intentFilter = new IntentFilter();
        //BluetoothDevice
        intentFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);//蓝牙设备绑定状态改变
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            intentFilter.addAction(BluetoothDevice.ACTION_PAIRING_REQUEST);//蓝牙设备配对请求
        }
        context.registerReceiver(getBltReceiver(), intentFilter);
    }


    //@RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void registerAllBltReceiver(Context context) {
        IntentFilter intentFilter = new IntentFilter();
        //BluetoothAdapter:本设备的蓝牙适配器对象
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);//蓝牙状态值发生改变
        //设备可检测性广播
        intentFilter.addAction(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);//蓝牙扫描状态(SCAN_MODE)发生改变,//蓝牙设备可检测性
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);//蓝牙扫描过程开始
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);//蓝牙扫描过程结束
        //BluetoothDevice
        intentFilter.addAction(BluetoothDevice.ACTION_FOUND);//搜索发现设备,扫描到任一远程蓝牙设备时，会发送此广播
        intentFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);//蓝牙设备绑定状态改变
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            intentFilter.addAction(BluetoothDevice.ACTION_PAIRING_REQUEST);//蓝牙设备配对请求
        }
        context.registerReceiver(getBltReceiver(), intentFilter);
    }

    /**
     * 反注册广播取消蓝牙的配对
     */
    public void unregisterReceiver(Context context) {
        if (null != context && null != bltReceiver) {
            context.unregisterReceiver(bltReceiver);
        }
        BltManager.getInstance().stopScan();
    }


    /**
     * 蓝牙广播
     */
    private BltReceiver bltReceiver;


    public BltReceiver getBltReceiver() {
        if (null == bltReceiver) {
            bltReceiver = new BltReceiver();
        }
        return bltReceiver;
    }

    private class BltReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (TextUtils.isEmpty(action)) {
                return;
            }
            switch (action) {
                //蓝牙状态的变化
                case BluetoothAdapter.ACTION_STATE_CHANGED:
                    doBltStateChanged(intent);
                    break;
                //搜索开始
                case BluetoothAdapter.ACTION_DISCOVERY_STARTED:
                    resetFoundDevices();
                    if (null != onBltScanListener) {
                        onBltScanListener.onScanStarted();
                    }
                    break;
                //搜索设备，扫描到了任一蓝牙设备,注意，这里有可能重复搜索同一设备
                case BluetoothDevice.ACTION_FOUND:
                    doBltFound(intent);
                    break;
                //搜索结束
                case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
                    doDiscoveryFinished(intent);
                    break;
                // 请求匹配
                case BluetoothDevice.ACTION_PAIRING_REQUEST:
                    doPairingRequest(this, intent);
                    break;
                //绑定状态改变
                case BluetoothDevice.ACTION_BOND_STATE_CHANGED:
                    doBltBondStateChanged(intent);
                    break;
                //设备可检测性，可检测到模式发生变化时收到通知
                case BluetoothAdapter.ACTION_SCAN_MODE_CHANGED:
                    doBltDiscoverable(intent);
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * 设备可检测性，可检测到模式发生变化时收到通知
     * 如果您将要发起到远程设备的连接，则无需启用设备可检测性。仅当您希望您的应用托管将用于接受传入连接的服务器套接字时，才有必要启用可检测性，因为远程设备必须能够发现该设备，然后才能发起连接。
     */
    private void doBltDiscoverable(Intent intent) {
        //它将包含额外字段 EXTRA_SCAN_MODE 和 EXTRA_PREVIOUS_SCAN_MODE，二者分别告知您新的和旧的扫描模式。
        int discoverStatus = intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE, -1);
        String msg = null;
        switch (discoverStatus) {
            //可检测到模式
            case BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE:
                //  msg = "可检测到模式";
                msg = "允许周围设备检测到";
                if (null != onBltScanModeChangedListener) {
                    onBltScanModeChangedListener.onScanModeConnectableDiscoverable(msg);
                }
                break;
            //未处于可检测到模式但仍能接收连接
            case BluetoothAdapter.SCAN_MODE_CONNECTABLE:
                //  msg = "未处于可检测到模式但仍能接收连接";
                msg = "仅让已配对设备检测到";
                if (null != onBltScanModeChangedListener) {
                    onBltScanModeChangedListener.onScanModeConnectable(msg);
                }
                break;
            //未处于可检测到模式并且无法接收连接
            case BluetoothAdapter.SCAN_MODE_NONE:
                msg = "未处于可检测到模式并且无法接收连接";
                if (null != onBltScanModeChangedListener) {
                    onBltScanModeChangedListener.onScanModeNone(msg);
                }
                break;
            default:
                break;
        }
        BltLog.e(TAG, "可检测性： " + msg);
    }

    /**
     * 配对请求
     */
    //配对密码
    private String[] bleDevicePasswords = {"0000", "1234"};

    //todo 带完善...
    private void doPairingRequest(BltReceiver bltReceiver, Intent intent) {
        BltLog.e(TAG, "doPairingRequest");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            int type = intent.getIntExtra(BluetoothDevice.EXTRA_PAIRING_VARIANT, BluetoothDevice.ERROR);
            BltLog.e(TAG, "doPairingRequest type: " + type);
        }

        BluetoothDevice bluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

        if (null != onBltBondStateChangedListener) {
            onBltBondStateChangedListener.onPairingRequest(bluetoothDevice, bltReceiver);
        }

        // bltReceiver.abortBroadcast();//如果没有将广播终止，则会出现一个一闪而过的配对框。

        //if (bluetoothDevice.getBondState() == BluetoothDevice.BOND_NONE) {
        // if (null != currentPairDevice && currentPairDevice.getAddress().equalsIgnoreCase(bluetoothDevice.getAddress())) {
      /*  try {
            *//**
         * 密码根据已知设备判断
         *//*
            ClsUtils.setPin(BluetoothDevice.class, bluetoothDevice, bleDevicePasswords[0]);
        } catch (Exception e) {
            BltLog.e(TAG, "0 Password 0000 PairingRequest: error: " + e.toString());
            e.printStackTrace();
        }*/

        //currentPairDevice = null;
               /* if (foundDevices.contains(bluetoothDevice)) {
                    foundDevices.remove(bluetoothDevice);
                }*/
        // unregisterReceiver();
        // }
        //  }
    }

    /**
     * 搜索完成
     */
    private void doDiscoveryFinished(Intent intent) {
        BltLog.e(TAG, "###  BluetoothDevice.ACTION_DISCOVERY_FINISHED  ###");
        if (foundDevices.size() > 0) {

        }
        if (null != onBltScanListener) {
            onBltScanListener.onScanFinished();
        }
    }


    /**
     * 搜索到设备
     *
     * @param intent
     */
    //搜索到的未绑定的蓝牙设备
    private ArrayList<BluetoothDevice> foundDevices = new ArrayList<>();

    public ArrayList<BluetoothDevice> getFoundDevices() {
        return foundDevices;
    }

    private void resetFoundDevices() {
        if (null != foundDevices) {
            foundDevices.clear();
        }
    }

    /**
     * 注意：执行设备发现对于蓝牙适配器而言是一个非常繁重的操作过程，并且会消耗大量资源。 在找到要连接的设备后，确保始终使用 cancelDiscovery() 停止发现，然后再尝试连接。 此外，如果您已经保持与某台设备的连接，那么执行发现操作可能会大幅减少可用于该连接的带宽，因此不应该在处于连接状态时执行发现操作。
     */
    private void doBltFound(Intent intent) {
        //此 Intent 将携带额外字段 EXTRA_DEVICE 和 EXTRA_CLASS，二者分别包含 BluetoothDevice 和 BluetoothClass
        BltLog.e(TAG, "###  BluetoothDevice.ACTION_FOUND  ###");
        BluetoothDevice btDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
        //未绑定
        if (null != btDevice /*&& btDevice.getBondState() == BluetoothDevice.BOND_NONE*/) {
            BltLog.e(TAG, "Name : " + btDevice.getName() + " Address: " + btDevice.getAddress() + "  Type: " + btDevice.getBluetoothClass().getDeviceClass());
            //mac地址不相同才加入
            if (!foundDevices.contains(btDevice)) {
                foundDevices.add(btDevice);
                if (null != onBltScanListener) {
                    onBltScanListener.onScanResult(btDevice, foundDevices);
                }
            }
        }
    }

    private void doBltBondStateChanged(Intent intent) {
        BltLog.e(TAG, "###  BluetoothDevice.ACTION_BOND_STATE_CHANGED  ###");
        int curBondState = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.BOND_NONE);
        int previousBondState = intent.getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, BluetoothDevice.BOND_NONE);
        BltLog.e(TAG, "### cur_bond_state ##" + curBondState + " ~~ previous_bond_state" + previousBondState);

        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
        switch (device.getBondState()) {
            case BluetoothDevice.BOND_BONDING://正在配对
                BltLog.e("doBltBondStateChanged", device.getName() + "-正在配对......");
                if (null != onBltBondStateChangedListener) {
                    onBltBondStateChangedListener.onBondBonding(device);
                }
                break;
            case BluetoothDevice.BOND_BONDED://已经配对
                BltLog.e("doBltBondStateChanged", device.getName() + "-已配对");
                if (null != onBltBondStateChangedListener) {
                    onBltBondStateChangedListener.onBondBonded(device);
                }
                break;
            case BluetoothDevice.BOND_NONE://取消配对/未配对
                BltLog.e("doBltBondStateChanged", device.getName() + "-取消配对/配对失败");
                if (null != onBltBondStateChangedListener) {
                    boolean isBOnded = (device.getBondState() == BluetoothDevice.BOND_BONDED);
                    onBltBondStateChangedListener.onBondCancel(device, isBOnded);
                }
            default:
                break;
        }
    }


    private void doBltStateChanged(Intent intent) {
        //新的蓝牙状态
        int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1);
        //旧的蓝牙状态
        int preState = intent.getIntExtra(BluetoothAdapter.EXTRA_PREVIOUS_STATE, -1);
        String msg = null;
        switch (state) {
            case BluetoothAdapter.STATE_TURNING_ON:
                msg = "打开中";
                if (null != onBltStateChangedListener) {
                    onBltStateChangedListener.onStateTurningOn();
                }
                break;
            case BluetoothAdapter.STATE_ON:
                msg = "已打开";
                if (null != onBltStateChangedListener) {
                    onBltStateChangedListener.onStateOn();
                }
                break;
            case BluetoothAdapter.STATE_TURNING_OFF:
                msg = "关闭中";
                if (null != onBltStateChangedListener) {
                    onBltStateChangedListener.onStateTurningoff();
                }
                break;
            case BluetoothAdapter.STATE_OFF:
                msg = "已关闭";
                if (null != onBltStateChangedListener) {
                    onBltStateChangedListener.onStateOff();
                }
                break;
            default:
                break;
        }
        BltLog.e(TAG, "蓝牙状态： " + msg);
        // Toast.makeText(MyApplication.getInstance(), "蓝牙状态： " + msg, Toast.LENGTH_SHORT).show();
    }

    /**
     * 已绑定的和已搜索到的
     *
     * @return
     */
    public ArrayList<BluetoothDevice> getAllBluetooothDevice() {
        ArrayList<BluetoothDevice> allBltDevices = new ArrayList<>();
        if (null != foundDevices) {
            allBltDevices.addAll(foundDevices);
        }
        Set<BluetoothDevice> bondedDevices = BltManager.getInstance().getBondedDevices();
        if (null != bondedDevices) {
            allBltDevices.addAll(bondedDevices);
        }
        return allBltDevices;
    }

}
