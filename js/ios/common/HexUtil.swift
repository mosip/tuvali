import Foundation

func hexStringToData(string: String) -> Data {
    let stringArray = Array(string)
    var data: Data = Data()
    for i in stride(from: 0, to: string.count, by: 2) {
        let pair: String = String(stringArray[i]) + String(stringArray[i+1])
        if let byteNum = UInt8(pair, radix: 16) {
            let byte = Data([byteNum])
            data.append(byte)
        } else {
            fatalError()
        }
    }
    return data
}

func stringToJson(jsonText: String) -> NSDictionary {
    var dictonary: NSDictionary?
    if let data = jsonText.data(using: String.Encoding.utf8) {
        do {
            dictonary = try JSONSerialization.jsonObject(with: data, options: []) as? [String:AnyObject] as NSDictionary?
        } catch let error as NSError {
            os_log(.error, " %{public}@ ", error)
        }
    }
    return dictonary!
}
