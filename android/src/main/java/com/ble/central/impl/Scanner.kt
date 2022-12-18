package com.ble.central.impl

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.le.*
import android.content.Context
import android.os.ParcelUuid
import android.util.Log
import java.security.SecureRandom
import java.util.*

class Scanner(context: Context) {
  private lateinit var advPayload: String
  private lateinit var onScanStartFailure: (Int) -> Unit
  private lateinit var onDeviceFound: (BluetoothDevice) -> Unit
  private var bluetoothLeScanner: BluetoothLeScanner
  private lateinit var peripheralDevice: BluetoothDevice

  init {
    val bluetoothManager: BluetoothManager =
      context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    val bluetoothAdapter = bluetoothManager.adapter
    bluetoothLeScanner = bluetoothAdapter.bluetoothLeScanner
  }

  private val leScanCallback: ScanCallback = object : ScanCallback() {
    override fun onScanResult(callbackType: Int, result: ScanResult) {
      Log.i("BLE Central", "Found the device: $result. The bytes are: ${result.scanRecord?.bytes?.toUByteArray()}")
      stopScan()

      //TODO: Compare advPayload before confirming peripheral
      peripheralDevice = result.device
      onDeviceFound(peripheralDevice)
    }

    override fun onScanFailed(errorCode: Int) {
      onScanStartFailure(errorCode)
    }
  }

  @SuppressLint("MissingPermission")
  fun start(
    serviceUUID: UUID,
    advPayload: String,
    onDeviceFound: (BluetoothDevice) -> Unit,
    onScanStartFailure: (Int) -> Unit
  ) {
    this.onDeviceFound = onDeviceFound;
    this.onScanStartFailure = onScanStartFailure;
    this.advPayload = advPayload;

    val filter = ScanFilter.Builder()
      .setServiceUuid(ParcelUuid(serviceUUID))
      .build()

    bluetoothLeScanner.startScan(
      mutableListOf(filter),
      ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build(),
      leScanCallback
    )
  }

  @SuppressLint("MissingPermission")
  fun stopScan() {
    bluetoothLeScanner.stopScan(leScanCallback)
  }

}
