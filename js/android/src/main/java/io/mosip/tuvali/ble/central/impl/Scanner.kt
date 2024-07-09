package io.mosip.tuvali.ble.central.impl

import android.annotation.SuppressLint
import android.bluetooth.BluetoothManager
import android.bluetooth.le.*
import android.content.Context
import android.os.ParcelUuid
import android.util.Log
import java.util.*
import io.mosip.tuvali.transfer.Util.Companion.getLogTag

class Scanner(context: Context) {
  private val logTag =  getLogTag(javaClass.simpleName)
  private lateinit var onScanStartFailure: (Int) -> Unit
  private lateinit var onDeviceFound: (ScanResult) -> Unit
  private var bluetoothLeScanner: BluetoothLeScanner

  init {
    val bluetoothManager: BluetoothManager =
      context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    val bluetoothAdapter = bluetoothManager.adapter
    bluetoothLeScanner = bluetoothAdapter.bluetoothLeScanner
  }

  private val leScanCallback: ScanCallback = object : ScanCallback() {
    override fun onScanResult(callbackType: Int, result: ScanResult) {
      Log.d(logTag, "Found the device: $result. The bytes are: ${result.scanRecord?.bytes}")

      onDeviceFound(result)
    }

    override fun onScanFailed(errorCode: Int) {
      onScanStartFailure(errorCode)
    }
  }

  @SuppressLint("MissingPermission")
  fun start(
    serviceUUID: UUID,
    onDeviceFound: (ScanResult) -> Unit,
    onScanStartFailure: (Int) -> Unit
  ) {
    this.onDeviceFound = onDeviceFound;
    this.onScanStartFailure = onScanStartFailure;

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
