package io.mosip.tuvali.verifier

import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import java.util.*

class GattService {
  //TODO: Update UUIDs as per specification
  companion object {
    val IDENTIFY_REQUEST_CHAR_UUID: UUID = UUID.fromString("00002030-0000-1000-8000-00805f9b34fb")
    val REQUEST_SIZE_CHAR_UUID: UUID = UUID.fromString("00002031-0000-1000-8000-00805f9b34fb")
    val REQUEST_CHAR_UUID: UUID = UUID.fromString("00002032-0000-1000-8000-00805f9b34fb")
    val RESPONSE_SIZE_CHAR_UUID: UUID = UUID.fromString("00002033-0000-1000-8000-00805f9b34fb")
    val SUBMIT_RESPONSE_CHAR_UUID: UUID = UUID.fromString("00002034-0000-1000-8000-00805f9b34fb")
    val TRANSFER_REPORT_REQUEST_CHAR_UUID: UUID = UUID.fromString("00002035-0000-1000-8000-00805f9b34fb")
    val TRANSFER_REPORT_RESPONSE_CHAR_UUID: UUID = UUID.fromString("00002036-0000-1000-8000-00805f9b34fb")
    val VERIFICATION_STATUS_CHAR_UUID: UUID = UUID.fromString("00002037-0000-1000-8000-00805f9b34fb")
    val DISCONNECT_CHAR_UUID: UUID = UUID.fromString("00002038-0000-1000-8000-00805f9b34fb")
  }

  fun create(): BluetoothGattService {
    val service = BluetoothGattService(
      Verifier.SERVICE_UUID,
      BluetoothGattService.SERVICE_TYPE_PRIMARY
    )

    val identifyRequestChar = BluetoothGattCharacteristic(
      IDENTIFY_REQUEST_CHAR_UUID,
      BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE or BluetoothGattCharacteristic.PROPERTY_WRITE,
      BluetoothGattCharacteristic.PERMISSION_WRITE
    )

    val requestSizeChar = BluetoothGattCharacteristic(
      REQUEST_SIZE_CHAR_UUID,
      BluetoothGattCharacteristic.PROPERTY_READ or BluetoothGattCharacteristic.PROPERTY_INDICATE,
      BluetoothGattCharacteristic.PERMISSION_READ
    )

    val requestChar = BluetoothGattCharacteristic(
      REQUEST_CHAR_UUID,
      BluetoothGattCharacteristic.PROPERTY_READ or BluetoothGattCharacteristic.PROPERTY_INDICATE,
      BluetoothGattCharacteristic.PERMISSION_READ
    )

    val responseSizeChar = BluetoothGattCharacteristic(
      RESPONSE_SIZE_CHAR_UUID,
      BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE or BluetoothGattCharacteristic.PROPERTY_WRITE,
      BluetoothGattCharacteristic.PERMISSION_WRITE
    )

    val submitResponseChar = BluetoothGattCharacteristic(
      SUBMIT_RESPONSE_CHAR_UUID,
      BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE or BluetoothGattCharacteristic.PROPERTY_WRITE,
      BluetoothGattCharacteristic.PERMISSION_WRITE
    )

    val transferReportRequestChar = BluetoothGattCharacteristic(
      TRANSFER_REPORT_REQUEST_CHAR_UUID,
      BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE,
      BluetoothGattCharacteristic.PERMISSION_WRITE
    )

    val transferReportResponseChar = BluetoothGattCharacteristic(
      TRANSFER_REPORT_RESPONSE_CHAR_UUID,
      BluetoothGattCharacteristic.PROPERTY_INDICATE,
      BluetoothGattCharacteristic.PERMISSION_READ
    )

    val verificationStatusChar = BluetoothGattCharacteristic(
      VERIFICATION_STATUS_CHAR_UUID,
      BluetoothGattCharacteristic.PROPERTY_INDICATE,
      BluetoothGattCharacteristic.PERMISSION_READ
    )

    val disconnectStatusChar = BluetoothGattCharacteristic(
      DISCONNECT_CHAR_UUID,
      BluetoothGattCharacteristic.PROPERTY_INDICATE,
      BluetoothGattCharacteristic.PERMISSION_READ
    )

    service.addCharacteristic(identifyRequestChar)
    service.addCharacteristic(requestSizeChar)
    service.addCharacteristic(requestChar)
    service.addCharacteristic(responseSizeChar)
    service.addCharacteristic(submitResponseChar)
    service.addCharacteristic(transferReportRequestChar)
    service.addCharacteristic(transferReportResponseChar)
    service.addCharacteristic(verificationStatusChar)
    service.addCharacteristic(disconnectStatusChar)

    return service
  }
}
