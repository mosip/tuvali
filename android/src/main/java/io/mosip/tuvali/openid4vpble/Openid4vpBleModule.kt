package io.mosip.tuvali.openid4vpble

import android.util.Log
import com.facebook.react.bridge.*
import com.facebook.react.modules.core.DeviceEventManagerModule.RCTDeviceEventEmitter
import io.mosip.tuvali.verifier.Verifier
import io.mosip.tuvali.wallet.Wallet
import org.json.JSONObject

class Openid4vpBleModule(private val reactContext: ReactApplicationContext) :
  ReactContextBaseJavaModule(reactContext) {
  private var verifier: Verifier? = null
  private var wallet: Wallet? = null
  private val mutex = Object()

  enum class InjiVerificationStates(val value: String) {
    ACCEPTED("\"ACCEPTED\""),
    REJECTED("\"REJECTED\"")
  }

  override fun getName(): String {
    return NAME
  }

  @ReactMethod(isBlockingSynchronousMethod = true)
  fun getConnectionParameters(): String {
    synchronized (mutex) {
      if (verifier == null) {
        verifier = Verifier(reactContext, this::emitNearbyEvent)
        verifier?.generateKeyPair()
      }
      val payload = verifier?.getAdvIdentifier("OVPMOSIP");
      return "{\"cid\":\"ilB8l\",\"pk\":\"${payload}\"}"
    }
  }

  @ReactMethod(isBlockingSynchronousMethod = true)
  fun getConnectionParametersDebug(): String {
    return getConnectionParameters()
  }

  @ReactMethod(isBlockingSynchronousMethod = true)
  fun setConnectionParameters(params: String) {
    if (wallet == null) {
      wallet = Wallet(reactContext, this::emitNearbyEvent)
    }
    val paramsObj = JSONObject(params)
    val firstPartOfPk = paramsObj.getString("pk")
    Log.d(LOG_TAG, "setConnectionParameters called with $params and $firstPartOfPk")
    wallet?.setAdvIdentifier(firstPartOfPk)
  }

  @ReactMethod
  fun createConnection(mode: String, callback: Callback) {
    Log.d(LOG_TAG, "createConnection: received request with mode $mode")
    when (mode) {
      "advertiser" -> {
        verifier?.startAdvertisement("OVPMOSIP", callback)
      }
      "discoverer" -> {
        wallet?.startScanning("OVPMOSIP", callback)
      }
    }
  }

  @ReactMethod(isBlockingSynchronousMethod = true)
  fun destroyConnection() {
    synchronized (mutex) {
      if (wallet != null) {
        stopWallet()
      }
      if (verifier != null) {
        stopVerifier()
      }
    }
  }

  private fun stopVerifier() {
    try {
      verifier?.stop()
    } finally {
      verifier = null
    }
  }

  private fun stopWallet() {
    try {
      wallet?.stop()
    } finally {
      wallet = null
    }
  }

  @ReactMethod
  fun send(message: String, callback: Callback) {
    Log.d(LOG_TAG, "send: message $message")
    val messageSplits = message.split("\n", limit = 2)
    when(messageSplits[0]) {
      "exchange-receiver-info" -> {
        callback()
      }
      "exchange-sender-info" -> {
        callback()
        wallet?.writeIdentity()
      }
      "send-vc" -> {
        callback()
        wallet?.sendData(messageSplits[1])
      }
      "send-vc:response" -> {
        verifier?.notifyVerificationStatus(messageSplits[1] == InjiVerificationStates.ACCEPTED.value)
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
