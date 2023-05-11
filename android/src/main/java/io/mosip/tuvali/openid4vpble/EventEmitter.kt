package io.mosip.tuvali.openid4vpble

import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.WritableMap
import com.facebook.react.modules.core.DeviceEventManagerModule
import io.mosip.tuvali.exception.ErrorCode

private const val EVENT_NAME = "DATA_EVENT"

class EventEmitter(private val reactContext: ReactApplicationContext) {

  private fun emitEvent(eventName: String, data: WritableMap?) {
    reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java).emit(eventName, data)
  }

  fun emitDataEvent(eventType: EventTypeWithoutData) {
    val writableMap = Arguments.createMap()
    writableMap.putString("type", eventType.value)
    emitEvent(EVENT_NAME, writableMap)
  }

  fun emitTransferUpdateEvent(status: TransferUpdateStatus) {
    val writableMap = Arguments.createMap()
    writableMap.putString("type", EventTypeWithData.TRANSFER_STATUS_UPDATE.value)
    writableMap.putString("status", status.value)
    emitEvent(EVENT_NAME, writableMap)
  }

  fun emitErrorEvent(message: String, code: ErrorCode) {
    val writableMap = Arguments.createMap()
    writableMap.putString("type", EventTypeWithData.ERROR.value)
    writableMap.putString("code", code.value)
    writableMap.putString("message", message)
    emitEvent(EVENT_NAME, writableMap)
  }

  fun emitVCReceivedEvent(vc: String) {
    val writableMap = Arguments.createMap()
    writableMap.putString("type", EventTypeWithData.VC_RECEIVED.value)
    writableMap.putString("vc", vc)
    emitEvent(EVENT_NAME, writableMap)
  }

  fun emitVerificationStatusEvent(status: VerificationStatus) {
    val writableMap = Arguments.createMap()
    writableMap.putString("type", EventTypeWithData.VERIFICATION_STATUS.value)
    writableMap.putString("status", status.value)
    emitEvent(EVENT_NAME, writableMap)
  }

  enum class TransferUpdateStatus(val value: String) {
    SUCCESS("SUCCESS"), FAILURE("FAILURE"), IN_PROGRESS("IN_PROGRESS"), CANCELLED("CANCELLED")
  }

  enum class EventTypeWithoutData(val value: String) {
    CONNECTED("onConnected"),
    KEY_EXCHANGE_SUCCESS("onKeyExchangeSuccess"),
    DISCONNECTED("onDisconnected"),
  }

  enum class EventTypeWithData(val value: String) {
    TRANSFER_STATUS_UPDATE("onTransferStatusUpdate"),
    VC_RECEIVED("onVCReceived"),
    ERROR("onError"),
    VERIFICATION_STATUS("onVerificationStatusReceived")
  }

  enum class VerificationStatus(val value: String) {
    ACCEPTED("ACCEPTED"), REJECTED("REJECTED")
  }

}
