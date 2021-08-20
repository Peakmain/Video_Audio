#include <jni.h>
#include <string>
#include <crash/JNIBridge.h>
#include <pthread.h>
#include "../include/log.h"
#include "../include/crash/CrashAnalyser.h"

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

