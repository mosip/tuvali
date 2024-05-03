import Foundation
import CoreBluetooth
import os
import React


@available(iOS 13.0, *)
class Central: NSObject, CBCentralManagerDelegate {

    var retryStrategy : BackOffStrategy = BackOffStrategy(MAX_RETRY_LIMIT: 10)
    var transferReportRequestRetryStrategy = BackOffStrategy(MAX_RETRY_LIMIT: 5)
    var centralManager: CBCentralManager!
    var connectedPeripheral: CBPeripheral?
    var cbCharacteristics: [String: CBCharacteristic] = [:]
    var delegate: PeripheralCommunicatorProtocol?
    var walletBleCommunicatorDelegate: WalletBleCommunicatorProtocol?
    var createConnection: (() -> Void)?

    override init() {
        super.init()
        centralManager = CBCentralManager(delegate: self, queue: nil)
    }

    func centralManagerDidUpdateState(_ central: CBCentralManager) {
        switch central.state {
        case .poweredOn:
            os_log(.info, "Central Manager state is powered ON")
            scanForPeripherals()
        case .poweredOff:
            os_log(.info, "Central Manager state is powered OFF")
            EventEmitter.sharedInstance.emitEvent(DisconnectedEvent())
        default:
            os_log(.debug, "Central Manager state is in state - %@",central.state as! CVarArg)
        }
    }

    deinit {
        os_log(.info, "Central is DeInitializing")
    }

    func scanForPeripherals() {
        centralManager.scanForPeripherals(withServices: [Peripheral.SERVICE_UUID], options: [CBCentralManagerScanOptionAllowDuplicatesKey: false])
        os_log(.info, "Scanning happening ")
    }

    /**
     * write(..) writes data on a charUUID without response
     */
    func writeWithResponse(serviceUuid: CBUUID, charUUID: CBUUID, data: Data) {
        if let connectedPeripheral = connectedPeripheral {
            guard let characteristic = self.cbCharacteristics[charUUID.uuidString] else {
                os_log(.info, "Did not find the characteristic to write")
                return
            }
            let messageData = Data(bytes: Array(data), count: data.count)
            connectedPeripheral.writeValue(messageData, for: characteristic, type: .withResponse)
        } else {
            os_log(.info, "connectedPeripheral is nil while writing with resp to char: %{public}s", charUUID.uuidString)
        }
    }

    /**
     * writeWithoutResp(...) writes data on a charUUID without response
     */
    func writeWithoutResp(serviceUuid: CBUUID, charUUID: CBUUID, data: Data) {
        if let connectedPeripheral = connectedPeripheral {
            guard let characteristic = self.cbCharacteristics[charUUID.uuidString] else {
                os_log(.info, "Did not find the characteristic to write")
                return
            }
            let messageData = Data(bytes: Array(data), count: data.count)
            connectedPeripheral.writeValue(messageData, for: characteristic, type: .withoutResponse)
        }
    }

    func disconnect() {
        if let connectedPeripheral = self.connectedPeripheral {
            centralManager.cancelPeripheralConnection(connectedPeripheral)
        }
    }
}
