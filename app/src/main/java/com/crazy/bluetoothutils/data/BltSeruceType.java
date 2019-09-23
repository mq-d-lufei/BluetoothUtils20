package com.crazy.bluetoothutils.data;

import android.support.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Description:
 * Created by Crazy on 2018/7/27.
 */
public interface BltSeruceType {

    @Retention(RetentionPolicy.SOURCE)
    @StringDef({SecureType.SECURE, SecureType.INSECURE})
    @interface SecureType {
        String SECURE = "SECURE";
        String INSECURE = "INSECURE";
    }
}
