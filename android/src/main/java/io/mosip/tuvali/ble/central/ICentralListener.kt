package io.mosip.tuvali.ble.central

import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanRecord
import io.mosip.tuvali.exception.BLEException
import java.util.*

interface ICentralListener {
  fun onScanStartedFailed(errorCode: Int)
  fun onDeviceFound(device: BluetoothDevice, scanRecord: ScanRecord?)
  fun onDeviceConnected(device: BluetoothDevice)
  fun onDeviceDisconnected(isManualDisconnect: Boolean)
  fun onWriteFailed(device: BluetoothDevice?, charUUID: UUID, err: Int)
  fun onWriteSuccess(device: BluetoothDevice?, charUUID: UUID)
  fun onServicesDiscovered(serviceUuids: List<UUID>)
  fun onServicesDiscoveryFailed(errorCode: Int)
  fun onRequestMTUSuccess(mtu: Int)
  fun onRequestMTUFailure(errorCode: Int)
  fun onReadSuccess(charUUID: UUID, value: ByteArray?)
  fun onReadFailure(charUUID: UUID?, err: Int)
  fun onSubscriptionSuccess(charUUID: UUID)
  fun onSubscriptionFailure(charUUID: UUID, err: Int)
  fun onNotificationReceived(charUUID: UUID, value: ByteArray?)
  fun onClosed()
  fun onException(exception: BLEException)
  fun onDestroy()
}
