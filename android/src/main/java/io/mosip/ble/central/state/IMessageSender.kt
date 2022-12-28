package io.mosip.ble.central.state

import io.mosip.ble.central.state.message.IMessage

interface IMessageSender {
  fun sendMessage(msg: IMessage)
}
