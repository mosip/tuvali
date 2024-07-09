package io.mosip.tuvali.ble.central.state.message

class DiscoverServicesFailureMessage(val errorCode: Int) : IMessage(
  CentralStates.DISCOVER_SERVICES_FAILURE
)
