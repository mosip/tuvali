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
    Init,
    GattServerReady,
    Advertising,
    ConnectedToDevice,
    CommunicationReady,
    Disconnecting,
    NotConnectedToDevice,
    Closing,
    Closed
  }

  private var currentState: States = States.Init

  override fun handleMessage(msg: Message) {
    // TODO: Figure out how to enforce exhaustive checks here
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
        //TODO: Handle this
      }

      IMessage.PeripheralMessageTypes.ADV_START.ordinal -> {
        Log.d(logTag, "start advertisement")
        controller.startAdvertisement(msg.obj as AdvertisementStartMessage)
      }
      IMessage.PeripheralMessageTypes.ADV_STOP.ordinal -> {
        Log.d(logTag, "stopping advertisement")
        controller.stopAdvertisement()
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
        Log.d(logTag, "on device not connected: status: ${deviceNotConnectedMessage.status}, newState: ${deviceNotConnectedMessage.newState} $currentState")

        if(currentState == States.Closing) {
          this.sendMessage(CloseServerMessage())
        } else {
          peripheralListener.onDeviceNotConnected(currentState >= States.Disconnecting, currentState == States.CommunicationReady)
        }
        currentState = States.NotConnectedToDevice
      }

      IMessage.PeripheralMessageTypes.MTU_CHANGED.ordinal -> {
        val mtuChangedMessage = msg.obj as MtuChangedMessage
        val mtuSize = mtuChangedMessage.mtuChanged
        peripheralListener.onMTUChanged(mtuSize)
      }

      IMessage.PeripheralMessageTypes.RECEIVED_WRITE.ordinal -> {
        val receivedWriteMessage = msg.obj as ReceivedWriteMessage
        Log.d(logTag, "received write: characteristicUUID: ${receivedWriteMessage.characteristic?.uuid}, dataSize: ${receivedWriteMessage.data?.size}")
        if (receivedWriteMessage.characteristic != null) {
          peripheralListener.onReceivedWrite(receivedWriteMessage.characteristic.uuid, receivedWriteMessage.data)
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
        currentState = States.Disconnecting
      }

      IMessage.PeripheralMessageTypes.DISCONNECT_AND_CLOSE_DEVICE.ordinal -> {
        Log.d(logTag, "disconnect and close gatt server")
        val disconnecting = controller.disconnect()

        currentState = if(disconnecting) {
          sendMessageDelayed(CloseOnDisconnectTimeoutMessage(), 50)
          States.Closing
        } else {
          this.sendMessage(CloseServerMessage())
          States.NotConnectedToDevice
        }
      }
      IMessage.PeripheralMessageTypes.CLOSE_ON_DISCONNECT_TIMEOUT.ordinal -> {
        if(currentState === States.Closing) {
          Log.d(logTag, "closing gatt client due to disconnect timeout")
          this.sendMessage(CloseServerMessage())
          currentState = States.NotConnectedToDevice
        }
      }
      IMessage.PeripheralMessageTypes.CLOSE_SERVER.ordinal -> {
        Log.d(logTag, "closing gatt server")
        controller.closeServer()
        currentState = States.Closed
        peripheralListener.onClosed()
      }
    }
  }

  override fun getCurrentState() : States {
    return currentState
  }

  override fun sendMessage(msg: IMessage) {
    val message = this.obtainMessage()
    message.what = msg.messageType.ordinal
    message.obj = msg
    val isSent = this.sendMessage(message)
    if (!isSent) {
      Log.e(logTag, "sendMessage to state handler for ${msg.messageType} failed")
    }
  }

  override fun sendMessageDelayed(msg: IMessage, delay: Long) {
    val message = this.obtainMessage()
    message.what = msg.messageType.ordinal
    message.obj = msg
    val isSent = this.sendMessageDelayed(message, delay)
    if (!isSent) {
      Log.e(logTag, "sendMessage to state handler for ${msg.messageType} failed")
    }
  }
}
