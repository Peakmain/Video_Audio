package com.peakmain.video_audio.utils

import android.content.Context
import java.io.*

/**
 * author ：Peakmain
 * createTime：2021/8/22
 * mail:2726449200@qq.com
 * describe：
 */
object VideoAudioUtils {
    @JvmStatic
    @Throws(IOException::class)
    fun getBytes(path: String?): ByteArray {
        val `is`: InputStream =
            DataInputStream(FileInputStream(File(path)))
        var len: Int
        val size = 1024
        var buf: ByteArray
        val bos = ByteArrayOutputStream()
        buf = ByteArray(size)
        while (`is`.read(buf, 0, size).also { len = it } != -1) bos.write(buf, 0, len)
        buf = bos.toByteArray()
        return buf
    }
    @JvmStatic
    @Throws(IOException::class)
    fun copyAssets(context:Context, assetsName: String, path: String) {
        val assetFileDescriptor = context.assets.openFd(assetsName)
        val from =
            FileInputStream(assetFileDescriptor.fileDescriptor).channel
        val to = FileOutputStream(path).channel
        from.transferTo(assetFileDescriptor.startOffset, assetFileDescriptor.length, to)
    }
}