package io.mosip.ble.central.state.message

class ScanStartFailureMessage(val errorCode: Int): IMessage(CentralStates.SCAN_START_FAILURE) {
}
