import Foundation
import CoreBluetooth

typealias CharacteristicTuple = (properties: CBCharacteristicProperties, permissions: CBAttributePermissions, value: Data?)

enum CharacteristicIds: String, CaseIterable {
    case IDENTITY_CHARACTERISTIC_UUID = "00002030-0000-1000-8000-00805f9b34fb"
    case REQUEST_SIZE_CHAR_UUID = "00002031-0000-1000-8000-00805f9b34fb"
    case REQUEST_CHAR_UUID = "00002032-0000-1000-8000-00805f9b34fb"
    case RESPONSE_SIZE_CHAR_UUID = "00002033-0000-1000-8000-00805f9b34fb"
    case RESPONSE_CHAR_UUID = "00002034-0000-1000-8000-00805f9b34fb"
    case SEMAPHORE_CHAR_UUID = "00002035-0000-1000-8000-00805f9b34fb"
    case VERIFICATION_STATUS_CHAR_UUID = "00002036-0000-1000-8000-00805f9b34fb"
}

let characteristics: [String: CharacteristicTuple] = [
    "00002030-0000-1000-8000-00805f9b34fb": (properties: CBCharacteristicProperties([.writeWithoutResponse, .write,]), permissions: CBAttributePermissions([.writeable]), value: nil),
    "00002031-0000-1000-8000-00805f9b34fb": (properties: CBCharacteristicProperties([.read, .indicate]), permissions: CBAttributePermissions([.readable]), value: nil),
    "00002032-0000-1000-8000-00805f9b34fb": (properties: CBCharacteristicProperties([.read, .indicate]), permissions: CBAttributePermissions([.readable]), value: nil),
    "00002033-0000-1000-8000-00805f9b34fb": (properties: CBCharacteristicProperties([.writeWithoutResponse, .write]), permissions: CBAttributePermissions([.writeable]), value: nil),
    "00002034-0000-1000-8000-00805f9b34fb": (properties: CBCharacteristicProperties([.writeWithoutResponse, .write]), permissions: CBAttributePermissions([.writeable]), value: nil),
    "00002035-0000-1000-8000-00805f9b34fb": (properties: CBCharacteristicProperties([.writeWithoutResponse, .write]), permissions: CBAttributePermissions([.readable, .writeable]), value: nil),
    "00002036-0000-1000-8000-00805f9b34fb": (properties: CBCharacteristicProperties([.read, .indicate]), permissions: CBAttributePermissions([.readable]), value: nil),
]
