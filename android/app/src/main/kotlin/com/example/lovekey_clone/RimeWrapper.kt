package com.example.lovekey_clone

/**
 * JNI wrapper for the librime-android C++ engine.
 *
 * This class provides the Kotlin interface to interact with the underlying
 * RIME (中州韵) input method engine via JNI.
 */
class RimeWrapper {

    companion object {
        init {
            // Load the native library compiled from our cpp/rime_jni.cpp
            // You will also need to load librime.so and its dependencies here later
            // e.g., System.loadLibrary("rime")
            System.loadLibrary("rime_jni")
        }
    }

    /**
     * Initializes the RIME engine.
     *
     * @param sharedDataDir The path where RIME's dictionaries and yaml configs are stored.
     * @param userDataDir The path for user-specific data (user dicts, logs).
     * @return true if initialization was successful.
     */
    external fun init(sharedDataDir: String, userDataDir: String): Boolean

    /**
     * Sends a key event (like a letter or backspace) to the RIME engine.
     *
     * @param keycode The integer keycode (e.g., ASCII value of 'a').
     * @return true if RIME consumed the key, false if it should be handled by the OS.
     */
    external fun processKey(keycode: Int): Boolean

    /**
     * Retrieves the current composing text (e.g., the pinyin "nihao").
     *
     * @return The composing text string.
     */
    external fun getComposingText(): String

    /**
     * Retrieves the list of candidate Chinese characters for the current composing text.
     *
     * @return An array of candidate strings (e.g., ["你好", "泥好"]).
     */
    external fun getCandidates(): Array<String>

    /**
     * Tells the RIME engine that the user has selected a specific candidate index.
     * This usually commits the text and clears/updates the composing state.
     *
     * @param index The 0-based index of the selected candidate.
     * @return The committed string.
     */
    external fun selectCandidate(index: Int): String

    /**
     * Clears the current session and composing state.
     */
    external fun clear()

    /**
     * Destroys the RIME engine and frees resources.
     */
    external fun destroy()
}
