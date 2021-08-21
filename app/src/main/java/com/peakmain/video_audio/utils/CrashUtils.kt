package com.peakmain.video_audio.utils

import android.os.Looper
import android.os.MessageQueue
import android.util.Log
import com.peakmain.ui.constants.BasicUIUtils
import com.peakmain.video_audio.utils.crash.JavaCrashMonitor
import com.peakmain.video_audio.utils.crash.NativeCrashMonitor
import com.peakmain.video_audio.utils.crash.callback.CrashListener
import java.io.File

/**
 * author ：Peakmain
 * createTime：2021/5/7
 * mail:2726449200@qq.com
 * describe：异常处理工具类
 */
object CrashUtils : MessageQueue.IdleHandler {
    private const val CRASH_DIR_JAVA = "javaCrash"
    private const val CRASH_DIR_NATIVE = "nativeCrash"
    private lateinit var mCrashListener: CrashListener
    fun init(listener: CrashListener) {
        Looper.myQueue().addIdleHandler(this)
        mCrashListener = listener
    }

    private fun getNativeCrashDir(): File {
        val file = File(BasicUIUtils.application!!.cacheDir, CRASH_DIR_NATIVE)
        if (!file.exists()) {
            file.mkdirs()
        }
        return file
    }

    private fun getJavaCrashDir(): File {
        val file = File(BasicUIUtils.application!!.cacheDir, CRASH_DIR_JAVA)
        if (!file.exists()) {
            file.mkdirs()
        }
        return file
    }

    var mListener: OnFileUploadListener? = null

    interface OnFileUploadListener {
        fun onFileUploadListener(file: File)
    }

    //文件上传
    fun setOnFileUploadListener(listener: OnFileUploadListener?) {
        this.mListener = listener
    }

    override fun queueIdle(): Boolean {
        val javaCrashDir = getJavaCrashDir()
        val nativeCrashDir = getNativeCrashDir()
        JavaCrashMonitor.init(javaCrashDir.absolutePath)
        NativeCrashMonitor().init { threadName, error ->
            mCrashListener.onCrash(threadName, error)
        }
        return false
    }
}