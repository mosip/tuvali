package io.mosip.tuvali.wallet

import io.mosip.tuvali.common.events.Event

interface IWallet {
  fun startConnection(uri: String)
  fun sendData(payload: String)
  fun disconnect()
  fun subscribe(consumer: (Event) -> Unit)
  fun unSubscribe()
}
