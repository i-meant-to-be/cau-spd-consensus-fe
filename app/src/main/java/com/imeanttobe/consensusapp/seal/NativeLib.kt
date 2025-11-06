package com.imeanttobe.consensusapp.seal

object NativeLib {
    init {
        System.loadLibrary("consensus_native")
    }

    external fun stringFromJNI(): String
}
