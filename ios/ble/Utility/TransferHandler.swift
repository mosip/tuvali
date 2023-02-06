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
        print("deinit happend in transferh")
    }

    private func handleMessage(msg: imessage) {
        if msg.msgType == .INIT_RESPONSE_TRANSFER {
            var responseData = msg.data!
            print("Total response size of data",responseData.count)
            // TODO: Init chunker to use the exchanged MTU size
            chunker = Chunker(chunkData: responseData, mtuSize: BLEConstants.DEFAULT_CHUNK_SIZE)
            print("MTU found to be", BLEConstants.DEFAULT_CHUNK_SIZE)
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
            print("failed to write response size")
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
            // NoOp: might need to use this later when sent with resp
            print("response chunk write failed")
        } else if msg.msgType == .RESPONSE_TRANSFER_COMPLETE {
            currentState = States.TransferComplete
            sendMessage(message: imessage(msgType: .READ_TRANSMISSION_REPORT))
        } else if msg.msgType == .RESPONSE_TRANSFER_FAILED {
            currentState = States.ResponseWriteFailed
            // TODO: should response transfer be retried
        } else {
            print("out of scope")
        }
    }

    private func sendRetryRespChunk(missingChunks: [Int]) {
        for chunkIndex in missingChunks {
            let chunk = chunker?.getChunkWithIndex(index: chunkIndex)
            Central.shared.write(serviceUuid: Peripheral.SERVICE_UUID, charUUID: NetworkCharNums.SUBMIT_RESPONSE_CHAR_UUID, data: chunk!)
            // checks if no more missing chunks exist on verifier
        }
        sendMessage(message: imessage(msgType: .READ_TRANSMISSION_REPORT, data: nil))
    }
    private func requestTransmissionReport() {
        var notifyObj: Data
        Central.shared.writeWithoutResp(serviceUuid: BLEConstants.SERVICE_UUID, charUUID: NetworkCharNums.TRANSFER_REPORT_REQUEST_CHAR_UUID, data: withUnsafeBytes(of: 1.littleEndian) { Data($0) })
        print("transmission report requested")
        NotificationCenter.default.addObserver(forName: Notification.Name(rawValue: "HANDLE_TRANSMISSION_REPORT"), object: nil, queue: nil) { [unowned self] notification in
            print("Handling notification for \(notification.name.rawValue)")
            if let notifyObj = notification.userInfo?["report"] as? Data {
                sendMessage(message: imessage(msgType: .HANDLE_TRANSMISSION_REPORT, data: notifyObj))
            } else {
                print("invalid report")
            }
        }
    }

    private func handleTransmissionReport(report: Data) {
        let r = TransferReport(bytes: report)
        print(" got the transfer report type \(r.type)")
        print("missing pages: ", r.totalPages)

        if (r.type == .SUCCESS) {
            currentState = States.TransferVerified
            EventEmitter.sharedInstance.emitNearbyMessage(event: "send-vc:response", data: "\"RECEIVED\"")
            print("Emitting send-vc:response RECEIVED message")
            Wallet.shared.registerCallbackForEvent(event: NotificationEvent.VERIFICATION_STATUS_RESPONSE) {
                notification in
                // TODO -- Add all React native events under an Enum
                let value = notification.userInfo?["status"] as? Data
                if let value =  value {
                    let status = Int(value[0])
                    if status == 0 {
                        EventEmitter.sharedInstance.emitNearbyMessage(event: "send-vc:response", data: "\"ACCEPTED\"")
                    } else if status == 1 {
                        EventEmitter.sharedInstance.emitNearbyMessage(event: "send-vc:response", data: "\"REJECTED\"")
                    }
                }
            }
        } else if r.type == .MISSING_CHUNKS {
            currentState = .PartiallyTransferred
            sendRetryRespChunk(missingChunks: r.missingSequences!)
        } else {
            print("handle transfer report parsing, report-type=\(r.type)")
            sendMessage(message: imessage(msgType: .RESPONSE_TRANSFER_FAILED, data: nil, dataSize: 0))
        }
    }

    private func sendResponseSize(size: Int) {
        // TODO: Send a stringified number in a byte array
        let decimalString = String(size)
        let d = decimalString.data(using: .utf8)
        print(d!)
        Central.shared.write(serviceUuid: Peripheral.SERVICE_UUID, charUUID: NetworkCharNums.RESPONSE_SIZE_CHAR_UUID, data: d!)
        NotificationCenter.default.addObserver(forName: Notification.Name(rawValue: "RESPONSE_SIZE_WRITE_SUCCESS"), object: nil, queue: nil) { [unowned self] notification in
            print("Handling notification for \(notification.name.rawValue)")
            sendMessage(message: imessage(msgType: .RESPONSE_SIZE_WRITE_SUCCESS, data: data))
        }
    }

    private func initResponseChunkSend() {
        print("initResponseChunkSend")
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
                print("chunker is nil !")
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




