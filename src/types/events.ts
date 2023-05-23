export type CommonDataEvent =
  | ConnectedEvent
  | DisconnectedEvent
  | ErrorEvent
  | SecureChannelEstablished;

export type WalletDataEvent =
  | CommonDataEvent
  | VerificationStatusReceivedEvent
  | DataSentEvent;

export type VerifierDataEvent = CommonDataEvent | DataReceivedEvent;

export type ConnectedEvent = { type: EventTypes.onConnected };

export type SecureChannelEstablished = {
  type: EventTypes.onSecureChannelEstablished;
};

export type DataReceivedEvent = {
  type: EventTypes.onDataReceived;
  data: string;
};

export type DataSentEvent = {
  type: EventTypes.onDataSent;
};

export type VerificationStatusReceivedEvent = {
  type: EventTypes.onVerificationStatusReceived;
  status: VerificationStatus;
};

export type DisconnectedEvent = {
  type: EventTypes.onDisconnected;
};

export type ErrorEvent = {
  type: EventTypes.onError;
  message: string;
  code: string;
};

export enum EventTypes {
  onSecureChannelEstablished = 'onSecureChannelEstablished',
  onVerificationStatusReceived = 'onVerificationStatusReceived',
  onDataSent = 'onDataSent',
  onDataReceived = 'onDataReceived',
  onError = 'onError',
  onDisconnected = 'onDisconnected',
  onConnected = 'onConnected',
}

export enum VerificationStatus {
  ACCEPTED = 'ACCEPTED',
  REJECTED = 'REJECTED',
}
