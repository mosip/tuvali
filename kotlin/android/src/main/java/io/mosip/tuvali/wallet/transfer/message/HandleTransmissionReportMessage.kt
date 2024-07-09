package io.mosip.tuvali.wallet.transfer.message

import io.mosip.tuvali.transfer.TransferReport

class HandleTransmissionReportMessage(val report: TransferReport): IMessage(TransferMessageTypes.HANDLE_TRANSMISSION_REPORT) {}
