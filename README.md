
> Warning: The library is under active development and no major version is released yet. Please anticipate non backward compatible changes to the API and functional behavior in the upcoming releases.

# Tuvali - React native module library
This is the react native module for the [OpenID for Verifiable Presentations over BLE](https://tlodderstedt.github.io/openid-for-verifiable-presentations-offline-1_0-00.html) implementation to support sending vc/vp using Bluetooth Low Energy local channel. 

This contains the source code for the ios, android modules as well as a sample app under `example/` folder. The sample app can be used for testing the modules being worked on in case it is needed.

## Installing this library as a dependency

```bash
# Install latest version
npm install react-native-openid4vp-ble

# or

# Install specific version
npm install react-native-openid4vp-ble@github:mosip/tuvali#v0.3.5
```

# API documentation
Firstly, for establishing the secured connection over BLE the connection params which include `cid` and `Public key` needs to be exchanged between two devices. The exchange of parameters can be accomplished, but is not limited to, by using a QR code.

For example use QR code generator to visually display params and QR code scanner to get params. A mobile app that displays a QR code can act as an `advertiser` by including its connection params as data in the QR code and another device can act as `discoverer` which scans the QR code, it can extract the connection params and initiate a BLE connection with the advertising device.

## Connection parameters exchange
The device on which the QR code is displayed shall generate connection parameters using getConnectionParameters() method:

```typescript
import OpenIdBle from 'react-native-openid4vp-ble';
const { Openid4vpBle } = OpenIdBle;

const params = Openid4vpBle.getConnectionParameters();
console.log(params);
```

The device that scans the QR code will extract the connection parameters from QR code and set its connection parameters using setConnectionParameters() method :

```typescript
Openid4vpBle.setConnectionParameters(params);
```
The connection params contains:

```json
{
"cid": "ilB8l",
"pk": "4f56504d4f5349505f66b067c008A4484AEC5A769CED2307F59E43DC81A3F768"
}
```
The `pk` part of the data is the same data that will be advertised by the `advertiser` device but in hex encoded form. 
E.g: OVPMOSIP_<first 5 bytes of public key>

Note: The "cid" parameter is there for legacy reasons and will be removed soon.

## Establising connection

The device that displays the QR code will become `advertiser` and waits for a connection:

```typescript
Openid4vpBle.createConnection('advertiser', () => {
  // A secure Bluetooth connection is created
  // Any device on which app is installed may call Openid4vpBle.send()
});
```

and the other device that scans the QR code will become `discoverer` and will attempt to discover the devices:

```typescript
Openid4vpBle.createConnection('discoverer', () => {
  // A secure Bluetooth connection is created
  // Any device on which app is installed may call Openid4vpBle.send()
});
```

## Share data

Once the connection is established, either app can send the data:

```typescript
Openid4vpBle.send(message, () => {
// data sent
// TODO: document message structure
});
```

Data received from other app via BLE can be subscribed to using:
```typescript
Openid4vpBle.handleNearbyEvents((event) => {
// data received as event
// TODO: Document event structure for data and error
})
```

## Connection closure

The device on which app is running can destroy the connection by calling destroyConnection() method:

```typescript
Openid4vpBle.destroyConnection();
```
