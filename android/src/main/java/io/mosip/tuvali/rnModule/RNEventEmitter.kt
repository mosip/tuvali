package io.mosip.tuvali.rnModule

import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.WritableMap
import com.facebook.react.modules.core.DeviceEventManagerModule
import io.mosip.tuvali.common.events.Event
import io.mosip.tuvali.common.events.DataReceivedEvent
import io.mosip.tuvali.common.events.ErrorEvent
import io.mosip.tuvali.common.events.VerificationStatusEvent
import io.mosip.tuvali.common.events.ConnectedEvent
import io.mosip.tuvali.common.events.DataSentEvent
import io.mosip.tuvali.common.events.DisconnectedEvent
import io.mosip.tuvali.common.events.SecureChannelEstablishedEvent
import kotlin.reflect.KVisibility
import kotlin.reflect.full.memberProperties

private const val EVENT_NAME = "DATA_EVENT"

class RNEventEmitter(private val reactContext: ReactApplicationContext): IRNEventEmitter {
  override fun emitEvent(event: Event) {
    val type = getEventType(event)
    val writableMap = Arguments.createMap()
    writableMap.putString("type", type)
    populateProperties(event, writableMap)
    emitEvent(writableMap)
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

  private fun populateProperties(event: Event, writableMap: WritableMap) {
    event::class.memberProperties.forEach { property ->
      if (property.visibility == KVisibility.PUBLIC) {
        try {
          val propertyValue = property.getter.call(event)
          if (propertyValue is Enum<*>) {
            handleEnumProperty(property.name, propertyValue, writableMap)
          } else {
            writableMap.putString(property.name, propertyValue.toString())
          }
        } catch (e: Exception) {
          println("Unable to populate RN event ${property.name}")
        }
      }
    }
  }

  private fun handleEnumProperty(propertyName: String, propertyValue: Enum<*>, writableMap: WritableMap) {
    val valueField = propertyValue::class.memberProperties.firstOrNull { it.name == "value" }
    when (val enumValue = valueField?.getter?.call(propertyValue)) {
      is Int -> {
        writableMap.putInt(propertyName, enumValue)
      }
      is String -> {
        writableMap.putString(propertyName, enumValue)
      }
      else -> {
        writableMap.putInt(propertyName, propertyValue.ordinal)
      }
    }
  }
}
