package com.ble.central.state

import com.ble.central.state.message.IMessage

interface IMessageSender {
  fun sendMessage(msg: IMessage)
}
