

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
        print("preSlicedChunks called ::: ")
        for i in 0..<totalChunkCount {
            print(i)
            preSlicedChunks.append(chunk(seqNumber: i))
            print(preSlicedChunks ?? [])
        }
    }
    
    func getTotalChunkCount(dataSize: Int) -> Double {
        var resulydouble = Double(dataSize)/Double(effectivePayloadSize)
        return Double(ceill(resulydouble))
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
        if seqNumber <= totalChunkCount - 1 {
            return (preSlicedChunks[seqNumber])
        }
       else
        {
           return Data()
       }
    }
    
    func chunkBySequenceNumber(num: Int) -> Data {
        return (preSlicedChunks[num])
    }
    
    private func chunk(seqNumber: Int) -> Data {
        let fromIndex = seqNumber * effectivePayloadSize
        if (seqNumber == (totalChunkCount - 1) && lastChunkByteCount > 0) {
            print( "fetching last chunk")
            let chunkLength = lastChunkByteCount + chunkMetaSize
            return frameChunk(seqNumber: seqNumber, chunkLength: chunkLength, fromIndex: fromIndex, toIndex: fromIndex + lastChunkByteCount)
        } else {
            let toIndex = (seqNumber + 1) * effectivePayloadSize
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
        print("fetching chunk size:",toIndex,"-", fromIndex,"}, chunkSequenceNumber(0-indexed):", seqNumber)
//        return intToTwoBytesBigEndian(num: seqNumber) + intToTwoBytesBigEndian(num: chunkLength) + chunkData!.subdata(in: fromIndex..<toIndex)
        if let chunkData = chunkData {
            return intToBytes(UInt16(seqNumber)) + intToBytes(UInt16(chunkLength)) + chunkData.subdata(in: fromIndex + chunkData.startIndex..<chunkData.startIndex + toIndex)
        }
        return Data() //
    }
    
    func isComplete() -> Bool {
        let isComplete = chunksReadCounter > (totalChunkCount - 1)
        if isComplete {
                print("isComplete: true, totalChunks: $totalChunkCount , chunkReadCounter(1-indexed): $chunksReadCounter")
        }
       return isComplete
    }
    
//    func intToTwoBytesBigEndian(num: Int) -> [UInt8] {
//        if num < 256 {
//            let minValue: UInt8 = 0
//            return [minValue, UInt8(num)]
//        }
//        return [UInt8(num/256), UInt8(num%256)]
//    }
    
    func intToBytes(_ value: UInt16) -> Data {
        var value = value.bigEndian
        return Data(bytes: &value, count: MemoryLayout<UInt16>.size)
    }
}

