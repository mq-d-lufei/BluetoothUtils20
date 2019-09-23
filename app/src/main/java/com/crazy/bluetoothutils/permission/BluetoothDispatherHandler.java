package com.crazy.bluetoothutils.permission;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.widget.Toast;

import static android.app.Activity.RESULT_OK;

/**
 * Description:
 * Created by Crazy on 2018/8/9.
 */
public abstract class BluetoothDispatherHandler implements BluetoothDispather.DispatherListener {

    public static final int BLUETOOTH_OPEN_SUCCESS = 0x000001;
    public static final int BLUETOOTH_OPEN_FAILURE = 0x000002;

    public static final String BLUETOOTH_OPENED = "Bluetooth_Opened";

    public Activity context;

    public BluetoothDispatherHandler(Activity context) {
        this.context = context;
    }

    /**
     * 检查位置权限与蓝牙打开状态
     */
    public void openBluetoothWithCheck(Activity target) {
        BluetoothDispather.getInstance().openBluetoothWithCheck(target, BluetoothDispatherHandler.this);
    }

    /**
     * 权限请求结果回调
     */
    public void onRequestPermissionResult(Activity target, int requestCode, int[] grantResults) {
        BluetoothDispather.getInstance().onRequestPermissionResult(target, requestCode, grantResults);
    }

    /**
     * 定位功能与蓝牙功能打开回调
     */
    public void onActivityResult(Activity target, int requestCode, int resultCode, Intent data) {
        BluetoothDispather.getInstance().onActivityResult(target, requestCode, resultCode, data);
    }


    @Override
    public void onDenyBluetoothFunction() {
        Toast.makeText(context, "拒绝打开蓝牙，该功能将无法使用", Toast.LENGTH_SHORT).show();
        doOnFinish(false);
    }

    @Override
    public void showRationaleForLocation(final BluetoothDispather.OpenBluetoothPermissionRequest request) {
        new AlertDialog.Builder(context)
                .setMessage("使用该功能需要获取位置信息")
                .setPositiveButton("允许使用", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        request.proceed();
                    }
                }).setNegativeButton("不允许使用", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                request.cancel();
            }
        }).show();
    }

    @Override
    public void onDenyLocationPermission() {
        Toast.makeText(context, "拒绝了获取位置信息权限，该功能将无法使用", Toast.LENGTH_SHORT).show();
        doOnFinish(false);
    }

    @Override
    public void onDenyLocationFunction() {
        Toast.makeText(context, "拒绝打开定位功能,蓝牙功能将无法正常使用", Toast.LENGTH_SHORT).show();
        doOnFinish(false);
    }

    @Override
    public void showNeverAskForLocationPermission() {
        Toast.makeText(context, "请允许该应用使用位置权限，在权限管理中设置", Toast.LENGTH_SHORT).show();
        doOnFinish(false);
    }

    /**
     * @param isSuccess 蓝牙是否打开
     */
    protected void doOnFinish(boolean isSuccess) {
        Intent intent = new Intent();
        intent.putExtra(BLUETOOTH_OPENED, isSuccess);
        context.setResult(RESULT_OK, intent);
        context.finish();
    }
}
