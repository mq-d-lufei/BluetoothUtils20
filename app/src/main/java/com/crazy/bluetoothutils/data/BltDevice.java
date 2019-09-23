package com.crazy.bluetoothutils.data;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import com.crazy.bluetoothutils.BltManager;

import java.io.Serializable;

/**
 * 蓝牙设备
 */

public class BltDevice implements Serializable {

    private BluetoothDevice mDevice;
    private BluetoothSocket mSocket;

    private int rssi;
    private String scanTimeString;
    private String connectedTimeString;

    @BltSeruceType.SecureType
    private String secureType;

    public BltDevice(BluetoothDevice mDevice, BluetoothSocket mSocket) {
        this.mDevice = mDevice;
        this.mSocket = mSocket;
    }

    public BltDevice(BluetoothDevice mDevice, String scanTimeString) {
        this.mDevice = mDevice;
        this.scanTimeString = scanTimeString;
    }

    public BluetoothSocket getSocket() {
        return mSocket;
    }

    public void setmSocket(BluetoothSocket mSocket) {
        this.mSocket = mSocket;
    }

    public BluetoothDevice getDevice() {
        return mDevice;
    }

    public void setmDevice(BluetoothDevice mDevice) {
        this.mDevice = mDevice;
    }

    public BluetoothDeviceType getDeviceType() {
        return BltManager.getInstance().getBluetoothDeviceType(mDevice.getBluetoothClass());
    }

    public int getRssi() {
        return rssi;
    }

    public void setRssi(int rssi) {
        this.rssi = rssi;
    }


    public String getScanTimeString() {
        return scanTimeString;
    }

    public void setScanTimeString(String scanTimeString) {
        this.scanTimeString = scanTimeString;
    }

    public String getConnectedTimeString() {
        return connectedTimeString;
    }

    public void setConnectedTimeString(String connectedTimeString) {
        this.connectedTimeString = connectedTimeString;
    }

    public String getSecureType() {
        return secureType;
    }

    public void setSecureType(@BltSeruceType.SecureType String secureType) {
        this.secureType = secureType;
    }

    public String getName() {
        if (mDevice != null)
            return mDevice.getName();
        return null;
    }

    public String getMac() {
        if (mDevice != null)
            return mDevice.getAddress();
        return null;
    }

    public String getKey() {
        if (mDevice != null)
            return mDevice.getName() + "&" + mDevice.getAddress();
        return "";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BltDevice bltDevice = (BltDevice) o;

        return mDevice.equals(bltDevice.mDevice);
    }

    @Override
    public int hashCode() {
        return mDevice.hashCode();
    }

    @Override
    public String toString() {
        return "BltDevice{" +
                "mDevice=" + mDevice +
                ", mSocket=" + mSocket +
                ", rssi=" + rssi +
                ", name=" + getName() +
                ", mac=" + getMac() +
                ", DeviceType=" + getDeviceType() +
                ", scanTimeString=" + scanTimeString +
                ", connectedTimeString=" + connectedTimeString +
                ", secureType=" + secureType +
                '}';
    }
}
