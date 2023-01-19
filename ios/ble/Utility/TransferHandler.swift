
import Foundation

@available(iOS 13.0, *)
class TransferHandler {
    var data: Data
    private var currentState: States = States.UnInitialised
    private var responseStartTimeInMillis: UInt64 = 0
    var chunker: Chunker?
    init(data: Data) {
        self.data = data
    }
    
    func sendMessage(message: imessage) {
        handleMessage(msg: message)
    }
    
    private func handleMessage(msg: imessage){
        if msg.msgType == .INIT_RESPONSE_TRANSFER {
            var responseData = msg.data!
            print("Total response size of data",responseData.count)
            chunker = Chunker(chunkData: responseData, mtuSize: BLEConstants.DEFAULT_CHUNK_SIZE)
            currentState = States.ResponseSizeWritePending
            sendMessage(message: imessage(msgType: .ResponseSizeWritePendingMessage, data: responseData, dataSize: responseData.count))
        }
        else if msg.msgType == .ResponseSizeWritePendingMessage {
            sendResponseSize(size: msg.dataSize)
        }
        else if msg.msgType == .RESPONSE_SIZE_WRITE_SUCCESS {
          responseStartTimeInMillis =Utils.currentTimeInMilliSeconds()
          currentState = States.ResponseSizeWriteSuccess
          initResponseChunkSend()
        } else if msg.msgType == .INIT_RESPONSE_CHUNK_TRANSFER {
            currentState = .ResponseWritePending
            sendResponseChunk()
        }
        else {
            print("out of scope")
        }
    }
    
    private func sendResponseSize(size: Int) {
        Central.shared.write(serviceUuid: Peripheral.SERVICE_UUID, charUUID: TransferService.responseSizeCharacteristic, data: Data([UInt8(size)]))
        NotificationCenter.default.addObserver(forName: Notification.Name(rawValue: "RESPONSE_SIZE_WRITE_SUCCESS"), object: nil, queue: nil) { [unowned self] notification in
            print("Handling notification for \(notification.name.rawValue)")
            sendMessage(message: imessage(msgType: .RESPONSE_SIZE_WRITE_SUCCESS))
        }
    }
    
    private func initResponseChunkSend() {
      print("initResponseChunkSend")
        sendMessage(message: imessage(msgType: .ResponseSizeWritePendingMessage))
    }
    
    private func sendResponseChunk() {
        if ((chunker?.isComplete()) != nil) {
            print("Data send complete")
            sendMessage(message: imessage(msgType: .RESPONSE_TRANSFER_COMPLETE))
            return
        }
        
        var done = false
        while !done {
            if let chunk = chunker?.next() {
                Central.shared.write(serviceUuid: Peripheral.SERVICE_UUID, charUUID: TransferService.responseCharacteristic, data: chunk)
            }
        }
    }
}

enum TransferMessageTypes {
    case INIT_RESPONSE_TRANSFER
    case ResponseSizeWritePendingMessage
    case RESPONSE_SIZE_WRITE_SUCCESS
    case INIT_RESPONSE_CHUNK_TRANSFER
    case RESPONSE_TRANSFER_COMPLETE
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
