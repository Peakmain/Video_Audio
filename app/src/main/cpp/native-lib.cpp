#include "native-lib.h"

/**
 * 捕获native异常
 */
extern "C"
JNIEXPORT void JNICALL
Java_com_peakmain_video_1audio_utils_crash_NativeCrashMonitor_nativeCrashInit(JNIEnv *env,
                                                                              jobject thiz,
                                                                              jobject nativeCrashMonitor,
                                                                              jobject callback) {
    //获取全局的jvm
    JavaVM *javaVm;
    env->GetJavaVM(&javaVm);
    //生成全局对象
    callback = env->NewGlobalRef(callback);
    jclass nativeCrashMonitorClass = env->GetObjectClass(nativeCrashMonitor);
    nativeCrashMonitorClass = (jclass) env->NewGlobalRef(nativeCrashMonitorClass);
    auto *jniBridge = new JNIBridge(javaVm, callback, nativeCrashMonitorClass);
    pthread_t pthread;
    //创建一个线程
    initCondition();
    //ret=0代表创建成功
    int ret = pthread_create(&pthread, NULL, threadCrashMonitor, jniBridge);
    if (ret < 0) {
        LOGE("%s", "pthread_create error");
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_com_peakmain_video_1audio_utils_crash_NativeCrashMonitor_nativeSetup(JNIEnv *env,
                                                                          jobject thiz) {
    installAlternateStack();
    installSignalHandlers();
}
//创建一个native异常
extern "C"
JNIEXPORT void JNICALL
Java_com_peakmain_video_1audio_utils_crash_NativeCrashMonitor_nativeCrashCreate(JNIEnv *env,
                                                                                jclass clazz) {
    int *num = NULL;
    *num = 100;
}
/**
 * MMKV
 */
extern "C"
JNIEXPORT void JNICALL
Java_com_peakmain_video_1audio_simple_mmkv_MMKV_mmkvInit(JNIEnv *env, jclass clazz,
                                                         jstring rootDir_) {
    const char *rootDir = env->GetStringUTFChars(rootDir_, 0);

    MMKV::initializeMMKV(rootDir);

    env->ReleaseStringUTFChars(rootDir_, rootDir);

}extern "C"
JNIEXPORT jlong JNICALL
Java_com_peakmain_video_1audio_simple_mmkv_MMKV_getDefaultMMKV(JNIEnv *env, jclass clazz) {
    MMKV *kv = MMKV::defaultMMKV();
    return reinterpret_cast<jlong>(kv);

}extern "C"
JNIEXPORT jint JNICALL
Java_com_peakmain_video_1audio_simple_mmkv_MMKV_getInt(JNIEnv *env, jobject thiz, jlong handle,
                                                       jstring key_, jint defaultValue) {

    const char *key = env->GetStringUTFChars(key_, 0);
    MMKV *kv = reinterpret_cast<MMKV *>(handle);
    int returnValue = kv->getInt(key, defaultValue);

    env->ReleaseStringUTFChars(key_, key);
    return returnValue;

}extern "C"
JNIEXPORT void JNICALL
Java_com_peakmain_video_1audio_simple_mmkv_MMKV_putInt(JNIEnv *env, jobject thiz, jlong handle,
                                                       jstring key_, jint value) {
    const char *key = env->GetStringUTFChars(key_, 0);
    MMKV *kv = reinterpret_cast<MMKV *>(handle);
    kv->putInt(key, value);
    env->ReleaseStringUTFChars(key_, key);
}