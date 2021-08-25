package com.peakmain.video_audio.simple.mmkv;

import android.content.Context;
import android.os.Environment;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * author ：Peakmain
 * createTime：2021/8/24
 * mail:2726449200@qq.com
 * describe：
 */
public class MMKV {
    private static  String rootDir = null;
    private long nativeHandle;

    private MMKV(long handle) {
        nativeHandle = handle;
    }
    public static String init(@NotNull Context context) {
        String root = Environment.getExternalStorageDirectory() + "/mmkv";
        return init(root);
    }
    public static String init(String rootDir) {
        MMKV.rootDir = rootDir;
        mmkvInit(rootDir);
        return rootDir;
    }
    @Nullable
    public static MMKV defaultMMKV() {
        long handle = getDefaultMMKV();
        return new MMKV(handle);
    }

    public int getInt(String key, int defaultValue) {
        return getInt(nativeHandle, key, defaultValue);
    }

    public void putInt(String key, int value) {
        putInt(nativeHandle, key, value);
    }

    private native static long getDefaultMMKV();
    private static native void mmkvInit(String rootDir);
    private native int getInt(long handle, String key, int defaultValue);
    private native void putInt(long handle, String key, int value);
    private native void putString(long handle, String key, String value);
    public void putString(@NotNull String key, @NotNull String value) {
        putString(nativeHandle,key,value);
    }
}
