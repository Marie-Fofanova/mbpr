package project.mibnd.miband;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Arrays;
import java.util.Set;

public class MainActivity extends Activity {


    Boolean isListeningHeartRate = false;

    BluetoothAdapter bluetoothAdapter;
    BluetoothGatt bluetoothGatt;
    BluetoothDevice bluetoothDevice;

    Button btnStartConnecting, btnGetBatteryInfo, btnGetHeartRate, btnWalkingInfo, btnStartVibrate, btnStopVibrate;
    EditText txtPhysicalAddress;
    TextView txtState, txtByte;
    final BluetoothGattCallback bluetoothGattCallback = new BluetoothGattCallback() {

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeObjects();
        initilaizeComponents();
        initializeEvents();

        getBoundedDevice();

    }

    void getBoundedDevice() {
        Set<BluetoothDevice> boundedDevice = bluetoothAdapter.getBondedDevices();
        for (BluetoothDevice bd : boundedDevice) {
            if (bd.getName().contains("MI Band 2")) {
                txtPhysicalAddress.setText(bd.getAddress());
            }
        }
    }

    void initializeObjects() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    void initilaizeComponents() {
        btnStartConnecting = (Button) findViewById(R.id.btnStartConnecting);
        btnGetBatteryInfo = (Button) findViewById(R.id.btnGetBatteryInfo);
        btnWalkingInfo = (Button) findViewById(R.id.btnWalkingInfo);
        btnStartVibrate = (Button) findViewById(R.id.btnStartVibrate);
        btnStopVibrate = (Button) findViewById(R.id.btnStopVibrate);
        btnGetHeartRate = (Button) findViewById(R.id.btnGetHeartRate);
        txtPhysicalAddress = (EditText) findViewById(R.id.txtPhysicalAddress);
        txtState = (TextView) findViewById(R.id.txtState);
        txtByte = (TextView) findViewById(R.id.txtByte);
    }

    void initializeEvents() {
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

    void startConnecting() {

        String address = txtPhysicalAddress.getText().toString();
        bluetoothDevice = bluetoothAdapter.getRemoteDevice(address);

        Log.v("test", "Connecting to " + address);
        Log.v("test", "Device name " + bluetoothDevice.getName());

        bluetoothGatt = bluetoothDevice.connectGatt(this, true, bluetoothGattCallback);

    }

    void stateConnected() {
        bluetoothGatt.discoverServices();
        txtState.setText("Connected");
    }

    void stateDisconnected() {
        bluetoothGatt.disconnect();
        txtState.setText("Disconnected");
    }

    void startScanHeartRate() {
        txtByte.setText("...");
        BluetoothGattCharacteristic bchar = bluetoothGatt.getService(BluetoothProfile.HeartRate.service)
                .getCharacteristic(BluetoothProfile.HeartRate.controlCharacteristic);
        bchar.setValue(new byte[]{21, 2, 1});
        bluetoothGatt.writeCharacteristic(bchar);
    }

    void listenHeartRate() {
        BluetoothGattCharacteristic bchar = bluetoothGatt.getService(BluetoothProfile.HeartRate.service)
                .getCharacteristic(BluetoothProfile.HeartRate.measurementCharacteristic);
        bluetoothGatt.setCharacteristicNotification(bchar, true);
        BluetoothGattDescriptor descriptor = bchar.getDescriptor(BluetoothProfile.HeartRate.descriptor);
        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        bluetoothGatt.writeDescriptor(descriptor);
        isListeningHeartRate = true;
    }

    void getBatteryStatus() {
        txtByte.setText("...");
        BluetoothGattCharacteristic bchar = bluetoothGatt.getService(BluetoothProfile.Basic.service)
                .getCharacteristic(BluetoothProfile.Basic.batteryCharacteristic);
        if (!bluetoothGatt.readCharacteristic(bchar)) {
            Toast.makeText(this, "Failed get battery info", Toast.LENGTH_SHORT).show();
        }

    }

    void startVibrate() {
        BluetoothGattCharacteristic bchar = bluetoothGatt.getService(BluetoothProfile.AlertNotification.service)
                .getCharacteristic(BluetoothProfile.AlertNotification.alertCharacteristic);
        bchar.setValue(new byte[]{2});
        if (!bluetoothGatt.writeCharacteristic(bchar)) {
            Toast.makeText(this, "Failed start vibrate", Toast.LENGTH_SHORT).show();
        }
    }

    void stopVibrate() {
        BluetoothGattCharacteristic bchar = bluetoothGatt.getService(BluetoothProfile.AlertNotification.service)
                .getCharacteristic(BluetoothProfile.AlertNotification.alertCharacteristic);
        bchar.setValue(new byte[]{0});
        if (!bluetoothGatt.writeCharacteristic(bchar)) {
            Toast.makeText(this, "Failed stop vibrate", Toast.LENGTH_SHORT).show();
        }
    }

    void walkingInfo() {
        txtByte.setText("...");
        BluetoothGattCharacteristic bchar = bluetoothGatt.getService(BluetoothProfile.Basic.service)
                .getCharacteristic(BluetoothProfile.Basic.stepsCharacteristic);
        if (!bluetoothGatt.readCharacteristic(bchar)) {
            Toast.makeText(this, "Failed get steps info", Toast.LENGTH_SHORT).show();
        }
    }

}