import { NativeEventEmitter, NativeModules, Platform } from 'react-native';
import type { NearbyEvent, OpenIDBLEShare } from './types/bleshare';
import * as fflate from 'fflate';

type event = Array<string>;

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

const ExportedOpenid4vpBle = {
  ...Openid4vpBle,
  send(message: string, callback: () => void) {
    const [messageType, messageData]: event = message.split('\n');
    if (messageType !== 'send-vc') {
      Openid4vpBle.send(message, callback);
    }

    const messageBytes = fflate.compressSync(
      fflate.strToU8(messageData as string, false),
      { level: 6, mem: 8 }
    );
    const messageString = fflate.strFromU8(messageBytes);
    Openid4vpBle.send(`${messageType}\n${messageString}`, callback);
  },
};

if (Platform.OS === 'android') {
  const eventEmitter = new NativeEventEmitter();
  ExportedOpenid4vpBle.handleNearbyEvents = (callback) => {
    return eventEmitter.addListener('EVENT_NEARBY', (event: NearbyEvent) => {
      if (event.type !== 'msg') {
        callback(event);
      }
      const [messageType, messageData]: event = event.data.split('\n');
      if (messageType !== 'send-vc') {
        callback(event);
      }
      const compressedBytes = fflate.strToU8(messageData as string);
      const uncompressedBytes = fflate.decompressSync(compressedBytes);
      const uncompressedString = fflate.strFromU8(uncompressedBytes);
      event.data = `${messageType}\n${uncompressedString}`;
      callback(event);
    });
  };
  ExportedOpenid4vpBle.handleLogEvents = (callback) =>
    eventEmitter.addListener('EVENT_LOG', callback);
}

export default {
  ExportedOpenid4vpBle,
};
