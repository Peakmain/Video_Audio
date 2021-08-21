package com.peakmain.video_audio.utils.crash;

import com.peakmain.video_audio.utils.crash.callback.CrashListener;

/**
 * author ：Peakmain
 * createTime：2021/8/20
 * mail:2726449200@qq.com
 * describe：
 */
public class NativeCrashMonitor {
    public static volatile boolean isInit = false;

    static {
        System.loadLibrary("native-lib");
    }

    public void init(CrashListener callback) {
        if (isInit) {
            return;
        }
        isInit = true;
        nativeCrashInit(this, callback);
    }

    private native void nativeCrashInit(NativeCrashMonitor nativeCrashMonitor, CrashListener callback);
}
