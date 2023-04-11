import Foundation

enum verifierErrorEnum: Error {
    case CorruptedChunkReceived
    case TooManyFailureChunks
    case UnsupportedMTUSizeException
}

extension verifierErrorEnum: CustomStringConvertible {
    public var description: String {
        switch self {
        case .CorruptedChunkReceived:
            return "Received corrupted chunks from the wallet"
        case .TooManyFailureChunks:
            return "Failing VC transfer as failure chunks are more than 70% of total chunks"
        case .UnsupportedMTUSizeException:
            return "Minimum 512 MTU is required for VC transfer"
        }
    }
}
