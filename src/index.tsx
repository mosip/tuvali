import { NativeEventEmitter, NativeModules, Platform } from 'react-native';
import type { OpenIDBLEShare } from './types/bleshare';
import { strFromU8, strToU8, compressSync } from 'fflate';

const LINKING_ERROR =
  `The package 'react-native-openid4vp-ble' doesn't seem to be linked. Make sure: \n\n` +
  Platform.select({ ios: "- You have run 'pod install'\n", default: '' }) +
  '- You rebuilt the app after installing the package\n' +
  '- You are not using Expo Go\n';

const Openid4vpBle: OpenIDBLEShare = NativeModules.Openid4vpBle
  ? NativeModules.Openid4vpBle
  : new Proxy(
      {},
      {
        get() {
          throw new Error(LINKING_ERROR);
        },
      }
    );

const newOpenid4vpBle: OpenIDBLEShare = {
  ...Openid4vpBle,
  send(message: string, callback: () => void) {
    const messageBytes = strToU8(message, false);
    const compressedMessage = compressSync(messageBytes, { level: 6, mem: 8 });
    Openid4vpBle.send(strFromU8(compressedMessage, false), callback);
  },
};

if (Platform.OS === 'android') {
  const eventEmitter = new NativeEventEmitter();
  newOpenid4vpBle.handleNearbyEvents = (callback) =>
    eventEmitter.addListener('EVENT_NEARBY', callback);
  newOpenid4vpBle.handleLogEvents = (callback) =>
    eventEmitter.addListener('EVENT_LOG', callback);
}

export default {
  newOpenid4vpBle,
};
