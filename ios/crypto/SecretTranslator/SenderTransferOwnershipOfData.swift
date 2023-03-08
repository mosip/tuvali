import Foundation

class SenderTransferOwnershipOfData: SecretTranslator {
    var senderCipherBox: CipherBox
    var receiverCipherBox: CipherBox
    var nonce: Data

    init(CipherPackage: CipherPackage, nonce: Data) {
        self.senderCipherBox = CipherPackage.getSelfCipherBox
        self.receiverCipherBox = CipherPackage.getOtherCipherBox
        self.nonce = nonce
    }

    func getNonce() -> Data {
        return self.nonce
    }

    func encryptToSend(data: Data) -> Data {
        let encrypt = (receiverCipherBox.encrypt(message: data))
        return encrypt
    }

    func decryptUponReceive(data: Data) -> Data {
        let decrypt = (senderCipherBox.decrypt(message: data))
        return decrypt
    }
}
