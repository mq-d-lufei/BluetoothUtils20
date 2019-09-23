package com.crazy.bluetoothutils.connect;

import com.crazy.bluetoothutils.data.BltSeruceType;
import com.crazy.bluetoothutils.data.BluetoothDeviceType;

import java.util.List;
import java.util.UUID;

/**
 * Description:
 * Created by Crazy on 2018/8/2.
 */
public class BltConnectRuleConfig {

    //secure type（必选）
    @BltSeruceType.SecureType
    private String secureType;

    //name service name for SDP record（客户端非必选，服务端必选）
    private String name;

    // uuid uuid for SDP record（必选）
    private UUID uuid;

    //buffer size（客户端使用，非必选）
    private int receiveBufferSize = 1024;

    //is read delay（客户端使用，非必选）
    private long delayReceiveTime = 0;

    //server  :  device type judge（服务端使用，非必选）
    private List<BluetoothDeviceType> bluetoothDeviceTypeList;

    public String getSecureType() {
        return secureType;
    }

    public String getName() {
        return name;
    }

    public UUID getUuid() {
        return uuid;
    }

    public int getReceiveBufferSize() {
        return receiveBufferSize;
    }

    public long getDelayReceiveTime() {
        return delayReceiveTime;
    }

    public List<BluetoothDeviceType> getBqfjBluetoothDeviceList() {
        return bluetoothDeviceTypeList;
    }


    public static class Builder {
        @BltSeruceType.SecureType
        private String secureType;
        private String name;
        private UUID uuid;
        private int receiveBufferSize = 1024;
        private long delayReceiveTime;
        private List<BluetoothDeviceType> bluetoothDeviceTypeList;

        public Builder setSecureType(@BltSeruceType.SecureType String secureType) {
            this.secureType = secureType;
            return this;
        }

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Builder setUuid(UUID uuid) {
            this.uuid = uuid;
            return this;
        }

        public Builder setReceiveBufferSize(int receiveBufferSize) {
            this.receiveBufferSize = receiveBufferSize;
            return this;
        }

        public Builder setDelayReceiveTime(long delayReceiveTime) {
            this.delayReceiveTime = delayReceiveTime;
            return this;
        }

        public Builder setBqfjBluetoothDeviceList(List<BluetoothDeviceType> bluetoothDeviceTypeList) {
            this.bluetoothDeviceTypeList = bluetoothDeviceTypeList;
            return this;
        }

        void applyConfig(BltConnectRuleConfig config) {
            config.secureType = this.secureType;
            config.name = this.name;
            config.uuid = this.uuid;
            config.receiveBufferSize = this.receiveBufferSize;
            config.bluetoothDeviceTypeList = this.bluetoothDeviceTypeList;
            config.delayReceiveTime = this.delayReceiveTime;
        }

        public BltConnectRuleConfig build() {
            BltConnectRuleConfig config = new BltConnectRuleConfig();
            applyConfig(config);
            return config;
        }
    }

    /**
     * name和uuid都不能相同
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BltConnectRuleConfig that = (BltConnectRuleConfig) o;

     /*   if (!name.equals(that.name)) return false;
        return uuid.equals(that.uuid);*/

        if (name.equals(that.name)) {
            return true;
        }
        if (uuid.equals(that.uuid)) {
            return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + uuid.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "BltConnectRuleConfig{" +
                "secureType=" + secureType +
                ", name='" + name + '\'' +
                ", uuid=" + uuid +
                ", receiveBufferSize=" + receiveBufferSize +
                ", delayReceiveTime=" + delayReceiveTime +
                ", bqfjBluetoothDeviceList=" + (null == bluetoothDeviceTypeList ? "null" : "size: " + bluetoothDeviceTypeList.size()) +
                '}';
    }
}
