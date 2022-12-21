package com.ble.central.state

import android.annotation.SuppressLint
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import com.ble.central.impl.Controller
import com.ble.central.ICentralListener
import com.ble.central.state.message.*

class StateHandler(
  looper: Looper,
  private val controller: Controller,
  private val listener: ICentralListener
) : Handler(looper), IMessageSender {
  private val logTag = "CentralHandlerThread"
  private var currentState: States = States.Init

  enum class States {
    Init,
    Scanning,
    WaitingToConnect,
    Connecting,
    Connected,
    DiscoveringServices,
    RequestingMTU,
    Writing,
    Reading
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
      IMessage.CentralStates.DEVICE_CONNECTED.ordinal -> {
        val deviceConnectedMessage = msg.obj as DeviceConnectedMessage
        Log.d(logTag, "device-${deviceConnectedMessage.device.name} connected successfully")

        listener.onDeviceConnected(deviceConnectedMessage.device)
        currentState = States.Connected
      }
      IMessage.CentralStates.DEVICE_DISCONNECTED.ordinal -> {
        Log.d(logTag, "device got disconnected.")
        listener.onDeviceDisconnected()
        currentState = States.Init
      }
      IMessage.CentralStates.DISCOVER_SERVICES.ordinal -> {
        Log.d(logTag, "discovering services.")
        controller.discoverServices()
        currentState = States.DiscoveringServices
      }
      IMessage.CentralStates.DISCOVER_SERVICES_SUCCESS.ordinal -> {
        Log.d(logTag, "discovered services.")
        listener.onServicesDiscovered()
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
      IMessage.CentralStates.WRITE_FAILED.ordinal -> {
        val writeFailedMessage = msg.obj as WriteFailedMessage;

        Log.d(logTag, "write failed for ${writeFailedMessage.charUUID} due to ${writeFailedMessage.err}")
        listener.onWriteFailed(writeFailedMessage.device, writeFailedMessage.charUUID, writeFailedMessage.err)
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
        Log.d(logTag, "Read successfully from ${readSuccessMessage.charUUID} and value${readSuccessMessage.value}")

        listener.onReadSuccess(readSuccessMessage.charUUID, readSuccessMessage.value)
        currentState = States.Reading
      }
      IMessage.CentralStates.READ_FAILED.ordinal -> {
        val readFailedMessage = msg.obj as ReadFailedMessage
        Log.d(logTag, "Read failed for ${readFailedMessage.charUUID} with err: ${readFailedMessage.err}")

        listener.onReadFailure(readFailedMessage.charUUID, readFailedMessage.err)
        currentState = States.Reading
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
