package com.ble.peripheral.state.message

class AdvertisementStartFailureMessage(val errorCode: Int): IMessage(PeripheralStates.ADV_START_FAILURE) {
}
