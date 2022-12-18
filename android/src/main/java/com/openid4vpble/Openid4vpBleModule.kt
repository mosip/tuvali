package com.openid4vpble

import android.util.Log
import com.facebook.react.bridge.*
import com.facebook.react.modules.core.DeviceEventManagerModule.RCTDeviceEventEmitter
import com.verifier.Verifier
import com.wallet.Wallet


class Openid4vpBleModule(reactContext: ReactApplicationContext) :
  ReactContextBaseJavaModule(reactContext) {
  private val verifier = Verifier(reactContext, this::emitNearbyEvent)
    private val wallet = Wallet(reactContext, this::emitNearbyEvent)

  private var activeMode: ModeOfOperation = ModeOfOperation.UnInitialised
  enum class ModeOfOperation {
    UnInitialised,
    Verifier,
    Wallet
  }

  override fun getName(): String {
    return NAME
  }

  @ReactMethod(isBlockingSynchronousMethod = true)
  fun getConnectionParameters(): String {
    return "{\"cid\":\"ilB8l\",\"pk\":\"${verifier.generateKeyPair()}\"}"
  }

  @ReactMethod(isBlockingSynchronousMethod = true)
  fun getConnectionParametersDebug(): String {
    return verifier.generateKeyPair()
  }

  @ReactMethod(isBlockingSynchronousMethod = true)
  fun setConnectionParameters(params: String) {
    Log.d(LOG_TAG, "setConnectionParameters called with $params")

    //TODO: Confirm the params structure(Assuming it to be Verifier PK)
    return wallet.setVerifierKey(params)
  }

  @ReactMethod
  fun createConnection(mode: String, callback: Callback) {
    Log.d(LOG_TAG, "createConnection: received request with mode $mode")
    when (mode) {
      "advertiser" -> {
        updateModeOfOperation(ModeOfOperation.Verifier)
        verifier.startAdvertisement("OVPMOSIP", callback)
      }
      "discoverer" -> {
        updateModeOfOperation(ModeOfOperation.Wallet)
        wallet.startScanning("OVPMOSIP", callback)
      }
    }
  }

  @ReactMethod(isBlockingSynchronousMethod = true)
  fun destroyConnection() {
    // TODO: Find the mode and call close
  }

  @ReactMethod
  fun send(message: String, callback: Callback) {
    // TODO: Find the mode and call send
    Log.d(LOG_TAG, "send: message $message")
    val messageSplits = message.split("\n", limit = 2)
    when(messageSplits[0]) {
      "exchange-receiver-info" -> {
        callback()
      }
    }
  }

  private fun emitEvent(eventName: String, data: WritableMap?) {
    reactApplicationContext
      .getJSModule(RCTDeviceEventEmitter::class.java)
      .emit(eventName, data)
  }

  private fun emitNearbyEvent(eventType: String, data: String) {
    val writableMap = Arguments.createMap()
    writableMap.putString("data", "$eventType\n${data}")
    writableMap.putString("type", "msg")
    emitEvent("EVENT_NEARBY", writableMap)
  }

  private fun emitLogEvent(eventType: String, data: Map<String, String>) {}

  private fun updateModeOfOperation(newMode: ModeOfOperation) {
    if (activeMode != newMode) {
      destroyConnection()
    }
    activeMode = newMode
  }

  private fun getPeerModeOfOperation(): ModeOfOperation {
    return when(activeMode) {
      ModeOfOperation.Wallet -> ModeOfOperation.Verifier
      ModeOfOperation.Verifier -> ModeOfOperation.Wallet
      else -> ModeOfOperation.UnInitialised
    }
  }

  //  noop: () => void;

  // Verifier uses this function
  // getConnectionParameters: () => string;

  // Wallet uses this function
  // params: {"cid":"sdfjdsfj", "pk":"sjdfdf"}
  // setConnectionParameters: (params: string) => void;

  // getConnectionParametersDebug: () => string;

  // Connects to requester from "discoverer/scanner". On successful connected, it triggers callback with type: "CONNECTED"
  // createConnection: (mode: ConnectionMode, callback: () => void) => void;

  // destroyConnection: () => void;

  // 1. During device info exchange: message would look like this -> "exchange-sender-info\n{"name":"sender-device-name"}"
  //    Also passes a callback which gets executed for every Google Nearby event, but we will be interested in event with type: "msg" and also data having type: "exchange-receiver-info"
  // 2. While sending VC: message would look like this -> "send-vc\n{"isChunked":"false", vc: <serialised entire vc here>}"
  //    Also passes a callback which gets executed for every Google Nearby event, but we will be interested in event with type: 'send-vc:response' and also data having status whose value would "ACCEPTED" if successful
  // send: (message: string, callback: () => void) => void;

  // handleNearbyEvents: (
  //   callback: (event: NearbyEvent) => void
  // ) => EmitterSubscription;

  // handleLogEvents: (
  //   callback: (event: NearbyLog) => void
  // ) => EmitterSubscription;

  companion object {
    const val NAME = "Openid4vpBle"
    const val LOG_TAG = "Openid4vpBleModule"
  }
}
