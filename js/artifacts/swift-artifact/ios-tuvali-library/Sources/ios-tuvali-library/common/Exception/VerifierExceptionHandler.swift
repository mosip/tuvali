import Foundation

enum VerifierErrorEnum: Error {
    case corruptedChunkReceived
    case tooManyFailureChunks
    case unsupportedMTUSizeException
}

extension VerifierErrorEnum: CustomStringConvertible {
    public var description: String {
        switch self {
        case .corruptedChunkReceived:
            return "Received corrupted chunks from the wallet"
        case .tooManyFailureChunks:
            return "Failing VC transfer as failure chunks are more than 70% of total chunks"
        case .unsupportedMTUSizeException:
            return "Minimum 512 MTU is required for VC transfer"
        }
    }
    
    public var code: String {
           switch self {
           case .corruptedChunkReceived:
               return "TVV_TRA_001"
           case .tooManyFailureChunks:
               return "TVV_TRA_002"
           case .unsupportedMTUSizeException:
               return "TVV_CON_001"
           }
       }
}
