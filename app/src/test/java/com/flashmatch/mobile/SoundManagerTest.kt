package com.flashmatch.mobile

import com.flashmatch.mobile.util.SoundManager
import org.junit.Test

/**
 * Feature 2 (sound effects) — SoundManager smoke tests.
 *
 * ToneGenerator is an Android SDK class whose methods are not available in the JVM
 * unit-test environment. We inject null via the internal constructor so every
 * play/release call is a safe no-op, and verify none of the public methods crash.
 */
class SoundManagerTest {

    private fun sm() = SoundManager(context = null)

    @Test fun instantiation_doesNotThrow() { sm() }

    @Test fun playFlip_doesNotThrow()     { sm().playFlip() }

    @Test fun playCorrect_doesNotThrow()  { sm().playCorrect() }

    @Test fun playWrong_doesNotThrow()    { sm().playWrong() }

    @Test fun playComplete_doesNotThrow() { sm().playComplete() }

    @Test fun release_doesNotThrow()      { sm().release() }

    @Test
    fun allMethodsInSequence_doesNotThrow() {
        val s = sm()
        s.playFlip(); s.playCorrect(); s.playWrong(); s.playComplete(); s.release()
    }
}
