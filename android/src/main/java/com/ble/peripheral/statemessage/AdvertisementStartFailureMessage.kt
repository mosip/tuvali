package com.ble.peripheral.statemessage

class AdvertisementStartFailureMessage(val errorCode: Int): IPeripheralMessage(PeripheralStates.ADV_START_FAILURE) {
}
