package io.mosip.tuvali.ble.central.state.message

import java.util.*

class WriteMessage(
  val serviceUUID: UUID,
  val charUUID: UUID,
  val data: ByteArray
) : IMessage(CentralStates.WRITE)
