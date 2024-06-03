import Foundation
import CoreBluetooth
import os
@available(iOS 13.0, *)
extension Central: CBPeripheralDelegate {
    func peripheral(_ peripheral: CBPeripheral, didDiscoverServices error: Error?) {
        if let error = error {
            retryServicesDiscovery(peripheral)
            return
        }

        guard let peripheralServices = peripheral.services else {
            retryServicesDiscovery(peripheral)
            return
        }

        let serviceUUIDS = peripheralServices.map({ service in
            return service.uuid
        })

        if !serviceUUIDS.contains(Peripheral.SERVICE_UUID) {
            retryServicesDiscovery(peripheral)
            return
        }

        for service in peripheralServices where Peripheral.SERVICE_UUID == service.uuid {
            peripheral.discoverCharacteristics(CharacteristicIds.allCases.map{CBUUID(string: $0.rawValue)}, for: service)
        }

        os_log(.info, "found %{public}@ services for peripheral %{public}@",String(describing: peripheral.services?.count),String(describing: peripheral.name))
    }

    func peripheral(_ peripheral: CBPeripheral, didDiscoverCharacteristicsFor service: CBService, error: Error?) {
        if let error = error {
            retryCharacteristicsDiscovery(peripheral,service)
            return
        }
        guard let serviceCharacteristics = service.characteristics else {
            retryCharacteristicsDiscovery(peripheral,service)
            return
        }

        let mtu = peripheral.maximumWriteValueLength(for: .withoutResponse);

        if mtu < 64 {
            ErrorHandler.sharedInstance.handleException(type: .walletException, error: .invalidMTUSizeError(mtu: mtu))
            return
        }

        for characteristic in serviceCharacteristics {
            self.cbCharacteristics[characteristic.uuid.uuidString] = characteristic
            if characteristic.uuid == NetworkCharNums.TRANSFER_REPORT_RESPONSE_CHAR_UUID ||
                characteristic.uuid == NetworkCharNums.VERIFICATION_STATUS_CHAR_UUID || characteristic.uuid == NetworkCharNums.DISCONNECT_CHAR_UUID
            {
                peripheral.setNotifyValue(true, for: characteristic)
            }
        }
        walletBleCommunicatorDelegate?.createConnectionHandler()
    }


    func retryServicesDiscovery(_ peripheral : CBPeripheral){
        if retryStrategy.shouldRetry() {
            let waitTime = retryStrategy.getWaitTime()
            os_log(.error, "Error while discovering services retrying again after %{public}d time", waitTime)
            DispatchQueue.main.asyncAfter(deadline: .now() + .milliseconds(Int(waitTime))) {
                peripheral.discoverServices([Peripheral.SERVICE_UUID])
            }
        }
        else {
            os_log(.error, "Error while discovering services after retrying multiple times")
            retryStrategy.reset()
            return
        }
    }

    func retryCharacteristicsDiscovery(_ peripheral : CBPeripheral, _ service : CBService){
        if retryStrategy.shouldRetry() {
            let waitTime = retryStrategy.getWaitTime()
            os_log(.error, "Error while discovering services retrying again after %{public}d time", waitTime)
            DispatchQueue.main.asyncAfter(deadline: .now() + .milliseconds(Int(waitTime))) {
                peripheral.discoverCharacteristics(CharacteristicIds.allCases.map{CBUUID(string: $0.rawValue)}, for: service)
            }
        }
        else {
            os_log(.error, "Error while discovering services after retrying multiple times")
            retryStrategy.reset()
            return
        }
    }

    func retryTransferReportRequest(){
        if transferReportRequestRetryStrategy.shouldRetry() {
            let waitTime = transferReportRequestRetryStrategy.getWaitTime()
            os_log(.error, "Error while requesting transfer report retrying again after  %{public}d time", waitTime)
            DispatchQueue.main.asyncAfter(deadline: .now() + .milliseconds(Int(waitTime))) {
                self.delegate?.onFailedToSendTransferReportRequest()
            }
        } else {
            os_log(.error, "Failed to request transfer report even after multiple retries")
            transferReportRequestRetryStrategy.reset()
            return
        }
    }

    func peripheral(_ peripheral: CBPeripheral, didUpdateValueFor characteristic: CBCharacteristic, error: Error?) {
        os_log(.info, "Central was able to update value for the characteristic: %{public}s", characteristic.uuid.uuidString)
        if let error = error {
            os_log(.info, "Unable to recieve updates from device: %{public}@", error.localizedDescription)
            return
        }
        if characteristic.uuid == NetworkCharNums.TRANSFER_REPORT_RESPONSE_CHAR_UUID {
            let report = characteristic.value as Data?
            delegate?.onTransmissionReportRequest(data: report)
        } else if characteristic.uuid == NetworkCharNums.VERIFICATION_STATUS_CHAR_UUID {
            let verificationStatus = characteristic.value as Data?
            delegate?.onVerificationStatusChange(data: verificationStatus)
        } else if characteristic.uuid == NetworkCharNums.DISCONNECT_CHAR_UUID {
            let disconnectStatus = characteristic.value as Data?
            walletBleCommunicatorDelegate?.onDisconnectStatusChange(data: disconnectStatus)
            peripheral.setNotifyValue(false, for: characteristic)
        }
    }

    func peripheral(_ peripheral: CBPeripheral, didWriteValueFor characteristic: CBCharacteristic, error: Error?) {
        if let error = error {
            os_log(.error, "Unable to write to characteristic: %{public}@", error.localizedDescription)
            if characteristic.uuid == NetworkCharNums.TRANSFER_REPORT_REQUEST_CHAR_UUID {
                retryTransferReportRequest()
            }
            return
        }
        if characteristic.uuid == NetworkCharNums.IDENTIFY_REQUEST_CHAR_UUID {
            walletBleCommunicatorDelegate?.onIdentifyWriteSuccess()
        } else if characteristic.uuid == NetworkCharNums.RESPONSE_SIZE_CHAR_UUID {
            delegate?.onResponseSizeWriteSuccess()
        }
    }

    func peripheral(_ peripheral: CBPeripheral, didModifyServices invalidatedServices: [CBService]) {
    }
}
