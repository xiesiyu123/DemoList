package com.whf.demolist.bluetooth.ble;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.whf.demolist.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 低功耗Ble蓝牙
 */
public class ClientActivity extends AppCompatActivity {

    private static final String TAG = Constants.TAG + ClientActivity.class;

    private int state;

    private static final int IDLE = 0x00000000;
    private static final int PENDING_SCAN = 0x00000001;
    private static final int PENDING_BROADCAST = 0x00000010;

    private Button btnScan;
    private Button btnBroadcast;
    private ListView lvBtInfo;

    private Map<String, BluetoothDevice> bluetoothMap;
    private ArrayAdapter<String> arrayAdapter;

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothLeScanner bluetoothLeScanner;
    private ScanCallback scanCallback;
    private BroadcastReceiver bluetoothStateReceiver;
    private BluetoothManager bluetoothManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);

        initView();
        initData();
        initReceiver();
    }

    private void initView() {
        btnScan = findViewById(R.id.btn_scan);
        btnBroadcast = findViewById(R.id.btn_broadcast);
        lvBtInfo = findViewById(R.id.lv_bluetooth_info);

        btnScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scanBluetooth();
            }
        });

        btnBroadcast.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendBroadcastFromPeripheral();
            }
        });

        lvBtInfo.setOnItemClickListener((parent, view, position, id) -> {
            stopScan();
            skipToInfo((TextView) view);
        });
    }

    private void skipToInfo(TextView view) {
        String info = (String) view.getText();
        Intent intent = new Intent(ClientActivity.this, InfoActivity.class);
        BluetoothDevice bluetoothDevice = bluetoothMap.get(info);
        intent.putExtra(Constants.REMOTE_BLUETOOTH, bluetoothDevice);
        startActivity(intent);
    }

    private void initData() {
        bluetoothMap = new HashMap<>();
        arrayAdapter = new ArrayAdapter<>(this, R.layout.item_bluetooth, new ArrayList<>());
        lvBtInfo.setAdapter(arrayAdapter);

        bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        if (bluetoothManager != null) {
            bluetoothAdapter = bluetoothManager.getAdapter();
        }

        scanCallback = new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                BluetoothDevice device = result.getDevice();
                String info = device.getAddress() + "  |  " + device.getName();
                if (bluetoothMap.get(info) == null) {
                    Log.d(TAG, "Scan Result = " + info);
                    bluetoothMap.put(info, device);
                    arrayAdapter.add(info);
                }
            }

            @Override
            public void onBatchScanResults(List<ScanResult> results) {
                super.onBatchScanResults(results);
            }

            @Override
            public void onScanFailed(int errorCode) {
                super.onScanFailed(errorCode);
            }
        };
    }

    /**
     * 注册蓝牙状态广播
     */
    private void initReceiver() {
        bluetoothStateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.STATE_OFF);
                bluetoothStateChange(state);
            }
        };

        IntentFilter intentFilter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        this.registerReceiver(bluetoothStateReceiver, intentFilter);
    }

    /**
     * 根据蓝牙状态进行相应处理
     */
    private void bluetoothStateChange(int state) {
        switch (state) {
            case BluetoothAdapter.STATE_ON:
                Log.d(TAG,"bluetooth state changed to on!");
                bluetoothStateOn();
                break;
            case BluetoothAdapter.STATE_OFF:
                Log.d(TAG,"bluetooth state changed to off!");
                break;
        }
    }

    private void bluetoothStateOn() {
        if ((state & PENDING_SCAN) != 0) {
            state &= ~PENDING_SCAN;
            scanBluetooth();
        }

        if ((state & PENDING_BROADCAST) != 0) {
            state &= ~PENDING_BROADCAST;
            sendBroadcastFromPeripheral();
        }
    }

    /**
     * 扫描周围的蓝牙设备，该操作为异步操作的，但是会消耗大量资源，一般扫描时长为12秒，建议找到需要的设备后，执行取消扫描
     */
    private void scanBluetooth() {
        if (!checkVariableNotNull()) {
            Log.e(TAG, "bluetooth not usable!");
            return;
        }
        if (bluetoothLeScanner == null) {
            bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
        }
        if (!bluetoothAdapter.isEnabled()) {
            Log.d(TAG,"bluetooth not enable!");
            bluetoothAdapter.enable();
            state |= PENDING_SCAN;
            return;
        }

        //设置各种过滤条件
        ArrayList<ScanFilter> scanFilterList = new ArrayList<>();
        ScanFilter.Builder filterBuild = new ScanFilter.Builder();
        scanFilterList.add(filterBuild.build());

        //扫描时的设置
        ScanSettings.Builder settingBuild = new ScanSettings.Builder();
        //这种扫描方式占用资源比较高，建议当应用处于前台时使用该模式
        settingBuild.setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY);

        Log.d(TAG,"start scan!");
        bluetoothLeScanner.startScan(scanFilterList, settingBuild.build(), scanCallback);
    }


    /**
     * 作为外围设备发送广播
     */
    private void sendBroadcastFromPeripheral() {
        if (!checkVariableNotNull()) {
            Log.e(TAG, "bluetooth not enable!");
            return;
        }
        if (bluetoothLeScanner == null) {
            bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
        }
        if (!bluetoothAdapter.isEnabled()) {
            Log.d(TAG,"bluetooth not enable!");
            bluetoothAdapter.enable();
            state |= PENDING_BROADCAST;
            return;
        }

        BluetoothLeAdvertiser advertiser = bluetoothAdapter.getBluetoothLeAdvertiser();
        BluetoothGattServerCallback serverCallback = new BluetoothGattServerCallback() {
            @Override
            public void onConnectionStateChange(BluetoothDevice device, int status, int newState) {
                super.onConnectionStateChange(device, status, newState);
            }
        };

        AdvertiseCallback advertiseCallback = new AdvertiseCallback() {
            @Override
            public void onStartSuccess(AdvertiseSettings settingsInEffect) {
                Log.d(TAG, "advertise success!");
            }

            @Override
            public void onStartFailure(int errorCode) {
                Log.d(TAG, "advertise fail!");
            }
        };

        Log.d(TAG,"start advertising!");
        advertiser.startAdvertising(buildAdvertiseSettings(), buildAdvertiseData(), advertiseCallback);
    }

    /**
     * 创建广播设置
     */
    private AdvertiseSettings buildAdvertiseSettings() {
        AdvertiseSettings.Builder builder = new AdvertiseSettings.Builder();
        return builder.build();
    }

    /**
     * 创建广播数据
     */
    private AdvertiseData buildAdvertiseData() {
        AdvertiseData.Builder dataBuilder = new AdvertiseData.Builder();
        //参数一为厂家ID，参数二为厂家的扩展值（byte[]）
        dataBuilder.addManufacturerData(0x34, new byte[]{0x56});

        return dataBuilder.build();
    }


    private boolean checkVariableNotNull() {
        if (bluetoothManager == null) {
            bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        }

        if (bluetoothManager != null && bluetoothAdapter == null) {
            bluetoothAdapter = bluetoothManager.getAdapter();
        }

        return bluetoothAdapter != null;
    }


    /**
     * 结束扫描
     */
    private void stopScan() {
        Log.i(TAG, "停止扫描");
        bluetoothLeScanner.stopScan(scanCallback);
    }


    /**
     * 扫描已绑定的设备
     */
    private void scanBinding() {
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "蓝牙未启动", Toast.LENGTH_SHORT).show();
            return;
        }
        StringBuilder info = new StringBuilder();
        Set<BluetoothDevice> deviceSet = bluetoothAdapter.getBondedDevices();
        for (BluetoothDevice bluetoothDevice : deviceSet) {
            info.append("name = " + bluetoothDevice.getName() + "; ");
            info.append("address = " + bluetoothDevice.getAddress() + "\n");
            Log.i(TAG, "name = " + bluetoothDevice.getName() + "; address = " + bluetoothDevice.getAddress());
        }
    }

    /**
     * 判断设备是否支持低功耗Ble蓝牙
     */
    private void isSupportBle() {
        //设备大于Android 4.3才支持低功耗Ble蓝牙
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
                Toast.makeText(this, "该设备支持低功耗蓝牙", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "该设备不支持低功耗蓝牙", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "该设备不支持低功耗蓝牙", Toast.LENGTH_SHORT).show();
        }
    }
}
