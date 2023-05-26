package io.mosip.tuvali.wallet

import android.util.Log
import com.facebook.react.bridge.*
import io.mosip.tuvali.common.safeExecute.TryExecuteSync
import io.mosip.tuvali.common.uri.URIUtils
import io.mosip.tuvali.exception.handlers.ExceptionHandler
import io.mosip.tuvali.common.events.IEventEmitter
import io.mosip.tuvali.common.events.withoutArgs.DisconnectedEvent
import io.mosip.tuvali.transfer.Util.Companion.getLogTag
import io.mosip.tuvali.wallet.exception.InvalidURIException

class Wallet(private val eventEmitter: IEventEmitter, private val reactContext: ReactApplicationContext) : IWallet {

  private val logTag = getLogTag(javaClass.simpleName)
  private var bleCommunicator: WalletBleCommunicator? = null
  private var bleExceptionHandler = ExceptionHandler(eventEmitter::emitError, this::stopBLE)
  private val tryExecuteSync = TryExecuteSync(bleExceptionHandler)


  override fun startConnection( uri: String) {
    Log.d(logTag, "startConnection with firstPartOfVerifierPK $uri at ${System.nanoTime()}")

    tryExecuteSync.run {
      if(!URIUtils.isValid(uri)) {
        throw InvalidURIException("Received Invalid URI: $uri")
      }

      if (bleCommunicator == null) {
        Log.d(logTag, "synchronized startConnection new wallet object with uri $uri at ${System.nanoTime()}")
        bleCommunicator = WalletBleCommunicator(reactContext, eventEmitter, bleExceptionHandler::handleException)
      }

      bleCommunicator?.setAdvPayload(URIUtils.extractPayload(uri))
      bleCommunicator?.startScanning()
    }
  }

  override fun sendData(payload: String) {
    Log.d(logTag, "send: message $payload at ${System.nanoTime()}")
    tryExecuteSync.run {
      bleCommunicator?.sendData(payload)
    }
  }

  override fun disconnect() {
    //TODO: Make sure callback can be called only once[Can be done once wallet and verifier split into different modules]
    Log.d(logTag, "destroyConnection called at ${System.nanoTime()}")
    tryExecuteSync.run {
      stopBLE {
        eventEmitter.emitEventWithoutArgs(DisconnectedEvent())
      }
    }
  }

  private fun stopBLE(callback: () -> Unit) {
    if (bleCommunicator == null) {
      callback()
    } else {
      Log.d(logTag, "synchronized destroyConnection called for wallet at ${System.nanoTime()}")
      stopWallet { callback() }
    }
  }

  private fun stopWallet(onDestroy: () -> Unit) {
    try {
      bleCommunicator?.stop(onDestroy)
    } catch (e: Exception) {
      Log.e(logTag, "stopWallet: exception: ${e.message}")
      Log.e(logTag, "stopWallet: exception: ${e.stackTrace}")
      Log.e(logTag, "stopWallet: exception: $e")
    } finally {
      Log.d(logTag, "stopWallet: setting to null")
      bleCommunicator = null
    }
  }
}
