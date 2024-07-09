package io.mosip.tuvali.ble.peripheral.impl

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
  private lateinit var onAdvStartSuccessCallback: () -> Unit
  private lateinit var onAdvStartFailureCallback: (Int) -> Unit

  private val advertiseCallbackImpl = object : AdvertiseCallback() {
    override fun onStartSuccess(settingsInEffect: AdvertiseSettings?) {
      onAdvStartSuccessCallback()
    }

    override fun onStartFailure(errorCode: Int) {
      onAdvStartFailureCallback(errorCode)
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
    advPayload: ByteArray,
    scanRespPayload: ByteArray,
    onStartSuccess: () -> Unit,
    onStartFailure: (Int) -> Unit
  ) {
    onAdvStartSuccessCallback = onStartSuccess
    onAdvStartFailureCallback = onStartFailure
    advertiser.startAdvertising(
      getSettings(),
      advertiseData(serviceUUID, advPayload, true),
      advertiseData(scanRespUUID, scanRespPayload, false),
      advertiseCallbackImpl
    )
  }

  private fun advertiseData(
    serviceUUID: UUID?,
    payload: ByteArray,
    includeServiceUUID: Boolean
  ): AdvertiseData? {
    val parcelUuid = ParcelUuid(serviceUUID)
    val advDataBuilder = AdvertiseData.Builder()
      .setIncludeDeviceName(false)
    if (includeServiceUUID) {
      advDataBuilder.addServiceUuid(parcelUuid)
    }
    return advDataBuilder
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

  @SuppressLint("MissingPermission")
  fun stop() {
    advertiser.stopAdvertising(advertiseCallbackImpl)
  }
}
