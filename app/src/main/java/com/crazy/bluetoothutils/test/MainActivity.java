package com.crazy.bluetoothutils.test;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.crazy.bluetoothutils.BltManager;
import com.crazy.bluetoothutils.R;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BltManager.getInstance().init(getApplication());
    }


    public void bt_client(View view) {
        startActivity(new Intent(this, BltClientActivity.class));
    }

    public void bt_server(View view) {
        startActivity(new Intent(this, BltServerActivity.class));

    }
}
