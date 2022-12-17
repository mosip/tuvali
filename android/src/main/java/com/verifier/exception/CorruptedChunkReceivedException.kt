package com.verifier.exception

class CorruptedChunkReceivedException(val size: Int, receivedSeqNumber: Int, receivedMtuSize: Int) : Throwable() {}
