package io.mosip.tuvali.verifier.transfer.message

class RemoteRequestedTransferReportMessage(val transferReportRequestCharValue: Int, val maxDataBytes: Int): IMessage(TransferMessageTypes.REMOTE_REQUESTED_TRANSFER_REPORT) {}
