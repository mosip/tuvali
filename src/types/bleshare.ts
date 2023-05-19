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
  startConnection: (advPayload: String) => void;
  sendData: (data: string) => void;
  handleDataEvents: (
    callback: (events: WalletDataEvent) => void
  ) => EmitterSubscription;
}

export type VerificationStatus = 'ACCEPTED' | 'REJECTED';

export type ConnectedEvent = { type: 'onConnected' };

export type SecureChannelEstablished = { type: 'onSecureChannelEstablished' };

export type DataReceivedEvent = {
  type: 'onDataReceived';
  data: string;
};

export type DataSentEvent = {
  type: 'onDataSent';
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
  | DisconnectedEvent
  | ErrorEvent
  | SecureChannelEstablished;

export type WalletDataEvent =
  | CommonDataEvent
  | VerificationStatusEvent
  | DataSentEvent;

export type VerifierDataEvent = CommonDataEvent | DataReceivedEvent;
