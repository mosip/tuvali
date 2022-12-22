import Foundation

class SenderTransfersOwnershipOfData: SecretsTranslator {

    var senderCipherBox: CipherBox
    var receiverCipherBox: CipherBox
    var initVector: Data

    init(cipherPackage: CipherPackage, initVector: Data) {
        self.senderCipherBox = cipherPackage.getSelfCipherBox
        self.receiverCipherBox = cipherPackage.getOtherCipherBox
        self.initVector = initVector
    }

    func initializationVector() -> Data {
        return initVector
    }

    func encryptToSend(data: Data) -> Data {
        let encrypt = (receiverCipherBox.encrypt(message: data))
        return encrypt
    }

    func decryptUponReceive(data: Data) -> Data {
        let encrypt = (senderCipherBox.decrypt(message: data))
        return encrypt

    }
}
