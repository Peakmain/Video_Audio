//
// Created by admin on 2021/7/9.
//

#ifndef NDKACTUALCOMBAT_UTILS_H
#define NDKACTUALCOMBAT_UTILS_H

#include <signal.h>
#include <stdlib.h>
#define PROCESS_NAME_LENGTH 512
#define THREAD_NAME_LENGTH 512
extern const char* desc_sig(int sig, int code);

extern const char* getProcessName(pid_t pid);

extern const char* getThreadName(pid_t tid);
extern bool is_dll(const char* dll_name);

#endif //NDKACTUALCOMBAT_UTILS_H
