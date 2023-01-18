# Tuvali - React native module library
This is the react native module for the openBle4VP implementation to support sending vc/vp using Bluetooth Low Energy local channel. This contains the source code for the ios, android modules as well as a sample app under `example/` folder. This is there for testing the modules being worked on in case it is needed.

The primary javascript source code that interacts with the native modules lives in the `src/` directory.
Any additional interfaces that are being exposed from the native side need to be exposed from this package.

Something to keep note of is to maintain the consistency between the interfaces being exposed from iOS and android modules especially in terms of naming convention, function arguments and return values. This will reduce contention between the two modules and allow the javascript wrapper to be simple to understand and also to export the required react interfaces for consumption above in the chain.

## ios Specifics
The way the native module is setup is it contains 3 primary parts.
```
 Swift files - These contain the primary business logic being exposed.
 Bridging Header - A objective-C bridging header that is needeed to use swift inside a objective-C project.
 Objective-C module file - This file exposes the modules to React Native via macros.
```
Whenever new functions are being added to the modules, the corresponding functions need to be exposed via the interface failing which the functions/methods will not be available on the react native side.

For installing dependencies for the ios module, install the required dependencies in the `react-native-openid4vp-ble.podspec` file.
Eg -
```
pod.dependency 'Tink'
```

To build the ios module
```
yarn
```

To test the module in the sample project.
```
yarn example ios
```

## android specifics
The android module is being written in kotlin. The primary way of exposing functionality on the android side is to write modules that become part of the package. The package as described in the `AndroidManifest.xml` is built and exposed to the main application instance from the `getPackages` method.
There are currently 3 packages that are being exposed as part of the module.
```
openid4vpble
Wallet
Verifier
```
All the logic for openid should reside in the `Wallet` or the `Verifier` package. These modules are already hooked up to be exposed to the react native side.

For installing dependencies for the android module, add the required dependencies in the `build.gradle` file.
Eg -
```
dependencies {
    // Dependency on a remote binary
    implementation 'com.example.android:app-magic:12.3'
}
```

To build the ios module
```
yarn
```

To test the module in the sample project.
```
yarn example android
```

## Project structure
```
- Root
 - android
 - ios
 - example (example app)
```

## Integrate and compile with Inji (Local)
#### Pre Requisites
1. Install yalc with `yarn global add yalc`

#### On Tuvali repo
1. Compile Tuvali with `yarn` command
2. Public Tuvali to local Yalc repo with `yalc publish`

### On Inji repo
1. Add locally published Tuvali repo to INJI with `yalc add react-native-openid4vp-ble`
2. `npm install`
3. Open app from CLI `npm run android:mosip`. Optionally can run from Android studio as well.

### Running INJI from Android Studio (workaround)
1. Bump up android gradle plugin to min of 4.2.2 (Ref: https://github.com/facebook/react-native/issues/35337)

## Notes from Design
### 1. Peripheral unable to disconnect issue

As specified in this Google issue - https://issuetracker.google.com/issues/37127644.
Gatt server cannot disconnect from the central,
it can only indicate Android that App is no longer using the connection.
Android decides when to force a disconnect based on internal timeout.

**Example scenario from INJI:**
1. Wallet scans for the verifier and connects.
2. Wallet establishes the Cypto connection and waits for the User to select VC.
3. If User doesn't select any VC, Verifier gets option to cancel the transfer.
4. Verifier(Peripheral) calls cancelConnection. Yet, Android OS doesn't disconnect the connection and connection is alive until timeout or Wallet destroys connection.

This creates a situation where Verifier is waiting for a new connection yet previous connection is still alive. If Wallet fails to destroy connection then reconnect will keep on failing due to existing connection.

#### Solution
Create a new characteristic on wallet for handling disconnection. Verifier notifies on the characteristic whenever it wants to cancel the connection.
