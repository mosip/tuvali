package com.openid4vpble

import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.Promise

class Openid4vpBleModule(reactContext: ReactApplicationContext) :
  ReactContextBaseJavaModule(reactContext) {

  override fun getName(): String {
    return NAME
  }

  // Example method
  // See https://reactnative.dev/docs/native-modules-android
  @ReactMethod
  fun multiply(a: Double, b: Double, promise: Promise) {
    promise.resolve(a * b)
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
  }
}
