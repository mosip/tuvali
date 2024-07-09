package io.mosip.tuvali.ble.central.state.message

import android.bluetooth.BluetoothDevice
import java.util.*

class WriteSuccessMessage(val device: BluetoothDevice?, val charUUID: UUID) : IMessage(CentralStates.WRITE_SUCCESS)
