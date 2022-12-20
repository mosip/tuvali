package com.ble.peripheral.impl

import android.bluetooth.BluetoothGattCharacteristic
import android.content.Context
import com.ble.peripheral.state.IMessageSender
import com.ble.peripheral.state.message.*

@OptIn(ExperimentalUnsignedTypes::class)
class Controller(context: Context) {
  private var advertiser: Advertiser
  private var gattServer: GattServer
  private lateinit var messageSender: IMessageSender

  init {
    gattServer = GattServer(context)
    advertiser = Advertiser(context)
    gattServer.start(this::onDeviceConnected, this::onDeviceNotConnected, this::onReceivedWrite, this::onRead)
  }

  fun setHandlerThread(messageSender: IMessageSender) {
    this.messageSender = messageSender
  }

  fun setupGattService(gattServiceMessage: SetupGattServiceMessage) {
    gattServer.addService(gattServiceMessage.service, this::onServiceAdded)
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

  private fun onRead(characteristic: BluetoothGattCharacteristic?, isRead: Boolean) {
    val onReadMessage = OnReadMessage(characteristic, isRead)
    messageSender.sendMessage(onReadMessage)
  }
}
