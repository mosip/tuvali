package io.mosip.tuvali.transfer

import android.util.Log
import io.mosip.tuvali.transfer.ByteCount.TwoBytes
import io.mosip.tuvali.transfer.Util.Companion.networkOrderedByteArrayToInt
import kotlin.math.ceil
import kotlin.math.min

private const val PAGE_NUMBER_SIZE_IN_BYTES = 2
private const val CHUNK_SEQUENCE_NUMBER_IN_BYTES = 2
private const val TYPE_SIZE_IN_BYTES = 1
class TransferReport  {
  val type: ReportType
  private val totalPages: Int
  val missingSequences: IntArray?
  private val logTag = Util.getLogTag(javaClass.simpleName)

  //TODO: give static number to respective types below
  enum class ReportType {
    MISSING_CHUNKS,
    SUCCESS
  }

  constructor(type: ReportType, missingSequences: IntArray, maxDataBytes: Int) {
    val transferReportPageSize: Int = (maxDataBytes - PAGE_NUMBER_SIZE_IN_BYTES - TYPE_SIZE_IN_BYTES) / CHUNK_SEQUENCE_NUMBER_IN_BYTES
    val missedSequenceNumberCount = missingSequences.size
    this.totalPages = ceil(missedSequenceNumberCount.toDouble() / transferReportPageSize).toInt()
    this.type = type
    this.missingSequences = missingSequences.sliceArray(0 until min(transferReportPageSize, missedSequenceNumberCount))
    Log.i(logTag, "Missed Sequence Number Count: $missedSequenceNumberCount, Total Pages: $totalPages, Transfer Report Page Size: $transferReportPageSize")

  }

  constructor(bytes: ByteArray){
    this.type = ReportType.values()[bytes[0].toInt()]
    this.totalPages = networkOrderedByteArrayToInt(byteArrayOf(bytes[1], bytes[2]), TwoBytes)
    val sequenceNumberByteArray = bytes.drop(PAGE_NUMBER_SIZE_IN_BYTES + TYPE_SIZE_IN_BYTES)
    this.missingSequences = sequenceNumberByteArray.chunked(2)
      .fold(intArrayOf())
      { acc, twoBytes ->
        if (twoBytes.size != 2) {
          acc
        } else {
          acc + networkOrderedByteArrayToInt(twoBytes.toByteArray(),TwoBytes)
        }
      }
  }

  /*
  +---------+------------------+---------------------+-------------------+-------------------+-------------------+
  |         |                  |                     |                   |                   |                   |
  | type    |   total pages    |    missed seq no.0  | missed seq no. 1  |  missed seq no.2  |      . . . . .    |
  |(1 byte) |    (2 bytes)     |       (2 bytes)     |    (2 bytes)      |     (2 bytes)     |                   |
  +---------+------------------+---------------------+-------------------+-------------------+-------------------+
  */
  fun toByteArray(): ByteArray {
    val missingSeqBytes = missingSequences?.fold(byteArrayOf()) { acc, sNo -> acc + Util.intToNetworkOrderedByteArray(sNo, TwoBytes) }
    val metadata = byteArrayOf(type.ordinal.toByte()) + Util.intToNetworkOrderedByteArray(totalPages, TwoBytes)

    return if(missingSeqBytes != null) {
      metadata + missingSeqBytes
    } else {
      metadata
    }
  }
}
