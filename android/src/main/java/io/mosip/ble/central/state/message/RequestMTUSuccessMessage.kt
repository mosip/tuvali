package io.mosip.ble.central.state.message

class RequestMTUSuccessMessage(val mtu: Int) : IMessage(
  CentralStates.REQUEST_MTU_SUCCESS
)
