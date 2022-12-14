package com.ble.peripheral

import android.annotation.SuppressLint
import android.bluetooth.BluetoothManager
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.BluetoothLeAdvertiser
import android.content.Context
import android.os.ParcelUuid
import java.util.UUID

// Need:
// service UUID
// Advertisement Data (identifier_pubkey16bytes)
// Scan Response Data (pubkey16bytes)
// Advertisement settings
class Advertiser(
  context: Context
) {
  private var advertiser: BluetoothLeAdvertiser
  private var advertiseSettings: AdvertiseSettings

  private val advertiseCallbackImpl = object : AdvertiseCallback() {
    override fun onStartSuccess(settingsInEffect: AdvertiseSettings?) {
      super.onStartSuccess(settingsInEffect)
    }

    override fun onStartFailure(errorCode: Int) {
      super.onStartFailure(errorCode)
    }
  }

  init {
    val bluetoothManager: BluetoothManager =
      context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    val mBluetoothAdapter = bluetoothManager.adapter
    advertiser = mBluetoothAdapter.bluetoothLeAdvertiser
    advertiseSettings = getSettings()
  }

  @SuppressLint("MissingPermission")
  fun start(
    serviceUUID: UUID,
    scanRespUUID: UUID,
    advPayload: String,
    scanRespPayload: String
  ) {
    // starts advertisement
    advertiser.startAdvertising(
      getSettings(),
      advertiseData(serviceUUID, advPayload.toByteArray()),
      advertiseData(scanRespUUID, scanRespPayload.toByteArray()),
      advertiseCallbackImpl
    )
  }

  private fun advertiseData(serviceUUID: UUID?, payload: ByteArray): AdvertiseData? {
    val parcelUuid = ParcelUuid(serviceUUID)
    return AdvertiseData.Builder()
      .setIncludeDeviceName(false)
      .addServiceUuid(parcelUuid)
      .addServiceData(parcelUuid, payload)
      .build()
  }

  private fun getSettings(): AdvertiseSettings {
    return AdvertiseSettings.Builder()
      .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
      .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_ULTRA_LOW)
      .setConnectable(true)
      .build()
  }
}
