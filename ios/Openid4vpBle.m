#import <Foundation/Foundation.h>
#import <React/RCTBridgeModule.h>
#import <React/RCTEventEmitter.h>

@interface RCT_EXTERN_MODULE(Openid4vpBle, RCTEventEmitter)

RCT_EXTERN__BLOCKING_SYNCHRONOUS_METHOD(getConnectionParameters)

RCT_EXTERN__BLOCKING_SYNCHRONOUS_METHOD(getConnectionParametersDebug)

/**
 set the connection parameters that will be required to exchange data with the verifier.
 params is of type { cid, pk } - pk is the partial public key being broadcast by the verifier.
 @param params a json object that has the pk needed for creating the shared key
 @return void
 */
RCT_EXTERN__BLOCKING_SYNCHRONOUS_METHOD(setConnectionParameters:(NSString *)params)

RCT_EXTERN_METHOD(send:(NSString *)message withCallback:(RCTResponseSenderBlock)callback)

RCT_EXTERN_METHOD(createConnection:(NSString *)mode withCallback:(RCTResponseSenderBlock)callback)

RCT_EXTERN_METHOD(destroyConnection:(RCTResponseSenderBlock)callback)

@end

