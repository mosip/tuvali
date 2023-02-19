package io.mosip.tuvali.ble.central.state.message

class RequestMTUMessage(val mtu: Array<Int>, val delayTime: Long) : IMessage(
  CentralStates.REQUEST_MTU
)
