package io.mosip.tuvali.transfer

class TransmissionReport  {
  val type: ReportType
  private val totalPages: Int
  val missingSequences: IntArray?

  public enum class ReportType {
    MISSING_CHUNKS,
    SUCCESS
  }

  constructor(type: ReportType, totalPages: Int, missingSequences: IntArray?) {
    this.type = type
    this.totalPages = totalPages
    this.missingSequences = missingSequences
  }

  constructor(bytes: ByteArray){
    this.type = ReportType.values()[bytes[0].toInt()]
    this.totalPages = Util.twoBytesToIntBigEndian(byteArrayOf(bytes[1], bytes[2]))
    this.missingSequences = bytes.drop(3).chunked(2)
                            .fold(intArrayOf())
                            { acc, twoBytes -> acc + Util.twoBytesToIntBigEndian(twoBytes.toByteArray())}
  }

  fun toByteArray(): ByteArray {
    val missingSeqBytes = missingSequences?.fold(byteArrayOf()) { acc, sNo -> acc + Util.intToTwoBytesBigEndian(sNo) }
    val metadata = byteArrayOf(type.ordinal.toByte(), totalPages.toByte())

    return if(missingSeqBytes != null) {
      metadata + missingSeqBytes
    } else {
      metadata
    }
  }
}
