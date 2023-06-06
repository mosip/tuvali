import Foundation

protocol WalletProtocol: AnyObject {
    func startConnection(_ uri: String)
    func disconnect()
    func send(_ payload: String)
    func subscribe(_ listener: @escaping (Event) -> Void)
    func unsubscribe()
}
