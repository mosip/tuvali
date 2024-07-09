package io.mosip.tuvali.ble.peripheral.state

import io.mosip.tuvali.ble.peripheral.state.message.IMessage

interface IMessageSender {
  fun sendMessage(msg: IMessage)
  fun getCurrentState() : StateHandler.States
  fun sendMessageDelayed(msg: IMessage, delay: Long)
}
