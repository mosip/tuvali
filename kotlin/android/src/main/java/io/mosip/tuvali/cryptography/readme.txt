(1)
// As a verifier
// phase-1: generate public key on either side
// let verifierCryptoBox = VerifierCryptoBoxBuilder().build();
// let verifierPublicKey = verifierCryptoBox.publicKey()
-----------------------network call----------------------------------------------
(2)
// As a wallet
// phase-1: generate public key on either side
// let walletCryptoBox = WalletCryptoBoxBuilder().build()
// let walletPublicKey = walletCryptoBox.publicKey()

(3)
// As a wallet
// phase-2: share and generate secretsTranslator
// let secretsTranslator = walletCryptoBox.buildSecretsTranslator(verifierPublicKey)
// let nonce = secretsTranslator.getNonce()

At this stage we have both 'nonce' and 'walletPublicKey'
-----------------------network call----------------------------------------------
(4)
// As a verifier
// phase-2: share and generate secretsTranslator
// let secretsTranslator = verifierCryptoBox.buildSecretsTranslator(nonce, walletPublicKey)

(5)
// As a verifier
// plainText -> cipher Text
// phase-3: Encrypt the data to transfer
// cipherText = secretsTranslator.encryptToSend(plainText)
-----------------------network call----------------------------------------------
(6)
// As a wallet receiving encrypted data from verifier
// encrypted payload -> plain text
// phase-3: Encrypt the data to transfer
// plainText = secretsTranslator.decryptUponReceive(cipherText)

(7)
// As a wallet writing sending encrypted data to verifier
// plain text -> encrypted payload
// phase-3: Encrypt the data to transfer
// cipherText = secretsTranslator.encryptToSend(plainText)
-----------------------network call----------------------------------------------
