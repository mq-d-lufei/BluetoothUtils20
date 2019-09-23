package com.crazy.bluetoothutils.xml;

import com.crazy.bluetoothutils.data.BluetoothDeviceType;

import java.io.InputStream;
import java.util.List;

/**
 * Description:
 * Created by Crazy on 2018/8/31.
 */
public interface BluetoothDeviceParser {

    /**
     * 解析输入流 得到BluetoothDeviceDao对象集合
     *
     * @param is
     * @return
     * @throws Exception
     */
    List<BluetoothDeviceType> parse(InputStream is) throws Exception;

    /**
     * 序列化BluetoothDeviceDao对象集合 得到XML形式的字符串
     *
     * @param books
     * @return
     * @throws Exception
     */
    String serialize(List<BluetoothDeviceType> books) throws Exception;
}
