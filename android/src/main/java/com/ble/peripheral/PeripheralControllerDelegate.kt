package com.ble.peripheral

import android.content.Context
import com.ble.peripheral.impl.Advertiser
import com.ble.peripheral.impl.GattServer
import com.ble.peripheral.statemessage.AdvertisementStartFailureMessage
import com.ble.peripheral.statemessage.AdvertisementStartMessage
import com.ble.peripheral.statemessage.AdvertisementStartSuccessMessage
import com.ble.peripheral.statemessage.IPeripheralMessage

class PeripheralControllerDelegate(context: Context) {
  private var advertiser: Advertiser
  private var gattServer: GattServer
  private lateinit var messageSender: IPeripheralSendMessage

  init {
    gattServer = GattServer(context)
    advertiser = Advertiser(context)
    gattServer.start()
  }

  fun setHandlerThread(messageSender: IPeripheralSendMessage) {
    this.messageSender = messageSender
  }

  fun startAdvertisement(advertisementStartMessage: AdvertisementStartMessage) {
    advertiser.start(
      advertisementStartMessage.serviceUUID,
      advertisementStartMessage.scanRespUUID,
      advertisementStartMessage.advPayload,
      advertisementStartMessage.scanRespPayload,
      this::onAdvertisementStartSuccess,
      this::onAdvertisementStartFailure
    )
  }

  private fun onAdvertisementStartSuccess() {
    val advertisementStartSuccessMessage = AdvertisementStartSuccessMessage()
    messageSender.sendMessage(advertisementStartSuccessMessage)
  }

  private fun onAdvertisementStartFailure(errorCode: Int) {
    val advertisementStartFailureMessage = AdvertisementStartFailureMessage(errorCode)
    messageSender.sendMessage(advertisementStartFailureMessage)
  }
}
