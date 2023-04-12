import Foundation

/*
 +---------+------------------+---------------------+-------------------+-------------------+-------------------+
 |         |                  |                     |                   |                   |                   |
 | type    |   total pages    |    missed seq no.0  | missed seq no. 1  |  missed seq no.2  |      . . . . .    |
 |(1 byte) |    (2 bytes)     |       (2 bytes)     |    (2 bytes)      |     (2 bytes)     |                   |
 +---------+------------------+---------------------+-------------------+-------------------+-------------------+
 */
class TransferReport  {
    var type: ReportType
    var totalPages: Int
    var missingSequences: [Int]?
    
    enum ReportType: Int {
        case MISSING_CHUNKS = 0, SUCCESS
    }
    
    init(type: ReportType, totalPages: Int, missingSequences: [Int]?) {
        self.type = type
        self.totalPages = totalPages
        self.missingSequences = missingSequences
    }
    
    init(bytes: Data) {
        if bytes.count >= 3 {
            type = ReportType(rawValue: Int(bytes[0]))!
            totalPages = Util.networkOrderedByteArrayToInt(num: bytes.subdata(in: (bytes.startIndex+1..<bytes.startIndex+3)))
            var missingChunksData = bytes.dropFirst(3)
            if missingChunksData.count > 0 {
                missingSequences = []
                while missingChunksData.count >= 2 {
                    let missingChunkSlice = missingChunksData.subdata(in: missingChunksData.startIndex..<missingChunksData.startIndex+2)
                    missingSequences?.append(Util.networkOrderedByteArrayToInt(num: missingChunkSlice))
                    missingChunksData = missingChunksData.dropFirst(2)
                }
                //os_log(.debug,"%{public}@", missingSequences)
            }
        } else {
            type = .MISSING_CHUNKS
            totalPages = 0
        }
    }
}

