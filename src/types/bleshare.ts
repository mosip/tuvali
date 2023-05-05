import type { EmitterSubscription } from 'react-native';

interface TuvaliModule {
  noop: () => void;
  disconnect: () => void;
  send: (message: string) => void;
  setTuvaliVersion: (version: string) => void;
}

export interface Verifier extends TuvaliModule {
  startAdvertisement: (advIdentifier: String) => string;
  sendVerificationStatus: (status: String) => void;
  handleDataEvents: (
    callback: (events: VerifierDataEvent) => void
  ) => EmitterSubscription;
}

export interface Wallet extends TuvaliModule {
  startConnection: (advIdentifier: String, advPayload: String) => void;
  handleDataEvents: (
    callback: (events: WalletDataEvent) => void
  ) => EmitterSubscription;
}

export type TransferUpdateStatus =
  | 'SUCCESS'
  | 'FAILURE'
  | 'IN_PROGRESS'
  | 'CANCELLED';

export type VerificationStatus = 'APPROVED' | 'REJECTED';

export type ConnectedEvent = { type: 'onConnected' };

export type KeyExchangeSuccess = { type: 'onKeyExchangeSuccess' };

export type TransferStatusUpdateEvent = {
  type: 'onTransferStatusUpdate';
  status: TransferUpdateStatus;
};

export type VCReceivedEvent = {
  type: 'onVCReceived';
  vc: string;
};

export type VerificationStatusEvent = {
  type: 'onVerificationStatusReceived';
  status: VerificationStatus;
};

export type DisconnectedEvent = {
  type: 'onDisconnected';
};

export type ErrorEvent = {
  type: 'onError';
  message: string;
  code: string;
};

export type CommonDataEvent =
  | ConnectedEvent
  | TransferStatusUpdateEvent
  | DisconnectedEvent
  | ErrorEvent;

export type WalletDataEvent = CommonDataEvent;

export type VerifierDataEvent = CommonDataEvent | VCReceivedEvent;

export interface NearbyLog {
  log: string;
}

export interface ConnectionParameters {
  cid: string;
  pk: string;
}
