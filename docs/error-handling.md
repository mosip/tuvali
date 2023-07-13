# Error Handling
 Tuvali might fail to perform BLE connection or transfer due to device issues or hardware issues.
 Whenever Tuvali faces these issue, `OnError` event will be sent with following structure.


`
{
  type: 'onError',
  message: string,
  code: string
}
`

* message - Short description of the error
* code - Three part error code with format of `[COMPONENT]_[STAGE]_[NUMBER]`. Each part is 3 character length.
  * Eg: `TVW_CON_001`
  * Component can be either
    * `TVW`(Tuvali Wallet)
    * `TVV`(Tuvali Verifier)
    * `Tuvali`(Tuvali without specific role)
  * Stage can be either
    * `CON`(Connection)
    * `KEX`(Key Exchange)
    * `ENC`(Encryption)
    * `TRA`(Transfer)
    * `REP`(Transfer Report)
    * `DEC`(Decryption)


## Error Codes
  Following are the list of error codes that are reported by Tuvali

### Known Stage Error Codes
  These Errors code gets reported when a known error case happens where Tuvali can't proceed.
1. `TVW_CON_001` - Wallet received a Invalid URI.
2. `TVW_CON_002` - Wallet failed negotiate MTU with a Verifier.
3. `TVW_CON_003` - Wallet failed to discover the services even after multiple retries.
4. `TVW_REP_001` - Wallet received a max failure frame retry limit or failed to request transfer report or received invalid transfer report summary from Verifier.
5. `TVV_CON_001` - MTU negotiated is not supported by the Verifier.
6. `TVV_TRA_001` - Verifier received corrupted response size from Wallet.
7. `TVV_TRA_002` - Verifier received more corrupted/missing chunks than tolerable limit of 70% from Wallet.

### Unknown Stage Error Codes
  These Errors code gets reported when internal exception is thrown by the platform
1. `TUV_UNK_001` - Tuvali Unknown Exception. This can be due to internal exception which is not caught.
2. `TVW_UNK_001` - Wallet's Unknown Exception. Uncaught internal exception within Wallet.
3. `TVW_UNK_002` - Wallet's State Handler Exception. Uncaught internal exception within Wallet's state handler or Central BLE layer.
4. `TVW_UNK_003` - Wallet's Transfer Handler Exception. Uncaught internal exception within Wallet's transfer handler or decryption layer.
5. `TVV_UNK_001` - Verifier's Unknown Exception. Uncaught internal exception within Verifier.
6. `TVV_UNK_002` - Verifier's State Handler Exception. Uncaught internal exception within Verifier's state handler or Peripheral BLE layer.
7. `TVV_UNK_003` - Wallet's Transfer Handler Exception. Uncaught internal exception within Wallet's transfer handler or decryption layer.
