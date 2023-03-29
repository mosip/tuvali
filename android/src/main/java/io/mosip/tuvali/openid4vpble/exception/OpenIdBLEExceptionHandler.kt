package io.mosip.tuvali.openid4vpble.exception

import android.util.Log
import com.facebook.react.bridge.Callback
import io.mosip.tuvali.openid4vpble.exception.exception.BLEException
import io.mosip.tuvali.openid4vpble.exception.exception.ErrorCode
import io.mosip.tuvali.transfer.Util
import io.mosip.tuvali.verifier.exception.VerifierException
import io.mosip.tuvali.verifier.exception.VerifierExceptionHandler
import io.mosip.tuvali.wallet.exception.WalletException
import io.mosip.tuvali.wallet.exception.WalletExceptionHandler

class OpenIdBLEExceptionHandler(private val sendError: (String, ErrorCode) -> Unit, private val stopBle: (Callback) -> Unit) {
  private val logTag = Util.getLogTag(javaClass.simpleName)
  private var walletExceptionHandler: WalletExceptionHandler = WalletExceptionHandler(sendError);
  private var verifierExceptionHandler: VerifierExceptionHandler = VerifierExceptionHandler(sendError);

  private fun handleWalletException(e: WalletException) {
    walletExceptionHandler.handleException(e)
  }

  private fun handleVerifierException(e: VerifierException) {
    verifierExceptionHandler.handleException(e)
  }

  fun handleException(e: BLEException) {
    when (e) {
      is WalletException -> {
        handleWalletException(e)
      }
      is VerifierException -> {
        handleVerifierException(e)
      }
      else -> {
        handleUnknownException(e)
      }
    }

    stopBle {}
  }

  private fun handleUnknownException(e: BLEException) {
    Log.e(logTag, "Unknown exception: $e")
    Log.e(logTag, "${e.stackTrace}")
    sendError(e.message ?: "Something went wrong in BLE: ${e.cause}", e.errorCode)
  }
}
