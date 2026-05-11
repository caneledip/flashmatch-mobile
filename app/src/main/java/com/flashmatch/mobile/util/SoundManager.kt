package com.flashmatch.mobile.util

import android.media.AudioManager
import android.media.ToneGenerator

class SoundManager {
    private val toneGen: ToneGenerator? = try {
        ToneGenerator(AudioManager.STREAM_MUSIC, 80)
    } catch (_: RuntimeException) { null }

    fun playFlip()     { toneGen?.startTone(ToneGenerator.TONE_PROP_BEEP,  80) }
    fun playCorrect()  { toneGen?.startTone(ToneGenerator.TONE_PROP_ACK,  150) }
    fun playWrong()    { toneGen?.startTone(ToneGenerator.TONE_PROP_NACK, 150) }
    fun playComplete() { toneGen?.startTone(ToneGenerator.TONE_SUP_DIAL,  400) }
    fun release()      { toneGen?.release() }
}
