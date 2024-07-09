package io.mosip.tuvali.ble.peripheral.impl

import android.bluetooth.BluetoothGattCharacteristic
import android.content.Context
import android.util.Log
import io.mosip.tuvali.ble.peripheral.state.IMessageSender
import io.mosip.tuvali.ble.peripheral.state.message.*
import io.mosip.tuvali.transfer.Util.Companion.getLogTag

const val MTU_HEADER_SIZE = 3

class Controller(val context: Context) {
  private var advertiser: Advertiser? = null
  private val logTag = getLogTag(javaClass.simpleName)
  private var gattServer: GattServer? = null
  private lateinit var messageSender: IMessageSender

  fun setHandlerThread(messageSender: IMessageSender) {
    this.messageSender = messageSender
  }

  fun setupGattService(gattServiceMessage: SetupGattServiceMessage) {
    gattServer = GattServer(context)
    gattServer?.start(this::onDeviceConnected, this::onDeviceNotConnected, this::onReceivedWrite, this::onMTUChanged)
    gattServer?.addService(gattServiceMessage.service, this::onServiceAdded)
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
    val isNotificationTriggered = gattServer?.writeToChar(
      sendDataMessage.serviceUUID,
      sendDataMessage.charUUID,
      sendDataMessage.data
    )
    if(isNotificationTriggered == null) {
      Log.i(logTag, "Gatt server is null, ignoring write to char: ${sendDataMessage.charUUID}")
      return
    }
    Log.i(logTag, "Is notification triggered for char ${sendDataMessage.charUUID}: $isNotificationTriggered")
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

  private fun onMTUChanged(mtu: Int) {
    val mtuChangedMessage = MtuChangedMessage(mtu - MTU_HEADER_SIZE)
    messageSender.sendMessage(mtuChangedMessage)
  }

  fun closeServer() {
    gattServer?.close()
  }

  fun disconnect(): Boolean? {
    //TODO: remove the if condition after spurious disconnect event issue is resolved
    if(gattServer == null) {
      Log.i(logTag, "Gatt server is null, ignoring disconnect")
      return true
    }
    return gattServer?.disconnect()
  }

  fun stopAdvertisement() {
    if (advertiser != null) {
      advertiser?.stop()
    }else {
      Log.i(logTag, "Bluetooth device not available to stop advertisement")
    }
  }
}
