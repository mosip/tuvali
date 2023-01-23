package io.mosip.tuvali.ble.central

import android.bluetooth.BluetoothDevice
import android.content.Context
import android.os.HandlerThread
import android.os.Process
import io.mosip.tuvali.ble.central.impl.Controller
import io.mosip.tuvali.ble.central.state.IMessageSender
import io.mosip.tuvali.ble.central.state.StateHandler
import io.mosip.tuvali.ble.central.state.message.*
import java.util.*

class Central(context: Context, centralLister: ICentralListener) {
  private val controller: Controller = Controller(context)
  private val handlerThread = HandlerThread("CentralHandlerThread", Process.THREAD_PRIORITY_DEFAULT)
  private var messageSender: IMessageSender

  init {
    handlerThread.start()
    messageSender = StateHandler(handlerThread.looper, controller, centralLister)
    controller.setHandlerThread(messageSender)
  }

  fun stop() {
    stopScan()
    disconnectAndClose()
  }

  fun quitHandler() {
    handlerThread.quitSafely()
  }

  fun disconnectAndClose() {
    messageSender.sendMessage(DisconnectAndCloseMessage())
  }

  fun scan(serviceUuid: UUID, advIdentifier: String) {
    val scanStartMessage = ScanStartMessage(serviceUuid, advIdentifier)

    messageSender.sendMessage(scanStartMessage)
  }

  fun connect(device: BluetoothDevice) {
    val connectDeviceMessage = ConnectDeviceMessage(device)

    messageSender.sendMessage(connectDeviceMessage)
  }

  fun write(serviceUuid: UUID, charUUID: UUID, data: ByteArray) {
    val writeMessage = WriteMessage(serviceUuid, charUUID, data)

    messageSender.sendMessage(writeMessage)
  }

  fun read(serviceUuid: UUID, charUUID: UUID) {
    val readMessage = ReadMessage(serviceUuid, charUUID)

    messageSender.sendMessage(readMessage)
  }

  fun subscribe(serviceUuid: UUID, charUUID: UUID) {
    val subscribeMessage = SubscribeMessage(serviceUuid, charUUID)

    messageSender.sendMessage(subscribeMessage)
  }

  fun unsubscribe(serviceUuid: UUID, charUUID: UUID) {
    val unsubscribeMessage = UnsubscribeMessage(serviceUuid, charUUID)

    messageSender.sendMessage(unsubscribeMessage)
  }

  fun discoverServices() {
    messageSender.sendMessage(DiscoverServicesMessage())
  }

  fun requestMTU(mtu: Int) {
    messageSender.sendMessage(RequestMTUMessage(mtu))
  }

  fun stopScan() {
    messageSender.sendMessage(ScanStopMessage())
  }

  fun disconnect() {
    messageSender.sendMessage(DisconnectDeviceMessage())
  }

  fun close() {
    messageSender.sendMessage(CloseMessage())
  }
}
