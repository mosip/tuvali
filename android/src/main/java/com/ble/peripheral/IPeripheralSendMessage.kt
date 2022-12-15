package com.ble.peripheral

import com.ble.peripheral.statemessage.IPeripheralMessage

interface IPeripheralSendMessage {
  fun sendMessage(msg: IPeripheralMessage)
}
