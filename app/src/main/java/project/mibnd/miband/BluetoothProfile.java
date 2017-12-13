package project.mibnd.miband;

import java.util.UUID;

class BluetoothProfile {

    public static class Basic {
        public static final UUID service = UUID.fromString("0000fee0-0000-1000-8000-00805f9b34fb");
        public static final UUID batteryCharacteristic = UUID.fromString("00000006-0000-3512-2118-0009af100700");
        public static final UUID stepsCharacteristic = UUID.fromString("00000007-0000-3512-2118-0009af100700");
    }

    public static class AlertNotification {
        public static final UUID service = UUID.fromString("00001802-0000-1000-8000-00805f9b34fb");
        public static final UUID alertCharacteristic = UUID.fromString("00002a06-0000-1000-8000-00805f9b34fb");
    }

    public static class HeartRate {
        public static final UUID service = UUID.fromString("0000180d-0000-1000-8000-00805f9b34fb");
        public static final UUID measurementCharacteristic = UUID.fromString("00002a37-0000-1000-8000-00805f9b34fb");
        public static final UUID descriptor = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
        public static final UUID controlCharacteristic = UUID.fromString("00002a39-0000-1000-8000-00805f9b34fb");
    }

}