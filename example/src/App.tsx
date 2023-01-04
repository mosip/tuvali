import * as React from 'react';

import { StyleSheet, View, Text } from 'react-native';
import openBle from 'react-native-openid4vp-ble';

const { Openid4vpBle } = openBle;

export default function App() {
  const [connectionParams, setConnectionParams] = React.useState<string>();

  React.useEffect(() => {
    Openid4vpBle.send('discoverer', () => {
      console.log('SEND CREATED');
    });
    setConnectionParams('SEND DONE');
  }, []);

  return (
    <View style={styles.container}>
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
