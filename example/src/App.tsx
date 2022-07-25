import * as React from 'react';

import { StyleSheet, View, Text, TouchableOpacity } from 'react-native';
import { multiply, turnBluetoothAdvertise } from 'react-native-awesome-module';

export default function App() {
  const [result, setResult] = React.useState<number | undefined>();
  const [status, setStatus] = React.useState<boolean | undefined>(false);

  React.useEffect(() => {
    multiply(3, 7).then(setResult);
  }, []);

  const startAdvertising = async () => {
    try {
      let result = await turnBluetoothAdvertise('supercool', !status);
      console.log(`result: ${result}`);
      setStatus(!status);
    } catch (e) {
      console.log((e as Error).message);
    }
  };

  return (
    <View style={styles.container}>
      <Text>Hello WOOORLD</Text>
      <Text>Result: {result}</Text>
      <TouchableOpacity style={styles.button} onPress={startAdvertising}>
        <Text style={styles.btnText}>
          {status ? 'Stop advertising' : 'Start advertising'}
        </Text>
      </TouchableOpacity>
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
  button: {
    width: 160,
    alignItems: 'center',
    justifyContent: 'center',
    borderRadius: 15,
    padding: 10,
    margin: 20,
    backgroundColor: '#0096FF',
  },
  btnText: {
    fontSize: 18,
  },
});
