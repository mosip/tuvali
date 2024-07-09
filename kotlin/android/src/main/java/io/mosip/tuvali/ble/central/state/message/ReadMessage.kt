package io.mosip.tuvali.ble.central.state.message

import java.util.*

class ReadMessage(
  val serviceUUID: UUID,
  val charUUID: UUID,
) : IMessage(CentralStates.READ)
