//
// Created by admin on 2021/8/20.
//

#ifndef VIDEO_AUDIO_JNIBRIDGE_H
#define VIDEO_AUDIO_JNIBRIDGE_H

#include <jni.h>
#include "unistd.h"
class JNIBridge {
private:
    JavaVM *javaVm;//全局的jvm
    jobject callbackObj;//Java回调的接口
    jclass nativeCrashHelperClass;//java的NativeCrashHelper类

public:
    JNIBridge(JavaVM *javaVm, jobject callbackObj, jclass nativeCrashMonitorClass);
};


#endif //VIDEO_AUDIO_JNIBRIDGE_H
