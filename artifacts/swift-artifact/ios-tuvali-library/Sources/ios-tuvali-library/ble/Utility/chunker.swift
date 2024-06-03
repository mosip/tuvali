import Foundation
import os
typealias ChunkSeqIndex = Int
typealias ChunkSeqNumber = Int

extension ChunkSeqIndex {
    func toSeqNumber() -> ChunkSeqNumber {
        return self + 1
    }
}

extension ChunkSeqNumber {
    func toSeqIndex() -> ChunkSeqIndex {
        return self - 1
    }
}

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
        for i: ChunkSeqIndex in 0..<totalChunkCount {
            preSlicedChunks.append(chunk(seqIndex: i))
        }
    }

    func getTotalChunkCount(dataSize: Int) -> Double {
        var totalChunkCount = Double(dataSize)/Double(effectivePayloadSize)
        return Double(ceil(totalChunkCount))
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
        let chunkIndex = chunksReadCounter
        chunksReadCounter += 1
       return preSlicedChunks[chunkIndex]
    }

    func chunkBySequenceNumber(missedSeqNumber: ChunkSeqNumber) -> Data {
        return (preSlicedChunks[missedSeqNumber.toSeqIndex()])
    }

    private func chunk(seqIndex: ChunkSeqIndex) -> Data {
        let fromIndex = seqIndex * effectivePayloadSize
        if isLastChunkSmallerSize(seqIndex: seqIndex) {
            let chunkLength = lastChunkByteCount + chunkMetaSize
            return frameChunk(seqNumber: seqIndex.toSeqNumber(), chunkLength: chunkLength, fromIndex: fromIndex, toIndex: fromIndex + lastChunkByteCount)
        } else {
            let toIndex = fromIndex + effectivePayloadSize
            return frameChunk(seqNumber: seqIndex.toSeqNumber(), chunkLength: mtuSize, fromIndex: fromIndex, toIndex: toIndex)
        }
    }

    private func isLastChunkSmallerSize(seqIndex: Int) -> Bool {
        return isLastChunkIndex(seqIndex: seqIndex) && lastChunkByteCount > 0
    }

    private func isLastChunkIndex(seqIndex: Int) -> Bool {
        return seqIndex == (totalChunkCount - 1)
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

    private func frameChunk(seqNumber: ChunkSeqNumber, chunkLength: Int, fromIndex: Int, toIndex: Int) -> Data {
        if let chunkData = chunkData {
            let payload = chunkData.subdata(in: fromIndex + chunkData.startIndex..<chunkData.startIndex + toIndex)
            let payloadCRC = CRC.evaluate(d: payload)
            return Util.intToNetworkOrderedByteArray(num: seqNumber, byteCount: Util.ByteCount.TwoBytes) + Util.intToNetworkOrderedByteArray(num: Int(payloadCRC), byteCount: Util.ByteCount.TwoBytes) + payload
        }
        return Data() //
    }

    func isComplete() -> Bool {
        let isComplete = chunksReadCounter >= totalChunkCount
        if isComplete {
            os_log(.info, "isComplete: true, totalChunks: %{public}d , chunkReadCounter(1-indexed): %{public}d", totalChunkCount, chunksReadCounter)
        }
       return isComplete
    }

}
