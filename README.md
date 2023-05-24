
> Warning: The library is under active development and no major version is released yet. Please anticipate non-backward compatible changes to the API and functional behavior in the upcoming releases.

# Tuvali - React native module library
This is the React native module for the [OpenID for Verifiable Presentations over BLE](https://tlodderstedt.github.io/openid-for-verifiable-presentations-offline-1_0-00.html) implementation to support sending vc/vp using Bluetooth Low Energy local channel.

This contains the source code for the ios, android modules as well as a sample app under `example/` folder. The sample app can be used for testing the modules being worked on in case it is needed.

## Installing this library as a dependency

```bash
# Install latest version
npm install mosip/tuvali

# or

# Install specific version
npm install mosip/tuvali#v0.3.7
```

# API documentation
Firstly, for establishing the secured connection over BLE the Verifier's URI needs to be exchanged between two devices. The exchange of URI can be accomplished, but is not limited to, by using a QR code.

For example use QR code generator to visually display URI and QR code scanner to read. A mobile app that displays a QR code can act as an `Verifier` by including its URI as data in the QR code and another device can act as `Wallet` which scans the QR code, it can extract the URI and initiate a BLE connection with the advertising device.

## URI exchange and Establishing connection

### Verifier
The Verifier device will be showing a QR code with URI. Verifier can generate URI and start advertisement using startAdvertisement() method. Once advertisement started, `Verifier` will keep on advertising URI and will wait for the `Wallet` to connect.

```typescript
import OpenIdBle from 'react-native-openid4vp-ble';
const { verifier } = OpenIdBle;

const uri = verifier.startAdvertisement();
console.log(uri);
```

The URI contains:

```
OPENID4VP://4f56504d4f5349505f66b067c008A4484AEC5A769CED2307F59E43DC81A3F768
```

`OPENID4VP` is used as identifier for URI. The part after the `//` of the data is the same data that will be advertised by the `Verifier` device but in hex encoded form.

E.g: OPENID4VP://OVPMOSIP_<first 5 bytes of public key>


### Wallet
The Wallet  device that scans the QR code will extract the URI from QR code and start scanning using startConnection() method.

```typescript
wallet.startConnection(uri);
```

The Wallet device will keep on scanning for a verifier that has same URI in its advertisement. If URI is matched, Wallet will initiate a connection with the Verifier and exchange Public Keys.

## Share data

Once the connection is established, Wallet can send the data by:

```typescript
wallet.send(vc);
```

Wallet will start sending VC in a secured way to the Verifier. At the moment, only VC data can be exchanged from Wallet to Verifier instead of VP response mentioned in the specification.

Note: Verifier send will be implemented in upcoming versions

## Verifier Response

Once Data is received, Verifier can send verification status by:

```typescript
verifier.sendVerificationStatus(status);
```

Status can be either `ACCEPTED` or `REJECTED`. Sending verification status acts as closure for transmission and devices will start disconnecting.


## Events from Tuvali

Tuvali sends multiple events to propagate connection status, received VC data etc. These events can be subscribed to by calling:

on Wallet:

```typescript
wallet.handleDataEvents((event: WalletDataEvent) => {
  // Add the code that needs to run once data is received
})
```

on Verifier:

```typescript
verifier.handleDataEvents((event: VerifierDataEvent) => {
  // Add the code that needs to run once data is received
})
```


Here are the different types of events that can be received

### Common Events
Events which are emitted by both Wallet and Verifier

1. onConnected
   * `{"type": "onConnected"}`
   * on BLE connection getting established between Wallet and Verifier
2. onSecureChannelEstablished
   * `{"type": "onSecureChannelEstablished"}`
   * on completion of key exchange between Wallet and Verifier
3. onError
   * `{"type": "onError", "message": "Something Went wrong in BLE", "code": "TVW_CON_001"}`
   * on any error in Wallet or Verifier
4. onDisconnected
   * `{"type": "onDisconnected"}`
   * on BLE disconnection between Wallet and Verifier


### Wallet Specific Events

1. onDataSent
   * `{"type": "onDataSent"}`
   * on completion of Data(VC) transfer from the Wallet Side
2. onVerificationStatusReceived
   * `{"type": "onVerificationStatusReceived", "status": "ACCEPTED"}`
   * on received verification status from Verifier

### Verifier Specific Events

1. onDataReceived
  * `{"type": "onDataReceived"}`
  * on receiving of Data(VC) from the Wallet Side

## Connection closure

The device on which app is running can destroy the connection by calling disconnect() method:

```typescript
wallet/verifier.disconnect();
```
