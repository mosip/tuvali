package com.ble

import android.content.Context
import com.ble.handlercommand.AdvertisementCommand
import com.ble.peripheral.Advertiser
import com.ble.peripheral.GattServer
import java.util.*

class PeripheralHandlerDelegate(context: Context) {
  private var advertiser: Advertiser
  private var gattServer: GattServer

  init {
    gattServer = GattServer(context)
    advertiser = Advertiser(context)
    gattServer.start()
  }

  fun startAdvertisement(advertisementCommand: AdvertisementCommand) {
    advertiser.start(advertisementCommand.serviceUUID, advertisementCommand.scanRespUUID, advertisementCommand.advPayload, advertisementCommand.scanRespPayload)
  }

  fun onAdvertisementStartSuccess() {

  }
}
