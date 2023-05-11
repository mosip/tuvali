import { NativeEventEmitter, NativeModules, Platform } from 'react-native';
import type { Verifier, Wallet } from './types/bleshare';
import { tuvaliVersion } from './tuvaliVersion';

const LINKING_ERROR =
  `The package 'react-native-openid4vp-ble' doesn't seem to be linked. Make sure: \n\n` +
  Platform.select({ ios: "- You have run 'pod install'\n", default: '' }) +
  '- You rebuilt the app after installing the package\n' +
  '- You are not using Expo Go\n';

const wallet: Wallet = NativeModules.WalletModule
  ? NativeModules.WalletModule
  : new Proxy(
      {},
      {
        get() {
          throw new Error(LINKING_ERROR);
        },
      }
    );

const verifier: Verifier = NativeModules.VerifierModule
  ? NativeModules.VerifierModule
  : new Proxy(
      {},
      {
        get() {
          throw new Error(LINKING_ERROR);
        },
      }
    );

setupModule(verifier);
setupModule(wallet);

//
// ErrorUtils.setGlobalHandler((error, isFatal) => {
//   const eventEmitter = new NativeEventEmitter(NativeModules.Openid4vpBle);
//
//   console.error(
//     `Exception in Tuvali: isFatal: ${isFatal}, error: ${JSON.stringify(
//       error,
//       null,
//       4
//     )}`
//   );
//
//   eventEmitter.emit('EVENT_NEARBY', { type: 'onError', message: '' });
//   Openid4vpBle.destroyConnection(() => {});
// });

function setupModule(module: any) {
  module.setTuvaliVersion(tuvaliVersion);

  if (Platform.OS === 'android') {
    const eventEmitter = new NativeEventEmitter();
    module.handleDataEvents = (callback: (event: any) => void) =>
      eventEmitter.addListener('DATA_EVENT', callback);
  }

  if (Platform.OS === 'ios') {
    console.log(`IOS PLATFORM`);
    const eventEmitter = new NativeEventEmitter(NativeModules.Openid4vpBle);
    module.handleDataEvents = (callback: (event: any) => void) =>
      eventEmitter.addListener('DATA_EVENT', callback);
  }
}

export default {
  verifier,
  wallet,
};
