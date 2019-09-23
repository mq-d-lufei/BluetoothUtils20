package com.crazy.bluetoothutils.connect.packet;

/**
 * Created by feaoes on 2017/11/15.
 */

public class BltSocketResponsePacket implements Cloneable {

    private final BltSocketResponsePacket self = this;

    //整包
    private byte[] responsePacket;

    //包长度
    private byte[] packetLengthData;

    //包头
    private byte[] packetHeaderData;

    //除包头包长以外的数据
    private byte[] packetContentData;

    //包尾
    private byte[] packetTailData;

    @Override
    public Object clone() {
        BltSocketResponsePacket socketResponsePacket = null;
        try {
            socketResponsePacket = (BltSocketResponsePacket) super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return socketResponsePacket;
    }

    /**
     * 整包数据
     */
   /* public byte[] getResponsePacket() {
        if (null != this.responsePacket) {
            return this.responsePacket;
        }
        if (null != packetHeaderData && null != packetLengthData && null != packetContentData) {
            int length = packetHeaderData.length + packetLengthData.length + packetContentData.length;
            byte[] newBuffer = new byte[length];
            System.arraycopy(packetHeaderData, 0, newBuffer, 0, packetHeaderData.length);
            System.arraycopy(packetLengthData, 0, newBuffer, packetHeaderData.length, packetLengthData.length);
            System.arraycopy(packetContentData, 0, newBuffer, packetHeaderData.length + packetLengthData.length, packetContentData.length);
            this.responsePacket = newBuffer;
            return this.responsePacket;
        }
        return null;
    }*/
    public byte[] getResponsePacket() {
        return responsePacket;
    }

    public void setResponsePacket(byte[] responsePacket) {
        this.responsePacket = responsePacket;
    }

    public byte[] getPacketLengthData() {
        return packetLengthData;
    }

    public void setPacketLengthData(byte[] packetLengthData) {
        this.packetLengthData = packetLengthData;
    }

    public byte[] getPacketHeaderData() {
        return packetHeaderData;
    }

    public void setPacketHeaderData(byte[] packetHeaderData) {
        this.packetHeaderData = packetHeaderData;
    }

    public byte[] getPacketContentData() {
        return packetContentData;
    }

    public void setPacketContentData(byte[] packetContentData) {
        this.packetContentData = packetContentData;
    }

    public byte[] getPacketTailData() {
        return packetTailData;
    }

    public void setPacketTailData(byte[] packetTailData) {
        this.packetTailData = packetTailData;
    }
}