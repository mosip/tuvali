import Foundation
import CoreBluetooth

@available(iOS 13.0, *)
class Peripheral: NSObject {
    private var peripheralManager: CBPeripheralManager!
    
    static let SERVICE_UUID = CBUUID(string: "0000AB29-0000-1000-8000-00805f9b34fb")
    static let SCAN_RESPONSE_SERVICE_UUID = CBUUID(string: "0000AB2A-0000-1000-8000-00805f9b34fb")
    
    override init() {
        super.init()
        peripheralManager = CBPeripheralManager(delegate: self, queue: nil, options: [CBPeripheralManagerOptionShowPowerAlertKey: true])
    }

    func setupPeripheralsAndStartAdvertising() {
        let bleService = CBMutableService(type: Self.SERVICE_UUID, primary: true)
        bleService.characteristics = Util.createCBMutableCharacteristics()
        peripheralManager.add(bleService)
        peripheralManager.startAdvertising([CBAdvertisementDataServiceUUIDsKey: [Self.SERVICE_UUID, Self.SCAN_RESPONSE_SERVICE_UUID], CBAdvertisementDataLocalNameKey: "verifier"])
    }
}
