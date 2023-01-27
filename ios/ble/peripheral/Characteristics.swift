import Foundation
import CoreBluetooth

typealias CharacteristicTuple = (properties: CBCharacteristicProperties, permissions: CBAttributePermissions, value: Data?)

struct CBcharatcteristic {

    let IDENTITY_CHARACTERISTIC = CBMutableCharacteristic(type: CBUUID(string: CharacteristicIds.IDENTITY_CHARACTERISTIC_UUID.rawValue), properties: characteristicsMap[CharacteristicIds.IDENTITY_CHARACTERISTIC_UUID.rawValue]!.properties, value: nil, permissions: characteristicsMap[CharacteristicIds.IDENTITY_CHARACTERISTIC_UUID.rawValue]!.permissions)
    
    let REQUEST_SIZE_CHAR = CBMutableCharacteristic(type: CBUUID(string: CharacteristicIds.REQUEST_SIZE_CHAR_UUID.rawValue), properties: characteristicsMap[CharacteristicIds.REQUEST_SIZE_CHAR_UUID.rawValue]!.properties, value: nil, permissions: characteristicsMap[CharacteristicIds.REQUEST_SIZE_CHAR_UUID.rawValue]!.permissions)

    let REQUEST_CHAR = CBMutableCharacteristic(type: CBUUID(string: CharacteristicIds.REQUEST_CHAR_UUID.rawValue), properties: characteristicsMap[CharacteristicIds.REQUEST_CHAR_UUID.rawValue]!.properties, value: nil, permissions: characteristicsMap[CharacteristicIds.REQUEST_CHAR_UUID.rawValue]!.permissions)

    let RESPONSE_SIZE_CHAR = CBMutableCharacteristic(type: CBUUID(string: CharacteristicIds.RESPONSE_SIZE_CHAR_UUID.rawValue), properties: characteristicsMap[CharacteristicIds.RESPONSE_SIZE_CHAR_UUID.rawValue]!.properties, value: nil, permissions: characteristicsMap[CharacteristicIds.RESPONSE_SIZE_CHAR_UUID.rawValue]!.permissions)
    
    let RESPONSE_CHAR = CBMutableCharacteristic(type: CBUUID(string: CharacteristicIds.RESPONSE_CHAR_UUID.rawValue), properties: characteristicsMap[CharacteristicIds.RESPONSE_CHAR_UUID.rawValue]!.properties, value: characteristicsMap[CharacteristicIds.RESPONSE_CHAR_UUID.rawValue]!.value, permissions: characteristicsMap[CharacteristicIds.RESPONSE_CHAR_UUID.rawValue]!.permissions)

    let SEMAPHORE_CHAR = CBMutableCharacteristic(type: CBUUID(string: CharacteristicIds.SEMAPHORE_CHAR_UUID.rawValue), properties: characteristicsMap[CharacteristicIds.SEMAPHORE_CHAR_UUID.rawValue]!.properties, value: characteristicsMap[CharacteristicIds.SEMAPHORE_CHAR_UUID.rawValue]!.value, permissions: characteristicsMap[CharacteristicIds.SEMAPHORE_CHAR_UUID.rawValue]!.permissions)
                                                     
    let VERIFICATION_STATUS_CHAR = CBMutableCharacteristic(type: CBUUID(string: CharacteristicIds.VERIFICATION_STATUS_CHAR_UUID.rawValue), properties: characteristicsMap[CharacteristicIds.VERIFICATION_STATUS_CHAR_UUID.rawValue]!.properties, value: characteristicsMap[CharacteristicIds.VERIFICATION_STATUS_CHAR_UUID.rawValue]!.value, permissions: characteristicsMap[CharacteristicIds.VERIFICATION_STATUS_CHAR_UUID.rawValue]!.permissions)
}

// TODO: Add conn status change everywhere

enum CharacteristicIds: String, CaseIterable {
    case IDENTITY_CHARACTERISTIC_UUID = "00002030-0000-1000-8000-00805f9b34fb"
    case REQUEST_SIZE_CHAR_UUID = "00002031-0000-1000-8000-00805f9b34fb"
    case REQUEST_CHAR_UUID = "00002032-0000-1000-8000-00805f9b34fb"
    case RESPONSE_SIZE_CHAR_UUID = "00002033-0000-1000-8000-00805f9b34fb"
    case RESPONSE_CHAR_UUID = "00002034-0000-1000-8000-00805f9b34fb"
    case SEMAPHORE_CHAR_UUID = "00002035-0000-1000-8000-00805f9b34fb"
    case VERIFICATION_STATUS_CHAR_UUID = "00002036-0000-1000-8000-00805f9b34fb"
    case CONNECTION_STATUS_CHANGE_CHAR_UUID = "00002037-0000-1000-8000-00805f9b34fb"
}

let characteristicsMap: [String: CharacteristicTuple] = [
    "00002030-0000-1000-8000-00805f9b34fb": (properties: CBCharacteristicProperties([.writeWithoutResponse, .write,]), permissions: CBAttributePermissions([.writeable]), value: nil),
    "00002031-0000-1000-8000-00805f9b34fb": (properties: CBCharacteristicProperties([.read, .indicate]), permissions: CBAttributePermissions([.readable]), value: nil),
    "00002032-0000-1000-8000-00805f9b34fb": (properties: CBCharacteristicProperties([.read, .indicate]), permissions: CBAttributePermissions([.readable]), value: nil),
    "00002033-0000-1000-8000-00805f9b34fb": (properties: CBCharacteristicProperties([.writeWithoutResponse, .write]), permissions: CBAttributePermissions([.writeable]), value: nil),
    "00002034-0000-1000-8000-00805f9b34fb": (properties: CBCharacteristicProperties([.writeWithoutResponse, .write]), permissions: CBAttributePermissions([.writeable]), value: nil),
    "00002035-0000-1000-8000-00805f9b34fb": (properties: CBCharacteristicProperties([.writeWithoutResponse, .write]), permissions: CBAttributePermissions([.readable, .writeable]), value: nil),
    "00002036-0000-1000-8000-00805f9b34fb": (properties: CBCharacteristicProperties([.read, .indicate]), permissions: CBAttributePermissions([.readable]), value: nil),
]

struct TransferService {
    static let identifyRequestCharacteristic = CBUUID(string: "00002030-0000-1000-8000-00805f9b34fb")
    static let requestSizeCharacteristic = CBUUID(string: "00002031-0000-1000-8000-00805f9b34fb")
    static let requestCharacteristic = CBUUID(string: "00002032-0000-1000-8000-00805f9b34fb")
    static let responseSizeCharacteristic = CBUUID(string: "00002033-0000-1000-8000-00805f9b34fb")
    static let responseCharacteristic = CBUUID(string: "00002034-0000-1000-8000-00805f9b34fb")
    static let semaphoreCharacteristic = CBUUID(string: "00002035-0000-1000-8000-00805f9b34fb")
    static let verificationStatusCharacteristic = CBUUID(string: "00002036-0000-1000-8000-00805f9b34fb")
    static let connectionStatusChangeCharacteristic = CBUUID(string: "00002037-0000-1000-8000-00805f9b34fb")

    static let serviceUUID = CBUUID(string: "0000AB29-0000-1000-8000-00805f9b34fb") // same
    static let scanResponseServiceUUID = CBUUID(string: "0000AB2A-0000-1000-8000-00805f9b34fb")

    // older stuff
    // @deprecated
    static let characteristicUUID = CBUUID(string: "00002032-0000-1000-8000-00805f9b34fb") //read characteristics
    static let writeCharacteristic = CBUUID(string: "00002031-0000-1000-8000-00805f9b34fb")
}

struct NetworkCharNums {
    static let identifyRequestCharacteristic = CBUUID(string: "2030")
    static let requestSizeCharacteristic = CBUUID(string: "2031")
    static let requestCharacteristic = CBUUID(string: "2032")
    static let responseSizeCharacteristic = CBUUID(string: "2033")
    static let responseCharacteristic = CBUUID(string: "2034")
    static let semaphoreCharacteristic = CBUUID(string: "2035")
    static let verificationStatusCharacteristic = CBUUID(string: "2036")
    static let connectionStatusChangeCharacteristic = CBUUID(string: "2037")
}

enum NotificationEvent: String {
    case EXCHANGE_RECEIVER_INFO = "EXCHANGE_RECEIVER_INFO"
    case CREATE_CONNECTION = "CREATE_CONNECTION"
    case RESPONSE_SIZE_WRITE_SUCCESS = "RESPONSE_SIZE_WRITE_SUCCESS"
    case HANDLE_TRANSMISSION_REPORT = "HANDLE_TRANSMISSION_REPORT"
    case INIT_RESPONSE_CHUNK_TRANSFER = "INIT_RESPONSE_CHUNK_TRANSFER"
    case VERIFICATION_STATUS_RESPONSE = "VERIFICATION_STATUS_RESPONSE"
    case CONNECTION_STATUS_CHANGE = "CONNECTION_STATUS_CHANGE"
    
}



