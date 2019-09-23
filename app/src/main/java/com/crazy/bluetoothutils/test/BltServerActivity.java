package com.crazy.bluetoothutils.test;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.crazy.bluetoothutils.BltManager;
import com.crazy.bluetoothutils.R;
import com.crazy.bluetoothutils.connect.BltConnectRuleConfig;
import com.crazy.bluetoothutils.connect.BltReverseConnector;
import com.crazy.bluetoothutils.connect.bltclient.BltServerSocketClient;
import com.crazy.bluetoothutils.connect.bltclient.BltSocketClient;
import com.crazy.bluetoothutils.connect.bltserver.BltSocketServer;
import com.crazy.bluetoothutils.connect.delegate.BltSocketClientReceivingDelegate;
import com.crazy.bluetoothutils.connect.delegate.BltSocketClientSendingDelegate;
import com.crazy.bluetoothutils.connect.delegate.BltSocketServerDelegate;
import com.crazy.bluetoothutils.connect.packet.BltSocketResponsePacket;
import com.crazy.bluetoothutils.connect.packet.BltSocketSendPacket;
import com.crazy.bluetoothutils.data.BltClientSource;
import com.crazy.bluetoothutils.data.BltConfig;
import com.crazy.bluetoothutils.data.BltSeruceType;
import com.crazy.bluetoothutils.exception.BltException;
import com.crazy.bluetoothutils.permission.BluePermissionActivity;
import com.crazy.bluetoothutils.permission.BluetoothDispatherHandler;
import com.crazy.bluetoothutils.utils.BltLog;

import java.util.List;
import java.util.UUID;

import static com.crazy.bluetoothutils.permission.BluePermissionActivity.REQUEST_CODE_START_BLUE_PERMISSION_ACTIVITY;

public class BltServerActivity extends AppCompatActivity implements View.OnClickListener {

    private final String TAG = BltServerActivity.this.getClass().getSimpleName();
    String uuidStr = "00001101-0000-1000-8000-00805F9B34F";

    private EditText serverSendEt;
    private Button serverSendBt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blt_server);

        BltManager.getInstance().init(getApplication());

        serverSendEt = findViewById(R.id.et_server_send_message);
        serverSendBt = findViewById(R.id.bt_server_send);
        serverSendBt.setOnClickListener(this);
    }

    /**
     * ***********************************************************
     * **************************请求权限*************************
     * ***********************************************************
     */

    /**
     * 请求权限
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
     * ****************************服务端*************************
     * ***********************************************************
     */
    /**
     * 服务端监听
     * 连接条件不能为空，服务端name与uuid都不能相同
     */
    public void bt_listen(View view) {

        BltConnectRuleConfig bltConnectRuleConfig = new BltConnectRuleConfig.Builder()
                .setSecureType(BltSeruceType.SecureType.SECURE)
                .setName("Server")
                .setUuid(BltConfig.SPPUUID).build();

        BltReverseConnector.getInstance().beginListen(bltConnectRuleConfig, new BltSocketServerDelegate() {
            @Override
            public void onServerBeginListen(BltSocketServer socketServer) {
                BltLog.e("onServerBeginListen： " + socketServer.getBltConnectRuleConfig().toString());
            }

            @Override
            public void onServerStopListen(BltSocketServer socketServer, boolean isDisconnectAllServerClients) {
                BltLog.e("onServerStopListen： " + socketServer.getBltConnectRuleConfig().toString());
            }

            @Override
            public void onClientConnected(BltSocketServer socketServer, BltServerSocketClient socketServerClient) {
                BltLog.e("onClientConnected： " + socketServerClient.getBltConnectRuleConfig().toString());
                setServerListeningSocketServerClient(socketServerClient);
            }

            @Override
            public void onClientDisconnected(BltSocketServer socketServer, BltServerSocketClient socketServerClient) {
                BltLog.e("onClientDisconnected： " + socketServer.getBltConnectRuleConfig().toString());
            }
        });
    }


    protected BltServerActivity setServerListeningSocketServerClient(BltServerSocketClient serverListeningBltServerSocketClient) {
        if (null == serverListeningBltServerSocketClient) {
            return this;
        }
        serverListeningBltServerSocketClient.registerBltSocketClientReceiveDelegate(new BltSocketClientReceivingDelegate() {
            @Override
            public void onReceivePacketBegin(BltSocketClient client, BltSocketResponsePacket packet) {
            }

            @Override
            public void onReceivePacketEnd(BltSocketClient client, BltSocketResponsePacket packet, boolean isCallbackInMainThread) {
                BltLog.e("onReceivePacketEnd： " + client.getDeviceKey() + "  -- packet: " + new String(packet.getResponsePacket()));
            }

            @Override
            public void onReceivePacketCancel(BltSocketClient client, BltSocketResponsePacket packet, BltException bltException) {
                BltLog.e("onReceivePacketCancel： " + client.getDeviceKey());
            }

            @Override
            public void onReceivingPacketInProgress(BltSocketClient client, BltSocketResponsePacket packet, float progress, int receivedLength) {

            }
        });
        return this;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_server_send:
                bt_server_client_send();
                break;
            default:
                break;
        }
    }

    /**
     * 给每个监听到的客户端发送数据
     */
    public void bt_server_client_send() {

        List<BltSocketClient> bltSocketClientList = BltManager.getInstance().getMultipleBluetoothController().getBltSocketClientList();

        BltLog.e("sending bltSocketClientList size: " + bltSocketClientList.size());
        for (BltSocketClient bltSocketClient : bltSocketClientList) {
            if (bltSocketClient.getClientSource().equals(BltClientSource.ClientSource.SERVER) && bltSocketClient.isConnected()) {
                //此处每个Client只注册一个发送监听
                if (bltSocketClient.geBltSocketClientSendingDelegates().size() < 1) {
                    bltSocketClient.registerBltSocketClientSendingDelegate(new BltSocketClientSendingDelegate() {
                        @Override
                        public void onSendPacketBegin(BltSocketClient client, BltSocketSendPacket packet) {
                            BltLog.e("onSendPacketBegin： " + client.getDeviceKey());
                        }

                        @Override
                        public void onSendPacketEnd(BltSocketClient client, BltSocketSendPacket packet) {
                            BltLog.e("onSendPacketEnd： " + client.getDeviceKey() + "  -- send packet: " + new String(packet.getSendPacket()));
                        }

                        @Override
                        public void onSendPacketCancel(BltSocketClient client, BltSocketSendPacket packet, BltException bltException) {
                            BltLog.e("onSendPacketCancel： " + client.getDeviceKey());
                        }

                        @Override
                        public void onSendingPacketInProgress(BltSocketClient client, BltSocketSendPacket packet, float progress, int sendedLength) {

                        }
                    });
                }

                // BltManager.getInstance().getMultipleBluetoothController().getBltSocketClient(bltSocketClient.getBltDevice()).sendPacket(getBltSocketSendPacket());
                bltSocketClient.sendPacket(getBltSocketSendPacket());
            }
        }

    }

    private BltSocketSendPacket getBltSocketSendPacket() {

        String serverSendMessage = serverSendEt.getText().toString().trim();
        return new BltSocketSendPacket(serverSendMessage.getBytes());
    }


    //测试最多可以同时监听3个UUID
    public void listenMore() {

        for (int i = 6; i < 10; i++) {
            //  BltReverseConnectRuleConfig bltReverseConnectRuleConfig = new BltReverseConnectRuleConfig(BltConfig.SecureType.Secure, "Server1", BltConfig.MAIN_UUID_SECURE);
            BltConnectRuleConfig bltConnectRuleConfig = new BltConnectRuleConfig.Builder()
                    .setSecureType(BltSeruceType.SecureType.SECURE)
                    .setName("Server" + i)
                    .setUuid(UUID.fromString(uuidStr + i)).build();

            BltReverseConnector.getInstance().beginListen(bltConnectRuleConfig, new BltSocketServerDelegate() {
                @Override
                public void onServerBeginListen(BltSocketServer socketServer) {
                    BltLog.e("onServerBeginListen： " + socketServer.getBltConnectRuleConfig().toString());
                }

                @Override
                public void onServerStopListen(BltSocketServer socketServer, boolean isDisconnectAllServerClients) {
                    BltLog.e("onServerStopListen： " + socketServer.getBltConnectRuleConfig().toString());
                }

                @Override
                public void onClientConnected(BltSocketServer socketServer, BltServerSocketClient socketServerClient) {
                    BltLog.e("onClientConnected： " + socketServer.getBltConnectRuleConfig().toString());

                }

                @Override
                public void onClientDisconnected(BltSocketServer socketServer, BltServerSocketClient socketServerClient) {
                    BltLog.e("onClientDisconnected： " + socketServer.getBltConnectRuleConfig().toString());
                }
            });
        }
    }


}
