//
// Created by peakmain on 2021/8/21.
//
#include "../include/crash/SignalHandler.h"
//额外的栈空间
void installAlternateStack() {
    stack_t newStack;
    stack_t oldStack;
    memset(&newStack, 0, sizeof(newStack));
    memset(&oldStack, 0, sizeof(oldStack));
    static const unsigned sigaltstackSize = std::max(16384, SIGSTKSZ);
    if (sigaltstack(NULL, &oldStack) == -1
        || !oldStack.ss_sp
        || oldStack.ss_size < sigaltstackSize) {
        newStack.ss_sp = calloc(1, sigaltstackSize);
        newStack.ss_size = sigaltstackSize;
        if (sigaltstack(&newStack, NULL) == -1) {
            free(newStack.ss_sp);
        }
    }
}
void signalPass(int code, siginfo_t *si, void *sc) {
    LOGE("监听到了native异常");
    // 这里要考虑非信号方式防止死锁
    signal(code, SIG_DFL);
    signal(SIGALRM, SIG_DFL);
    (void) alarm(8);
    // 解析栈信息，回调给 java 层，上报到后台或者保存本地文件
    notifyCaughtSignal(code, si, sc);
    // 给系统原来默认的处理，否则就会进入死循环
    oldHandlers[code].sa_sigaction(code, si, sc);
}

/**
 * 安装信号捕获到native crash
 */
bool installSignalHandlers() {
    //保存原来的信号处理
    for (int i = 0; i < exceptionSignalsNumber; i++) {
        // signum：代表信号编码，可以是除SIGKILL及SIGSTOP外的任何一个特定有效的信号，如果为这两个信号定义自己的处理函数，将导致信号安装错误。
        // act：指向结构体sigaction的一个实例的指针，该实例指定了对特定信号的处理，如果设置为空，进程会执行默认处理。
        // oldact：和参数act类似，只不过保存的是原来对相应信号的处理，也可设置为NULL。
        // int sigaction(int signum, const struct sigaction *act, struct sigaction *oldact));
        if (sigaction(exceptionSignals[i], NULL, &oldHandlers[exceptionSignals[i]]) == -1) {
            return false;
        }
    }
    struct sigaction sa{};
    memset(&sa, 0, sizeof(sa));
    //不同堆栈处理并且可将参数传递下去
    sa.sa_flags = SA_ONSTACK | SA_SIGINFO;
    // 指定信号处理的回调函数
    sa.sa_sigaction = signalPass;
    //处理当前信号量的时候不考虑其他的
    for (int i = 0; i < exceptionSignalsNumber; ++i) {
        //阻塞其他信号的
        sigaddset(&sa.sa_mask, exceptionSignals[i]);
    }
    for (int i = 0; i < exceptionSignalsNumber; ++i) {
        //处理自己的信号，如果成功返回0，失败返回-1
        if (sigaction(exceptionSignals[i], &sa, NULL) == -1) {
            // 可以输出一个警告
        }
    }
    return true;
}
