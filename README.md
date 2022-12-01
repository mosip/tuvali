# React native module library
This is the react native module for the openBle4VP implementation. This contains the source code for the ios, android modules as well as a sample app under `example/` folder. This is there for testing the modules being worked on in case it is needed.

The primary javascript source code that interacts with the native modules lives in the `src/` directory.
Any additional interfaces that are being exposed from the native side need to be exposed from this package.

Something to keep note of is to maintain the consistency between the interfaces being exposed from iOS and android modules especially in terms of naming convention, function arguments and return values. This will reduce contention betwen the two modules and allow the javascript wrapper to be simple to understand and also to export the required react interfaces for consumption above in the chain.

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