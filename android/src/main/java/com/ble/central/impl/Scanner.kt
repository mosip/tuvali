package com.ble.central.impl

import android.content.Context
import java.util.*

class Scanner(context: Context) {
  fun start(
    serviceUUID: UUID,
    scanRespUUID: UUID,
    advPayload: String,
    onScanStartSuccess: () -> Unit,
    onScanStartFailure  : (Int) -> Unit
  ) {
    TODO("Not yet implemented")
  }
}
