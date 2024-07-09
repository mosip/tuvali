## Design notes
### 1. Peripheral unable to disconnect issue

As specified in this Google issue - https://issuetracker.google.com/issues/37127644.
Gatt server cannot disconnect from the central,
it can only indicate Android that App is no longer using the connection.
Android decides when to force a disconnect based on internal timeout.

**Example scenario from INJI:**
1. Wallet scans for the verifier and connects.
2. Wallet establishes the Cypto connection and waits for the User to select VC.
3. If User doesn't select any VC, Verifier gets option to cancel the transfer.
4. Verifier(Peripheral) calls cancelConnection. Yet, Android OS doesn't disconnect the connection and connection is alive until timeout or Wallet destroys connection.

This creates a situation where Verifier is waiting for a new connection yet previous connection is still alive. If Wallet fails to destroy connection then reconnect will keep on failing due to existing connection.

### Solution
Create a new characteristic on wallet for handling disconnection. Verifier notifies on the characteristic whenever it wants to cancel the connection.
