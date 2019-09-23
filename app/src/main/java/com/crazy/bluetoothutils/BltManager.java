package com.crazy.bluetoothutils;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresPermission;
import android.widget.Toast;

import com.crazy.bluetoothutils.bond.BltBonder;
import com.crazy.bluetoothutils.callback.BltBondCallback;
import com.crazy.bluetoothutils.callback.BltScanAndBondAndConnectCallback;
import com.crazy.bluetoothutils.callback.BltScanAndBondCallback;
import com.crazy.bluetoothutils.callback.BltScanCallback;
import com.crazy.bluetoothutils.connect.BltConnectRuleConfig;
import com.crazy.bluetoothutils.connect.BltConnector;
import com.crazy.bluetoothutils.connect.BltReverseConnector;
import com.crazy.bluetoothutils.connect.MultipleBluetoothController;
import com.crazy.bluetoothutils.connect.bltclient.BltSocketClient;
import com.crazy.bluetoothutils.connect.delegate.BltSocketClientDelegate;
import com.crazy.bluetoothutils.connect.delegate.BltSocketClientReceivingDelegate;
import com.crazy.bluetoothutils.connect.delegate.BltSocketClientSendingDelegate;
import com.crazy.bluetoothutils.connect.delegate.BltSocketServerDelegate;
import com.crazy.bluetoothutils.connect.packet.BltSocketSendPacket;
import com.crazy.bluetoothutils.data.BltDevice;
import com.crazy.bluetoothutils.data.BluetoothDeviceType;
import com.crazy.bluetoothutils.scan.BltScanRuleConfig;
import com.crazy.bluetoothutils.scan.BltScanner;
import com.crazy.bluetoothutils.utils.BltLog;
import com.crazy.bluetoothutils.utils.ClsUtils;
import com.crazy.bluetoothutils.xml.PullBluetoothDeviceParser;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Set;

import static android.content.ContentValues.TAG;


/**
 * 蓝牙管理器
 */

public class BltManager implements IBltManager {

    private Application context;
    //蓝牙适配器
    private BluetoothAdapter mBluetoothAdapter;
    //蓝牙设备连接管理器
    private MultipleBluetoothController multipleBluetoothController;
    //XML中已知的设备列表
    private ArrayList<BluetoothDeviceType> bluetoothDeviceTypeList;

    private BltManager() {
    }

    private static class HolderClass {
        private static final BltManager BLT_MANAGER = new BltManager();
    }

    public static BltManager getInstance() {
        return HolderClass.BLT_MANAGER;
    }


    /**
     * 初始化
     */
    public void init(Application app) {
        if (null == context && null != app) {
            context = app;
        }
        multipleBluetoothController = new MultipleBluetoothController();
    }


    public Context getContext() {
        return context;
    }

    public MultipleBluetoothController getMultipleBluetoothController() {
        return multipleBluetoothController;
    }

    public BluetoothAdapter getBluetoothAdapter() {

        if (null == mBluetoothAdapter) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                final BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
                if (bluetoothManager != null) {
                    mBluetoothAdapter = bluetoothManager.getAdapter();
                }
            } else {
                mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            }
        }
        return mBluetoothAdapter;
    }

    /**
     * 是否支持蓝牙
     * <p>
     * null if Bluetooth is not supported on this hardware platform
     */
    @Override
    public boolean isSupportBluetooth() {
        return null != getBluetoothAdapter();
    }

    /**
     * 是否支持蓝牙4.0
     */
    @Override
    public boolean isSupportBLE() {
        // 检查当前手机是否支持ble 蓝牙,如果不支持退出程序
        if (!context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(context, "ble_not_supported", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }


    /**
     * 蓝牙是否打开
     * <p>
     * true if the local adapter is turned on
     */
    @RequiresPermission(Manifest.permission.BLUETOOTH)
    @Override
    public boolean isBluetoothOpen() {
        return getBluetoothAdapter().isEnabled();
    }

    public boolean isBluetoothEnable() {
        if (!isSupportBluetooth()) {
            BltLog.e(TAG, "null , Bluetooth is not supported on this hardware platform");
            return false;
        }
        if (!isBluetoothOpen()) {
            BltLog.e(TAG, "true if the local adapter is turned on");
            return false;
        }
        return true;
    }

    public static final int Bluetooth_Openeing = 0;
    public static final int Bluetooth_Opened = 1;
    public static final int Bluetooth_No_Support = -1;

    /**
     * 跳转开启蓝牙
     *
     * @param context
     * @return 是否跳转
     */
    @RequiresPermission(Manifest.permission.BLUETOOTH)
    @Override
    public int enableBluetooth(Context context) {
        synchronized (BltMonitor.class) {
            if (!isSupportBluetooth()) {
                BltLog.e(TAG, "null , Bluetooth is not supported on this hardware platform");
                return Bluetooth_No_Support;
            }
            if (isBluetoothOpen()) {
                BltLog.e(TAG, "true if the local adapter is turned on");
                return Bluetooth_Opened;
            }
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            context.startActivity(intent);
            return Bluetooth_Openeing;
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH)
    @Override
    public int enableBluetooth(Context context, int requestCode) {
        synchronized (BltMonitor.class) {
            if (!isSupportBluetooth()) {
                BltLog.e(TAG, "null , Bluetooth is not supported on this hardware platform");
                Toast.makeText(context, "Bluetooth is not supported on this hardware platform", Toast.LENGTH_SHORT).show();
                return Bluetooth_No_Support;
            }
            if (isBluetoothOpen()) {
                BltLog.e(TAG, "true , the local adapter is turned on");
                //  Toast.makeText(context, "the local adapter is turned on", Toast.LENGTH_SHORT).show();
                return Bluetooth_Opened;
            }
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            ((Activity) context).startActivityForResult(intent, requestCode);
            return Bluetooth_Openeing;
        }
    }

    /**
     * 启用设备可检测性
     * 默认情况下，设备将变为可检测到并持续 120 秒钟,
     * 最大持续时间为 3600 秒，值为 0 则表示设备始终可检测到。 任何小于 0 或大于 3600 的值都会自动设为 120 秒
     */
    public void enableDeviceDiscoverable(Context context, int duration) {
        Intent discoverableIntent = new
                Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        //discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, duration);
        context.startActivity(discoverableIntent);
    }

    /**
     * 关闭蓝牙
     */
    @RequiresPermission(allOf = {Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.BLUETOOTH})
    @Override
    public boolean disableBluetooth() {
        BluetoothAdapter bluetoothAdapter = getBluetoothAdapter();
        if (bluetoothAdapter.isEnabled()) {
            return bluetoothAdapter.disable();
        } else {
            BltLog.e(TAG, "false. your device has been turn off Bluetooth.");
            return false;
        }
    }

    /**
     * 开始扫描，查找新设备
     */
    @RequiresPermission(Manifest.permission.BLUETOOTH)
    public boolean startScan() {
        boolean isStartScan;
        stopScan();
        BluetoothAdapter bluetoothAdapter = getBluetoothAdapter();
        isStartScan = bluetoothAdapter.startDiscovery();
        BltLog.e("bluetooth", "开始扫描...isStartScan: " + isStartScan);
        return isStartScan;
    }

    /**
     * 停止扫描
     */
    public boolean stopScan() {
        boolean isStopScan = false;
        BluetoothAdapter bluetoothAdapter = getBluetoothAdapter();
        if (bluetoothAdapter.isDiscovering()) {
            isStopScan = bluetoothAdapter.cancelDiscovery();
            BltLog.e("bluetooth", "取消扫描...");
        }
        return isStopScan;
    }

    /**
     * 绑定远程蓝牙设备
     *
     * @param bluetoothDevice
     * @return
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    public boolean bondDevice(BluetoothDevice bluetoothDevice) {
        return bluetoothDevice.createBond();
    }

    /**
     * 反射配对
     *
     * @param device
     * @return false: 未执行配对  true: 执行配对
     */
    public boolean bondDeviceWithReflectPair(BluetoothDevice device) {
        if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
            return false;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            try {
                // 调用配对的方法，此方法是异步的，系统会触发BluetoothDevice.ACTION_PAIRING_REQUEST的广播
                // 收到此广播后，设置配对的密码
                ClsUtils.createBond(BluetoothDevice.class, device);
                BltLog.e(TAG, "doNormalPair: WithReflectPair");
            } catch (Exception e) {
                BltLog.e(TAG, "doNormalPair: error: " + e.toString());
                e.printStackTrace();
                return false;
            }
        } else {
            BltManager.getInstance().bondDevice(device);
            BltLog.e(TAG, "doNormalPair: With normal boud");
        }
        return true;
    }

    //反射来调用BluetoothDevice.removeBond取消设备的配对
    public boolean unpairDevice(BluetoothDevice device) {
        try {
            Method m = device.getClass()
                    .getMethod("removeBond", (Class[]) null);
            return (boolean) m.invoke(device, (Object[]) null);
        } catch (Exception e) {
            BltLog.e(TAG, e.getMessage());
        }
        return false;
    }

    /**
     * 已绑定的设备
     * 这个要在打开蓝牙的情况下才可以，不然为空集
     */
    public Set<BluetoothDevice> getBondedDevices() {
        BluetoothAdapter bluetoothAdapter = getBluetoothAdapter();
        Set<BluetoothDevice> bondedDevices = bluetoothAdapter.getBondedDevices();
        return bondedDevices;
    }

    /**
     * 日志打印控制
     */
    public BltManager enableLog(boolean enable) {
        BltLog.isPrint = enable;
        return this;
    }

    /**
     * Scan
     */
    public void scan(@NonNull BltScanRuleConfig bltScanRuleConfig, @NonNull final BltScanCallback bltScanCallback) {
        BltScanner.getInstance().scan(bltScanRuleConfig, bltScanCallback);
    }

    public void scanAndBond(@NonNull BltScanRuleConfig bltScanRuleConfig, @NonNull final BltScanAndBondCallback bltScanAndBondCallback) {
        BltScanner.getInstance().scanAndBond(bltScanRuleConfig, bltScanAndBondCallback);
    }

    public void scanAndBondAndConnect(@NonNull BltScanRuleConfig bltScanRuleConfig, @NonNull final BltScanAndBondAndConnectCallback bltScanAndBondAndConnectCallback) {
        BltScanner.getInstance().scanAndBondAndConnect(bltScanRuleConfig, bltScanAndBondAndConnectCallback);
    }

    /**
     * Bond
     */
    public void bond(@NonNull String bltMacStr, @NonNull BltBondCallback bltBondCallback) {
        BltBonder.getInstance().bond(bltMacStr, bltBondCallback);
    }

    public void bond(@NonNull BluetoothDevice bluetoothDevice, @NonNull BltBondCallback bltBondCallback) {
        BltBonder.getInstance().bond(bluetoothDevice, bltBondCallback);
    }

    public void bond(@NonNull BltDevice bltDevice, @NonNull BltBondCallback bltBondCallback) {
        BltBonder.getInstance().bond(bltDevice, bltBondCallback);
    }

    public void bondAuto(@NonNull BltDevice bltDevice, @NonNull BltScanRuleConfig bltScanRuleConfig, @NonNull BltScanAndBondCallback bltScanAndBondCallback) {
        BltBonder.getInstance().autoBond(bltDevice, bltScanRuleConfig, bltScanAndBondCallback);
    }

    /**
     * Connect
     */
    public void connect(@NonNull String bltMacStr, @NonNull BltConnectRuleConfig bltConnectRuleConfig, @NonNull BltSocketClientDelegate bltSocketClientDelegate) {
        BltConnector.getInstance().connect(bltMacStr, bltConnectRuleConfig, bltSocketClientDelegate);
    }

    public void connect(@NonNull BluetoothDevice bluetoothDevice, @NonNull BltConnectRuleConfig bltConnectRuleConfig, @NonNull BltSocketClientDelegate bltSocketClientDelegate) {
        BltConnector.getInstance().connect(bluetoothDevice, bltConnectRuleConfig, bltSocketClientDelegate);
    }

    public void connect(@NonNull BltDevice bltDevice, @NonNull BltConnectRuleConfig bltConnectRuleConfig, @NonNull BltSocketClientDelegate bltSocketClientDelegate) {
        BltConnector.getInstance().connect(bltDevice, bltConnectRuleConfig, bltSocketClientDelegate);
    }

    public void connect(@NonNull BltDevice bltDevice, @NonNull BltConnectRuleConfig bltConnectRuleConfig, @NonNull BltSocketClientDelegate bltSocketClientDelegate, @NonNull BltSocketClientSendingDelegate bltSocketClientSendingDelegate, @NonNull BltSocketClientReceivingDelegate bltSocketClientReceivingDelegate) {
        BltConnector.getInstance().connect(bltDevice, bltConnectRuleConfig, bltSocketClientDelegate, bltSocketClientSendingDelegate, bltSocketClientReceivingDelegate);
    }

    /**
     * Disconnect
     */
    public void disconnect(@NonNull BltDevice bltDevice, boolean isReconnect) {
        BltConnector.getInstance().disconnect(bltDevice, isReconnect);
    }

    /**
     * Register SendingDelegate ReceiveDelegate
     */
    public void registerBltSocketClientSendingDelegate(@NonNull BltDevice bltDevice, @NonNull BltSocketClientSendingDelegate bltSocketClientSendingDelegate) {
        BltConnector.getInstance().registerBltSocketClientSendingDelegate(bltDevice, bltSocketClientSendingDelegate);
    }

    public void registerBltSocketClientReceiveDelegate(@NonNull BltDevice bltDevice, @NonNull BltSocketClientReceivingDelegate bltSocketClientReceivingDelegate) {
        BltConnector.getInstance().registerBltSocketClientReceiveDelegate(bltDevice, bltSocketClientReceivingDelegate);
    }

    /**
     * Begin Listen
     */
    public boolean beginListen(@NonNull BltConnectRuleConfig bltConnectRuleConfig, BltSocketServerDelegate bltSocketServerDelegate) {
        return BltReverseConnector.getInstance().beginListen(bltConnectRuleConfig, bltSocketServerDelegate);
    }

    /**
     * Stop Listen
     */
    public void stopListen(@NonNull BltConnectRuleConfig bltConnectRuleConfig) {
        BltReverseConnector.getInstance().stopListen(bltConnectRuleConfig);
    }

    public void stopListen(@NonNull BltConnectRuleConfig bltConnectRuleConfig, boolean isDisconnectAllServerClients) {
        BltReverseConnector.getInstance().stopListen(bltConnectRuleConfig, isDisconnectAllServerClients);
    }

    public void stopAllListen() {
        BltReverseConnector.getInstance().stopAllListen();
    }

    /**
     * Read
     */
    public void read(@NonNull BltDevice bltDevice, BltSocketClientReceivingDelegate bltSocketClientReceivingDelegate) {
        BltSocketClient bltSocketClient = multipleBluetoothController.getBltSocketClient(bltDevice);
        if (null != bltSocketClient) {
            bltSocketClient.registerBltSocketClientReceiveDelegate(bltSocketClientReceivingDelegate);
        }
    }

    /**
     * Write
     */
    public void write(@NonNull BltDevice bltDevice, BltSocketSendPacket packet) {
        BltSocketClient bltSocketClient = multipleBluetoothController.getBltSocketClient(bltDevice);
        if (null != bltSocketClient) {
            bltSocketClient.sendPacket(packet);
        }
    }

    public void write(@NonNull BltDevice bltDevice, BltSocketSendPacket packet, BltSocketClientSendingDelegate bltSocketClientSendingDelegate) {
        BltSocketClient bltSocketClient = multipleBluetoothController.getBltSocketClient(bltDevice);
        if (null != bltSocketClient) {
            bltSocketClient.registerBltSocketClientSendingDelegate(bltSocketClientSendingDelegate);
            bltSocketClient.sendPacket(packet);
        }
    }


    /**
     * 获取已知设备类型
     */
    public ArrayList<BluetoothDeviceType> getBluetoothDeviceTypeList() {
        if (null == bluetoothDeviceTypeList || bluetoothDeviceTypeList.size() == 0) {
            try {
                InputStream openIs = context.getAssets().open("BluetoothDevice");
                PullBluetoothDeviceParser pullBluetoothDeviceParser = new PullBluetoothDeviceParser();
                bluetoothDeviceTypeList = pullBluetoothDeviceParser.parse(openIs);
                BltLog.e("bluetoothDeviceDaoList size: " + ((null != bluetoothDeviceTypeList) ? bluetoothDeviceTypeList.size() : 0));
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return bluetoothDeviceTypeList;
    }

    /**
     * 获取设备类型
     */
    public BluetoothDeviceType getBluetoothDeviceType(BluetoothClass btClass) {
        int serviceCls = 0;
        if (null == btClass) {
            return getNoneTypeDevice(serviceCls);
        }

        serviceCls = btClass.hashCode() & 0xFFE000;
        BltLog.e("serviceClass == " + serviceCls + "  btClass.hashCode():" + btClass.hashCode());

        if (btClass.getMajorDeviceClass() == BluetoothClass.Device.Major.UNCATEGORIZED) {
            //BluetoothClass.Service.BITMASK=0xFFE000
            ArrayList<BluetoothDeviceType> bluetoothDeviceTypeList = getBluetoothDeviceTypeList();

            for (BluetoothDeviceType bluetoothDeviceType : bluetoothDeviceTypeList) {
                if (serviceCls == bluetoothDeviceType.getServiceClass()) {
                    return bluetoothDeviceType;
                }
            }
        }
        return getNoneTypeDevice(serviceCls);
    }

    //未知设备
    private BluetoothDeviceType getNoneTypeDevice(int serviceCls) {
        return new BluetoothDeviceType(serviceCls, "GROUP_NONE", "TYPE_NONE_00", "未知设备");
    }

    //获取ServiceClass
    public int getBluetoothDeviceServiceClass(BluetoothClass btClass) {
        //BluetoothClass.Service.BITMASK=0xFFE000
        BltLog.e("getServiceClass(): bltClass.hashCode: " + btClass.hashCode() + "  --serviceClass: " + (btClass.hashCode() & 0xFFE000));
        return btClass.hashCode() & 0xFFE000;
    }

}
