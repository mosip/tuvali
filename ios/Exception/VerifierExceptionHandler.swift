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
}
