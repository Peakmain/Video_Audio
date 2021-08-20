//
// Created by admin on 2021/8/20.
//

#include "../../include/crash/CrashAnalyser.h"
#include "../../include/crash/JNIBridge.h"

//锁的条件变量
pthread_cond_t signalCond;
pthread_mutex_t signalLock;
pthread_cond_t exceptionCond;
pthread_mutex_t exceptionLock;
native_handler_context *handlerContext;

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
    handlerContext->processName =
}

void analysisNativeException() {

}