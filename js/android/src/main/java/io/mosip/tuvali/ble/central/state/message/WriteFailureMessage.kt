package io.mosip.tuvali.ble.central.state.message

import android.bluetooth.BluetoothDevice
import java.util.*

class WriteFailureMessage(val device: BluetoothDevice?, val charUUID: UUID, val err: Int) : IMessage(CentralStates.WRITE_FAILURE)
