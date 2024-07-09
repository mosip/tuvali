package io.mosip.tuvali.ble.peripheral.state.message

class AdvertisementStartFailureMessage(val errorCode: Int): IMessage(PeripheralMessageTypes.ADV_START_FAILURE) {
}
