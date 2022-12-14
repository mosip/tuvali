package com.ble.statemessage

class AdvertisementStartFailureMessage(commandType: PeripheralStates, val errorCode: Int): IMessage(commandType) {
}
