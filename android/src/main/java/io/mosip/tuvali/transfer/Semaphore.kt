package io.mosip.tuvali.transfer

class Semaphore {
  enum class SemaphoreMarker {
    UnInitialised,
    ProcessChunkPending,
    ProcessChunkComplete,
    FailedToRead,
    ResendChunk,
    Error
  }
}
