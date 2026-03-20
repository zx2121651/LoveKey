#include <jni.h>
#include <string>
#include <vector>
#include <android/log.h>

#define LOG_TAG "RimeJNI"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

// NOTE: You need to #include <rime_api.h> here once you integrate librime-android

extern "C" JNIEXPORT jboolean JNICALL
Java_com_example_lovekey_1clone_RimeWrapper_init(JNIEnv *env, jobject thiz, jstring shared_data_dir, jstring user_data_dir) {
    const char *shared_dir = env->GetStringUTFChars(shared_data_dir, 0);
    const char *user_dir = env->GetStringUTFChars(user_data_dir, 0);

    LOGI("Initializing RIME...");
    LOGI("Shared Dir: %s", shared_dir);
    LOGI("User Dir: %s", user_dir);

    // TODO: Call RimeApi::setup() and RimeApi::initialize() here
    // RimeTraits traits;
    // ...
    // RIME_INIT(&traits);

    env->ReleaseStringUTFChars(shared_data_dir, shared_dir);
    env->ReleaseStringUTFChars(user_data_dir, user_dir);

    return JNI_TRUE;
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_example_lovekey_1clone_RimeWrapper_processKey(JNIEnv *env, jobject thiz, jint keycode) {
    // TODO: Send keycode to RIME session
    // RimeApi::process_key(session_id, keycode, 0);

    // For demonstration, we just return true indicating we "handled" it
    return JNI_TRUE;
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_example_lovekey_1clone_RimeWrapper_getComposingText(JNIEnv *env, jobject thiz) {
    // TODO: Get composing text from RIME context
    // RimeContext context;
    // RimeApi::get_context(session_id, &context);
    // return env->NewStringUTF(context.composition.preedit);

    return env->NewStringUTF("nihao"); // Mock
}

extern "C" JNIEXPORT jobjectArray JNICALL
Java_com_example_lovekey_1clone_RimeWrapper_getCandidates(JNIEnv *env, jobject thiz) {
    // TODO: Fetch real candidates from RIME context
    // Iterate through context.menu.candidates and construct a jstring array

    // Mock implementation
    jclass stringClass = env->FindClass("java/lang/String");
    jobjectArray candidates = env->NewObjectArray(2, stringClass, env->NewStringUTF(""));
    env->SetObjectArrayElement(candidates, 0, env->NewStringUTF("你好"));
    env->SetObjectArrayElement(candidates, 1, env->NewStringUTF("泥好"));

    return candidates;
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_example_lovekey_1clone_RimeWrapper_selectCandidate(JNIEnv *env, jobject thiz, jint index) {
    // TODO: Tell RIME to select this candidate index
    // RimeApi::select_candidate(session_id, index);
    // Then get the committed text
    // RimeCommit commit;
    // RimeApi::get_commit(session_id, &commit);
    // return commit text

    return env->NewStringUTF("你好"); // Mock
}

extern "C" JNIEXPORT void JNICALL
Java_com_example_lovekey_1clone_RimeWrapper_clear(JNIEnv *env, jobject thiz) {
    // TODO: Clear session composing state
    // RimeApi::clear_composition(session_id);
}

extern "C" JNIEXPORT void JNICALL
Java_com_example_lovekey_1clone_RimeWrapper_destroy(JNIEnv *env, jobject thiz) {
    // TODO: Free session and finalize
    // RimeApi::destroy_session(session_id);
    // RimeApi::finalize();
}
