//
// Created by admin on 2021/8/20.
//

#ifndef VIDEO_AUDIO_JNIBRIDGE_H
#define VIDEO_AUDIO_JNIBRIDGE_H
#include "CrashAnalyser.h"
#include <jni.h>
#include "unistd.h"
class JNIBridge {
private:
    JavaVM *javaVm;//全局的jvm
    jobject callbackObj;//Java回调的接口
    jclass nativeCrashMonitorClass;//java的nativeCrashMonitorClass类

public:
    JNIBridge(JavaVM *javaVm, jobject callbackObj, jclass nativeCrashMonitorClass);
public:
    void throwException2Java(native_handler_context *handlerContext);
};


#endif //VIDEO_AUDIO_JNIBRIDGE_H
