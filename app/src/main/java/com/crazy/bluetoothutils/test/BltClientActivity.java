package com.crazy.bluetoothutils.test;

import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.crazy.bluetoothutils.BltManager;
import com.crazy.bluetoothutils.BltMonitorManager;
import com.crazy.bluetoothutils.R;
import com.crazy.bluetoothutils.bond.BltBondRuleConfig;
import com.crazy.bluetoothutils.callback.BltBondCallback;
import com.crazy.bluetoothutils.callback.BltScanAndBondAndConnectCallback;
import com.crazy.bluetoothutils.callback.BltScanAndBondCallback;
import com.crazy.bluetoothutils.callback.BltScanCallback;
import com.crazy.bluetoothutils.connect.BltConnectRuleConfig;
import com.crazy.bluetoothutils.connect.bltclient.BltSocketClient;
import com.crazy.bluetoothutils.connect.delegate.BltSocketClientReceivingDelegate;
import com.crazy.bluetoothutils.connect.delegate.BltSocketClientSendingDelegate;
import com.crazy.bluetoothutils.connect.packet.BltSocketResponsePacket;
import com.crazy.bluetoothutils.connect.packet.BltSocketSendPacket;
import com.crazy.bluetoothutils.data.BltConfig;
import com.crazy.bluetoothutils.data.BltDevice;
import com.crazy.bluetoothutils.data.BltScanMode;
import com.crazy.bluetoothutils.data.BltSeruceType;
import com.crazy.bluetoothutils.data.BluetoothDeviceType;
import com.crazy.bluetoothutils.exception.BltException;
import com.crazy.bluetoothutils.listener.OnBltScanModeChangedListener;
import com.crazy.bluetoothutils.permission.BluePermissionActivity;
import com.crazy.bluetoothutils.permission.BluetoothDispatherHandler;
import com.crazy.bluetoothutils.scan.BltScanRuleConfig;
import com.crazy.bluetoothutils.utils.BltLog;
import com.crazy.bluetoothutils.utils.ClsUtils;
import com.crazy.bluetoothutils.weight.BltDialogFragment;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.crazy.bluetoothutils.permission.BluePermissionActivity.REQUEST_CODE_START_BLUE_PERMISSION_ACTIVITY;

public class BltClientActivity extends AppCompatActivity implements BltDialogFragment.OnItemClickListener, View.OnClickListener {

    private final String TAG = BltClientActivity.this.getClass().getSimpleName();
    String uuidStr = "00001101-0000-1000-8000-00805F9B34F";

    TextView receiveTv;
    EditText sendMessageEdit;
    Button sendMessageBt;
    Button scanBondConnectBt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blt_client);

        receiveTv = findViewById(R.id.tv_receive_messge);

        sendMessageEdit = findViewById(R.id.et_send_message);
        sendMessageBt = findViewById(R.id.bt_send);
        scanBondConnectBt = findViewById(R.id.bt_scan_bond_connect);

        scanBondConnectBt.setOnClickListener(this);
        sendMessageBt.setOnClickListener(this);


    }

    /**
     * ***********************************************************
     * **************************请求权限*************************
     * ***********************************************************
     */
    public void bt_request_permission(View view) {
        BluePermissionActivity.startBluePermissionActivity(this, REQUEST_CODE_START_BLUE_PERMISSION_ACTIVITY);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_START_BLUE_PERMISSION_ACTIVITY && resultCode == RESULT_OK) {
            if (null != data && null != data.getExtras()) {
                boolean isOpened = data.getExtras().getBoolean(BluetoothDispatherHandler.BLUETOOTH_OPENED, false);
                if (isOpened) {
                    onBluetoothOpened();
                }
            }
        }
    }

    public void onBluetoothOpened() {
        Toast.makeText(this, "onBluetoothOpened()", Toast.LENGTH_SHORT).show();
        Log.e(TAG, "onBluetoothOpened()");
    }

    /**
     * ***********************************************************
     * **************************可检测性*************************
     * ***********************************************************
     */
    public void bt_scan_mode(View view) {

        BltMonitorManager.getInstance().registerBltMonitor(this, "ScanModeMonitor", onBltBondStateChangedListener);

        int timeout = 10;
        ClsUtils.setDiscoverableModeTimeout(timeout);
        // BltManager.getInstance().enableDeviceDiscoverable(this, 100);

        view.getHandler().postDelayed(new Runnable() {
            @Override
            public void run() {
                BltMonitorManager.getInstance().registerBltMonitor(BltClientActivity.this, "ScanModeMonitor", onBltBondStateChangedListener);
                ClsUtils.closeDiscoverableModeTimeout();
            }
        }, 1000 * timeout);
    }

    OnBltScanModeChangedListener onBltBondStateChangedListener = new OnBltScanModeChangedListener() {

        @Override
        public void onScanModeConnectableDiscoverable(String describe) {
            BltMonitorManager.getInstance().unregisterBltMonitor(BltClientActivity.this, "ScanModeMonitor");
        }

        @Override
        public void onScanModeConnectable(String describe) {
            BltMonitorManager.getInstance().unregisterBltMonitor(BltClientActivity.this, "ScanModeMonitor");
        }

        @Override
        public void onScanModeNone(String describe) {
            BltMonitorManager.getInstance().unregisterBltMonitor(BltClientActivity.this, "ScanModeMonitor");
        }
    };

    /**
     * ***********************************************************
     * ****************************客户端*************************
     * ***********************************************************
     */

    /**
     * 仅扫描（可根据扫描条件扫描）
     */
    private String BLT_FRAGMENT_DIALOG = "blt_fragment_dialog";
    private BltDialogFragment bltDialogFragment;

    public void bt_scan(View view) {
        bltDialogFragment = BltDialogFragment.newInstance();

        //设备类型判断
        //ArrayList<BluetoothDeviceType> bluetoothDeviceTypeList = new ArrayList<>();
        //bluetoothDeviceTypeList.add(new BluetoothDeviceType().setServiceClass(5898240));

        BltScanRuleConfig scanConfig = new BltScanRuleConfig.Builder()
                .setScanMode(BltScanMode.ScanMode.SCAN)
                //.setBluetoothDeviceTypeList(bluetoothDeviceTypeList)
                //.setDeviceNames(new String[]{"HUAWEI"})
                .build();

        BltManager.getInstance().scan(scanConfig, new BltScanCallback() {
            @Override
            public void onScanFinished(List<BltDevice> scanResultList) {
                BltLog.e("onScanFinished...");
            }

            @Override
            public void onScanResult(BltDevice bltDevice) {
                BltLog.e("onScanResult..." + bltDevice.toString());
                BltManager.getInstance().getBluetoothDeviceServiceClass(bltDevice.getDevice().getBluetoothClass());
            }

            @Override
            public void onScanStarted(boolean success) {
                BltLog.e("onScanStarted..." + success);
            }

            @Override
            public void onScanningFilter(BltDevice bltDevice) {
                BltLog.e("onScanningFilter..." + bltDevice.toString());
                if (!bltDialogFragment.isAdded()) {
                    BltLog.e("isAdded..." + bltDialogFragment.isAdded());
                    bltDialogFragment.showNow(getSupportFragmentManager(), BLT_FRAGMENT_DIALOG);
                    BltLog.e("isAdded..." + bltDialogFragment.isAdded());
                }
                bltDialogFragment.addBltDevice(bltDevice);
            }

        });
    }

    @Override
    public void onBltDialogSelected(BltDevice bltDevice) {
        Toast.makeText(this, "onDekaDialogSelected(): " + bltDevice.getName(), Toast.LENGTH_SHORT).show();
        bltDialogFragment.dismiss();
    }

    /**
     * 扫码绑定（可根据扫描条件扫描、绑定条件是否自动绑定）
     * 注意：
     * 必须指定扫描类型
     * 必须指定扫描规则与配对规则，一旦扫描到设备就停止扫描并绑定设备
     * 如果是自动绑定(配对)，将isAutoPaired设置为true，并指定pairPassword
     */
    public void bt_scan_bond(View view) {
        BltBondRuleConfig bltBondRuleConfig = new BltBondRuleConfig();
        bltBondRuleConfig.setAutoPaired(false);

        BltScanRuleConfig scanBondConfig = new BltScanRuleConfig.Builder()
                .setScanMode(BltScanMode.ScanMode.SCAN_BOND)
                .setBltBondRuleConfig(bltBondRuleConfig)
                .setDeviceNames(new String[]{"HUAWEI"}).build();

        final AtomicBoolean isBondSuccess = new AtomicBoolean(false);

        BltManager.getInstance().scanAndBond(scanBondConfig, new BltScanAndBondCallback() {

            @Override
            public void onBondBonding(BluetoothDevice device) {
                BltLog.e("onBondBonding...");
            }

            @Override
            public void onBondBonded(BluetoothDevice device) {
                BltLog.e("onBondBonded...");
                isBondSuccess.set(true);
            }

            @Override
            public void onBondCancel(BluetoothDevice device, boolean isBonded) {
                BltLog.e("onBondCancel...: " + isBonded);
            }

            @Override
            public void onScanStarted(boolean success) {
                BltLog.e("onScanStarted...");
            }

            @Override
            public void onScanningFilter(BltDevice bltDevice) {
                BltLog.e("onScanningFilter...");
            }

            @Override
            public void onScanFinished(BltDevice scanResult) {
                BltLog.e("onScanFinished...");

                if (null == scanResult) {
                    onBondFailure();
                }

            }

            private void onBondFailure() {
                BltLog.e("onBondFailure...");
            }
        });

    }

    /**
     * 扫码绑定连接（可根据扫描条件扫描、绑定条件是否自动绑定、连接条件）
     */
    private BltSocketClient connectedClient;

    public BltSocketClient getConnectedClient() {
        return connectedClient;
    }

    public void bt_scan_bond_connect() {
        BltBondRuleConfig bltBondRuleConfig = new BltBondRuleConfig();
        bltBondRuleConfig.setAutoPaired(false);

        BltConnectRuleConfig bltConnectRuleConfig = new BltConnectRuleConfig.Builder()
                .setSecureType(BltSeruceType.SecureType.SECURE)
                .setUuid(BltConfig.SPPUUID)
                .build();

        BltScanRuleConfig scanBondConnectConfig = new BltScanRuleConfig.Builder()
                .setScanMode(BltScanMode.ScanMode.SCAN_BOND_CONNECT)
                //.setDeviceNames(new String[]{"PLK-AL10"})
                .setDeviceNames(new String[]{"Lenovo S856"})
                // .setDeviceNames(new String[]{"HUAWEI"})
                .setBltBondRuleConfig(bltBondRuleConfig)
                .setBltConnectRuleConfig(bltConnectRuleConfig)
                .build();

        BltManager.getInstance().scanAndBondAndConnect(scanBondConnectConfig, new BltScanAndBondAndConnectCallback() {
            @Override
            public void onScanFinished(BltDevice scanResult) {
                BltLog.e("onScanFinished...");
            }

            @Override
            public void onBondBonding(BluetoothDevice device) {
                BltLog.e("onBondBonding...");
            }

            @Override
            public void onBondBonded(BluetoothDevice device) {
                BltLog.e("onBondBonded...");
            }

            @Override
            public void onBondCancel(BluetoothDevice device, boolean isBonded) {
                BltLog.e("onBondCancel...");
            }

            @Override
            public void onScanStarted(boolean success) {
                BltLog.e("onScanStarted...");
            }

            @Override
            public void onScanningFilter(BltDevice bltDevice) {
                BltLog.e("onScanningFilter...");
            }

            @Override
            public void onConnected(BltSocketClient client) {
                BltLog.e("onConnected...");
                connectedClient = client;

                //注册接收监听
                client.registerBltSocketClientReceiveDelegate(bltSocketClientReceivingDelegate);
            }

            @Override
            public void onConnectFail(BltSocketClient client, BltException bltException) {
                BltLog.e("onConnectFail...");
            }

            @Override
            public void onDisconnected(BltSocketClient client, boolean isReconnect, BltException bltException) {
                BltLog.e("onDisconnected...");
            }

            @Override
            public void onResponse(BltSocketClient client, @NonNull BltSocketResponsePacket responsePacket, boolean isCallbackInMainThread) {
                BltLog.e("onResponse...response: " + new String(responsePacket.getResponsePacket(), Charset.forName("UTF-8")));
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_send:
                bt_client_send();
                break;
            case R.id.bt_scan_bond_connect:
                bt_scan_bond_connect();
                break;
            default:
                break;
        }
    }

    /**
     * 发送数据
     */
    public void bt_client_send() {

        String sendMessage = sendMessageEdit.getText().toString().trim();
        if (null != getConnectedClient() && !TextUtils.isEmpty(sendMessage)) {
            BltSocketSendPacket bltSocketSendPacket = new BltSocketSendPacket().setSendPacket(sendMessage.getBytes());
            BltManager.getInstance().write(getConnectedClient().getBltDevice(), bltSocketSendPacket, bltSocketClientSendingDelegate);
            //receiveMessage();
        }
    }

    BltSocketClientSendingDelegate bltSocketClientSendingDelegate = new BltSocketClientSendingDelegate() {
        @Override
        public void onSendPacketBegin(BltSocketClient client, BltSocketSendPacket packet) {
            BltLog.e("onSendPacketBegin...");
        }

        @Override
        public void onSendPacketEnd(BltSocketClient client, BltSocketSendPacket packet) {
            BltLog.e("onSendPacketEnd...packet: " + new String(packet.getSendPacket()));
        }

        @Override
        public void onSendPacketCancel(BltSocketClient client, BltSocketSendPacket packet, BltException bltException) {
            BltLog.e("onSendPacketCancel...");
        }

        @Override
        public void onSendingPacketInProgress(BltSocketClient client, BltSocketSendPacket packet, float progress, int sendedLength) {
            BltLog.e("onSendingPacketInProgress...");
        }
    };


    BltSocketClientReceivingDelegate bltSocketClientReceivingDelegate = new BltSocketClientReceivingDelegate() {
        @Override
        public void onReceivePacketBegin(BltSocketClient client, BltSocketResponsePacket packet) {
            BltLog.e("onReceivePacketBegin...");
        }

        @Override
        public void onReceivePacketEnd(final BltSocketClient client, final BltSocketResponsePacket packet, boolean isCallbackInMainThread) {
            if (!isCallbackInMainThread) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        onReceivePacketEnd(client, packet, true);
                    }
                });
            }
            String receiveData = new String(packet.getResponsePacket());
            BltLog.e("onReceivePacketEnd...packet: " + receiveData);
            String text = receiveTv.getText().toString();
            text = text + " *** " + receiveData;
            receiveTv.setText(text);
        }

        @Override
        public void onReceivePacketCancel(BltSocketClient client, BltSocketResponsePacket packet, BltException bltException) {
            BltLog.e("onReceivePacketCancel...");
        }

        @Override
        public void onReceivingPacketInProgress(BltSocketClient client, BltSocketResponsePacket packet, float progress, int receivedLength) {
            BltLog.e("onReceivingPacketInProgress...");
        }
    };

    private boolean isRead = false;

    //方式二：注册接收监听
    public void receiveMessage() {
        if (null != getConnectedClient() && getConnectedClient().isConnected() && !isRead) {

            BltManager.getInstance().read(getConnectedClient().getBltDevice(), bltSocketClientReceivingDelegate);

            isRead = true;
        }
    }
    /**
     * 发送数据
     */

    /**
     * 仅绑定（可根据扫描条件扫描）
     */
    public void bondOnly(View view) {
        final String DEKA_MAC_ADDRESS = "88:1B:99:20:81:79";
        //String macStr_HUAWEI = "F0:C8:50:C7:1D:8C";
        BltManager.getInstance().bond(DEKA_MAC_ADDRESS, new BltBondCallback() {
            @Override
            public void onBondBonding(BluetoothDevice device) {
                Toast.makeText(getApplication(), "onBondBonding()", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onBondBonded(BluetoothDevice device) {
                Toast.makeText(getApplication(), "onBondBonded()", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onBondCancel(BluetoothDevice device, boolean isBonded) {
                Toast.makeText(getApplication(), "onBondCancel()", Toast.LENGTH_SHORT).show();
            }
        });
    }


    public void onGetBluetoothDeviceList(View view) {
        ArrayList<BluetoothDeviceType> bluetoothDeviceTypeList = BltManager.getInstance().getBluetoothDeviceTypeList();
        if (null != bluetoothDeviceTypeList) {
            BltLog.e(bluetoothDeviceTypeList.toString());
        }
    }
}
