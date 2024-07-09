import { NativeEventEmitter, NativeModules, Platform } from 'react-native';
import type { Verifier, VersionModule, Wallet } from './types/interface';
import { tuvaliVersion } from './tuvaliVersion';
import { EventTypes, VerificationStatus } from './types/events';

const LINKING_ERROR =
  `The package '@mosip/tuvali' doesn't seem to be linked. Make sure: \n\n` +
  Platform.select({ ios: "- You have run 'pod install'\n", default: '' }) +
  '- You rebuilt the app after installing the package\n' +
  '- You are not using Expo Go\n';

const VERIFIER_NOT_IMPLEMENTATED_ERROR = `Verifier is not yet implemented on IOS. Please remove Verifier usage on IOS Platform`;
const isIOS = Platform.OS === 'ios';

const versionModule: VersionModule = NativeModules.VersionModule
  ? NativeModules.VersionModule
  : new Proxy(
      {},
      {
        get() {
          throw new Error(LINKING_ERROR);
        },
      }
    );

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

// TODO: Use Actual Verifier module on IOS once Verifier is implemented
let verifier: Verifier = NativeModules.VerifierModule
  ? NativeModules.VerifierModule
  : new Proxy(
      {},
      {
        get() {
          throw new Error(
            isIOS ? VERIFIER_NOT_IMPLEMENTATED_ERROR : LINKING_ERROR
          );
        },
      }
    );

versionModule.setTuvaliVersion(tuvaliVersion);

if (!isIOS) {
  setupModule(verifier);
}

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
  if (Platform.OS === 'android') {
    const eventEmitter = new NativeEventEmitter();
    module.handleDataEvents = (callback: (event: any) => void) =>
      eventEmitter.addListener('DATA_EVENT', callback);
  }

  if (isIOS) {
    const eventEmitter = new NativeEventEmitter(module);
    module.handleDataEvents = (callback: (event: any) => void) =>
      eventEmitter.addListener('DATA_EVENT', callback);
  }
}

export default {
  verifier,
  wallet,
  EventTypes,
  VerificationStatus,
};
