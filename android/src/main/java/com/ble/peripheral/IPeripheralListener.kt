package com.ble.peripheral

interface IPeripheralListener {
  fun onAdvertisementStartSuccessful()
  fun onAdvertisementStartFailed(errorCode: Int)
}
