import Foundation
import CoreBluetooth
import os
import React

@available(iOS 13.0, *)
class Central: NSObject, CBCentralManagerDelegate {

    var retryStrategy : BackOffStrategy = BackOffStrategy(MAX_RETRY_LIMIT: 10)
    var centralManager: CBCentralManager!
    var connectedPeripheral: CBPeripheral?
    var cbCharacteristics: [String: CBCharacteristic] = [:]
    var tuvaliVersion: String?
    var delegate: PeripheralCommunicatorProtocol?
    var walletDelegate: WalletProtocol?
    var createConnection:(()->Void)?

    public static var shared = Central()

    func initialize() {
        walletDelegate = Wallet.shared
        centralManager = CBCentralManager(delegate: self, queue: nil)
    }

    func centralManagerDidUpdateState(_ central: CBCentralManager) {
        switch central.state {
        case .poweredOn:
            os_log("Central Manager state is powered ON : v%{public}@", tuvaliVersion!)
            scanForPeripherals()
        default:
            os_log("Central Manager state is powered OFF : v%{public}@", tuvaliVersion!)
        }
    }

    deinit {
        os_log("Central is DeInitializing : v%{public}@", tuvaliVersion!)
    }

    func scanForPeripherals() {
        centralManager.scanForPeripherals(withServices: [Peripheral.SERVICE_UUID], options: [CBCentralManagerScanOptionAllowDuplicatesKey: false])
        os_log("scanning happening :::::::  v%{public}@", tuvaliVersion!)
    }

    /**
     * write(..) writes data on a charUUID without response
     */
    func write(serviceUuid: CBUUID, charUUID: CBUUID, data: Data) {
        if let connectedPeripheral = connectedPeripheral {
            if connectedPeripheral.canSendWriteWithoutResponse {
                guard let characteristic = self.cbCharacteristics[charUUID.uuidString] else {
                    os_log("Did not find the characteristic to write : v%{public}@",tuvaliVersion!)
                    return
                }
                let messageData = Data(bytes: Array(data), count: data.count)
                connectedPeripheral.writeValue(messageData, for: characteristic, type: .withResponse)
            }
        }
    }

    /**
     * writeWithoutResp(...) writes data on a charUUID without response
     */
    func writeWithoutResp(serviceUuid: CBUUID, charUUID: CBUUID, data: Data) {
        if let connectedPeripheral = connectedPeripheral {
            guard let characteristic = self.cbCharacteristics[charUUID.uuidString] else {
                os_log("Did not find the characteristic to write : v%{public}@",tuvaliVersion!)
                return
            }
            let messageData = Data(bytes: Array(data), count: data.count)
            connectedPeripheral.writeValue(messageData, for: characteristic, type: .withoutResponse)
        }
    }
}
