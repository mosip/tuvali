package io.mosip.tuvali.rnModule

import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.WritableMap
import com.facebook.react.modules.core.DeviceEventManagerModule
import io.mosip.tuvali.common.events.Event
import io.mosip.tuvali.common.events.withArgs.DataReceivedEvent
import io.mosip.tuvali.common.events.withArgs.ErrorEvent
import io.mosip.tuvali.common.events.withArgs.VerificationStatusEvent
import io.mosip.tuvali.common.events.withoutArgs.ConnectedEvent
import io.mosip.tuvali.common.events.withoutArgs.DataSentEvent
import io.mosip.tuvali.common.events.withoutArgs.DisconnectedEvent
import io.mosip.tuvali.common.events.withoutArgs.SecureChannelEstablishedEvent
import kotlin.reflect.KVisibility
import kotlin.reflect.full.memberProperties

private const val EVENT_NAME = "DATA_EVENT"

class RNEventEmitter(private val reactContext: ReactApplicationContext) {

  fun emitEvent(event: Event) {
    val type = getEventType(event)
    val writableMap = Arguments.createMap()
    writableMap.putString("type", type)
    populateProperties(event, writableMap)
    emitEvent(writableMap)
  }

  private fun populateProperties(event: Event, writableMap: WritableMap) {
    event::class.memberProperties.forEach {
      if (it.visibility === KVisibility.PUBLIC) try {
        writableMap.putString(it.name, it.getter.call(event).toString())
      }
      catch (e: Exception){
        println("unable to populate RN event ${it.name}")
      }
    }
  }

  private fun emitEvent(data: WritableMap?) {
    reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java).emit(EVENT_NAME, data)
  }

  private fun getEventType(event: Event): String{
    return when(event) {
      is DataReceivedEvent -> "onDataReceived"
      is ErrorEvent -> "onError"
      is VerificationStatusEvent -> "onVerificationStatusReceived"
      is ConnectedEvent -> "onConnected"
      is DataSentEvent -> "onDataSent"
      is DisconnectedEvent -> "onDisconnected"
      is SecureChannelEstablishedEvent -> "onSecureChannelEstablished"
      else -> {
        println("Invalid event type")
        return ""
      }
    }
  }
}
