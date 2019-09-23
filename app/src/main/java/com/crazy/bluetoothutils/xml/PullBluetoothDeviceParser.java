package com.crazy.bluetoothutils.xml;

import android.util.Xml;

import com.crazy.bluetoothutils.data.BluetoothDeviceType;

import org.xmlpull.v1.XmlPullParser;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Description:
 * Created by Crazy on 2018/8/31.
 */
public class PullBluetoothDeviceParser implements BluetoothDeviceParser {

    @Override
    public ArrayList<BluetoothDeviceType> parse(InputStream is) throws Exception {

        ArrayList<BluetoothDeviceType> bluetoothDeviceTypeList = null;
        BluetoothDeviceType bluetoothDeviceType = null;
        try {
            //由android.util.Xml创建一个XmlPullParser实例
            XmlPullParser pullParser = Xml.newPullParser();
            //设置输入流 并指明编码方式
            pullParser.setInput(is, "UTF-8");

            int eventType = pullParser.getEventType();

            while (eventType != XmlPullParser.END_DOCUMENT) {
                switch (eventType) {
                    //开始标签
                    case XmlPullParser.START_TAG: {
                        if (pullParser.getName().equals("BluetoothDeviceTypeList")) {
                            bluetoothDeviceTypeList = new ArrayList<>();
                        } else if (pullParser.getName().equals("BluetoothDeviceType")) {
                            bluetoothDeviceType = new BluetoothDeviceType();
                        } else if (pullParser.getName().equals("serviceClass")) {
                            eventType = pullParser.next();
                            if (bluetoothDeviceType != null) {
                                if (pullParser.getText().contains("0x")) {
                                    String text = pullParser.getText();
                                    String substring = text.substring(2, text.length());
                                    bluetoothDeviceType.setServiceClass(Integer.valueOf(substring, 16));
                                } else {
                                    bluetoothDeviceType.setServiceClass(Integer.valueOf(pullParser.getText()));
                                }
                            }
                        } else if (pullParser.getName().equals("deviceGroup")) {
                            eventType = pullParser.next();
                            if (bluetoothDeviceType != null) {
                                bluetoothDeviceType.setDeviceGroup(pullParser.getText());
                            }
                        } else if (pullParser.getName().equals("deviceType")) {
                            eventType = pullParser.next();
                            if (bluetoothDeviceType != null) {
                                bluetoothDeviceType.setDeviceType(pullParser.getText());
                            }
                        } else if (pullParser.getName().equals("deviceName")) {
                            eventType = pullParser.next();
                            if (bluetoothDeviceType != null) {
                                bluetoothDeviceType.setDeviceName(pullParser.getText());
                            }
                        }
                    }
                    break;
                    //结束标签
                    case XmlPullParser.END_TAG:
                        if (pullParser.getName().equals("BluetoothDeviceType")) {
                            if (null != bluetoothDeviceTypeList && null != bluetoothDeviceType) {
                                bluetoothDeviceTypeList.add(bluetoothDeviceType);
                            }
                        }
                        break;
                    default:
                        break;
                }
                //继续往下读取标签类型
                eventType = pullParser.next();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return bluetoothDeviceTypeList;
    }

    @Override
    public String serialize(List<BluetoothDeviceType> books) throws Exception {
        return null;
    }
}
