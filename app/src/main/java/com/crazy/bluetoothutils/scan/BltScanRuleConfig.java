package com.crazy.bluetoothutils.scan;

import android.support.annotation.NonNull;

import com.crazy.bluetoothutils.bond.BltBondRuleConfig;
import com.crazy.bluetoothutils.connect.BltConnectRuleConfig;
import com.crazy.bluetoothutils.data.BltScanMode;
import com.crazy.bluetoothutils.data.BluetoothDeviceType;

import java.util.List;
import java.util.UUID;

/**
 * Description: 扫描规则配置类
 * Created by Crazy on 2018/7/27.
 * <p>
 * 扫描规则：
 * 1、scanMode必选
 * 2、扫描条件优先根据BQFJBluetoothDevice判断、没有再根据Mac地址、Name判断
 */

public class BltScanRuleConfig {

    //设备MAC
    private String deviceMac = null;
    //设备名称
    private String[] deviceNames = null;
    //UUID
    private UUID[] serviceUUIDs = null;
    //是否模糊扫描
    private boolean fuzzyQuery = false;
    //设备类型
    private List<BluetoothDeviceType> bluetoothDeviceTypeList;
    //扫描超时时间
    private long scanTimeout = 1000 * 10;
    //扫描模式
    @NonNull
    @BltScanMode.ScanMode
    private String scanMode = BltScanMode.ScanMode.SCAN;
    //绑定规则
    private BltBondRuleConfig bltBondRuleConfig;
    //连接规则
    private BltConnectRuleConfig bltConnectRuleConfig;


    public String getDeviceMac() {
        return deviceMac;
    }

    public String[] getDeviceNames() {
        return deviceNames;
    }

    public UUID[] getServiceUUIDs() {
        return serviceUUIDs;
    }

    public boolean isFuzzyQuery() {
        return fuzzyQuery;
    }

    public List<BluetoothDeviceType> getBluetoothDeviceTypeList() {
        return bluetoothDeviceTypeList;
    }

    public long getScanTimeout() {
        return scanTimeout;
    }

    public String getScanMode() {
        return scanMode;
    }

    public BltBondRuleConfig getBltBondRuleConfig() {
        return bltBondRuleConfig;
    }

    public BltConnectRuleConfig getBltConnectRuleConfig() {
        return bltConnectRuleConfig;
    }

    /**
     * Builder
     */
    public static class Builder {
        //设备MAC
        private String deviceMac = null;
        //设备名称
        private String[] deviceNames = null;
        //UUID
        private UUID[] serviceUUIDs = null;
        //是否模糊扫描
        private boolean fuzzyQuery = false;
        //设备类型
        private List<BluetoothDeviceType> bluetoothDeviceTypeList;
        //扫描超时时间
        private long scanTimeout = 1000 * 10;
        //扫描模式
        @BltScanMode.ScanMode
        private String scanMode = BltScanMode.ScanMode.SCAN;

        private BltBondRuleConfig bltBondRuleConfig;

        private BltConnectRuleConfig bltConnectRuleConfig;

        public Builder setDeviceMac(String deviceMac) {
            this.deviceMac = deviceMac;
            return this;
        }

        public Builder setDeviceNames(String[] deviceNames) {
            this.deviceNames = deviceNames;
            return this;
        }

        public Builder setServiceUUIDs(UUID[] serviceUUIDs) {
            this.serviceUUIDs = serviceUUIDs;
            return this;
        }

        public Builder setFuzzyQuery(boolean fuzzyQuery) {
            this.fuzzyQuery = fuzzyQuery;
            return this;
        }

        public Builder setBluetoothDeviceTypeList(List<BluetoothDeviceType> bluetoothDeviceTypeList) {
            this.bluetoothDeviceTypeList = bluetoothDeviceTypeList;
            return this;
        }

        public Builder setScanTimeout(long scanTimeout) {
            this.scanTimeout = scanTimeout;
            return this;
        }

        public Builder setScanMode(String scanMode) {
            this.scanMode = scanMode;
            return this;
        }

        public Builder setBltBondRuleConfig(BltBondRuleConfig bltBondRuleConfig) {
            this.bltBondRuleConfig = bltBondRuleConfig;
            return this;
        }

        public Builder setBltConnectRuleConfig(BltConnectRuleConfig bltConnectRuleConfig) {
            this.bltConnectRuleConfig = bltConnectRuleConfig;
            return this;
        }

        void applyConfig(BltScanRuleConfig config) {
            config.deviceMac = this.deviceMac;
            config.deviceNames = this.deviceNames;
            config.serviceUUIDs = this.serviceUUIDs;
            config.fuzzyQuery = this.fuzzyQuery;
            config.bluetoothDeviceTypeList = this.bluetoothDeviceTypeList;
            config.scanTimeout = this.scanTimeout;
            config.scanMode = this.scanMode;
            config.bltBondRuleConfig = this.bltBondRuleConfig;
            config.bltConnectRuleConfig = this.bltConnectRuleConfig;
        }

        public BltScanRuleConfig build() {
            BltScanRuleConfig config = new BltScanRuleConfig();
            applyConfig(config);
            return config;
        }
    }
}
