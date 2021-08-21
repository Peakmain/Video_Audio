//
// Created by admin on 2021/8/20.
//

#ifndef VIDEO_AUDIO_CRASHANALYSER_H
#define VIDEO_AUDIO_CRASHANALYSER_H
#include <pthread.h>
#include "log.h"
#include "threads.h"
#include <malloc.h>
using namespace std;
#define BACKTRACE_FRAMES_MAX 32
typedef struct native_handler_context_struct {
    int code;
    siginfo_t *si;
    void *sc;
    pid_t pid;
    pid_t tid;
    const char *processName;
    const char *threadName;
    int frame_size;
    uintptr_t frames[BACKTRACE_FRAMES_MAX];
} native_handler_context;

extern void initCondition();
void *threadCrashMonitor(void *argv);

extern void waitForSignal();

extern void analysisNativeException();

extern void notifyCaughtSignal(int code, siginfo_t *si, void *sc);

extern void copyInfo2Context(int code, siginfo_t *si, void *sc);
#endif //VIDEO_AUDIO_CRASHANALYSER_H
