import Foundation

class SenderTransferOwnershipOfData: SecretTranslator {
    var senderCiperBox: CipherBox
    var receiverCipherBox: CipherBox
    var initVector: Data
    
    init(CipherPackage: CipherPackage, initVector: Data) {
        self.senderCiperBox = CipherPackage.getSelfCipherBox
        self.receiverCipherBox = CipherPackage.getOtherCipherBox
        self.initVector = initVector
    }
    
    func initializationVector() -> Data {
        return self.initVector
    }
    
    func encryptToSend(data: Data) -> Data {
        let encrypt = (receiverCipherBox.encrypt(message: data))
        return encrypt
    }
    
    func decryptUponReceive(data: Data) -> Data {
        let decrypt = (senderCiperBox.decrypt(message: data))
        return decrypt
    }
}
