import type { EmitterSubscription } from 'react-native';

interface TuvaliModule {
  noop: () => void;
  disconnect: () => void;
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
  sendData: (message: string) => void;
  handleDataEvents: (
    callback: (events: WalletDataEvent) => void
  ) => EmitterSubscription;
}

export type TransferUpdateStatus =
  | 'SUCCESS'
  | 'FAILURE'
  | 'IN_PROGRESS'
  | 'CANCELLED';

export type VerificationStatus = 'ACCEPTED' | 'REJECTED';

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
  | ErrorEvent
  | KeyExchangeSuccess;

export type WalletDataEvent = CommonDataEvent | VerificationStatusEvent;

export type VerifierDataEvent = CommonDataEvent | VCReceivedEvent;
