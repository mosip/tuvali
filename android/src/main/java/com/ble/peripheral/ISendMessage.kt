package com.ble.peripheral

import com.ble.statemessage.IMessage

interface ISendMessage {
  fun sendMessage(msg: IMessage)
}
