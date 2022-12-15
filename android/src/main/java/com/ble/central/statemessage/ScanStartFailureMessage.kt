package com.ble.central.statemessage

class ScanStartFailureMessage(val errorCode: Int): ICentralMessage(CentralStates.SCAN_START_FAILURE) {
}
