package com.crazy.bluetoothutils.utils;

import android.util.Log;

/**
 * Created by feaoes on 2018/7/27.
 */

public final class BltLog {

    public static boolean isPrint = true;
    private static String defaultTag = "CrazyBlt";

    public static void d(String msg) {
        if (isPrint && msg != null)
            Log.d(defaultTag, msg);
    }

    public static void i(String msg) {
        if (isPrint && msg != null)
            Log.i(defaultTag, msg);
    }

    public static void w(String msg) {
        if (isPrint && msg != null)
            Log.w(defaultTag, msg);
    }

    public static void e(String msg) {
        if (isPrint && msg != null)
            Log.e(defaultTag, msg);
    }

    /**
     * @param tag
     * @param msg
     */

    public static void d(String tag, String msg) {
        if (isPrint && msg != null)
            Log.d(defaultTag + "_" + tag, msg);
    }

    public static void i(String tag, String msg) {
        if (isPrint && msg != null)
            Log.i(defaultTag + "_" + tag, msg);
    }

    public static void w(String tag, String msg) {
        if (isPrint && msg != null)
            Log.w(defaultTag + "_" + tag, msg);
    }

    public static void e(String tag, String msg) {
        if (isPrint && msg != null)
            Log.e(defaultTag + "_" + tag, msg);
    }
}
