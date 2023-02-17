import type { EmitterSubscription } from 'react-native';

export interface OpenIDBLEShare {
  noop: () => void;
  getConnectionParameters: () => string;
  setConnectionParameters: (params: string) => void;
  getConnectionParametersDebug: () => string;
  createConnection: (mode: ConnectionMode, callback: () => void) => void;
  destroyConnection: (callback: () => void) => void;
  send: (message: string, callback: () => void) => void;
  handleNearbyEvents: (
    callback: (events: NearbyEvent) => void
  ) => EmitterSubscription;
  handleLogEvents: (
    callback: (event: NearbyLog) => void
  ) => EmitterSubscription;
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
  | { type: 'onError'; message: string };

export interface NearbyLog {
  log: string;
}

export interface ConnectionParameters {
  cid: string;
  pk: string;
}
