package com.ble.central.state.message

import android.bluetooth.BluetoothDevice
import java.util.*

class WriteFailedMessage(val device: BluetoothDevice, val charUUID: UUID, val err: Int) : IMessage(CentralStates.WRITE_FAILED)
