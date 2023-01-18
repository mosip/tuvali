
import Foundation

class TransferHandler {
    var data: Data
    private var currentState: States = States.UnInitialised
    private var responseStartTimeInMillis: UInt64 = 0
    init(data: Data) {
        self.data = data
    }
    
    func sendMessage(msg: imessage) {
        handleMessage(msg: message)
    }
    
    private func handleMessage(msg: imessage){
        if msg.msgType == .INIT_RESPONSE_TRANSFER {
            var responseData = msg.data
            print("Total response size of data",responseData.count)
            let chunker = Chunker(chunkData: responseData)
            currentState = States.ResponseSizeWritePending
            sendMessage(msg: imessage(msgType: .ResponseSizeWritePendingMessage, data: responseData, dataSize: responseData.count))
        }
        else if msg.msgType == .ResponseSizeWritePendingMessage {
            sendResponseSize(msg.dataSize)
        }
        else if msg.msgType == .RESPONSE_SIZE_WRITE_SUCCESS{
          responseStartTimeInMillis = Utils.currentTimeInMilliSeconds()
          currentState = States.ResponseSizeWriteSuccess
          initResponseChunkSend()
        }
        else {
            print("out of scope")
        }
    }
    
    private func sendResponseSize(size: Int) {
        Central.write(serviceUuid: serviceUUID, charUUID: TransferService.responseSizeCharacteristic, data: Data(size))
        NotificationCenter.default.addObserver(forName: Notification.Name(rawValue: "RESPONSE_SIZE_WRITE_SUCCESS"), object: nil, queue: nil) { [unowned self] notification in
            print("Handling notification for \(notification.name.rawValue)")
            sendMessage(msg: imessage(msgType: .RESPONSE_SIZE_WRITE_SUCCESS))
        }
    }
    
    private func initResponseChunkSend() {
      print(logTag, "initResponseChunkSend")
      sendMessage(imessage(msgType: .ResponseSizeWritePendingMessage))
    }
}

enum TransferMessageTypes {
    case INIT_RESPONSE_TRANSFER
    case ResponseSizeWritePendingMessage
    case RESPONSE_SIZE_WRITE_SUCCESS
    case INIT_RESPONSE_CHUNK_TRANSFER
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
