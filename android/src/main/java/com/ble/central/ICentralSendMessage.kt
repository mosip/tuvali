package com.ble.central

import com.ble.central.statemessage.ICentralMessage

interface ICentralSendMessage {
  fun sendMessage(msg: ICentralMessage)
}
