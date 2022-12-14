package com.ble.peripheral

import android.content.Context
import com.ble.peripheral.impl.Advertiser
import com.ble.peripheral.impl.GattServer
import com.ble.statemessage.AdvertisementStartFailureMessage
import com.ble.statemessage.AdvertisementStartMessage
import com.ble.statemessage.AdvertisementStartSuccessMessage
import com.ble.statemessage.IMessage

class PeripheralControllerDelegate(context: Context) {
  private var advertiser: Advertiser
  private var gattServer: GattServer
  private lateinit var messageSender: ISendMessage

  init {
    gattServer = GattServer(context)
    advertiser = Advertiser(context)
    gattServer.start()
  }

  fun setHandlerThread(messageSender: ISendMessage) {
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
    val advertisementStartSuccessMessage =
      AdvertisementStartSuccessMessage(IMessage.PeripheralStates.ADV_START_SUCCESS)
    messageSender.sendMessage(advertisementStartSuccessMessage)
  }

  private fun onAdvertisementStartFailure(errorCode: Int) {
    val advertisementStartFailureMessage =
      AdvertisementStartFailureMessage(IMessage.PeripheralStates.ADV_START_FAILURE, errorCode)
    messageSender.sendMessage(advertisementStartFailureMessage)
  }
}
