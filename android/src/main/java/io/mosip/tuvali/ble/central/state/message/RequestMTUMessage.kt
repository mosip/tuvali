package io.mosip.tuvali.ble.central.state.message

class RequestMTUMessage(val mtu: Int) : IMessage(
  CentralStates.REQUEST_MTU
)
