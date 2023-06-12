package io.mosip.tuvali.rnModule

import com.facebook.react.ReactPackage
import com.facebook.react.bridge.NativeModule
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.uimanager.ViewManager
import io.mosip.tuvali.verifier.Verifier
import io.mosip.tuvali.wallet.Wallet


class RNTuvali : ReactPackage {
  override fun createNativeModules(reactContext: ReactApplicationContext): List<NativeModule> {
    return listOf(
      RNWalletModule(RNEventEmitter(reactContext), Wallet(reactContext), reactContext),
      RNVerifierModule(RNEventEmitter(reactContext), Verifier(reactContext), reactContext),
      RNVersionModule(reactContext)
    )
  }

  override fun createViewManagers(reactContext: ReactApplicationContext): List<ViewManager<*, *>> {
    return emptyList()
  }
}
