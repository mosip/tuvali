package io.mosip.tuvali.openid4vpble

import android.util.Log
import com.facebook.react.bridge.*
import com.facebook.react.modules.core.DeviceEventManagerModule.RCTDeviceEventEmitter
import io.mosip.tuvali.common.safeExecute.TryExecuteSync
import io.mosip.tuvali.exception.handlers.OpenIdBLEExceptionHandler
import io.mosip.tuvali.exception.ErrorCode
import io.mosip.tuvali.transfer.Util.Companion.getLogTag
import io.mosip.tuvali.verifier.Verifier
import io.mosip.tuvali.wallet.Wallet
import org.json.JSONObject

class VerifierModule(private val reactContext: ReactApplicationContext) :
  ReactContextBaseJavaModule(reactContext) {

  private val reactUtils = ReactUtils(reactContext)
  private val logTag = getLogTag(javaClass.simpleName)
  private var verifier: Verifier? = null
  private var bleExceptionHandler = OpenIdBLEExceptionHandler(reactUtils::emitNearbyErrorEvent, this::stopBLE)
  private val tryExecuteSync = TryExecuteSync(bleExceptionHandler)

  //Inji contract requires double quotes around the states.
  enum class VCResponseStates(val value: String) {
    RECEIVED("\"RECEIVED\""),
    ACCEPTED("\"ACCEPTED\""),
    REJECTED("\"REJECTED\"")
  }

  enum class NearbyEvents(val value: String) {
    EXCHANGE_RECEIVER_INFO("exchange-receiver-info"),
    EXCHANGE_SENDER_INFO("exchange-sender-info"),
    SEND_VC("send-vc"),
    SEND_VC_RESPONSE("send-vc:response")
  }

  override fun getName(): String {
    return NAME
  }

  @ReactMethod(isBlockingSynchronousMethod = true)
  fun setTuvaliVersion(version: String) {
    tuvaliVersion = version
  }

  @ReactMethod(isBlockingSynchronousMethod = true)
  fun getConnectionParameters(): String {
    Log.d(logTag, "getConnectionParameters new verifier object at ${System.nanoTime()}")

    return tryExecuteSync.run {
      if (verifier == null) {
        Log.d(logTag, "synchronized getConnectionParameters new verifier object at ${System.nanoTime()}")
        verifier = Verifier(reactContext, reactUtils::emitNearbyMessage, reactUtils::emitNearbyEvent, bleExceptionHandler::handleException)
        verifier?.generateKeyPair()
      }

      val payload = verifier?.getAdvIdentifier("OVPMOSIP")
      Log.d(
        logTag,
        "synchronized getConnectionParameters called with adv identifier $payload at ${System.nanoTime()} and verifier hashcode: ${verifier.hashCode()}"
      )

      return@run "{\"cid\":\"ilB8l\",\"pk\":\"${payload}\"}"
    }.orEmpty()
  }

  @ReactMethod(isBlockingSynchronousMethod = true)
  fun getConnectionParametersDebug(): String {
    return getConnectionParameters()
  }

  @ReactMethod
  fun createConnection(callback: Callback) {
    Log.d(logTag, "createConnection: received request at ${System.nanoTime()}")

    tryExecuteSync.run {
      Log.d(logTag, "synchronized createConnection: received request at ${System.nanoTime()}")
      verifier?.startAdvertisement("OVPMOSIP", callback)
    }
  }

  @ReactMethod(isBlockingSynchronousMethod = true)
  fun destroyConnection(callback: Callback) {
    //TODO: Make sure callback can be called only once[Can be done once wallet and verifier split into different modules]
    Log.d(logTag, "destroyConnection called at ${System.nanoTime()}")
    tryExecuteSync.run {
      stopBLE(callback)
    }
  }

  private fun stopBLE(callback: Callback) {
    if (verifier == null) {
      callback()
    } else {
      Log.d(logTag, "synchronized destroyConnection called for verifier at ${System.nanoTime()}")
      stopVerifier { callback() }
    }
  }

  private fun stopVerifier(onDestroy: Callback) {
    try {
      verifier?.stop(onDestroy)
    } finally {
      Log.d(logTag, "stopVerifier: setting to null")
      verifier = null
    }
  }

  @ReactMethod
  fun send(message: String, callback: Callback) {
    Log.d(logTag, "send: message $message at ${System.nanoTime()}")

    tryExecuteSync.run {
      val messageSplits = message.split("\n", limit = 2)
      when (messageSplits[0]) {
        NearbyEvents.EXCHANGE_RECEIVER_INFO.value -> {
          callback()
        }
        NearbyEvents.SEND_VC_RESPONSE.value -> {
          verifier?.notifyVerificationStatus(messageSplits[1] == VCResponseStates.ACCEPTED.value)
        }
        else -> {
          Log.d(logTag, "send: received send with unrecognized event")
        }
      }
    }
  }

  companion object {
    var tuvaliVersion: String = "unknown"
    const val NAME = "VerifierModule"
  }
}
