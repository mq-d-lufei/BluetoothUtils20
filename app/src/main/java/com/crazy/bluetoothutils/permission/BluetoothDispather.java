package com.crazy.bluetoothutils.permission;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.widget.Toast;

import com.crazy.bluetoothutils.BltManager;

import java.lang.ref.WeakReference;

/**
 * Description:
 * Created by Crazy on 2018/8/8.
 */
public class BluetoothDispather {

    //权限请求码
    private static final int REQUEST_BLUETOOTH_PERMISSION = 0X0001;
    //定位功能请求码
    private static final int REQUEST_CODE_OPEN_LOCATION = 0X0002;
    //蓝牙功能请求码
    private static final int REQUEST_CODE_OPEN_BLUETOOTH = 0X0003;
    //位置权限(粗略与精确)
    private static final String[] PERMISSION_OPEN_BLUETOOTH = new String[]{"android.permission.ACCESS_COARSE_LOCATION", "android.permission.ACCESS_FINE_LOCATION"};

    private DispatherListener mDispatherListener;

    private BluetoothDispather() {
    }

    private static final class DispatherHolder {
        private static final BluetoothDispather INSTANCE = new BluetoothDispather();
    }

    public static BluetoothDispather getInstance() {
        return DispatherHolder.INSTANCE;
    }

    /**
     * 检查位置权限与蓝牙打开状态
     */
    public void openBluetoothWithCheck(@NonNull Activity target, @NonNull DispatherListener dispatherListener) {
        if (!BltManager.getInstance().isSupportBluetooth()) {
            Toast.makeText(target, "该设备不支持蓝牙功能", Toast.LENGTH_LONG).show();
            return;
        }

        mDispatherListener = dispatherListener;

        if (PermissionUtils.hasSelfPermissions(target, PERMISSION_OPEN_BLUETOOTH)) {
            onPermissionGranted(target);
        } else {
            if (PermissionUtils.shouldShowRequestPermissionRationale(target, PERMISSION_OPEN_BLUETOOTH)) {
                mDispatherListener.showRationaleForLocation(new OpenBluetoothPermissionRequest(target));
            } else {
                ActivityCompat.requestPermissions(target, PERMISSION_OPEN_BLUETOOTH, REQUEST_BLUETOOTH_PERMISSION);
            }
        }
    }

    /**
     * 权限可用
     */
    private void onPermissionGranted(final Activity target) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !isLocationOpen(target)) {
            new AlertDialog.Builder(target)
                    .setTitle("提示")
                    .setMessage("当前手机使用蓝牙需要打开定位功能")
                    .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mDispatherListener.onDenyLocationFunction();
                        }
                    })
                    .setPositiveButton("前往设置", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            target.startActivityForResult(intent, REQUEST_CODE_OPEN_LOCATION);
                        }
                    }).show();
        } else {
            enableBluetooth(target);
        }
    }

    /**
     * 权限请求结果回调
     */
    public void onRequestPermissionResult(Activity target, int requestCode, int[] grantResults) {
        if (null != target) {
            switch (requestCode) {
                case REQUEST_BLUETOOTH_PERMISSION:
                    if (PermissionUtils.verifyPermissions(grantResults)) {
                        onPermissionGranted(target);
                    } else {
                        if (!PermissionUtils.shouldShowRequestPermissionRationale(target, PERMISSION_OPEN_BLUETOOTH)) {
                            mDispatherListener.showNeverAskForLocationPermission();
                        } else {
                            mDispatherListener.onDenyLocationPermission();
                        }
                    }
                    break;
                default:
                    break;
            }
        }
    }


    /**
     * 定位功能与蓝牙功能打开回调
     */
    public void onActivityResult(Activity target, int requestCode, int resultCode, Intent data) {
        if (REQUEST_CODE_OPEN_LOCATION == requestCode) {
            if (Activity.RESULT_OK == resultCode && isLocationOpen(target)) {
                enableBluetooth(target);
            } else {
                mDispatherListener.onDenyLocationFunction();
            }
        } else if (REQUEST_CODE_OPEN_BLUETOOTH == requestCode) {
            if (Activity.RESULT_OK == resultCode && BltManager.getInstance().isBluetoothOpen()) {
                mDispatherListener.openBluetoothSuccess();
            } else {
                mDispatherListener.onDenyBluetoothFunction();
            }
        }
    }


    /**
     *
     */
    public final class OpenBluetoothPermissionRequest implements PermissionRequest {

        private final WeakReference<Activity> weakTarget;

        private OpenBluetoothPermissionRequest(Activity target) {
            this.weakTarget = new WeakReference<Activity>(target);
        }

        @Override
        public void proceed() {
            Activity target = weakTarget.get();
            if (null != target) {
                ActivityCompat.requestPermissions(target, PERMISSION_OPEN_BLUETOOTH, REQUEST_BLUETOOTH_PERMISSION);
            }
        }

        @Override
        public void cancel() {
            Activity target = weakTarget.get();
            if (null != target) {
                mDispatherListener.onDenyLocationPermission();
            }
        }
    }

    /**
     * 判断位置信息是否打开
     */
    public static boolean isLocationOpen(final Context context) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (null == locationManager) {
            return false;
        }
        //gps定位
        boolean isGpsProvider = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        //网络定位
        boolean isNetWorkProvider = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        return isGpsProvider || isNetWorkProvider;
    }


    /**
     * 蓝牙开关是否打开
     */
    public void enableBluetooth(Activity target) {
        int statusValue = BltManager.getInstance().enableBluetooth(target, REQUEST_CODE_OPEN_BLUETOOTH);
        if (statusValue == BltManager.Bluetooth_Opened) {
            mDispatherListener.openBluetoothSuccess();
        }
    }

    interface DispatherListener {
        //蓝牙打开成功，位置权限已有、开关已开
        void openBluetoothSuccess();

        void onDenyBluetoothFunction();

        //提示框
        void showRationaleForLocation(final OpenBluetoothPermissionRequest request);

        //打开位置权限被拒绝
        void onDenyLocationPermission();

        //打开定位功能被拒绝
        void onDenyLocationFunction();

        //提示框
        void showNeverAskForLocationPermission();
    }

    public interface PermissionRequest {

        void proceed();

        void cancel();
    }
}
