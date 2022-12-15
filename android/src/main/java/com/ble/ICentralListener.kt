package com.ble

interface ICentralListener {
  fun onScanStartedSuccessfully()
  fun onScanStartedFailed(errorCode: Int)
}
