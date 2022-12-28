package io.mosip.ble.central.state.message

import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanRecord

class DeviceFoundMessage(var device: BluetoothDevice, val scanRecord: ScanRecord?) : IMessage(CentralStates.DEVICE_FOUND)
