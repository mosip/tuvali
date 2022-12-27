package com.ble.central.state.message

import java.util.*

class SubscribeFailureMessage(val charUUID: UUID, val err: Int) : IMessage(CentralStates.SUBSCRIBE_FAILURE)
