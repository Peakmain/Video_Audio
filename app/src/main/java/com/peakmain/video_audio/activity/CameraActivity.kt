package com.peakmain.video_audio.activity

import android.media.MediaCodec
import android.view.View
import com.peakmain.video_audio.R
import com.peakmain.video_audio.basic.BaseActivity
import com.peakmain.video_audio.utils.CameraHelper
import com.peakmain.video_audio.utils.FileUtils
import kotlinx.android.synthetic.main.activity_camera.*

class CameraActivity : BaseActivity() {
    lateinit var mCameraHelper: CameraHelper

    override fun getLayoutId(): Int {
        return R.layout.activity_camera
    }

    override fun initView() {
        mCameraHelper = CameraHelper(surface_view)

        mCameraHelper.setCameraListener { data, size ->
            val mediaCodec = mCameraHelper.getMediaCodec()
            val info = MediaCodec.BufferInfo()
            //yuv数据需要去解析
            val inIndex = mediaCodec.dequeueInputBuffer(10000)
            if (inIndex >= 0) {
                val byteBuffer = mediaCodec.getInputBuffer(inIndex)
                byteBuffer.clear()
                byteBuffer.put(data, 0, data!!.size)
                mediaCodec.queueInputBuffer(inIndex, 0, data.size, 0, 0)
            }
            val outIndex = mediaCodec.dequeueOutputBuffer(info, 10000)
            if (outIndex >= 0) {
                val byteBuffer = mediaCodec.getOutputBuffer(outIndex)
                val ba = ByteArray(byteBuffer.remaining())
                byteBuffer[ba]
                FileUtils.writeBytes(ba, "Camera.h264")
                mediaCodec.releaseOutputBuffer(outIndex,false)
            }
        }
    }



    override fun initData() {
    }

    fun cameraClick(view: View) {
        mCameraHelper.start()
    }
}