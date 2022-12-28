package io.mosip.ble.central.state.message

import java.util.*

class UnsubscribeFailureMessage(val charUUID: UUID, val err: Int) : IMessage(CentralStates.UNSUBSCRIBE_FAILURE)
