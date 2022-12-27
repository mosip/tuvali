package com.ble.central.state.message

import java.util.*

class SubscribeMessage(
  val serviceUUID: UUID,
  val charUUID: UUID,
) : IMessage(CentralStates.SUBSCRIBE)
