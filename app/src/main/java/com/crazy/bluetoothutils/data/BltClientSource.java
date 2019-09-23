package com.crazy.bluetoothutils.data;

import android.support.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Description:SocketClient是直连还是反连创建
 * Created by Crazy on 2018/7/27.
 */
public interface BltClientSource {

    @Retention(RetentionPolicy.SOURCE)
    @StringDef({ClientSource.CLIENT, ClientSource.SERVER})
    @interface ClientSource {
        String CLIENT = "CLIENT";
        String SERVER = "SERVER";
    }
}
