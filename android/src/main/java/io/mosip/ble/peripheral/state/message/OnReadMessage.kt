package io.mosip.ble.peripheral.state.message

import android.bluetooth.BluetoothGattCharacteristic

class OnReadMessage(val characteristic: BluetoothGattCharacteristic?, val isRead: Boolean): IMessage(PeripheralMessageTypes.ON_READ) {}
