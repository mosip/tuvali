package io.mosip.ble.peripheral.state

import io.mosip.ble.peripheral.state.message.IMessage

interface IMessageSender {
  fun sendMessage(msg: IMessage)
  fun getCurrentState() : StateHandler.States
}
