package io.mosip.tuvali.wallet

import android.content.Context
import android.util.Log
import io.mosip.tuvali.common.events.DisconnectedEvent
import io.mosip.tuvali.common.events.Event
import io.mosip.tuvali.common.events.EventEmitter
import io.mosip.tuvali.common.safeExecute.TryExecuteSync
import io.mosip.tuvali.common.uri.OpenId4vpURI
import io.mosip.tuvali.exception.handlers.ExceptionHandler
import io.mosip.tuvali.transfer.Util.Companion.getLogTag
import io.mosip.tuvali.wallet.exception.InvalidURIException

class Wallet(private val context: Context) : IWallet {
  private val logTag = getLogTag(javaClass.simpleName)
  private var bleCommunicator: WalletBleCommunicator? = null
  private var eventEmitter = EventEmitter()
  private var bleExceptionHandler = ExceptionHandler(eventEmitter::emitErrorEvent, this::stopBLE)
  private val tryExecuteSync = TryExecuteSync(bleExceptionHandler)


  override fun startConnection( uri: String) {
    Log.d(logTag, "startConnection with firstPartOfVerifierPK $uri at ${System.nanoTime()}")

    tryExecuteSync.run {
      Log.d(logTag, "synchronized startConnection wallet object with uri $uri at ${System.nanoTime()}")

      val openId4vpURI = OpenId4vpURI(uri)

      if(!openId4vpURI.isValid()) {
        throw InvalidURIException("Received Invalid URI: $uri")
      }

      bleCommunicator = WalletBleCommunicator(context, eventEmitter, bleExceptionHandler::handleException)
      bleCommunicator?.setAdvPayload(openId4vpURI.getName(), openId4vpURI.getHexPK())
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
        eventEmitter.emitEvent(DisconnectedEvent())
      }
    }
  }

  override fun handleDisconnect(status: Int, newState: Int) {
    bleCommunicator?.onDeviceDisconnected(false)
  }

  override fun subscribe(listener: (Event) -> Unit) {
    Log.d(logTag, "got subscribe at ${System.nanoTime()}")
    tryExecuteSync.run {
      eventEmitter.addListener(listener)
    }
  }

  override fun unSubscribe() {
    Log.d(logTag, "got unsubscribe at ${System.nanoTime()}")
    tryExecuteSync.run {
      eventEmitter.removeListeners()
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
      bleCommunicator = null //TODO : set this after VC transfer
    }
  }
}
