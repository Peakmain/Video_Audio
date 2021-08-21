//
// Created by admin on 2021/8/20.
//

#include <string>
#include "../../include/crash/JNIBridge.h"
#include <dlfcn.h>
#include <crash/stringprintf.h>
#include <cinttypes>
#include "cxxabi.h"
JNIBridge::JNIBridge(JavaVM *javaVm, jobject callbackObj, jclass nativeCrashMonitorClass) {
    this->javaVm = javaVm;
    this->callbackObj = callbackObj;
    this->nativeCrashMonitorClass = nativeCrashMonitorClass;
}

//将异常抛给Java
void JNIBridge::throwException2Java(native_handler_context *handlerContext) {
    LOGE("throwException2Java");
    //子线程获取env
    JNIEnv *env = NULL;
    if (this->javaVm->AttachCurrentThread(&env, NULL) != JNI_OK) {
        LOGE("AttachCurrentThread failed");
    }
    const char *sig = "(Ljava/lang/String;)Ljava/lang/String;";
    jmethodID getStackInfoByThreadNameMid = env->GetStaticMethodID(this->nativeCrashMonitorClass,
                                                                   "getStackInfoByThreadName", sig);
    //private stifatic String getStackInfoByThreadName(String threadName)
    jstring jThreadName = env->NewStringUTF(handlerContext->threadName);
    jobject javaStackInfo = env->CallStaticObjectMethod(this->nativeCrashMonitorClass,
                                                        getStackInfoByThreadNameMid, jThreadName);
    //java的String->native的string
    const char *javaExceptionStackInfo = env->GetStringUTFChars((jstring) javaStackInfo, JNI_FALSE);
    //获取c++堆栈信息
    int frame_size = handlerContext->frame_size;
    string result;
    for (int index = 0; index < frame_size; ++index) {
        uintptr_t pc = handlerContext->frames[index];
        //获取到加载的内存的起始地址
        Dl_info stack_info;
        void *const addr = (void *) pc;
        if (dladdr(addr, &stack_info) != 0 && stack_info.dli_fname != NULL) {
            if (stack_info.dli_fbase == 0) {
                // No valid map associated with this frame.
                result += "  <unknown>";
            } else if (stack_info.dli_fname) {
                std::string so_name = std::string(stack_info.dli_fname);
                result += "  " + so_name;
            } else {
                result += android::base::StringPrintf("  <anonymous:%" PRIx64 ">",
                                                      (uint64_t) stack_info.dli_fbase);
            }
            if (stack_info.dli_sname) {
                char *demangled_name = abi::__cxa_demangle(stack_info.dli_sname, nullptr, nullptr,
                                                           nullptr);
                if (demangled_name == nullptr) {
                    result += " (";
                    result += stack_info.dli_sname;
                } else {
                    result += " (";
                    result += demangled_name;
                    free(demangled_name);
                }
                if (stack_info.dli_saddr != 0) {
                    uintptr_t offset = pc - (uintptr_t) stack_info.dli_saddr;
                    result += android::base::StringPrintf(":%" PRId64, (uint64_t) offset);
                }
                result += ')';
            }
            result += '\n';
        }
    }
    //回掉Java的接口
    jclass crashClass = env->GetObjectClass(this->callbackObj);
    jmethodID crashMethod = env->GetMethodID(crashClass, "onCrash",
                                             "(Ljava/lang/String;Ljava/lang/Error;)V");
    jclass jErrorClass = env->FindClass("java/lang/Error");
    jmethodID jErrorInitMethod = env->GetMethodID(jErrorClass, "<init>", "(Ljava/lang/String;)V");
    result = result += javaExceptionStackInfo;
    jstring errorMessage = env->NewStringUTF(result.c_str());
    //错误信息给Error
    jobject errorObject = env->NewObject(jErrorClass, jErrorInitMethod, errorMessage);
    env->CallVoidMethod(this->callbackObj, crashMethod, jThreadName, errorObject);
    if (this->javaVm->DetachCurrentThread() != JNI_OK) {
        LOGE("DetachCurrentThread failed!");
    }
}
