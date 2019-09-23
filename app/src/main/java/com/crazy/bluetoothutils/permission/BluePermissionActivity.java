package com.crazy.bluetoothutils.permission;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.crazy.bluetoothutils.R;
import com.crazy.bluetoothutils.utils.BltLog;

/**
 * Description:
 * Created by Crazy on 2018/8/9.
 */
public class BluePermissionActivity extends AppCompatActivity {

    //权限请求码
    public static final int REQUEST_CODE_START_BLUE_PERMISSION_ACTIVITY = 0X000012;

    public static void startBluePermissionActivity(Activity context, int requestCode) {
        Intent intent = new Intent(context, BluePermissionActivity.class);
        context.startActivityForResult(intent, requestCode);
    }

    public static void startBluePermissionActivity(Context context) {
        Intent intent = new Intent(context, BluePermissionActivity.class);
        context.startActivity(intent);
    }

    private BluetoothDispatherHandler bluetoothDispatherHandler;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_blue_permission);

        initWindow();
        initView();
        initDispather();
    }

    private void initDispather() {
        bluetoothDispatherHandler = new BluetoothDispatherHandler(this) {
            @Override
            public void openBluetoothSuccess() {
                doOnFinish(true);
            }
        };
        bluetoothDispatherHandler.openBluetoothWithCheck(this);
    }

    private void initWindow() {
        //窗口对齐屏幕宽度
        Window win = this.getWindow();
        win.getDecorView().setPadding(0, 0, 0, 0);
        WindowManager.LayoutParams layoutParams = win.getAttributes();
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT;
        layoutParams.gravity = Gravity.BOTTOM;//设置对话框在底部显示
        win.setAttributes(layoutParams);
    }

    protected void initView() {
        findViewById(R.id.iv_left).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bluetoothDispatherHandler.doOnFinish(false);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        bluetoothDispatherHandler.onRequestPermissionResult(BluePermissionActivity.this, requestCode, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        BltLog.e("BluePermissionActivity onActivityResult");
        bluetoothDispatherHandler.onActivityResult(BluePermissionActivity.this, requestCode, resultCode, data);
    }

}
