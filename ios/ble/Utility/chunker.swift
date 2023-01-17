

import Foundation

class Chunker {
    /*
     (private val data: ByteArray, private val mtuSize: Int = DEFAULT_CHUNK_SIZE) :
     ChunkerBase(mtuSize)
     **/
    
    private var logTag = "Chunker"
    private var chunksReadCounter: Int = 0
    private var preSlicedChunks: [Data]?
    private var chunkData: Data?
    private var mtuSize: Int = BLEConstants.DEFAULT_CHUNK_SIZE
    private var chunkMetaSize = BLEConstants.seqNumberReservedByteSize + BLEConstants.mtuReservedByteSize
    
    
    //  init {
    //    val startTime = System.currentTimeMillis()
    //    for (idx in 0 until totalChunkCount) {
    //      preSlicedChunks[idx] = chunk(idx)
    //    }
    //    Log.d(logTag, "Chunks pre-populated in ${System.currentTimeMillis() - startTime} ms time")
    //  }
    
    init(chunkData: Data, mtuSize: Int?) {
        self.chunkData = chunkData
        self.mtuSize = mtuSize!
    }
    
    func getLastChunkByteCount(dataSize: Int) -> Int {
        return dataSize % effectivePayloadSize
    }
    
    func getTotalChunkCount(dataSize: Int) -> Double {
        var resulydouble = Double(dataSize)/Double(effectivePayloadSize)
        return ceill(resulydouble)
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
        var seqNumber = chunksReadCounter
        chunksReadCounter += 1
        return (preSlicedChunks?[seqNumber])!
    }
    
    func chunkBySequenceNumber(num: Int) -> Data {
        return (preSlicedChunks?[num])!
    }
    
    private func chunk(seqNumber: Int) -> Data {
        var fromIndex = seqNumber * effectivePayloadSize
        if (seqNumber == (totalChunkCount - 1) && lastChunkByteCount > 0) {
            print( "fetching last chunk")
            var chunkLength = lastChunkByteCount + chunkMetaSize
            return frameChunk(seqNumber: seqNumber, chunkLength: chunkLength, fromIndex: fromIndex, toIndex: fromIndex + lastChunkByteCount)
        } else {
            var toIndex = (seqNumber + 1) * effectivePayloadSize
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
        print("fetching chunk size: ${toIndex - fromIndex}, chunkSequenceNumber(0-indexed): $seqNumber"
        )
        return intToTwoBytesBigEndian(num: seqNumber) + intToTwoBytesBigEndian(num: chunkLength) + chunkData!.copyOfRange(
            fromIndex,
            toIndex
        )
    }
    
    func isComplete() -> Bool {
        var isComplete = chunksReadCounter > (totalChunkCount - 1)
        if (isComplete) {
                print("isComplete: true, totalChunks: $totalChunkCount , chunkReadCounter(1-indexed): $chunksReadCounter"
            )
        }
        return isComplete
    }
    
    func intToTwoBytesBigEndian(num: Int) -> Data {
        if (num < 256) {
          var minValue = 0
            var result = num - minValue
            return Data(result)
        }
        var result1 = num/256 - (num%256)
        return Data(result1)
      }
}
