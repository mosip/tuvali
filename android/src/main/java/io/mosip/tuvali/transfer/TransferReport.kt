package io.mosip.tuvali.transfer

import android.util.Log
import kotlin.math.ceil
import kotlin.math.min

private const val PAGE_NUMBER_SIZE_IN_BYTES = 2
private const val CHUNK_SEQUENCE_NUMBER_IN_BYTES = 2
private const val TYPE_SIZE_IN_BYTES = 1
class TransferReport  {
  val type: ReportType
  private val totalPages: Int
  val missingSequences: IntArray?

  //TODO: give static number to respective types below
  enum class ReportType {
    MISSING_CHUNKS,
    SUCCESS
  }

  constructor(type: ReportType, missingSequences: IntArray, maxDataBytes: Int) {
    val transferReportPageSize: Int = (maxDataBytes - PAGE_NUMBER_SIZE_IN_BYTES - TYPE_SIZE_IN_BYTES) / CHUNK_SEQUENCE_NUMBER_IN_BYTES
    val missedCount = missingSequences.size
    this.totalPages = ceil(missedCount.toDouble() / transferReportPageSize).toInt()
    this.type = type
    this.missingSequences = missingSequences.sliceArray(0 until min(transferReportPageSize, missedCount))
    Log.i("TransferReport", "Missed Chunks Count: $missedCount, Total Pages: $totalPages, Transfer Report Page Size: $transferReportPageSize")

  }

  constructor(bytes: ByteArray){
    this.type = ReportType.values()[bytes[0].toInt()]
    this.totalPages = Util.twoBytesToIntBigEndian(byteArrayOf(bytes[1], bytes[2]))
    val sequenceNumberByteArray = bytes.drop(PAGE_NUMBER_SIZE_IN_BYTES + TYPE_SIZE_IN_BYTES)
    this.missingSequences = sequenceNumberByteArray.chunked(2)
      .fold(intArrayOf())
      { acc, twoBytes ->
        if (twoBytes.size != 2) {
          acc
        } else {
          acc + Util.twoBytesToIntBigEndian(twoBytes.toByteArray())
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
    val missingSeqBytes = missingSequences?.fold(byteArrayOf()) { acc, sNo -> acc + Util.intToTwoBytesBigEndian(sNo) }
    val metadata = byteArrayOf(type.ordinal.toByte()) + Util.intToTwoBytesBigEndian(totalPages)

    return if(missingSeqBytes != null) {
      metadata + missingSeqBytes
    } else {
      metadata
    }
  }
}
