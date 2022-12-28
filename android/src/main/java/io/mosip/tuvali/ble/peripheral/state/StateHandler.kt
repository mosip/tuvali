package io.mosip.tuvali.ble.peripheral.state

import android.bluetooth.BluetoothGatt
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import io.mosip.tuvali.ble.peripheral.IPeripheralListener
import io.mosip.tuvali.ble.peripheral.impl.Controller
import io.mosip.tuvali.ble.peripheral.state.message.*
import com.facebook.common.util.Hex

class StateHandler(
  looper: Looper,
  private val controller: Controller,
  private val peripheralListener: IPeripheralListener
) : Handler(looper), IMessageSender {
  private val logTag = "PeripheralHandlerThread"

  enum class States {
    GattServerReady,
    Advertising,
    ConnectedToDevice,
    CommunicationReady
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
        Log.e(logTag, "advertisement start failed")
        val failureMsg = msg.obj as AdvertisementStartFailureMessage
        peripheralListener.onAdvertisementStartFailed(failureMsg.errorCode)
      }

      IMessage.PeripheralMessageTypes.DEVICE_CONNECTED.ordinal -> {
        val deviceConnectedMessage = msg.obj as DeviceConnectedMessage
        Log.d(logTag, "on device connected: status: ${deviceConnectedMessage.status}, newState: ${deviceConnectedMessage.newState}")
        currentState = States.ConnectedToDevice
        peripheralListener.onDeviceConnected()
      }
      IMessage.PeripheralMessageTypes.DEVICE_NOT_CONNECTED.ordinal -> {
        val deviceNotConnectedMessage = msg.obj as DeviceNotConnectedMessage
        Log.d(logTag, "on device not connected: status: ${deviceNotConnectedMessage.status}, newState: ${deviceNotConnectedMessage.newState}")
      }

      IMessage.PeripheralMessageTypes.RECEIVED_WRITE.ordinal -> {
        val receivedWriteMessage = msg.obj as ReceivedWriteMessage
        Log.d(logTag, "received write: characteristicUUID: ${receivedWriteMessage.characteristic?.uuid}, dataSize: ${receivedWriteMessage.data?.size}")
        if (receivedWriteMessage.characteristic != null) {
          peripheralListener.onReceivedWrite(receivedWriteMessage.characteristic.uuid, receivedWriteMessage.data)
        }
      }

      // TODO: Can be removed not hit
      IMessage.PeripheralMessageTypes.ON_READ.ordinal -> {
        val onReadMessage = msg.obj as OnReadMessage
        Log.d(logTag, "on Read: characteristicUUID: ${onReadMessage.characteristic?.uuid}, isReadSuccessful: ${onReadMessage.isRead}")
        if (onReadMessage.characteristic != null) {
          peripheralListener.onRead(onReadMessage.characteristic.uuid, onReadMessage.isRead)
        }
      }

      IMessage.PeripheralMessageTypes.ENABLE_COMMUNICATION.ordinal -> {
        currentState = States.CommunicationReady
        Log.d(logTag, "enabled communication")
      }

      IMessage.PeripheralMessageTypes.SEND_DATA.ordinal -> {
        val sendDataMessage = msg.obj as SendDataMessage
        Log.d(logTag, "sendData: uuid: ${sendDataMessage.charUUID}, " +
          "dataSize: ${sendDataMessage.data.size}, data: ${Hex.encodeHex(sendDataMessage.data, false)}")
        controller.sendData(sendDataMessage)
      }

      IMessage.PeripheralMessageTypes.SEND_DATA_NOTIFIED.ordinal -> {
        val sendDataNotifiedMessage = msg.obj as SendDataTriggeredMessage
        Log.d(logTag, "sendData: uuid: ${sendDataNotifiedMessage.charUUID}, isNotified: ${sendDataNotifiedMessage.isNotificationTriggered}")
        peripheralListener.onSendDataNotified(sendDataNotifiedMessage.charUUID, sendDataNotifiedMessage.isNotificationTriggered)
      }

      IMessage.PeripheralMessageTypes.DISCONNECT.ordinal -> {
        Log.d(logTag, "disconnecting device")
        controller.disconnect()
      }

      IMessage.PeripheralMessageTypes.CLOSE_SERVER.ordinal -> {
        Log.d(logTag, "closing gatt server")
        controller.closeServer()
      }
    }
  }

  override fun getCurrentState() : States {
    return currentState
  }

  override fun sendMessage(msg: IMessage) {
    val message = this.obtainMessage()
    message.what = msg.commandType.ordinal
    message.obj = msg
    this.sendMessage(message)
  }
}
