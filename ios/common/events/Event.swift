import Foundation

protocol Event {
    var type: String { get }
    func getData() -> [String: String]
}
