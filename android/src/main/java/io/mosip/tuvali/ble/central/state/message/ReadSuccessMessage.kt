package io.mosip.tuvali.ble.central.state.message

import java.util.*

class ReadSuccessMessage(val charUUID: UUID, val value: ByteArray?) : IMessage(CentralStates.READ_SUCCESS)
