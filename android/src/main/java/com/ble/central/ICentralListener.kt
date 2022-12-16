package com.ble.central

interface ICentralListener {
  fun onScanStartedSuccessfully()
  fun onScanStartedFailed(errorCode: Int)
}
