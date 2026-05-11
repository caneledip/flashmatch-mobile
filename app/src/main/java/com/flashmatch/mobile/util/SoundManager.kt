package com.flashmatch.mobile.util

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import com.flashmatch.mobile.R

class SoundManager(context: Context? = null) {

    private val pool: SoundPool? = context?.let {
        SoundPool.Builder()
            .setMaxStreams(4)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            .build()
    }

    private val sFlip     = pool?.load(context, R.raw.flip,     1)
    private val sCorrect  = pool?.load(context, R.raw.correct,  1)
    private val sWrong    = pool?.load(context, R.raw.wrong,    1)
    private val sComplete = pool?.load(context, R.raw.complete, 1)

    fun playFlip()     { sFlip?.let     { pool?.play(it, 1f, 1f, 1, 0, 1f) } }
    fun playCorrect()  { sCorrect?.let  { pool?.play(it, 1f, 1f, 1, 0, 1f) } }
    fun playWrong()    { sWrong?.let    { pool?.play(it, 1f, 1f, 1, 0, 1f) } }
    fun playComplete() { sComplete?.let { pool?.play(it, 1f, 1f, 1, 0, 1f) } }
    fun release()      { pool?.release() }
}
