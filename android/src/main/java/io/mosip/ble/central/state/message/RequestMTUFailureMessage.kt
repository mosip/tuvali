package io.mosip.ble.central.state.message

class RequestMTUFailureMessage(val errorCode: Int) : IMessage(
  CentralStates.REQUEST_MTU_FAILURE
)
