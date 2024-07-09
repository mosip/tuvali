package io.mosip.tuvali.verifier

import android.content.Context
import android.util.Log
import io.mosip.tuvali.common.events.DisconnectedEvent
import io.mosip.tuvali.common.events.Event
import io.mosip.tuvali.common.events.EventEmitter
import io.mosip.tuvali.common.events.VerificationStatusEvent
import io.mosip.tuvali.common.safeExecute.TryExecuteSync
import io.mosip.tuvali.common.uri.OpenId4vpURI
import io.mosip.tuvali.exception.handlers.ExceptionHandler
import io.mosip.tuvali.transfer.Util.Companion.getLogTag
import org.bouncycastle.util.encoders.Hex

class Verifier(private val context: Context): IVerifier {
  private val logTag = getLogTag(javaClass.simpleName)
  private var communicator: VerifierBleCommunicator? = null
  private var eventEmitter = EventEmitter()
  private var bleExceptionHandler = ExceptionHandler(eventEmitter::emitErrorEvent, this::stopBLE)
  private val tryExecuteSync = TryExecuteSync(bleExceptionHandler)

  override fun startAdvertisement(advIdentifier: String): String {
    Log.d(logTag, "startAdvertisement called with advIdentifier $advIdentifier at ${System.nanoTime()}")

    return tryExecuteSync.run {
      Log.d(logTag, "synchronized startAdvertisement called with adv identifier $advIdentifier at ${System.nanoTime()} and verifier hashcode: ${communicator.hashCode()}")

      initializeBLECommunicator()
      communicator?.startAdvertisement(advIdentifier)

      return@run OpenId4vpURI(advIdentifier, Hex.toHexString(communicator!!.publicKey)).toString()
    }.orEmpty()
  }

  override fun disconnect() {
    Log.d(logTag, "destroyConnection called at ${System.nanoTime()}")
    tryExecuteSync.run {
      stopBLE { eventEmitter.emitEvent(DisconnectedEvent()) }
    }
  }

  override fun sendVerificationStatus(status: Int) {
    Log.d(logTag, "sendVerificationStatus status $status at ${System.nanoTime()}")

    tryExecuteSync.run {
      communicator?.notifyVerificationStatus(status == VerificationStatusEvent.VerificationStatus.ACCEPTED.value)
    }
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

  private fun initializeBLECommunicator() {
    Log.d(logTag, "Initializing new verifier object at ${System.nanoTime()}")
    communicator = VerifierBleCommunicator(context, eventEmitter, this::stopBLE, bleExceptionHandler::handleException)
    communicator?.generateKeyPair()
  }

  private fun stopBLE(callback: () -> Unit) {
    if (communicator == null) {
      callback()
    } else {
      Log.d(logTag, "synchronized destroyConnection called for verifier at ${System.nanoTime()}")
      stopVerifier { callback() }
    }
  }

  private fun stopVerifier(callback: () -> Unit) {
    try {
      communicator?.stop(callback)
    } finally {
      Log.d(logTag, "stopVerifier: setting to null")
      communicator = null
    }
  }
}
