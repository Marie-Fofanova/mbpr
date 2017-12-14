package project.mibnd.miband;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Arrays;
import java.util.Set;

public class MainActivity extends Activity {

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothGatt bluetoothGatt;
    private BluetoothDevice bluetoothDevice;

    private Button btnStartConnecting;
    private Button btnGetBatteryInfo;
    private Button btnGetHeartRate;
    private Button btnWalkingInfo;
    private Button btnStartVibrate;
    private Button btnStopVibrate;
    private EditText txtPhysicalAddress;
    private TextView txtState;
    private TextView txtByte;
    private ProgressBar battery;
    private final BluetoothGattCallback bluetoothGattCallback = new BluetoothGattCallback() {

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            Log.v("test", "onConnectionStateChange");

            if (newState == android.bluetooth.BluetoothProfile.STATE_CONNECTED) {
                stateConnected();
            } else if (newState == android.bluetooth.BluetoothProfile.STATE_DISCONNECTED) {
                stateDisconnected();
            }

        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            Log.v("test", "onServicesDiscovered");
            listenHeartRate();
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
            Log.v("test", "onCharacteristicRead");
            byte[] data = characteristic.getValue();
            txtByte.setText(Arrays.toString(data));
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
            Log.v("test", "onCharacteristicWrite");
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            Log.v("test", "onCharacteristicChanged");
            byte[] data = characteristic.getValue();
            txtByte.setText(Arrays.toString(data));
        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorRead(gatt, descriptor, status);
            Log.v("test", "onDescriptorRead");
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorWrite(gatt, descriptor, status);
            Log.v("test", "onDescriptorWrite");
        }

        @Override
        public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
            super.onReliableWriteCompleted(gatt, status);
            Log.v("test", "onReliableWriteCompleted");
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            super.onReadRemoteRssi(gatt, rssi, status);
            Log.v("test", "onReadRemoteRssi");
        }

        @Override
        public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
            super.onMtuChanged(gatt, mtu, status);
            Log.v("test", "onMtuChanged");
        }

    };

    int REQUEST_ENABLE_BT = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeObjects();
        initilaizeComponents();
        initializeEvents();

        getBoundedDevice();

    }

    private void getBoundedDevice() {
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            Log.v("test", "Try turn on bluetooth" );
            if (!bluetoothAdapter.isEnabled()){
                txtState.setText(R.string.disabled);
                Toast.makeText(this, R.string.disabled, Toast.LENGTH_LONG).show();
                Log.v("test", "Bluetooth is not available" );
            }
        }
        else {
            Set<BluetoothDevice> boundedDevice = bluetoothAdapter.getBondedDevices();
            for (BluetoothDevice bd : boundedDevice) {
                if (bd.getName().contains("MI Band 2")) {
                    txtPhysicalAddress.setText(bd.getAddress());
                }
            }
        }
    }

    private void initializeObjects() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            try{
                Thread.sleep(20000);
            }
            catch (InterruptedException e) {
                Log.v("test", e.toString());
            };
            finish();
        }
    }

    private void initilaizeComponents() {
        btnStartConnecting = findViewById(R.id.btnStartConnecting);
        btnGetBatteryInfo = findViewById(R.id.btnGetBatteryInfo);
        btnWalkingInfo = findViewById(R.id.btnWalkingInfo);
        btnStartVibrate = findViewById(R.id.btnStartVibrate);
        btnStopVibrate = findViewById(R.id.btnStopVibrate);
        btnGetHeartRate = findViewById(R.id.btnGetHeartRate);
        txtPhysicalAddress = findViewById(R.id.txtPhysicalAddress);
        txtState = findViewById(R.id.txtState);
        txtByte = findViewById(R.id.txtByte);
        battery = findViewById(R.id.Battery);
        //battery.setVisibility(View.INVISIBLE);
    }

    private void initializeEvents()  {
        final byte[] buf=new byte[]{2};
        btnStartConnecting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startConnecting();
            }
        });
        btnGetBatteryInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getBatteryStatus();
            }
        });
        btnStartVibrate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startVibrate();
            }
        });
        btnStopVibrate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopVibrate();
            }
        });
        btnGetHeartRate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startScanHeartRate();
            }
        });
        btnWalkingInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                walkingInfo();
            }
        });
    }

    private void startConnecting() {

        if (!bluetoothAdapter.isEnabled()){
            Toast.makeText(this, R.string.disabled, Toast.LENGTH_LONG).show();
            Log.v("test", "Bluetooth is not available" );
            finish();
        }
        else {
            String address = txtPhysicalAddress.getText().toString();
            txtState.setText("Connecting...");
            bluetoothDevice = bluetoothAdapter.getRemoteDevice(address);

            Log.v("test", "Connecting to " + address);
            Log.v("test", "Device name " + bluetoothDevice.getName());

            bluetoothGatt = bluetoothDevice.connectGatt(this, true, bluetoothGattCallback);

        }
    }

    private void stateConnected() {
        if (!bluetoothAdapter.isEnabled()){
            txtState.setText(R.string.disabled);
            Toast.makeText(this, R.string.disabled, Toast.LENGTH_LONG).show();
            Log.v("test", "Bluetooth is not available" );
            finish();
        }
        else {
            bluetoothGatt.discoverServices();
            txtState.setText(R.string.connected);
        }
    }

    private void stateDisconnected() {
        if (!bluetoothAdapter.isEnabled()){
            txtState.setText(R.string.disabled);
            Toast.makeText(this, R.string.disabled, Toast.LENGTH_LONG).show();
            Log.v("test", "Bluetooth is not available" );
            finish();
        }
        else {
            bluetoothGatt.disconnect();
            txtState.setText(R.string.disconnected);
        }
    }

    private void startScanHeartRate() {
        txtByte.setText("...");
        BluetoothGattCharacteristic bchar = bluetoothGatt.getService(BluetoothProfile.HeartRate.service)
                .getCharacteristic(BluetoothProfile.HeartRate.controlCharacteristic);
        bchar.setValue(new byte[]{21, 2, 1});
        bluetoothGatt.writeCharacteristic(bchar);
    }

    private void listenHeartRate() {
        BluetoothGattCharacteristic bchar = bluetoothGatt.getService(BluetoothProfile.HeartRate.service)
                .getCharacteristic(BluetoothProfile.HeartRate.measurementCharacteristic);
        bluetoothGatt.setCharacteristicNotification(bchar, true);
        BluetoothGattDescriptor descriptor = bchar.getDescriptor(BluetoothProfile.HeartRate.descriptor);
        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        bluetoothGatt.writeDescriptor(descriptor);
        //isListeningHeartRate = true;
    }

    private void getBatteryStatus() {
        txtByte.setText("...");
        BluetoothGattCharacteristic bchar = bluetoothGatt.getService(BluetoothProfile.Basic.service)
                .getCharacteristic(BluetoothProfile.Basic.batteryCharacteristic);
        battery.setVisibility(View.VISIBLE);
        battery.setProgress(87);
        if (!bluetoothGatt.readCharacteristic(bchar)) {
            Toast.makeText(this, "Failed get battery info", Toast.LENGTH_SHORT).show();
        }

    }

    private void startVibrate()  {
        BluetoothGattCharacteristic bchar = bluetoothGatt.getService(BluetoothProfile.AlertNotification.service)
                .getCharacteristic(BluetoothProfile.AlertNotification.alertCharacteristic);
        bchar.setValue(new byte[]{2});
        if (!bluetoothGatt.writeCharacteristic(bchar)) {
            Toast.makeText(this, "Failed start vibrate", Toast.LENGTH_SHORT).show();
        } else{
            btnStartVibrate.setVisibility(View.INVISIBLE);
            btnStopVibrate.setVisibility(View.VISIBLE);
        }
    }

    private void stopVibrate() {
        BluetoothGattCharacteristic bchar = bluetoothGatt.getService(BluetoothProfile.AlertNotification.service)
                .getCharacteristic(BluetoothProfile.AlertNotification.alertCharacteristic);
        bchar.setValue(new byte[]{0});
        if (!bluetoothGatt.writeCharacteristic(bchar)) {
            Toast.makeText(this, "Failed stop vibrate", Toast.LENGTH_SHORT).show();
        } else{
            btnStopVibrate.setVisibility(View.INVISIBLE);
            btnStartVibrate.setVisibility(View.VISIBLE);
        }
    }

    private void walkingInfo() {
        txtByte.setText("...");
        BluetoothGattCharacteristic bchar = bluetoothGatt.getService(BluetoothProfile.Basic.service)
                .getCharacteristic(BluetoothProfile.Basic.stepsCharacteristic);
        if (!bluetoothGatt.readCharacteristic(bchar)) {
            Toast.makeText(this, "Failed get steps info", Toast.LENGTH_SHORT).show();
        }
    }

}