package io.mosip.tuvali.transfer

class TransferReport  {
  val type: ReportType
  private val totalPages: Int
  val missingSequences: IntArray?

  //TODO: give static number to respective types below
  enum class ReportType {
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
    val sequenceNumberByteArray = bytes.drop(3)
    if (sequenceNumberByteArray.size % 2 != 0) {
      sequenceNumberByteArray.dropLast(1)
    }
    this.missingSequences = sequenceNumberByteArray.chunked(2)
                            .fold(intArrayOf())
                            { acc, twoBytes -> acc + Util.twoBytesToIntBigEndian(twoBytes.toByteArray())}
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
