package io.mosip.tuvali.ble.central.state.message



class NegotiateAndRequestMTU(val mtuSize: Int): IMessage(CentralStates.NEGOTIATE_AND_REQUEST_MTU) {

}
