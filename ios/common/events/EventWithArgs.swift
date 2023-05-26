import Foundation

protocol EventWithArgs: Event {
  func getData() -> [String: String]
}
