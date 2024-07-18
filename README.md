
> Warning: The library is under active development and no major version is released yet. Please anticipate non-backward compatible changes to the API and functional behavior in the upcoming releases.

# Tuvali - A library to send vc/vp using BLE.
This is the module for the [OpenID for Verifiable Presentations over BLE](https://tlodderstedt.github.io/openid-for-verifiable-presentations-offline-1_0-00.html) implementation to support sending vc/vp using Bluetooth Low Energy local channel.

This contains the source code for the ios, android modules as well as a sample app under `example/` folder. The sample app can be used for testing the modules being worked on in case it is needed.

## Usage as a Kotlin library (for native android)
The Tuvali kotlin artifact (.aar) has been published to Maven.
#### Adding as a Maven dependency.
- In settings.gradle.kts of your app modify the following:
  ```
      dependencyResolutionManagement {
      repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
      repositories {
        google()
        mavenCentral()
        maven("https://oss.sonatype.org/content/repositories/snapshots/")
      }
    }
  ```
- In build.gradle.kts add the following:
  ``` kotlin
    dependencies {
        implementation("io.mosip:tuvali:0.5.0-SNAPSHOT")
    }
   ```
The kotlin library has been added to your project.

## Usage as a Swift library (for native ios)

- Visit [tuvali-ios-swift](https://github.com/mosip/tuvali-ios-swift) to get details on swift artifact.

# API documentation
Firstly, for establishing the secured connection over BLE the Verifier's URI needs to be exchanged between two devices. The exchange of URI can be accomplished, but is not limited to, by using a QR code.

For example use QR code generator to visually display URI and QR code scanner to read. A mobile app that displays a QR code can act as an `Verifier` by including its URI as data in the QR code and another device can act as `Wallet` which scans the QR code, it can extract the URI and initiate a BLE connection with the advertising device.

## URI exchange and Establishing connection

### Verifier
The Verifier device can show a QR code with the URI. Verifier can generate URI through startAdvertisement() method. Once advertisement is started, Verifier will keep advertising with an advertisement payload derived from URI.

```kotlin
var verifier = Verifier()
var uri = verifier.startAdvertisement()
println(uri)
```

The URI contains:

```
OPENID4VP://connect?name=STADONENTRY&key=8520f0098930a754748b7ddcb43ef75a0dbf3a0d26381af4eba4a98eaa9b4e6a
```

URI structure can be found in the spec --> https://bitbucket.org/openid/connect/src/master/openid-4-verifiable-presentations-over-ble/openid-4-verifiable-presentations-over-ble-1_0.md

E.g: OPENID4VP://connect?name=<CLIENT_NAME>&key=<VERIFIER_PUBLIC_KEY>


### Wallet
The Wallet  device that scans the QR code will extract the URI from QR code and start scanning using startConnection() method.

```kotlin
wallet.startConnection(uri)
```

The Wallet device will keep on scanning for a verifier that has same URI in its advertisement. If URI is matched, Wallet will initiate a connection with the Verifier and exchange Public Keys.

## Share data

Once the connection is established, Wallet can send the data by:

```kotlin
wallet.sendData(payload)
```

Wallet will start sending data in a secured way to the Verifier.

Note: At this moment, we currently support data transfer from Wallet to Verifier only.

## Verifier Response

Once Data is received, Verifier can send verification status by:

```kotlin
verifier.sendVerificationStatus(status)
```

Status can be either `ACCEPTED` or `REJECTED`. Sending verification status acts as closure for transmission and devices will start disconnecting.


## Events from Tuvali

Tuvali sends multiple events to propagate connection status, received data etc. These events can be subscribed to by calling:

on Wallet:

```kotlin
wallet.subscribe {
  event  ->
  // Add the code that needs to run once event is received
}
```

on Verifier:

```kotlin
verifier.subscribe {
  event  ->
  // Add the code that needs to run once data is received
}
```


Here are the different types of events that can be received

### Common Events
Events which are emitted by both Wallet and Verifier

1. ConnectedEvent
   * on BLE connection getting established between Wallet and Verifier
2. SecureChannelEstablishedEvent
   * on completion of key exchange between Wallet and Verifier
3. ErrorEvent
   * on any error in Wallet or Verifier
4. DisconnectedEvent
   * on BLE disconnection between Wallet and Verifier


### Wallet Specific Events

1. DataSentEvent
   * on completion of Data transfer from the Wallet Side
2. VerificationStatusReceivedEvent
   * on received verification status from Verifier

### Verifier Specific Events

1. DataReceivedEvent
   * on receiving data from the Wallet Side

## Connection closure

The device on which app is running can destroy the connection by calling disconnect() method:

```kotlin
wallet/verifier.disconnect()
```

