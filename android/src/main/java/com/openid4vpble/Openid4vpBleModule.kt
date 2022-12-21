package com.openid4vpble

import android.util.Log
import com.facebook.react.bridge.*
import com.facebook.react.modules.core.DeviceEventManagerModule.RCTDeviceEventEmitter
import com.verifier.Verifier
import com.wallet.Wallet
import org.json.JSONObject

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
    verifier.generateKeyPair()
    val payload = verifier.getAdvIdentifier("OVPMOSIP");
    return "{\"cid\":\"ilB8l\",\"pk\":\"${payload}\"}"
  }

  @ReactMethod(isBlockingSynchronousMethod = true)
  fun getConnectionParametersDebug(): String {
    return getConnectionParameters()
  }

  @ReactMethod(isBlockingSynchronousMethod = true)
  fun setConnectionParameters(params: String) {
    val paramsObj = JSONObject(params)
    val firstPartOfPk = paramsObj.getString("pk")
    Log.d(LOG_TAG, "setConnectionParameters called with $params and $firstPartOfPk")

    return wallet.setAdvIdentifier(firstPartOfPk)
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
    Log.d(LOG_TAG, "send: message $message")
    val messageSplits = message.split("\n", limit = 2)
    when(messageSplits[0]) {
      "exchange-receiver-info" -> {
        callback()
      }
      "exchange-sender-info" -> {
        callback()
        wallet.writeIdentity()
      }
      "send-vc" -> {
        val data = JSONObject(messageSplits[1])
        val vcData = data.getString("vc")
        Log.d(LOG_TAG, "vc received: $vcData")
        wallet.sendData(vcData)
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
