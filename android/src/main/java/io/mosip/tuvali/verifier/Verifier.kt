package io.mosip.tuvali.verifier

import android.content.Context
import android.util.Log
import io.mosip.tuvali.common.events.Event
import io.mosip.tuvali.common.events.EventProducer
import io.mosip.tuvali.common.safeExecute.TryExecuteSync
import io.mosip.tuvali.common.uri.URIUtils
import io.mosip.tuvali.exception.handlers.ExceptionHandler
import io.mosip.tuvali.common.events.withArgs.VerificationStatusEvent
import io.mosip.tuvali.common.events.withoutArgs.DisconnectedEvent
import io.mosip.tuvali.transfer.Util.Companion.getLogTag

class Verifier(private val context: Context): IVerifier {
  private val logTag = getLogTag(javaClass.simpleName)
  private var communicator: VerifierBleCommunicator? = null
  private var eventProducer: EventProducer = EventProducer()
  private var bleExceptionHandler = ExceptionHandler(eventProducer::emitErrorEvent, this::stopBLE)
  private val tryExecuteSync = TryExecuteSync(bleExceptionHandler)

  override fun startAdvertisement(advIdentifier: String): String {
    Log.d(logTag, "startAdvertisement called with advIdentifier $advIdentifier at ${System.nanoTime()}")

    return tryExecuteSync.run {
      if (communicator == null) {
        initializeBLECommunicator()
      }

      val payload = communicator!!.getAdvPayloadInHex(advIdentifier)
      Log.d(logTag, "synchronized startAdvertisement called with adv identifier $payload at ${System.nanoTime()} and verifier hashcode: ${communicator.hashCode()}")

      communicator?.startAdvertisement(advIdentifier)

      return@run URIUtils.build(payload)
    }.orEmpty()
  }

  override fun disconnect() {
    //TODO: Make sure callback can be called only once[Can be done once wallet and verifier split into different modules]
    Log.d(logTag, "destroyConnection called at ${System.nanoTime()}")
    tryExecuteSync.run {
      stopBLE { eventProducer.emitEvent(DisconnectedEvent()) }
    }
  }

  override fun sendVerificationStatus(status: String) {
    Log.d(logTag, "sendVerificationStatus status $status at ${System.nanoTime()}")

    tryExecuteSync.run {
      communicator?.notifyVerificationStatus(status == VerificationStatusEvent.VerificationStatus.ACCEPTED.value)
    }
  }

  override fun subscribe(consumer: (Event) -> Unit) {
    Log.d(logTag, "got subscribe at ${System.nanoTime()}")
    tryExecuteSync.run {
      eventProducer.addConsumer(consumer)
    }
  }

  override fun unSubscribe() {
    Log.d(logTag, "got unsubscribe at ${System.nanoTime()}")
    tryExecuteSync.run {
      eventProducer.removeConsumer()
    }
  }

  private fun initializeBLECommunicator() {
    Log.d(logTag, "Initializing new verifier object at ${System.nanoTime()}")
    communicator = VerifierBleCommunicator(context, eventProducer, bleExceptionHandler::handleException)
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
