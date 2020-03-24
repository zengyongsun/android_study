package com.zy.android_study.permission;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.zy.android_study.R;

import java.util.List;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

/**
 * 通过 EasyPermissions 来进行权限适配配
 */
public class PermissionStudyActivity extends AppCompatActivity implements View.OnClickListener,
        EasyPermissions.PermissionCallbacks, GpsManager.MyLocationListener {

    private GpsManager gpsManager;
    private TextView locationMsg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permission_study);

        findViewById(R.id.locationBt).setOnClickListener(this);
        findViewById(R.id.recordBt).setOnClickListener(this);
        findViewById(R.id.readBt).setOnClickListener(this);

        locationMsg = findViewById(R.id.locationMsg);
        gpsManager = new GpsManager(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.locationBt:
                actionLocation();
                break;
            case R.id.recordBt:
                actionRecord();
                break;
            case R.id.readBt:
                actionReadFile();
                break;
            default:
                break;
        }
    }

    private void actionRecord() {

    }

    private void actionReadFile() {

    }

    private void actionLocation() {
        if (methodRequiresPermission()) {
            startLocation();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        gpsManager.stopLocation();
    }

    private void startLocation() {
        int result = gpsManager.startLocation();
        if (result == GpsManager.GPS_NO_OPEN) {
            openGpsAction();
        } else {
            gpsManager.setLocationListener(this);
            Log.d("PermissionStudyActivity", "成功了！！");
        }
    }

    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    //+++++++++++++++获取权限代码++++++++++++++++++++++++++++++++++++
    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //将结果转发给 EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions,
                grantResults, this);
    }

    public static final int LOCATION_CODE = 100;
    public static final int GPS_CODE = 200;

    @AfterPermissionGranted(LOCATION_CODE)
    private boolean methodRequiresPermission() {
        //需要使用的权限
        String[] perms = {Manifest.permission.ACCESS_FINE_LOCATION};
        if (EasyPermissions.hasPermissions(this, perms)) {
            //已经有权限，进行操作
            return true;
        } else {
            //没有权限，进行权限请求
            EasyPermissions.requestPermissions(this,
                    "需要获取定位权限！否则无法工作", LOCATION_CODE, perms);
            return false;
        }
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        if (requestCode == AppSettingsDialog.DEFAULT_SETTINGS_REQ_CODE) {
            //用户从应用设置屏幕返回后，执行一些操作，例如显示Toast.
            Toast.makeText(this, R.string.app_name, Toast.LENGTH_SHORT).show();
            startLocation();
        }
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        Log.d("PermissionStudyActivity", "onPermissionsDenied:" + requestCode + ":" + perms.size());
        // 可选）检查用户是否拒绝了任何权限并选中了“永不再次询问”。
        //这将显示一个对话框，指导他们启用应用程序设置中的权限。
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            new AppSettingsDialog.Builder(this).build().show();
        }
    }

    private void openGpsAction() {
        Toast.makeText(this, "系统检测到未开启GPS定位服务,请开启",
                Toast.LENGTH_SHORT).show();
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        startActivityForResult(intent, GPS_CODE);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GPS_CODE) {
            startLocation();
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        locationMsg.setText(location.toString());
    }

    @Override
    public void onStatusChanged(int status) {

    }
}
