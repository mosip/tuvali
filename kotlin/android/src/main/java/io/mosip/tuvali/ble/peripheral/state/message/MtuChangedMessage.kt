package io.mosip.tuvali.ble.peripheral.state.message

class MtuChangedMessage (val mtuChanged : Int):IMessage(PeripheralMessageTypes.MTU_CHANGED)
