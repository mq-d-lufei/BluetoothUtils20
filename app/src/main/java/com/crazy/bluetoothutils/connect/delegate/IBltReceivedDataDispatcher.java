package com.crazy.bluetoothutils.connect.delegate;


import com.crazy.bluetoothutils.connect.bltclient.BltSocketClient;
import com.crazy.bluetoothutils.connect.packet.BltSocketResponsePacket;

/**
 * Created by feaoes on 2018/7/5.
 */

public interface IBltReceivedDataDispatcher {

    void bltReceivedDataDispather(BltSocketClient client, BltSocketResponsePacket bltSocketResponsePacket);
}
