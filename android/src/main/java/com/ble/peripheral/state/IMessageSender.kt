package com.ble.peripheral.state

import com.ble.peripheral.state.message.IMessage

interface IMessageSender {
  fun sendMessage(msg: IMessage)
}
