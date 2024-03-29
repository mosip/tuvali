import Foundation

@available(iOS 13.0, *)

class VersionModule: RCTEventEmitter {
    var tuvaliVersion: String = "unknown"
    
    @objc func setTuvaliVersion(_ version: String) -> String{
        tuvaliVersion = version
        os_log("Tuvali version - %{public}@",tuvaliVersion);
        return tuvaliVersion
    }
    
    @objc override func supportedEvents() -> [String]! {
        return []
    }

}
