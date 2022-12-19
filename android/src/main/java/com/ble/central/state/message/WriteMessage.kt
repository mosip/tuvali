package com.ble.central.state.message

import java.util.*

class WriteMessage(
  val serviceUUID: UUID,
  val charUUID: UUID,
  val data: String
) : IMessage(CentralStates.WRITE)
