package com.peakmain.video_audio

import android.app.Application
import android.util.Log
import com.peakmain.ui.utils.LogUtils
import com.peakmain.video_audio.utils.CrashUtils
import com.peakmain.video_audio.utils.crash.callback.CrashListener
import java.lang.Error

/**
 * author ：Peakmain
 * createTime：2021/8/21
 * mail:2726449200@qq.com
 * describe：
 */

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        CrashUtils.init(CrashListener { threadName, error ->
            Log.e("Video_Audio","threadName:$threadName\nerror info : $error")
        })

    }
}