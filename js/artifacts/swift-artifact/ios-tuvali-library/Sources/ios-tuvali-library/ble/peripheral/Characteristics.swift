import Foundation
import CoreBluetooth

typealias CharacteristicTuple = (properties: CBCharacteristicProperties, permissions: CBAttributePermissions, value: Data?)

struct CBcharatcteristic {

    let identifyRequestChar = CBMutableCharacteristic(type: CBUUID(string: CharacteristicIds.IDENTIFY_REQUEST_CHAR_UUID.rawValue), properties: characteristicsMap[CharacteristicIds.IDENTIFY_REQUEST_CHAR_UUID.rawValue]!.properties, value: nil, permissions: characteristicsMap[CharacteristicIds.IDENTIFY_REQUEST_CHAR_UUID.rawValue]!.permissions)

    let requestSizeChar = CBMutableCharacteristic(type: CBUUID(string: CharacteristicIds.REQUEST_SIZE_CHAR_UUID.rawValue), properties: characteristicsMap[CharacteristicIds.REQUEST_SIZE_CHAR_UUID.rawValue]!.properties, value: nil, permissions: characteristicsMap[CharacteristicIds.REQUEST_SIZE_CHAR_UUID.rawValue]!.permissions)

    let requestChar = CBMutableCharacteristic(type: CBUUID(string: CharacteristicIds.REQUEST_CHAR_UUID.rawValue), properties: characteristicsMap[CharacteristicIds.REQUEST_CHAR_UUID.rawValue]!.properties, value: nil, permissions: characteristicsMap[CharacteristicIds.REQUEST_CHAR_UUID.rawValue]!.permissions)

    let responseSizeChar = CBMutableCharacteristic(type: CBUUID(string: CharacteristicIds.RESPONSE_SIZE_CHAR_UUID.rawValue), properties: characteristicsMap[CharacteristicIds.RESPONSE_SIZE_CHAR_UUID.rawValue]!.properties, value: nil, permissions: characteristicsMap[CharacteristicIds.RESPONSE_SIZE_CHAR_UUID.rawValue]!.permissions)

    let submitResponseChar = CBMutableCharacteristic(type: CBUUID(string: CharacteristicIds.SUBMIT_RESPONSE_CHAR_UUID.rawValue), properties: characteristicsMap[CharacteristicIds.SUBMIT_RESPONSE_CHAR_UUID.rawValue]!.properties, value: characteristicsMap[CharacteristicIds.SUBMIT_RESPONSE_CHAR_UUID.rawValue]!.value, permissions: characteristicsMap[CharacteristicIds.SUBMIT_RESPONSE_CHAR_UUID.rawValue]!.permissions)

    let transferReportRequestChar = CBMutableCharacteristic(type: CBUUID(string: CharacteristicIds.TRANSFER_REPORT_REQUEST_CHAR_UUID.rawValue), properties: characteristicsMap[CharacteristicIds.TRANSFER_REPORT_REQUEST_CHAR_UUID.rawValue]!.properties, value: characteristicsMap[CharacteristicIds.TRANSFER_REPORT_REQUEST_CHAR_UUID.rawValue]!.value, permissions: characteristicsMap[CharacteristicIds.TRANSFER_REPORT_REQUEST_CHAR_UUID.rawValue]!.permissions)

    let transferReportResponseChar = CBMutableCharacteristic(type: CBUUID(string: CharacteristicIds.TRANSFER_REPORT_RESPONSE_CHAR_UUID.rawValue), properties: characteristicsMap[CharacteristicIds.TRANSFER_REPORT_RESPONSE_CHAR_UUID.rawValue]!.properties, value: characteristicsMap[CharacteristicIds.TRANSFER_REPORT_RESPONSE_CHAR_UUID.rawValue]!.value, permissions: characteristicsMap[CharacteristicIds.TRANSFER_REPORT_RESPONSE_CHAR_UUID.rawValue]!.permissions)

    let verificationStatusChar = CBMutableCharacteristic(type: CBUUID(string: CharacteristicIds.VERIFICATION_STATUS_CHAR_UUID.rawValue), properties: characteristicsMap[CharacteristicIds.VERIFICATION_STATUS_CHAR_UUID.rawValue]!.properties, value: characteristicsMap[CharacteristicIds.VERIFICATION_STATUS_CHAR_UUID.rawValue]!.value, permissions: characteristicsMap[CharacteristicIds.VERIFICATION_STATUS_CHAR_UUID.rawValue]!.permissions)
}

// TODO: Add conn status change everywhere

enum CharacteristicIds: String, CaseIterable {
    case IDENTIFY_REQUEST_CHAR_UUID = "00002030-0000-1000-8000-00805f9b34fb"
    case REQUEST_SIZE_CHAR_UUID = "00002031-0000-1000-8000-00805f9b34fb"
    case REQUEST_CHAR_UUID = "00002032-0000-1000-8000-00805f9b34fb"
    case RESPONSE_SIZE_CHAR_UUID = "00002033-0000-1000-8000-00805f9b34fb"
    case SUBMIT_RESPONSE_CHAR_UUID = "00002034-0000-1000-8000-00805f9b34fb"
    case TRANSFER_REPORT_REQUEST_CHAR_UUID = "00002035-0000-1000-8000-00805f9b34fb"
    case TRANSFER_REPORT_RESPONSE_CHAR_UUID = "00002036-0000-1000-8000-00805f9b34fb"
    case VERIFICATION_STATUS_CHAR_UUID = "00002037-0000-1000-8000-00805f9b34fb"
    case DISCONNECT_CHAR_UUID = "00002038-0000-1000-8000-00805f9b34fb"
}

let characteristicsMap: [String: CharacteristicTuple] = [
    "00002030-0000-1000-8000-00805f9b34fb": (properties: CBCharacteristicProperties([.writeWithoutResponse, .write]), permissions: CBAttributePermissions([.writeable]), value: nil),
    "00002031-0000-1000-8000-00805f9b34fb": (properties: CBCharacteristicProperties([.read, .indicate]), permissions: CBAttributePermissions([.readable]), value: nil),
    "00002032-0000-1000-8000-00805f9b34fb": (properties: CBCharacteristicProperties([.read, .indicate]), permissions: CBAttributePermissions([.readable]), value: nil),
    "00002033-0000-1000-8000-00805f9b34fb": (properties: CBCharacteristicProperties([.writeWithoutResponse, .write]), permissions: CBAttributePermissions([.writeable]), value: nil),
    "00002034-0000-1000-8000-00805f9b34fb": (properties: CBCharacteristicProperties([.writeWithoutResponse, .write]), permissions: CBAttributePermissions([.writeable]), value: nil),
    "00002035-0000-1000-8000-00805f9b34fb": (properties: CBCharacteristicProperties([.writeWithoutResponse, .write]), permissions: CBAttributePermissions([.writeable]), value: nil),
    "00002036-0000-1000-8000-00805f9b34fb": (properties: CBCharacteristicProperties([.indicate]), permissions: CBAttributePermissions([.readable]), value: nil),
    "00002037-0000-1000-8000-00805f9b34fb": (properties: CBCharacteristicProperties([.indicate]), permissions: CBAttributePermissions([.readable]), value: nil),
]

struct NetworkCharNums {
    static let IDENTIFY_REQUEST_CHAR_UUID = CBUUID(string: "2030")
    static let REQUEST_SIZE_CHAR_UUID = CBUUID(string: "2031")
    static let REQUEST_CHAR_UUID = CBUUID(string: "2032")
    static let RESPONSE_SIZE_CHAR_UUID = CBUUID(string: "2033")
    static let SUBMIT_RESPONSE_CHAR_UUID = CBUUID(string: "2034")
    static let TRANSFER_REPORT_REQUEST_CHAR_UUID = CBUUID(string: "2035")
    static let TRANSFER_REPORT_RESPONSE_CHAR_UUID = CBUUID(string: "2036")
    static let VERIFICATION_STATUS_CHAR_UUID = CBUUID(string: "2037")
    static let DISCONNECT_CHAR_UUID = CBUUID(string: "2038")
}

enum NotificationEvent: String {
    case EXCHANGE_RECEIVER_INFO = "EXCHANGE_RECEIVER_INFO"
    case CREATE_CONNECTION = "CREATE_CONNECTION"
    case RESPONSE_SIZE_WRITE_SUCCESS = "RESPONSE_SIZE_WRITE_SUCCESS"
    case HANDLE_TRANSMISSION_REPORT = "HANDLE_TRANSMISSION_REPORT"
    case INIT_RESPONSE_CHUNK_TRANSFER = "INIT_RESPONSE_CHUNK_TRANSFER"
    case VERIFICATION_STATUS_RESPONSE = "VERIFICATION_STATUS_RESPONSE"
    case DISCONNECT_STATUS_CHANGE = "DISCONNECT_STATUS_CHANGE"
    case ERROR = "ERROR"
}



