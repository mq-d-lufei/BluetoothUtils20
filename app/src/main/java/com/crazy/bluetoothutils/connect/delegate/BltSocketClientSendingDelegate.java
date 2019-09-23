package com.crazy.bluetoothutils.connect.delegate;


import com.crazy.bluetoothutils.connect.bltclient.BltSocketClient;
import com.crazy.bluetoothutils.connect.packet.BltSocketSendPacket;
import com.crazy.bluetoothutils.exception.BltException;

/**
 * Socket发送数据时的相关操作抽象
 */

public interface BltSocketClientSendingDelegate {
    /**
     * 开始发送
     */
    void onSendPacketBegin(BltSocketClient client, BltSocketSendPacket packet);

    /**
     * 发送完成
     */
    void onSendPacketEnd(BltSocketClient client, BltSocketSendPacket packet);

    /**
     * 取消发送
     */
    void onSendPacketCancel(BltSocketClient client, BltSocketSendPacket packet,BltException bltException);

    /**
     * 发送进度回调
     *
     * @param client       Socket管理类
     * @param packet       数据包
     * @param progress     进度（0.0f - 1.0f）
     * @param sendedLength 已发送的字节数
     */
    void onSendingPacketInProgress(BltSocketClient client, BltSocketSendPacket packet, float progress, int sendedLength);
}
