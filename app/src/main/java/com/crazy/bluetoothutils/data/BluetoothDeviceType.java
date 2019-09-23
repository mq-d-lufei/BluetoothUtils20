package com.crazy.bluetoothutils.data;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 蓝牙设备类型
 * Created by Crazy on 2018/8/31.
 */
public class BluetoothDeviceType implements Parcelable {

    /**
     * 例如:
     * (GROUP_NONE, 0x00, "未知设备"),
     * (GROUP_C30,  0x01, "科曼C30"),
     */

    //btClass.hashCode() & (BluetoothClass.Service.BITMASK=0xFFE000);
    private int serviceClass;

    //设备所属组名
    private String deviceGroup;

    //组内设备类型
    private String deviceType;

    //组内设备名称
    private String deviceName;

    public BluetoothDeviceType() {
    }

    public BluetoothDeviceType(String deviceGroup, String deviceType, String deviceName) {
        this.deviceGroup = deviceGroup;
        this.deviceType = deviceType;
        this.deviceName = deviceName;
    }

    public BluetoothDeviceType(int serviceClass, String deviceGroup, String deviceType, String deviceName) {
        this.serviceClass = serviceClass;
        this.deviceGroup = deviceGroup;
        this.deviceType = deviceType;
        this.deviceName = deviceName;
    }

    public int getServiceClass() {
        return serviceClass;
    }

    public BluetoothDeviceType setServiceClass(int serviceClass) {
        this.serviceClass = serviceClass;
        return this;
    }

    public String getDeviceGroup() {
        return deviceGroup;
    }

    public BluetoothDeviceType setDeviceGroup(String deviceGroup) {
        this.deviceGroup = deviceGroup;
        return this;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public BluetoothDeviceType setDeviceType(String deviceType) {
        this.deviceType = deviceType;
        return this;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public BluetoothDeviceType setDeviceName(String deviceName) {
        this.deviceName = deviceName;
        return this;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.serviceClass);
        dest.writeString(this.deviceGroup);
        dest.writeString(this.deviceType);
        dest.writeString(this.deviceName);
    }

    protected BluetoothDeviceType(Parcel in) {
        this.serviceClass = in.readInt();
        this.deviceGroup = in.readString();
        this.deviceType = in.readString();
        this.deviceName = in.readString();
    }

    public static final Parcelable.Creator<BluetoothDeviceType> CREATOR = new Parcelable.Creator<BluetoothDeviceType>() {
        @Override
        public BluetoothDeviceType createFromParcel(Parcel source) {
            return new BluetoothDeviceType(source);
        }

        @Override
        public BluetoothDeviceType[] newArray(int size) {
            return new BluetoothDeviceType[size];
        }
    };

    @Override
    public String toString() {
        return "BluetoothDeviceEntity{" +
                "serviceClass=" + serviceClass +
                ", deviceGroup='" + deviceGroup + '\'' +
                ", deviceType=" + deviceType +
                ", deviceName='" + deviceName + '\'' +
                '}';
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BluetoothDeviceType that = (BluetoothDeviceType) o;

        return serviceClass == that.serviceClass;
    }

    @Override
    public int hashCode() {
        return serviceClass;
    }
}
