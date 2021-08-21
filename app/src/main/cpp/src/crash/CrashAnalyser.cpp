//
// Created by admin on 2021/8/20.
//

#include <crash/Utils.h>
#include "../../include/crash/CrashAnalyser.h"
#include "../../include/crash/JNIBridge.h"
#include <unwind.h>

//锁的条件变量
pthread_cond_t signalCond;
pthread_mutex_t signalLock;
pthread_cond_t exceptionCond;
pthread_mutex_t exceptionLock;
native_handler_context *handlerContext;

_Unwind_Reason_Code unwind_callback(struct _Unwind_Context *context, void *arg) {
    native_handler_context *const s = static_cast<native_handler_context *const>(arg);
    //pc是每个堆栈的栈顶
    const uintptr_t pc = _Unwind_GetIP(context);
    if (pc != 0x0) {
        // 把 pc 值保存到 native_handler_context
        s->frames[s->frame_size++] = pc;
    }
    if (s->frame_size == BACKTRACE_FRAMES_MAX) {
        return _URC_END_OF_STACK;
    } else {
        return _URC_NO_REASON;
    }
}

void initCondition() {
    handlerContext = (native_handler_context *) malloc(sizeof(native_handler_context_struct));
    pthread_mutex_init(&signalLock, NULL);
    pthread_cond_init(&signalCond, NULL);
    pthread_mutex_init(&exceptionLock, NULL);
    pthread_cond_init(&exceptionCond, NULL);
}

void *threadCrashMonitor(void *argv) {
    JNIBridge *jniBridge = static_cast<JNIBridge *>(argv);

    while (true) {
        //等待信号处理函数唤醒
        waitForSignal();
        //唤醒之后，分析native堆栈
        analysisNativeException();

        //抛给java
        jniBridge->throwException2Java(handlerContext);
    }
}

//等待信号
void waitForSignal() {
    pthread_mutex_lock(&signalLock);
    LOGE("waitForSignal start.");
    pthread_cond_wait(&signalCond, &signalLock);
    LOGE("waitForSignal finish.");
    pthread_mutex_unlock(&signalLock);

}

// 唤醒等待
void notifyCaughtSignal(int code, siginfo_t *si, void *sc) {
    copyInfo2Context(code, si, sc);
    pthread_mutex_lock(&signalLock);
    pthread_cond_signal(&signalCond);
    pthread_mutex_unlock(&signalLock);
}

//保存唤醒后的信息
void copyInfo2Context(int code, siginfo_t *si, void *sc) {
    handlerContext->code = code;
    handlerContext->si = si;
    handlerContext->sc = sc;
    handlerContext->pid = getpid();
    handlerContext->tid = gettid();
    handlerContext->processName = getProcessName(handlerContext->pid);
    if (handlerContext->pid == handlerContext->tid) {
        handlerContext->threadName = "main";
    } else {
        handlerContext->threadName = getThreadName(handlerContext->tid);
    }
    handlerContext->frame_size = 0;
    //捕获c/c++的堆栈信息
    _Unwind_Backtrace(unwind_callback, handlerContext);
}
//分析native的异常
void analysisNativeException() {
    const char *posixDesc = desc_sig(handlerContext->si->si_signo, handlerContext->si->si_code);
    LOGD("posixDesc -> %s", posixDesc);
    LOGD("signal -> %d", handlerContext->si->si_signo);
    LOGD("address -> %p", handlerContext->si->si_addr);
    LOGD("processName -> %s", handlerContext->processName);
    LOGD("threadName -> %s", handlerContext->threadName);
    LOGD("pid -> %d", handlerContext->pid);
    LOGD("tid -> %d", handlerContext->tid);
}