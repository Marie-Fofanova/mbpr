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
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.IOException;
import java.util.Arrays;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import project.mibnd.miband.R;

public class MainActivity extends Activity {

    int REQUEST_ENABLE_BT = 1;
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
            if (chkBytes.isChecked()) {
                switch (data[0]) {
                    case (15):
                        byte value = data[1];
                        txtByte.setText(Arrays.toString(data));
                        txtByte.append("\n" + String.valueOf(value) + "%");
                        break;
                    case (12):
                        int steps = ((((data[1] & 255) | ((data[2] & 255) << 8))));
                        int cal = ((((data[9] & 255) | ((data[10] & 255) << 8)) | (data[11] & 16711680)) | ((data[12] & 255) << 24));
                        int meters = ((((data[5] & 255) | ((data[6] & 255) << 8)) | (data[7] & 16711680)) | ((data[8] & 255) << 24));
                        txtByte.setText(Arrays.toString(data));
                        txtByte.append("\n" + String.valueOf(steps) + " steps\n" + String.valueOf(cal) + " calories\n" + String.valueOf(meters) + " m");
                        break;
                }
            } else {
                switch (data[0]) {
                    case (15):
                        byte value = data[1];
                        txtByte.setText("\n" + String.valueOf(value) + "%");
                        break;
                    case (12):
                        int steps = ((((data[1] & 255) | ((data[2] & 255) << 8))));
                        int cal = ((((data[9] & 255) | ((data[10] & 255) << 8)) | (data[11] & 16711680)) | ((data[12] & 255) << 24));
                        int meters = ((((data[5] & 255) | ((data[6] & 255) << 8)) | (data[7] & 16711680)) | ((data[8] & 255) << 24));
                        txtByte.append("\n" + String.valueOf(steps) + " steps\n" + String.valueOf(cal) + " calories\n" + String.valueOf(meters) + " m");
                        break;

                }
            }
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
            if (chkBytes.isChecked()) {
                txtByte.setText(Arrays.toString(data));
                txtByte.append(String.valueOf(data[1]));
            } else {
                txtByte.setText(String.valueOf(data[1]));
            }
            Get test = new Get();
            test.run("http://api.thingspeak.com/update?api_key=UP9XHQ2BU9NAN9TD&field1=" + String.valueOf(data[1]),
                    new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            String mMessage = e.getMessage().toString();
                            txtByte.setText(mMessage);
                            Log.w("failure Response", mMessage);
                            //call.cancel();
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            String mMessage = response.body().string();
                            if (response.isSuccessful()) {
                                try {
                                    JSONObject json = new JSONObject(mMessage);
                                    final String serverResponse = json.getString("Your Index");
                                    txtByte.setText(serverResponse);

                                } catch (Exception e) {
                                    e.printStackTrace();
                                    ;
                                }
                            }
                        }
                    });

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
    private ProgressBar battery;
    private CheckBox chkBytes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeObjects();
        initilaizeComponents();
        initializeEvents();

        getBoundedDevice();

    }

    private boolean format(String s) {
        Pattern pattern = Pattern.compile("[A-Za-z0-9]{2,2}:[A-Za-z0-9]{2,2}:[A-Za-z0-9]{2,2}:[A-Za-z0-9]{2,2}:[A-Za-z0-9]{2,2}:[A-Za-z0-9]{2,2}");
        Matcher matcher = pattern.matcher(s);
        return matcher.matches();
    }

    private void getBoundedDevice() {
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            Log.v("test", "Try turn on bluetooth");
            if (!bluetoothAdapter.isEnabled()) {
                Toast.makeText(this, R.string.disabled, Toast.LENGTH_LONG).show();
                Log.v("test", "Bluetooth is not available");
            }
        }
        Set<BluetoothDevice> boundedDevice = bluetoothAdapter.getBondedDevices();
        for (BluetoothDevice bd : boundedDevice) {
            if (bd.getName().contains("MI Band 2")) {
                txtPhysicalAddress.setText(bd.getAddress());
            }
        }
    }

    private void initializeObjects() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            try {
                Thread.sleep(20000);
            } catch (InterruptedException e) {
                Log.v("test", e.toString());
            }
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
        chkBytes = findViewById(R.id.chBytes);
        //battery.setVisibility(View.INVISIBLE);
    }

    private void initializeEvents() {
        btnStartConnecting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!bluetoothAdapter.isEnabled()) {
                    Snackbar.make(v, R.string.disabled, Snackbar.LENGTH_LONG).show();
                    Log.v("test", "Bluetooth is not available");
                } else {
                    startConnecting();
                }
            }
        });
        btnGetBatteryInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!bluetoothAdapter.isEnabled() || !("Connected").equals(txtState.getText())) {
                    Snackbar.make(v, R.string.disabled, Snackbar.LENGTH_LONG).show();
                    Log.v("test", "Bluetooth is not available");
                } else {
                    getBatteryStatus();
                }
            }
        });
        btnStartVibrate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!bluetoothAdapter.isEnabled() || !("Connected").equals(txtState.getText())) {
                    Snackbar.make(v, R.string.disabled, Snackbar.LENGTH_LONG).show();
                    Log.v("test", "Bluetooth is not available");
                } else {
                    startVibrate();
                }
            }
        });
        btnStopVibrate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!bluetoothAdapter.isEnabled() || !("Connected").equals(txtState.getText())) {
                    Snackbar.make(v, R.string.disabled, Snackbar.LENGTH_LONG).show();
                    Log.v("test", "Bluetooth is not available");
                } else {
                    stopVibrate();
                }
            }
        });
        btnGetHeartRate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!bluetoothAdapter.isEnabled() || !("Connected").equals(txtState.getText())) {
                    Snackbar.make(v, R.string.disabled, Snackbar.LENGTH_LONG).show();
                    Log.v("test", "Bluetooth is not available");
                } else {
                    startScanHeartRate();
                }
            }
        });
        btnWalkingInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!bluetoothAdapter.isEnabled() || !("Connected").equals(txtState.getText())) {
                    Snackbar.make(v, R.string.disabled, Snackbar.LENGTH_LONG).show();
                    Log.v("test", "Bluetooth is not available");
                } else {
                    walkingInfo();
                }
            }
        });
    }

    private void startConnecting() {

        if (!bluetoothAdapter.isEnabled()) {
            Toast.makeText(this, R.string.disabled, Toast.LENGTH_LONG).show();
            Log.v("test", "Bluetooth is not available");
        } else {
            String address = txtPhysicalAddress.getText().toString();
            txtState.setText(R.string.connecting);
            if (format(address)) {
                bluetoothDevice = bluetoothAdapter.getRemoteDevice(address);

                Log.v("test", "Connecting to " + address);
                Log.v("test", "Device name " + bluetoothDevice.getName());
                bluetoothGatt = bluetoothDevice.connectGatt(this, true, bluetoothGattCallback);
            } else {
                txtState.setText(R.string.wrong);
            }

        }
    }

    private void stateConnected() {
        bluetoothGatt.discoverServices();
        txtState.setText(R.string.connected);
    }

    private void stateDisconnected() {
        bluetoothGatt.disconnect();
        txtState.setText(R.string.disconnected);

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
        txtByte.setText(R.string.w);
        BluetoothGattCharacteristic bchar = bluetoothGatt.getService(BluetoothProfile.Basic.service)
                .getCharacteristic(BluetoothProfile.Basic.batteryCharacteristic);
        battery.setVisibility(View.VISIBLE);
        battery.setProgress(87);
        if (!bluetoothGatt.readCharacteristic(bchar)) {
            Toast.makeText(this, "Failed get battery info", Toast.LENGTH_SHORT).show();
        }

    }

    private void startVibrate() {
        BluetoothGattCharacteristic bchar = bluetoothGatt.getService(BluetoothProfile.AlertNotification.service)
                .getCharacteristic(BluetoothProfile.AlertNotification.alertCharacteristic);
        bchar.setValue(new byte[]{2});
        if (!bluetoothGatt.writeCharacteristic(bchar)) {
            Toast.makeText(this, "Failed start vibrate", Toast.LENGTH_SHORT).show();
        } else {
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
        } else {
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