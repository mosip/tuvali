package io.mosip.tuvali.ble.central.state

import android.annotation.SuppressLint
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import io.mosip.tuvali.ble.central.impl.Controller
import io.mosip.tuvali.ble.central.ICentralListener
import io.mosip.tuvali.ble.central.state.message.*

class StateHandler(
  looper: Looper,
  private val controller: Controller,
  private val listener: ICentralListener
) : Handler(looper), IMessageSender {
  private val logTag = "CentralHandlerThread"
  private var currentState: States = States.Init

  //TODO: Add explicit ordinal value for below enum
  enum class States {
    Init,
    Scanning,
    WaitingToConnect,
    Connecting,
    Disconnecting,
    Disconnected,
    Connected,
    DiscoveringServices,
    RequestingMTU,
    Writing,
    Reading,
    Subscribing,
    Unsubscribing,
    Closing,
    Closed
  }

  @SuppressLint("MissingPermission")
  override fun handleMessage(msg: Message) {
    when (msg.what) {
      IMessage.CentralStates.SCAN_START.ordinal -> {
        Log.d(logTag, "starting scan.")
        controller.scan(msg.obj as ScanStartMessage)
        currentState = States.Scanning
      }
      IMessage.CentralStates.SCAN_STOP.ordinal -> {
        Log.d(logTag, "stopping scan.")
        controller.stopScan()
        currentState = States.Init
      }
      IMessage.CentralStates.SCAN_START_FAILURE.ordinal -> {
        Log.d(logTag, "scan failed to start.")
        val failureMsg = msg.obj as ScanStartFailureMessage
        listener.onScanStartedFailed(failureMsg.errorCode)

        currentState = States.Init
      }
      IMessage.CentralStates.DEVICE_FOUND.ordinal -> {
        Log.d(logTag, "peripheral device found successfully with ${msg.obj}")
        val deviceFoundMessage = msg.obj as DeviceFoundMessage

        listener.onDeviceFound(deviceFoundMessage.device, deviceFoundMessage.scanRecord)
        currentState = States.WaitingToConnect
      }
      IMessage.CentralStates.CONNECT_DEVICE.ordinal -> {
        val connectDeviceMessage = msg.obj as ConnectDeviceMessage
        Log.d(logTag, "connecting to device ${connectDeviceMessage.device.name}")

        controller.connect(connectDeviceMessage.device)
        currentState = States.Connecting
      }
      IMessage.CentralStates.DISCONNECT_DEVICE.ordinal -> {
        Log.d(logTag, "disconnecting device")

        controller.disconnect()
        currentState = States.Disconnecting
      }
      IMessage.CentralStates.DISCONNECT_AND_CLOSE_DEVICE.ordinal -> {
        Log.d(logTag, "disconnect and close gatt client")

        val disconnecting = controller.disconnect()

        currentState = if(disconnecting) {
          sendMessageDelayed(CloseOnDisconnectTimeoutMessage(), 50)
          States.Closing
        } else {
          this.sendMessage(CloseMessage())
          States.Disconnected
        }
      }
      IMessage.CentralStates.CLOSE_ON_DISCONNECT_TIMEOUT.ordinal -> {
        if(currentState === States.Closing) {
          Log.d(logTag, "closing gatt client due to disconnect timeout")
          this.sendMessage(CloseMessage())
          currentState = States.Disconnected
        }
      }
      IMessage.CentralStates.CLOSE.ordinal -> {
        Log.d(logTag, "closing gatt client")

        controller.close()
        currentState = States.Closed
        listener.onClosed()
      }
      IMessage.CentralStates.DEVICE_CONNECTED.ordinal -> {
        val deviceConnectedMessage = msg.obj as DeviceConnectedMessage
        Log.d(logTag, "device-${deviceConnectedMessage.device.name} connected successfully")

        listener.onDeviceConnected(deviceConnectedMessage.device)
        currentState = States.Connected
      }
      IMessage.CentralStates.DEVICE_DISCONNECTED.ordinal -> {
        Log.d(logTag, "device got disconnected.")

        if(currentState == States.Closing) {
          this.sendMessage(CloseMessage())
        } else {
          listener.onDeviceDisconnected(currentState == States.Disconnecting)
        }
        currentState = States.Disconnected
      }
      IMessage.CentralStates.DISCOVER_SERVICES.ordinal -> {
        Log.d(logTag, "discovering services.")
        controller.discoverServices()
        currentState = States.DiscoveringServices
      }
      IMessage.CentralStates.DISCOVER_SERVICES_SUCCESS.ordinal -> {
        Log.d(logTag, "discovered services.")
        val discoverServicesSuccessMessage = msg.obj as DiscoverServicesSuccessMessage
        listener.onServicesDiscovered(discoverServicesSuccessMessage.serviceUuids)
        currentState = States.Connected
      }
      IMessage.CentralStates.DISCOVER_SERVICES_FAILURE.ordinal -> {
        Log.d(logTag, "failed to discover services.")
        val discoverServicesFailureMessage = msg.obj as DiscoverServicesFailureMessage
        listener.onServicesDiscoveryFailed(discoverServicesFailureMessage.errorCode)
        currentState = States.Connected
      }
      IMessage.CentralStates.REQUEST_MTU.ordinal -> {
        val requestMTUMessage = msg.obj as RequestMTUMessage
        Log.d(logTag, "request mtu change to ${requestMTUMessage.mtu}")
        controller.requestMTU(requestMTUMessage.mtu)
        currentState = States.RequestingMTU
      }
      IMessage.CentralStates.REQUEST_MTU_SUCCESS.ordinal -> {
        val requestMTUSuccessMessage = msg.obj as RequestMTUSuccessMessage

        Log.d(logTag, "MTU changed to ${requestMTUSuccessMessage.mtu}.")
        listener.onRequestMTUSuccess(requestMTUSuccessMessage.mtu)
        currentState = States.Connected
      }
      IMessage.CentralStates.REQUEST_MTU_FAILURE.ordinal -> {
        Log.d(logTag, "failed to request MTU Change.")
        val requestMTUFailureMessage = msg.obj as RequestMTUFailureMessage
        listener.onRequestMTUFailure(requestMTUFailureMessage.errorCode)
        currentState = States.Connected
      }
      IMessage.CentralStates.WRITE.ordinal -> {
        val writeMessage = msg.obj as WriteMessage
        Log.d(logTag, "starting write to ${writeMessage.charUUID}")

        controller.write(writeMessage)
        currentState = States.Writing
      }
      IMessage.CentralStates.WRITE_SUCCESS.ordinal -> {
        val writeSuccessMessage = msg.obj as WriteSuccessMessage;
        Log.d(logTag, "Completed writing to ${writeSuccessMessage.charUUID} successfully")

        listener.onWriteSuccess(writeSuccessMessage.device, writeSuccessMessage.charUUID)
        currentState = States.Connected
      }
      IMessage.CentralStates.WRITE_FAILURE.ordinal -> {
        val writeFailureMessage = msg.obj as WriteFailureMessage;

        Log.d(logTag, "write failed for ${writeFailureMessage.charUUID} due to ${writeFailureMessage.err}")
        listener.onWriteFailed(writeFailureMessage.device, writeFailureMessage.charUUID, writeFailureMessage.err)
        currentState = States.Connected
      }
      IMessage.CentralStates.READ.ordinal -> {
        val readMessage = msg.obj as ReadMessage
        Log.d(logTag, "starting read from ${readMessage.charUUID}")

        controller.read(readMessage)
        currentState = States.Reading
      }
      IMessage.CentralStates.READ_SUCCESS.ordinal -> {
        val readSuccessMessage = msg.obj as ReadSuccessMessage
        Log.d(logTag, "Read successfully from ${readSuccessMessage.charUUID} and value${readSuccessMessage.value?.decodeToString()}")

        listener.onReadSuccess(readSuccessMessage.charUUID, readSuccessMessage.value)
        currentState = States.Connected
      }
      IMessage.CentralStates.READ_FAILURE.ordinal -> {
        val readFailureMessage = msg.obj as ReadFailureMessage
        Log.d(logTag, "Read failed for ${readFailureMessage.charUUID} with err: ${readFailureMessage.err}")

        listener.onReadFailure(readFailureMessage.charUUID, readFailureMessage.err)
        currentState = States.Connected
      }

      IMessage.CentralStates.SUBSCRIBE.ordinal -> {
        val subscribeMessage = msg.obj as SubscribeMessage
        Log.d(logTag, "subscribing to ${subscribeMessage.charUUID}")

        controller.subscribe(subscribeMessage)
        currentState = States.Subscribing
      }
      IMessage.CentralStates.SUBSCRIBE_SUCCESS.ordinal -> {
        val subscribeSuccessMessage = msg.obj as SubscribeSuccessMessage
        Log.d(logTag, "Subscribed successfully to ${subscribeSuccessMessage.charUUID}")

        listener.onSubscriptionSuccess(subscribeSuccessMessage.charUUID)
        currentState = States.Connected
      }
      IMessage.CentralStates.SUBSCRIBE_FAILURE.ordinal -> {
        val subscribeFailureMessage = msg.obj as SubscribeFailureMessage
        Log.d(logTag, "failed to subscribe ${subscribeFailureMessage.charUUID} with err: ${subscribeFailureMessage.err}")

        listener.onSubscriptionFailure(subscribeFailureMessage.charUUID, subscribeFailureMessage.err)
        currentState = States.Connected
      }
      IMessage.CentralStates.NOTIFICATION_RECEIVED.ordinal -> {
        val notificationReceivedMessage = msg.obj as NotificationReceivedMessage
        Log.d(logTag, "Received notification from ${notificationReceivedMessage.charUUID} with value: ${notificationReceivedMessage.value}")

        listener.onNotificationReceived(notificationReceivedMessage.charUUID, notificationReceivedMessage.value)
      }

      IMessage.CentralStates.UNSUBSCRIBE.ordinal -> {
        val unsubscribeMessage = msg.obj as UnsubscribeMessage
        Log.d(logTag, "Unsubscribing to ${unsubscribeMessage.charUUID}")

        controller.unsubscribe(unsubscribeMessage)
        currentState = States.Unsubscribing
      }
      IMessage.CentralStates.UNSUBSCRIBE_SUCCESS.ordinal -> {
        val unsubscribeSuccessMessage = msg.obj as UnsubscribeSuccessMessage
        Log.d(logTag, "Unsubscribed successfully to ${unsubscribeSuccessMessage.charUUID}")

        listener.onSubscriptionSuccess(unsubscribeSuccessMessage.charUUID)
        currentState = States.Connected
      }
      IMessage.CentralStates.UNSUBSCRIBE_FAILURE.ordinal -> {
        val unsubscribeFailureMessage = msg.obj as UnsubscribeFailureMessage
        Log.d(logTag, "Failed to unsubscribe ${unsubscribeFailureMessage.charUUID} with err: ${unsubscribeFailureMessage.err}")

        listener.onSubscriptionFailure(unsubscribeFailureMessage.charUUID, unsubscribeFailureMessage.err)
        currentState = States.Connected
      }
    }
  }

  override fun getCurrentState(): States {
    return currentState
  }

  override fun sendMessage(msg: IMessage) {
    val message = this.obtainMessage()
    message.what = msg.commandType.ordinal
    message.obj = msg
    val isSent = this.sendMessage(message)
    if (!isSent) {
      Log.e(logTag, "sendMessage to state handler for ${msg.commandType} failed")
    }
  }

  override fun sendMessageDelayed(msg: IMessage, delay: Long) {
    val message = this.obtainMessage()
    message.what = msg.commandType.ordinal
    message.obj = msg
    val isSent = this.sendMessageDelayed(message, delay)
    if (!isSent) {
      Log.e(logTag, "sendMessageDelayed to state handler for ${msg.commandType} failed")
    }
  }
}
