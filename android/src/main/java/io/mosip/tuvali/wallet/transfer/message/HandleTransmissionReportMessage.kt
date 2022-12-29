package io.mosip.tuvali.wallet.transfer.message

import io.mosip.tuvali.transfer.TransmissionReport

class HandleTransmissionReportMessage(val report: TransmissionReport): IMessage(TransferMessageTypes.HANDLE_TRANSMISSION_REPORT) {}
