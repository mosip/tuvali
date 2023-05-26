#import <Foundation/Foundation.h>
#import <React/RCTBridgeModule.h>
#import <React/RCTEventEmitter.h>

@interface RCT_EXTERN_MODULE(WalletModule, RCTEventEmitter)

RCT_EXTERN_METHOD(startConnection:(NSString *) uri)

RCT_EXTERN_METHOD(sendData:(NSString *) data)

RCT_EXTERN_METHOD(disconnect)

@end

@interface RCT_EXTERN_MODULE(VersionModule, RCTEventEmitter)

RCT_EXTERN__BLOCKING_SYNCHRONOUS_METHOD(setTuvaliVersion:(NSString *) version)

@end

