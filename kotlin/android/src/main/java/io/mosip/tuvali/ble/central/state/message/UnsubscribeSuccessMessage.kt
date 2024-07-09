package io.mosip.tuvali.ble.central.state.message

import java.util.*

class UnsubscribeSuccessMessage(val charUUID: UUID) : IMessage(CentralStates.UNSUBSCRIBE_SUCCESS)
