package io.mosip.tuvali.rnModule

import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.WritableMap
import io.mosip.tuvali.common.events.*
import kotlin.reflect.KProperty1
import kotlin.reflect.KVisibility
import kotlin.reflect.full.memberProperties

class RNEventMapper {
  companion object {
    fun toMap(event: Event): WritableMap {
      val writableMap = Arguments.createMap()

      writableMap.putString("type", getEventType(event))
      populateProperties(event, writableMap)

      return writableMap
    }

    private fun getEventType(event: Event): String {
      return when (event) {
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
            populateProperty(property, event, writableMap)
          } catch (e: Exception) {
            println("Unable to populate RN event ${property.name}")
          }
        }
      }
    }

    private fun populateProperty(property: KProperty1<out Event, *>, event: Event, writableMap: WritableMap) {
      var propertyValue = property.getter.call(event)

      if (propertyValue is Enum<*>) {
        propertyValue = readEnumValue(propertyValue)
      }

      when (propertyValue) {
        is Int -> {
          writableMap.putInt(property.name, propertyValue)
        }
        else -> {
          writableMap.putString(property.name, propertyValue.toString())
        }
      }
    }

    private fun readEnumValue(enumValue: Enum<*>): Any {
      val valueField = enumValue::class.memberProperties.firstOrNull { it.name == "value" }

      return valueField?.getter?.call(enumValue) ?: enumValue.ordinal
    }
  }
}
