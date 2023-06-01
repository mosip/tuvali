import Foundation

class OpenId4vpURI {
    var URI_IDENTIFIER = "OPENID4VP"
    let uri: String
    
    init(uri: String) {
        self.uri = uri
    }
    
    func isValid() -> Bool {
        return self.uri.range(of: URI_IDENTIFIER + "://") != nil
    }
    
    func extractPayload() -> String {
        uri.components(separatedBy: "OPENID4VP://")[1]
    }
}
