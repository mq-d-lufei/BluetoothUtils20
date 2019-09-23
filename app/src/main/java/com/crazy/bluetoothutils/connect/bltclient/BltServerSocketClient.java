package com.crazy.bluetoothutils.connect.bltclient;


import com.crazy.bluetoothutils.connect.BltConnectRuleConfig;
import com.crazy.bluetoothutils.data.BltClientSource;
import com.crazy.bluetoothutils.data.BltDevice;

/**
 * Server accept client
 */

public class BltServerSocketClient extends BltSocketClient {

    public BltServerSocketClient(BltDevice mBltDevice, BltConnectRuleConfig bltConnectRuleConfig) {
        super(mBltDevice, bltConnectRuleConfig);

        setClientSource(BltClientSource.ClientSource.SERVER);
        setRunningBltSocket(mBltDevice.getSocket());

        internalOnConnected();
    }
}
