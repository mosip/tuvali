#import <Foundation/Foundation.h>
#import <React/RCTBridgeModule.h>

@interface RCT_EXTERN_MODULE(Openid4vpBle, NSObject)

RCT_EXTERN__BLOCKING_SYNCHRONOUS_METHOD(getConnectionParameters)

RCT_EXTERN__BLOCKING_SYNCHRONOUS_METHOD(getConnectionParametersDebug)

RCT_EXTERN__BLOCKING_SYNCHRONOUS_METHOD(setConnectionParameters:(nonnull NSString *)params)

RCT_EXTERN_METHOD(send:(NSString *)message withCallback:(RCTResponseSenderBlock)callback)

RCT_EXTERN_METHOD(createConnection:(NSString *)mode withCallback:(RCTResponseSenderBlock)callback)

RCT_EXTERN__BLOCKING_SYNCHRONOUS_METHOD(destroyConnection)

@end

