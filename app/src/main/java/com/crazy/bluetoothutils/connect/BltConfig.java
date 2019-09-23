package com.crazy.bluetoothutils.connect;

import java.util.UUID;

/**
 * Created by feaoes on 2018/7/2.
 */

public class BltConfig {

    // Name for the SDP record when creating server socket
    public static final String NAME_SECURE = "BluetoothServiceNameSecure";
    public static final String NAME_INSECURE = "BluetoothServiceNameInsecure";

    // Unique UUID for this application
    public static final UUID MY_UUID_SECURE = UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");
    public static final UUID MY_UUID_INSECURE = UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66");

    public static final UUID SPPUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    public static UUID MAIN_UUID_SECURE = SPPUUID;
    public static UUID MAIN_UUID_INSECURE = SPPUUID;

    public static final String Lenovo_Old_Mac = "AC:38:70:25:2F:75";
    public static final String Lenovo_New_Mac = "AC:38:70:14:9F:40";
    public static final String C30_IVT_MPM02 = "00:15:83:6A:6A:97";

}
