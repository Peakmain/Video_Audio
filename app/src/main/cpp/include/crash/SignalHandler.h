//
// Created by admin on 2021/7/9.
//

#ifndef NDKACTUALCOMBAT_SIGNALHANDLER_H
#define NDKACTUALCOMBAT_SIGNALHANDLER_H
#include <signal.h>
#include <string>
#include <unistd.h>
#include "../include/crash/CrashAnalyser.h"
extern bool installSignalHandlers();
extern void installAlternateStack();
// 异常信号量
const int exceptionSignals[] = {SIGSEGV, SIGABRT, SIGFPE, SIGILL, SIGBUS, SIGTRAP};
const int exceptionSignalsNumber = sizeof(exceptionSignals)/ sizeof(exceptionSignals[0]);

static struct sigaction oldHandlers[NSIG];
#endif //NDKACTUALCOMBAT_SIGNALHANDLER_H
