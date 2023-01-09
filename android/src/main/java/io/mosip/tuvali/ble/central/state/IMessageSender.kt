package io.mosip.tuvali.ble.central.state

import io.mosip.tuvali.ble.central.state.message.IMessage

interface IMessageSender {
  fun sendMessage(msg: IMessage)
  fun getCurrentState() : StateHandler.States
}
