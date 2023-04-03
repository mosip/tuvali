import Foundation

class Chunker {

    private var logTag = "Chunker"
    private var chunksReadCounter: Int = 0
    private var preSlicedChunks: [Data] = []
    private var chunkData: Data?
    private var mtuSize: Int = BLEConstants.DEFAULT_CHUNK_SIZE
    private var chunkMetaSize = BLEConstants.seqNumberReservedByteSize + BLEConstants.mtuReservedByteSize

    init(chunkData: Data, mtuSize: Int?) {
        self.chunkData = chunkData
        self.mtuSize = mtuSize!
        assignPreSlicedChunks()
    }

    func getLastChunkByteCount(dataSize: Int) -> Int {
        return dataSize % effectivePayloadSize
    }

    func assignPreSlicedChunks(){
        os_log(.info, "expected total data size: %{public}d and totalChunkCount: %{public}d ", (chunkData?.count)!, totalChunkCount)
        for i in 0..<totalChunkCount {
            preSlicedChunks.append(chunk(seqIndex: i))
        }
    }

    func getTotalChunkCount(dataSize: Int) -> Double {
        var totalChunkCount = Double(dataSize)/Double(effectivePayloadSize)
        return Double(ceill(totalChunkCount))
    }

    var lastChunkByteCount: Int {
        return getLastChunkByteCount(dataSize: chunkData!.count)
    }

    var totalChunkCount: Int {
        return Int(getTotalChunkCount(dataSize: chunkData!.count))
    }

    var effectivePayloadSize: Int {
       return mtuSize - chunkMetaSize
    }

    func next() -> Data {
        var seqIndex = chunksReadCounter
        chunksReadCounter += 1
        if seqIndex <= totalChunkCount - 1 {
            return (preSlicedChunks[seqIndex])
        }
       else
        {
           return Data()
       }
    }

    func chunkBySequenceNumber(missedSeqNumber: Int) -> Data {
        let missedSeqIndex = missedSeqNumber - 1
        return (preSlicedChunks[missedSeqIndex])
    }

    private func chunk(seqIndex: Int) -> Data {
        let fromIndex = seqIndex * effectivePayloadSize
        let seqNumber = seqIndex + 1
        if (seqIndex == (totalChunkCount - 1) && lastChunkByteCount > 0) {
            let chunkLength = lastChunkByteCount + chunkMetaSize
            return frameChunk(seqNumber: seqNumber, chunkLength: chunkLength, fromIndex: fromIndex, toIndex: fromIndex + lastChunkByteCount)
        } else {
            let toIndex = (seqIndex + 1) * effectivePayloadSize
            return frameChunk(seqNumber: seqNumber, chunkLength: mtuSize, fromIndex: fromIndex, toIndex: toIndex)
        }
    }

    /*
     <------------------------------------------------------- MTU ------------------------------------------------------------------->
     +-----------------------+-----------------------------+-------------------------------------------------------------------------+
     |                       |                             |                                                                         |
     |  chunk sequence no    |     total chunk length      |         chunk payload                                                   |
     |      (2 bytes)        |         (2 bytes)           |       (upto MTU-4 bytes)                                                |
     |                       |                             |                                                                         |
     +-----------------------+-----------------------------+-------------------------------------------------------------------------+
     */

    private func frameChunk(seqNumber: Int, chunkLength: Int, fromIndex: Int, toIndex: Int) -> Data {
        if let chunkData = chunkData {
            let payload = chunkData.subdata(in: fromIndex + chunkData.startIndex..<chunkData.startIndex + toIndex)
            let payloadCRC = CRC.evaluate(d: payload)
            return intToBytes(UInt16(seqNumber)) + intToBytes(payloadCRC) + payload
        }
        return Data() //
    }

    func isComplete() -> Bool {
        let isComplete = chunksReadCounter > (totalChunkCount - 1)
        if isComplete {
            os_log(.info, "isComplete: true, totalChunks: %{public}d , chunkReadCounter(1-indexed): %{public}d", totalChunkCount, chunksReadCounter)
        }
       return isComplete
    }

    func intToBytes(_ value: UInt16) -> Data {
        var value = value.bigEndian
        return Data(bytes: &value, count: MemoryLayout<UInt16>.size)
    }
}
