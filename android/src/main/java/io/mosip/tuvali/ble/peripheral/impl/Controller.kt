package io.mosip.tuvali.ble.peripheral.impl

import android.bluetooth.BluetoothGattCharacteristic
import android.content.Context
import io.mosip.tuvali.ble.peripheral.state.IMessageSender
import io.mosip.tuvali.ble.peripheral.state.message.*

class Controller(val context: Context) {
  private var advertiser: Advertiser? = null
  private lateinit var gattServer: GattServer
  private lateinit var messageSender: IMessageSender

  fun setHandlerThread(messageSender: IMessageSender) {
    this.messageSender = messageSender
  }

  fun setupGattService(gattServiceMessage: SetupGattServiceMessage) {
    gattServer = GattServer(context)
    gattServer.start(this::onDeviceConnected, this::onDeviceNotConnected, this::onReceivedWrite)
    gattServer.addService(gattServiceMessage.service, this::onServiceAdded)
  }

  fun startAdvertisement(advertisementStartMessage: AdvertisementStartMessage) {
    advertiser = Advertiser(context)
    advertiser?.start(
      advertisementStartMessage.serviceUUID,
      advertisementStartMessage.scanRespUUID,
      advertisementStartMessage.advPayload,
      advertisementStartMessage.scanRespPayload,
      this::onAdvertisementStartSuccess,
      this::onAdvertisementStartFailure
    )
  }

  fun sendData(sendDataMessage: SendDataMessage) {
    val isNotificationTriggered = gattServer.writeToChar(
      sendDataMessage.serviceUUID,
      sendDataMessage.charUUID,
      sendDataMessage.data
    )
    val sendDataNotifiedMessage =
      SendDataTriggeredMessage(sendDataMessage.charUUID, isNotificationTriggered)
    messageSender.sendMessage(sendDataNotifiedMessage)
  }

  private fun onServiceAdded(status: Int) {
    val gattServiceAddedMessage = GattServiceAddedMessage(status)
    messageSender.sendMessage(gattServiceAddedMessage)
  }

  private fun onAdvertisementStartSuccess() {
    val advertisementStartSuccessMessage = AdvertisementStartSuccessMessage()
    messageSender.sendMessage(advertisementStartSuccessMessage)
  }

  private fun onAdvertisementStartFailure(errorCode: Int) {
    val advertisementStartFailureMessage = AdvertisementStartFailureMessage(errorCode)
    messageSender.sendMessage(advertisementStartFailureMessage)
  }

  private fun onDeviceConnected(status: Int, newState: Int) {
    val deviceConnectedMessage = DeviceConnectedMessage(status, newState)
    messageSender.sendMessage(deviceConnectedMessage)
  }

  private fun onDeviceNotConnected(status: Int, newState: Int) {
    val deviceNotConnectedMessage = DeviceNotConnectedMessage(status, newState)
    messageSender.sendMessage(deviceNotConnectedMessage)
  }

  private fun onReceivedWrite(characteristic: BluetoothGattCharacteristic?, value: ByteArray?) {
    val receivedWriteMessage = ReceivedWriteMessage(characteristic, value)
    messageSender.sendMessage(receivedWriteMessage)
  }

  fun closeServer() {
    gattServer.close()
  }

  fun disconnect(): Boolean {
    return gattServer.disconnect()
  }

  fun stopAdvertisement() {
    if (advertiser != null) {
      advertiser?.stop()
    }
  }
}
