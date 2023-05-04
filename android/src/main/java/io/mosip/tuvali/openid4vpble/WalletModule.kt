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

class WalletModule(private val reactContext: ReactApplicationContext) :
  ReactContextBaseJavaModule(reactContext) {

  private val reactUtils = ReactUtils(reactContext)
  private val logTag = getLogTag(javaClass.simpleName)
  private var wallet: Wallet? = null
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
  fun setConnectionParameters(params: String) {
    Log.d(logTag, "setConnectionParameters at ${System.nanoTime()}")

    tryExecuteSync.run {
      if (wallet == null) {
        Log.d(logTag, "synchronized setConnectionParameters new wallet object at ${System.nanoTime()}")
        wallet = Wallet(reactContext, reactUtils::emitNearbyMessage, reactUtils::emitNearbyEvent, bleExceptionHandler::handleException)
      }
      val paramsObj = JSONObject(params)
      val firstPartOfPk = paramsObj.getString("pk")
      Log.d(
        logTag,
        "synchronized setConnectionParameters called with $params and $firstPartOfPk at ${System.nanoTime()}"
      )
      wallet?.setAdvIdentifier(firstPartOfPk)
    }
  }

  @ReactMethod
  fun createConnection(callback: Callback) {
    Log.d(logTag, "createConnection: received request at ${System.nanoTime()}")

    tryExecuteSync.run {
      Log.d(logTag, "synchronized createConnection: received request at ${System.nanoTime()}")
      wallet?.startScanning("OVPMOSIP", callback)
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
    if (wallet == null) {
      callback()
    } else {
      Log.d(logTag, "synchronized destroyConnection called for wallet at ${System.nanoTime()}")
      stopWallet { callback() }
    }
  }

  private fun stopWallet(onDestroy: Callback) {
    try {
      wallet?.stop(onDestroy)
    } catch (e: Exception) {
      Log.e(logTag, "stopWallet: exception: ${e.message}")
      Log.e(logTag, "stopWallet: exception: ${e.stackTrace}")
      Log.e(logTag, "stopWallet: exception: $e")
    } finally {
      Log.d(logTag, "stopWallet: setting to null")
      wallet = null
    }
  }

  @ReactMethod
  fun send(message: String, callback: Callback) {
    Log.d(logTag, "send: message $message at ${System.nanoTime()}")

    tryExecuteSync.run {
      val messageSplits = message.split("\n", limit = 2)
      when (messageSplits[0]) {
        NearbyEvents.EXCHANGE_SENDER_INFO.value -> {
          callback()
          wallet?.writeToIdentifyRequest()
        }
        NearbyEvents.SEND_VC.value -> {
          callback()
          wallet?.sendData(messageSplits[1])
        }
        else -> {
          Log.d(logTag, "send: received send with unrecognized event")
        }
      }
    }
  }

  companion object {
    var tuvaliVersion: String = "unknown"
    const val NAME = "Wallet"
  }
}
