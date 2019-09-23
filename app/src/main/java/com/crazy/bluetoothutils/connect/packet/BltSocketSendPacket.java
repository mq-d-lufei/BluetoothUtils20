package com.crazy.bluetoothutils.connect.packet;

/**
 * Created by feaoes on 2018/7/2.
 */

public class BltSocketSendPacket {


    private byte[] sendPacket;

    public BltSocketSendPacket() {
    }

    public BltSocketSendPacket(byte[] sendPacket) {
        this.sendPacket = sendPacket;
    }


    public byte[] getSendPacket() {
        return sendPacket;
    }

    public BltSocketSendPacket setSendPacket(byte[] sendPacket) {
        this.sendPacket = sendPacket;
        return this;
    }
}
