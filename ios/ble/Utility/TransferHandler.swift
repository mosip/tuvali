
import Foundation

class TransferHandler {
    var data: Data

    init(data: Data) {
        self.data = data
       
    }
    
    func sendMessage(msg: TransferMessageTypes) {
        var message = this.obtainMessage()
        message.what = msg.msgType.ordinal
        message.obj = msg
        this.sendMessage(message)
        handleMessage(msg: message)
    }
    
    func handleMessage(msg: TransferMessageTypes){
        if TransferMessageTypes.INIT_RESPONSE_TRANSFER {
            var initResponseTransferMessage = msg.obj as InitResponseTransferMessage
            var responseData = initResponseTransferMessage.data
            print("Total response size of data",responseData.count)
            let chunker = Chunker(chunkData: responseData)
            currentState = States.ResponseSizeWritePending
            //sendMessage(msg: responseData)
        }
    }
    
    func InitResponseTransferMessage() -> TransferMessageTypes {
        return TransferMessageTypes.INIT_RESPONSE_TRANSFER
    }
}

enum TransferMessageTypes {
    case INIT_RESPONSE_TRANSFER
    case ResponseSizeWritePendingMessage
}
