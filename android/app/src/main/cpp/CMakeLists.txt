cmake_minimum_required(VERSION 3.22.1)

project("rime_jni")

# Add the main JNI wrapper source code
add_library(
    rime_jni
    SHARED
    rime_jni.cpp
)

# Search for the standard Android log library
find_library(
    log-lib
    log
)

# Link the JNI wrapper against the Android log library.
# Later, you will also link the imported `librime-android` (.so) here.
target_link_libraries(
    rime_jni
    ${log-lib}
)
