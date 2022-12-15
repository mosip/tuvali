import * as React from 'react';

import { StyleSheet, View, Text } from 'react-native';
import openBle from 'react-native-openid4vp-ble';

const { Wallet, Verifier, Openid4vpBle } = openBle;

export default function App() {
  const [walletName, setWalletName] = React.useState<string>();
  const [verifierName, setVerifierName] = React.useState<string>();
  const [connectionParams, setConnectionParams] = React.useState<string>();

  React.useEffect(() => {
    Wallet.getModuleName().then((name: string) => setWalletName(name));
    Verifier.getModuleName().then((name: string) => setVerifierName(name));
    setConnectionParams(Openid4vpBle.getConnectionParameters());
  }, []);

  return (
    <View style={styles.container}>
      <Text>Wallet Name: {walletName}</Text>
      <Text>Verifier Name: {verifierName}</Text>
      <Text>Connection Params: {connectionParams}</Text>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
  },
  box: {
    width: 60,
    height: 60,
    marginVertical: 20,
  },
});
