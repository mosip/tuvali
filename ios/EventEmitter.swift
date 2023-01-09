import Foundation

class EventEmitter {
  
  public static var sharedInstance = EventEmitter()
  
  static var eventEmitter: Openid4vpBle!
  
  private  init() {}
  
  func registerEventEmitter(eventEmitter: Openid4vpBle) {
    EventEmitter.eventEmitter = eventEmitter
  }
  
  func dispatch(name: String, body: Any?) {
    EventEmitter.eventEmitter.sendEvent(withName: name, body: body)
  }
  
  lazy var allEvents: [String] = {
    return [
      "EVENT_NEARBY",
      "EVENT_LOG",
    ]
  }()
}

