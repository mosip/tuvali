package com.ble.central.state.message

import android.bluetooth.BluetoothDevice
import java.util.*

class ReadFailedMessage(val charUUID: UUID?, val err: Int) : IMessage(CentralStates.READ_FAILED)
