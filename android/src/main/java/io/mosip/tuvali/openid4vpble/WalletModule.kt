package io.mosip.tuvali.openid4vpble

import android.util.Log
import com.facebook.react.bridge.*
import io.mosip.tuvali.common.safeExecute.TryExecuteSync
import io.mosip.tuvali.common.uri.OpenId4vpURI
import io.mosip.tuvali.exception.handlers.OpenIdBLEExceptionHandler
import io.mosip.tuvali.openid4vpble.events.withoutArgs.DisconnectedEvent
import io.mosip.tuvali.transfer.Util.Companion.getLogTag
import io.mosip.tuvali.wallet.Wallet
import io.mosip.tuvali.wallet.exception.InvalidURIException

class WalletModule(private val reactContext: ReactApplicationContext) :
  ReactContextBaseJavaModule(reactContext) {

  private val eventEmitter = EventEmitter(reactContext)
  private val logTag = getLogTag(javaClass.simpleName)
  private var wallet: Wallet? = null
  private var bleExceptionHandler = OpenIdBLEExceptionHandler(eventEmitter::emitError, this::stopBLE)
  private val tryExecuteSync = TryExecuteSync(bleExceptionHandler)


  override fun getName(): String {
    return NAME
  }

  @ReactMethod(isBlockingSynchronousMethod = true)
  fun setTuvaliVersion(version: String) {
    tuvaliVersion = version
  }


  @ReactMethod(isBlockingSynchronousMethod = true)
  fun startConnection( uri: String) {
    Log.d(logTag, "startConnection with firstPartOfVerifierPK $uri at ${System.nanoTime()}")

    tryExecuteSync.run {
      val openId4vpURI = OpenId4vpURI(uri)

      if(!openId4vpURI.isValid()) {
        throw InvalidURIException("Received Invalid URI: $uri")
      }

      if (wallet == null) {
        Log.d(logTag, "synchronized startConnection new wallet object with uri $uri at ${System.nanoTime()}")
        wallet = Wallet(reactContext, eventEmitter, bleExceptionHandler::handleException)
      }

      wallet?.setAdvPayload(openId4vpURI.getName(), openId4vpURI.getHexPK())
      wallet?.startScanning()
    }
  }

  @ReactMethod(isBlockingSynchronousMethod = true)
  fun disconnect() {
    //TODO: Make sure callback can be called only once[Can be done once wallet and verifier split into different modules]
    Log.d(logTag, "destroyConnection called at ${System.nanoTime()}")
    tryExecuteSync.run {
      stopBLE {
        eventEmitter.emitEventWithoutArgs(DisconnectedEvent())
      }
    }
  }

  private fun stopBLE(callback: () -> Unit) {
    if (wallet == null) {
      callback()
    } else {
      Log.d(logTag, "synchronized destroyConnection called for wallet at ${System.nanoTime()}")
      stopWallet { callback() }
    }
  }

  private fun stopWallet(onDestroy: () -> Unit) {
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
  fun sendData(data: String) {
    Log.d(logTag, "send: message $data at ${System.nanoTime()}")
    tryExecuteSync.run {
      wallet?.sendData(data)
    }
  }

  companion object {
    var tuvaliVersion: String = "unknown"
    const val NAME = "WalletModule"
  }
}
