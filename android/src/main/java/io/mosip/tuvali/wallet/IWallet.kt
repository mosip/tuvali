package io.mosip.tuvali.wallet

interface IWallet {
  fun startConnection(uri: String)
  fun sendData(payload: String)
  fun disconnect()
}
