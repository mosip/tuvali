## Understanding Project structure
```
- /
 - src
 - android
 - ios
 - example (example app)
```

The primary javascript source code that interacts with the native modules lives in the `src/` directory.
Any additional interfaces that are being exposed from the native side need to be exposed from this package.

Something to keep note of is to maintain the consistency between the interfaces being exposed from iOS and android modules especially in terms of naming convention, function arguments and return values. This will reduce contention between the two modules and allow the javascript wrapper to be simple to understand and also to export the required react interfaces for consumption above in the chain.

### IOS Module specifics
The way the native module is setup is it contains 3 primary parts.

 - Swift files - These contain the primary business logic being exposed.
 - Bridging Header (Tuvali-Bridging-Header.h) - A obj-C bridging header that is needeed to use swift inside a objective-C project.
 - Objective-C module file (Tuvali.m) - This file exposes the modules to React Native via macros.

Whenever new functions are being added to the modules, the corresponding functions need to be exposed via the interface failing which the functions/methods will not be available on the react native side.

For installing dependencies for the ios module, install the required dependencies in the `tuvali.podspec` file.
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

### Android Module specifics
The android module is written in kotlin. The primary way of exposing functionality on the android side is to write modules that become part of the package. The package as described in the `AndroidManifest.xml` is built and exposed to the main application instance from the `getPackages` method.

There is one module that is exposed as part of the package
```
TuvaliModule
```

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

## Local development steps with [INJI App](https://github.com/mosip/inji)

We use an npm package called `yalc` to publish changes to a local repository for quick validation with INJI app.

### Pre Requisites
1. Install yalc with `yarn global add yalc`

### On Tuvali repo
1. Compile Tuvali with `yarn` command
2. Public Tuvali to local Yalc repo with `yalc publish`

### On Inji repo
1. Add locally published Tuvali repo to INJI with `yalc add @mosip/tuvali`
2. `npm install`
3. Open app from CLI `npm run android:mosip`. Optionally can run from Android studio as well.

#### Running INJI from Android Studio (workaround)
1. Bump up android gradle plugin to min of 4.2.2 (Ref: https://github.com/facebook/react-native/issues/35337)
