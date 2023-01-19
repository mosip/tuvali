package io.mosip.tuvali.ble.central.state.message

import java.util.UUID

class DiscoverServicesSuccessMessage(val services : List<UUID>): IMessage(
  CentralStates.DISCOVER_SERVICES_SUCCESS
)
