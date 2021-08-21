package com.peakmain.video_audio.utils.crash;

import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.Keep;

import com.peakmain.video_audio.utils.crash.callback.CrashListener;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * author ：Peakmain
 * createTime：2021/8/20
 * mail:2726449200@qq.com
 * describe：
 */
public class NativeCrashMonitor {
    public static volatile boolean isInit = false;
    private static ThreadGroup systemThreadGroup;
    static {
        System.loadLibrary("native-lib");
        try {
            Class<?> threadGroupClass = Class.forName("java.lang.ThreadGroup");
            Field threadGroupField = threadGroupClass.getDeclaredField("systemThreadGroup");
            threadGroupField.setAccessible(true);
            systemThreadGroup = (ThreadGroup) threadGroupField.get(null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void init(CrashListener callback) {
        if (isInit) {
            return;
        }
        isInit = true;
        nativeCrashInit(this, callback);
        nativeSetup();
    }
    /**
     * 根据线程名获取当前线程的堆栈信息
     */
    @Keep
    private static String getStackInfoByThreadName(String threadName) {
        Thread thread = getThreadByName(threadName);
        StringBuilder sb = new StringBuilder();
        StackTraceElement[] stackTraceElements = thread.getStackTrace();
        for (StackTraceElement stackTraceElement : stackTraceElements) {
            sb.append(stackTraceElement.toString()).append("\r\n");
        }
        return sb.toString();
    }
    public static Thread getThreadByName(String threadName) {
        if (TextUtils.isEmpty(threadName)) {
            return null;
        }
        Thread theThread = null;
        if (threadName.equals("main")) {
            theThread = Looper.getMainLooper().getThread();
        } else {
            Thread[] threadArray = new Thread[]{};
            try {
                Set<Thread> threadSet = getAllStackTraces().keySet();
                threadArray = threadSet.toArray(new Thread[threadSet.size()]);
            } catch (Exception e) {
                e.printStackTrace();
            }
            for (Thread thread : threadArray) {
                if (thread.getName().equals(threadName)) {
                    theThread = thread;
                    Log.e("TAG", "find it." + threadName);
                }
            }
        }
        return theThread;
    }

    //获取线程堆栈的map.
    private static Map<Thread, StackTraceElement[]> getAllStackTraces() {
        if (systemThreadGroup == null) {
            return Thread.getAllStackTraces();
        } else {
            Map<Thread, StackTraceElement[]> map = new HashMap<>();
            int count = systemThreadGroup.activeCount();
            Thread[] threads = new Thread[count + count / 2];
            Log.d("TAG", "activeCount: " + count);
            //赋值所有存活对象到threads
            count = systemThreadGroup.enumerate(threads);
            for (int i = 0; i < count; i++) {
                try {
                    map.put(threads[i], threads[i].getStackTrace());
                } catch (Throwable e) {
                    Log.e("TAG", "fail threadName: " + threads[i].getName(), e);
                }
            }
            return map;
        }
    }
    private native void nativeSetup();

    private native void nativeCrashInit(NativeCrashMonitor nativeCrashMonitor, CrashListener callback);

    public static native void nativeCrashCreate();
}
