package com.whf.demolist;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.whf.demolist.anim.AnimActivity;
import com.whf.demolist.bluetooth.ble.ClientActivity;
import com.whf.demolist.camera.CameraActivity;
import com.whf.demolist.notification.NotificationActivity;
import com.whf.demolist.qrcode.QrCodeActivity;
import com.whf.demolist.surfaceview.SurfaceViewActivity;
import com.whf.demolist.wifi.WifiActivity;
import com.whf.demolist.video.VideoInfoActivity;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
    }

    private void initView() {
        findViewById(R.id.btn_anim).setOnClickListener(this);
        findViewById(R.id.btn_qrcode).setOnClickListener(this);
        findViewById(R.id.btn_camera).setOnClickListener(this);
        findViewById(R.id.btn_surface).setOnClickListener(this);
        findViewById(R.id.btn_bluetooth).setOnClickListener(this);
        findViewById(R.id.btn_binder).setOnClickListener(this);
        findViewById(R.id.btn_notification).setOnClickListener(this);
        findViewById(R.id.btn_wifi).setOnClickListener(this);
        findViewById(R.id.btn_video).setOnClickListener(this);
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_anim:
                startActivity(new Intent(this, AnimActivity.class));
                break;
            case R.id.btn_qrcode:
                startActivity(new Intent(this, QrCodeActivity.class));
                break;
            case R.id.btn_camera:
                startActivity(new Intent(this, CameraActivity.class));
                break;
            case R.id.btn_surface:
                startActivity(new Intent(this, SurfaceViewActivity.class));
                break;
            case R.id.btn_bluetooth:
                startActivity(new Intent(this, ClientActivity.class));
                break;
            case R.id.btn_binder:
                startActivity(new Intent(this, com.whf.demolist.binder.ClientActivity.class));
                break;
            case R.id.btn_notification:
                startActivity(new Intent(this, NotificationActivity.class));
            case R.id.btn_wifi:
                startActivity(new Intent(this, WifiActivity.class));
                break;
            case R.id.btn_video:
                startActivity(new Intent(this, VideoInfoActivity.class));
            default:
                break;
        }
    }
}
