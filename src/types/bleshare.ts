import type { EmitterSubscription } from 'react-native';

interface TuvaliModule {
  noop: () => void;
  createConnection: (callback: () => void) => void;
  destroyConnection: (callback: () => void) => void;
  send: (message: string, callback: () => void) => void;
  handleNearbyEvents: (
    callback: (events: NearbyEvent) => void
  ) => EmitterSubscription;
  handleLogEvents: (
    callback: (event: NearbyLog) => void
  ) => EmitterSubscription;
  setTuvaliVersion: (version: string) => void;
}

export interface Verifier extends TuvaliModule {
  getConnectionParameters: () => string;
}

export interface Wallet extends TuvaliModule {
  setConnectionParameters: (params: string) => void;
}

export type ConnectionMode = 'dual' | 'advertiser' | 'discoverer';

export type TransferUpdateStatus =
  | 'SUCCESS'
  | 'FAILURE'
  | 'IN_PROGRESS'
  | 'CANCELLED';

export type NearbyEvent =
  | { type: 'msg'; data: string }
  | { type: 'transferupdate'; data: TransferUpdateStatus }
  | { type: 'onDisconnected'; data: string }
  | { type: 'onError'; message: string; code: string };

export interface NearbyLog {
  log: string;
}

export interface ConnectionParameters {
  cid: string;
  pk: string;
}
