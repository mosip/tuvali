package com.ble.central.state.message

import android.bluetooth.BluetoothDevice
import java.util.*

class WriteMessage(
  val device: BluetoothDevice,
  val serviceUUID: UUID,
  val charUUID: UUID,
  val data: String
) : IMessage(CentralStates.WRITE)
