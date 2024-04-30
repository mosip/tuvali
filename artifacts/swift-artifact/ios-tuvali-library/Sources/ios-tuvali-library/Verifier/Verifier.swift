import Foundation

@objc(Verifier)
class Verifier: NSObject {
    
    func getModuleName(completion: @escaping ([String]) -> Void) {
            DispatchQueue.global(qos: .userInitiated).async {
                completion(["iOS Verifier"])
            }
        }
        
    
    @objc
    static func requiresMainQueueSetup() -> Bool {
        return true
    }
    
    @objc
    func constantsToExport() -> [AnyHashable: Any] {
        return [
            "name": "verifier",
            "platform": "ios"
        ]
    }
}
