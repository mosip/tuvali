package io.mosip.tuvali.retrymechanism.lib

import kotlin.math.min
import kotlin.math.pow

const val DEFAULT_MAX_RETRY_LIMIT: Int = 10
const val DEFAULT_MAX_EXPONENT: Int = 5
const val DEFAULT_MAX_ELAPSE_TIME: Long = 100
const val DEFAULT_INITIAL_WAIT_TIME: Long = 2
const val DEFAULT_MULTIPLIER: Int = 2

class BackOffStrategy(
  private var maxRetryLimit: Int = DEFAULT_MAX_RETRY_LIMIT,
  private var maxElapsedTime: Long = DEFAULT_MAX_ELAPSE_TIME,
  private var maxExponent: Int = DEFAULT_MAX_EXPONENT,
  private var initialWaitTime: Long = DEFAULT_INITIAL_WAIT_TIME,
  private var multiplier: Int = DEFAULT_MULTIPLIER,
) {

    var retryCount = 0
    private var waitTime: Long = 0
    private var timeDiff: Long = 0
    private var startTime: Long = 0L

    fun shouldRetry(): Boolean {
        return retryCount == 0 || (retryCount <= maxRetryLimit && didNotExceedTimeLimit())
    }

    private fun didNotExceedTimeLimit(): Boolean {
        val currentTime: Long = System.currentTimeMillis()
        timeDiff = (currentTime - startTime)
        return (timeDiff <= maxElapsedTime)
    }

    fun getWaitTime(): Long {
        if (retryCount == 0) {
            startTime = System.currentTimeMillis()
        }
        waitTime = calculateWaitTime(retryCount).toLong()
        retryCount++
        return waitTime
    }

    private fun calculateWaitTime(retryCount: Int): Double {
        return initialWaitTime * multiplier.toDouble().pow(min(retryCount, maxExponent).toDouble())
    }

    fun reset() {
        multiplier = 0
        retryCount = 0
        startTime = 0L
    }
}





