package io.mosip.tuvali.ble.central.state.message

import java.util.*

class ReadFailureMessage(val charUUID: UUID?, val err: Int) : IMessage(CentralStates.READ_FAILURE)
