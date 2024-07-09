package io.mosip.tuvali.wallet

import io.mosip.tuvali.common.events.Event

interface IWallet {
  fun startConnection(uri: String)
  fun sendData(payload: String)
  fun disconnect()
  fun subscribe(listener: (Event) -> Unit)
  fun unSubscribe()
  fun handleDisconnect(status: Int, newState: Int)
}
