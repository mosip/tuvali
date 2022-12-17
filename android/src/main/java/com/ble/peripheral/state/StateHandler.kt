package com.ble.peripheral.state

import android.bluetooth.BluetoothGatt
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import com.ble.peripheral.IPeripheralListener
import com.ble.peripheral.impl.Controller
import com.ble.peripheral.state.message.*

class StateHandler(
  looper: Looper,
  private val controller: Controller,
  private val peripheralListener: IPeripheralListener
) : Handler(looper), IMessageSender {
  private val logTag = "PeripheralHandlerThread"

  enum class States {
    GattServerReady,
    Advertising,
    ConnectedToDevice
  }

  private lateinit var currentState: States

  override fun handleMessage(msg: Message) {
    when (msg.what) {
      IMessage.PeripheralMessageTypes.SETUP_SERVICE.ordinal -> {
        Log.d(logTag, "setup service for gatt server")
        val setupGattServiceMessage = msg.obj as SetupGattServiceMessage
        controller.setupGattService(setupGattServiceMessage)
      }
      IMessage.PeripheralMessageTypes.SERVICE_ADD_STATUS.ordinal -> {
        val gattServiceAddedMessage = msg.obj as GattServiceAddedMessage
        Log.d(logTag, "gatt service added status ${gattServiceAddedMessage.status}")
        if (gattServiceAddedMessage.status == BluetoothGatt.GATT_SUCCESS) {
          currentState = States.GattServerReady
        }
      }

      IMessage.PeripheralMessageTypes.ADV_START.ordinal -> {
        Log.d(logTag, "start advertisement")
        controller.startAdvertisement(msg.obj as AdvertisementStartMessage)
      }
      IMessage.PeripheralMessageTypes.ADV_START_SUCCESS.ordinal -> {
        Log.d(logTag, "advertisement started successfully")
        peripheralListener.onAdvertisementStartSuccessful()
        currentState = States.Advertising
      }
      IMessage.PeripheralMessageTypes.ADV_START_FAILURE.ordinal -> {
        Log.d(logTag, "advertisement start failed")
        val failureMsg = msg.obj as AdvertisementStartFailureMessage
        peripheralListener.onAdvertisementStartFailed(failureMsg.errorCode)
      }

      IMessage.PeripheralMessageTypes.DEVICE_CONNECTED.ordinal -> {
        val deviceConnectedMessage = msg.obj as DeviceConnectedMessage
        Log.d(logTag, "on device connected: status: ${deviceConnectedMessage.status}, newState: ${deviceConnectedMessage.newState}")
        currentState = States.ConnectedToDevice
        //TODO: Send this info to higher layer
      }
      IMessage.PeripheralMessageTypes.DEVICE_NOT_CONNECTED.ordinal -> {
        val deviceNotConnectedMessage = msg.obj as DeviceNotConnectedMessage
        Log.d(logTag, "on device not connected: status: ${deviceNotConnectedMessage.status}, newState: ${deviceNotConnectedMessage.newState}")
      }

      IMessage.PeripheralMessageTypes.RECEIVED_WRITE.ordinal -> {
        val receivedWriteMessage = msg.obj as ReceivedWriteMessage
        Log.d(logTag, "received write: characteristicUUID: ${receivedWriteMessage.characteristic?.uuid}, dataSize: ${receivedWriteMessage.data?.size}")
        //TODO: Send this info to higher layer
      }
    }
  }

  override fun sendMessage(msg: IMessage) {
    val message = this.obtainMessage()
    message.what = msg.commandType.ordinal
    message.obj = msg
    this.sendMessage(message)
  }
}
