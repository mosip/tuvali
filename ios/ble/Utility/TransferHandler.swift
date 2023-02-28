import Foundation

@available(iOS 13.0, *)
class TransferHandler {
    var data: Data?
    private var currentState: States = States.UnInitialised
    private var responseStartTimeInMillis: UInt64 = 0
    private var chunker: Chunker?

    public static var shared = TransferHandler()

    func initialize(initdData: Data) {
        data = initdData
    }

    func sendMessage(message: imessage) {
        handleMessage(msg: message)
    }
    deinit{
        os_log(.debug, "deinit happend in transferhandler")
    }

    private func handleMessage(msg: imessage) {
        if msg.msgType == .INIT_RESPONSE_TRANSFER {
            var responseData = msg.data!
            os_log(.info, "Total response size of data %{public}d",(responseData.count))
            chunker = Chunker(chunkData: responseData, mtuSize: msg.mtuSize)
            os_log(.info, "MTU found to be %{public}d", BLEConstants.DEFAULT_CHUNK_SIZE)
            currentState = States.ResponseSizeWritePending
            sendMessage(message: imessage(msgType: .RESPONSE_SIZE_WRITE_PENDING, data: responseData, dataSize: responseData.count))
        }
        else if msg.msgType == .RESPONSE_SIZE_WRITE_PENDING {
            sendResponseSize(size: msg.dataSize!)
        }
        else if msg.msgType == .RESPONSE_SIZE_WRITE_SUCCESS {
            responseStartTimeInMillis = Utils.currentTimeInMilliSeconds()
            currentState = States.ResponseSizeWriteSuccess
            initResponseChunkSend()
        } else if msg.msgType == .RESPONSE_SIZE_WRITE_FAILED {
            os_log(.error, "Failed to write response size")
            currentState = States.ResponseWriteFailed
        } else if msg.msgType == .INIT_RESPONSE_CHUNK_TRANSFER {
            currentState = .ResponseWritePending
            sendResponseChunk()
        }
        else if msg.msgType == .READ_TRANSMISSION_REPORT {
            currentState = States.WaitingForTransferReport
            requestTransmissionReport()
        }
        else if msg.msgType == .HANDLE_TRANSMISSION_REPORT {
            currentState = States.HandlingTransferReport
            var handleTransmissionReportMessage = msg.data
            handleTransmissionReport(report: handleTransmissionReportMessage!)
        } else if msg.msgType == .RESPONSE_CHUNK_WRITE_SUCCESS {
            // NoOp: iOS lacks support for writeWithoutResponse callbacks unlike Android
        } else if msg.msgType == .RESPONSE_CHUNK_WRITE_FAILURE {
            os_log(.error, "Response chunk write failed")
        } else if msg.msgType == .RESPONSE_TRANSFER_COMPLETE {
            currentState = States.TransferComplete
            sendMessage(message: imessage(msgType: .READ_TRANSMISSION_REPORT))
        } else if msg.msgType == .RESPONSE_TRANSFER_FAILED {
            currentState = States.ResponseWriteFailed
        } else {
            os_log(.error, "Out of scope")
        }
    }

    private func sendRetryRespChunk(missingChunks: [Int]) {
        for chunkIndex in missingChunks {
            let chunk = chunker?.getChunkWithIndex(index: chunkIndex)
            Central.shared.write(serviceUuid: Peripheral.SERVICE_UUID, charUUID: NetworkCharNums.SUBMIT_RESPONSE_CHAR_UUID, data: chunk!)
        }
        sendMessage(message: imessage(msgType: .READ_TRANSMISSION_REPORT, data: nil))
    }

    private func requestTransmissionReport() {
        var notifyObj: Data
        Central.shared.write(serviceUuid: BLEConstants.SERVICE_UUID, charUUID: NetworkCharNums.TRANSFER_REPORT_REQUEST_CHAR_UUID, data: withUnsafeBytes(of: 1.littleEndian) { Data($0) })
        os_log(.info, "transmission report requested")
    }

    private func handleTransmissionReport(report: Data) {
        let r = TransferReport(bytes: report)
        os_log(.info, "Got the transfer report :  %{public}d", (r.type.rawValue))
        os_log(.info, "Missing pages: %{public}d ", (r.totalPages))

        if (r.type == .SUCCESS) {
            currentState = States.TransferVerified
            EventEmitter.sharedInstance.emitNearbyMessage(event: "send-vc:response", data: "\"RECEIVED\"")
            os_log(.info, "Emitting send-vc:response RECEIVED message")
        } else if r.type == .MISSING_CHUNKS {
            currentState = .PartiallyTransferred
            sendRetryRespChunk(missingChunks: r.missingSequences!)
        } else {
            os_log(.info, "handle transfer report parsing, report-type= %{public}d", r.type.rawValue)
            sendMessage(message: imessage(msgType: .RESPONSE_TRANSFER_FAILED, data: nil, dataSize: 0))
        }
    }

    private func sendResponseSize(size: Int) {
        let decimalString = String(size)
        let d = decimalString.data(using: .utf8)
        Central.shared.write(serviceUuid: Peripheral.SERVICE_UUID, charUUID: NetworkCharNums.RESPONSE_SIZE_CHAR_UUID, data: d!)
    }

    private func initResponseChunkSend() {
        sendMessage(message: imessage(msgType: .INIT_RESPONSE_CHUNK_TRANSFER, data: data, dataSize: data?.count))
    }

    private func sendResponseChunk() {
        if let chunker = chunker {
            while !chunker.isComplete() {
                let chunk = chunker.next()
                Central.shared.writeWithoutResp(serviceUuid: Peripheral.SERVICE_UUID, charUUID: NetworkCharNums.SUBMIT_RESPONSE_CHAR_UUID, data: chunk)
                Thread.sleep(forTimeInterval: 0.020)
            }
            sendMessage(message: imessage(msgType: .READ_TRANSMISSION_REPORT))
        } else {
            os_log(.error, "chunker is nil !")
        }
    }
}

enum TransferMessageTypes {
    case INIT_RESPONSE_TRANSFER
    case RESPONSE_SIZE_WRITE_PENDING
    case RESPONSE_SIZE_WRITE_SUCCESS
    case RESPONSE_SIZE_WRITE_FAILED
    case INIT_RESPONSE_CHUNK_TRANSFER
    case CHUNK_WRITE_TO_REMOTE_STATUS_UPDATED
    case RESPONSE_CHUNK_WRITE_SUCCESS
    case RESPONSE_CHUNK_WRITE_FAILURE
    case RESPONSE_TRANSFER_COMPLETE
    case RESPONSE_TRANSFER_FAILED

    case READ_TRANSMISSION_REPORT
    case HANDLE_TRANSMISSION_REPORT

    case INIT_RETRY_TRANSFER
}

struct imessage {
    var msgType: TransferMessageTypes
    var data: Data?
    var dataSize: Int?
    var mtuSize: Int?
}

enum  States {
    case UnInitialised
    case ResponseSizeWritePending
    case ResponseSizeWriteSuccess
    case ResponseSizeWriteFailed
    case ResponseWritePending
    case ResponseWriteFailed
    case TransferComplete
    case WaitingForTransferReport
    case HandlingTransferReport
    case TransferVerified
    case PartiallyTransferred
}

enum SemaphoreMarker: Int {
    case UnInitialised = 0
    case RequestReport = 1
    case Error = 2
}

extension TransferHandler: PeripheralCommunicatorProtocol {
    func onTransmissionReportRequest(data: Data?) {
        if let data {
            sendMessage(message: imessage(msgType: .HANDLE_TRANSMISSION_REPORT, data: data))
        }
    }

    func onResponseSizeWriteSuccess() {
        sendMessage(message: imessage(msgType: .RESPONSE_SIZE_WRITE_SUCCESS, data: data))
    }

    func onVerificationStatusChange(data: Data?) {
        let value = data
        if let value =  value {
            let status = Int(value[0])
            if status == 0 {
                EventEmitter.sharedInstance.emitNearbyMessage(event: "send-vc:response", data: "\"ACCEPTED\"")
            } else if status == 1 {
                EventEmitter.sharedInstance.emitNearbyMessage(event: "send-vc:response", data: "\"REJECTED\"")
            }
            Wallet.shared.destroyConnection(isSelfDisconnect: true)
        }
    }

    func onFailedToSendTransferReportRequest() {
        requestTransmissionReport()
    }
}
