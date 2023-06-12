import Foundation

let DEFAULT_MAX_EXPONENT: Int = 5
let DEFAULT_MAX_ELAPSE_TIME: Int = 100
let DEFAULT_INITIAL_WAIT_TIME: Double = 2
let DEFAULT_MULTIPLIER:Int = 2

class BackOffStrategy {
    var MAX_RETRY_LIMIT : Int = 10
    
    var retryCount = 0
    private var waitTime: Int = 0
    private var startTime: Double = 0
    
    init(MAX_RETRY_LIMIT: Int){
        self.MAX_RETRY_LIMIT = MAX_RETRY_LIMIT
    }
    
    func shouldRetry() -> Bool {
        return retryCount == 0 || (retryCount <= MAX_RETRY_LIMIT && didNotExceedTimeLimit())
    }
    
    private func didNotExceedTimeLimit() -> Bool {
        let currentTime: Double = Date().timeIntervalSinceReferenceDate
        let timeDiffInMs = Int((currentTime - startTime) * 1_000)
        return (timeDiffInMs <= DEFAULT_MAX_ELAPSE_TIME)
    }
     
    func getWaitTime() -> Int {
        if (retryCount == 0) {
            startTime = Date().timeIntervalSinceReferenceDate
        }
        waitTime = calculateWaitTime(retryCount)
        retryCount += 1
        return waitTime
     }
     
     private func calculateWaitTime(_ retryCount: Int) -> Int {
         return Int(DEFAULT_INITIAL_WAIT_TIME * (pow(Double(DEFAULT_MULTIPLIER), Double(min(retryCount, DEFAULT_MAX_EXPONENT)))))
     }
     
     func reset() {
         retryCount = 0
         startTime = 0
     }
}
