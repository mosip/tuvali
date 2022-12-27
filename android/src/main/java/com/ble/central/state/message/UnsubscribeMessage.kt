package com.ble.central.state.message

import java.util.*

class UnsubscribeMessage(
  val serviceUUID: UUID,
  val charUUID: UUID,
) : IMessage(CentralStates.UNSUBSCRIBE)
