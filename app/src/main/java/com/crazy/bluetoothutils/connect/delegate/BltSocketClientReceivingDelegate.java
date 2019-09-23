package com.crazy.bluetoothutils.connect.delegate;


import com.crazy.bluetoothutils.connect.bltclient.BltSocketClient;
import com.crazy.bluetoothutils.connect.packet.BltSocketResponsePacket;
import com.crazy.bluetoothutils.exception.BltException;

/**
 * Socket接收数据是的相关操作抽象
 */

public interface BltSocketClientReceivingDelegate {
    /**
     * 开始接收响应包
     *
     * @param client
     * @param packet
     */
    void onReceivePacketBegin(BltSocketClient client, BltSocketResponsePacket packet);

    /**
     * 接收完成
     *
     * @param client
     * @param packet
     */
    void onReceivePacketEnd(BltSocketClient client, BltSocketResponsePacket packet, boolean isCallbackInMainThread);

    /**
     * 取消接收
     *
     * @param client
     * @param packet
     */
    void onReceivePacketCancel(BltSocketClient client, BltSocketResponsePacket packet, BltException bltException);

    /**
     * 接收进度回调
     *
     * @param client         Socket管理类
     * @param packet         响应数据包
     * @param progress       进度
     * @param receivedLength 已接收的字节数
     */
    void onReceivingPacketInProgress(BltSocketClient client, BltSocketResponsePacket packet, float progress, int receivedLength);


}
