package com.crazy.bluetoothutils.connect.delegate;

import android.support.annotation.NonNull;

import com.crazy.bluetoothutils.connect.bltclient.BltSocketClient;
import com.crazy.bluetoothutils.connect.packet.BltSocketResponsePacket;
import com.crazy.bluetoothutils.exception.BltException;


/**
 * Socekt 连接状态管理代理类
 */

public interface BltSocketClientDelegate {
    /**
     * Socket已连接
     */
    void onConnected(BltSocketClient client);

    /**
     * Socket连接失败
     */
    void onConnectFail(BltSocketClient client, BltException bltException);

    /**
     * Socket未连接
     */
    void onDisconnected(BltSocketClient client, boolean isReconnect, BltException bltException);

    /**
     * Socket处理数据
     *
     * @param client         Socket管理类
     * @param responsePacket 响应包
     */
    void onResponse(BltSocketClient client, @NonNull BltSocketResponsePacket responsePacket, boolean isCallbackInMainThread);

}
