package com.peakmain.video_audio.activity

import android.media.MediaCodec
import android.media.MediaFormat
import android.view.Surface
import android.view.SurfaceHolder
import com.peakmain.ui.utils.LogUtils
import com.peakmain.ui.utils.SizeUtils
import com.peakmain.video_audio.R
import com.peakmain.video_audio.basic.BaseActivity
import com.peakmain.video_audio.simple.BasicSurfaceHolderCallback
import com.peakmain.video_audio.utils.socket.SocketAcceptLive
import kotlinx.android.synthetic.main.activity_project_screen.*

/**
 * author ：Peakmain
 * createTime：2021/8/31
 * mail:2726449200@qq.com
 * describe：接收端
 */
class ProjectScreenAcceptActivity : BaseActivity(), SocketAcceptLive.SocketCallback {
    var mSurface: Surface? = null
    lateinit var mMediaCodec: MediaCodec
    private val mWidth: Int = SizeUtils.screenWidth
    private val mHeight: Int = SizeUtils.screenHeight
    override fun getLayoutId(): Int {
        return R.layout.activity_project_screen
    }

    override fun initView() {
        mNavigationBuilder?.setTitleText("H265实现手机投屏(接收端)")?.create()
        surface_view.holder.addCallback(object : BasicSurfaceHolderCallback() {
            override fun surfaceCreated(holder: SurfaceHolder?) {
                mSurface = holder?.surface
                initSocket()
                initDecoder(mSurface)
            }
        })
    }

    //初始化解码器
    private fun initDecoder(surface: Surface?) {
        mMediaCodec = MediaCodec.createDecoderByType(MediaFormat.MIMETYPE_VIDEO_HEVC)
        val format =
            MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_HEVC, mWidth, mHeight)
        format.setInteger(MediaFormat.KEY_BIT_RATE, mWidth * mHeight)
        format.setInteger(MediaFormat.KEY_FRAME_RATE, 20)
        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1)
        mMediaCodec.configure(
            format,
            surface,
            null, 0
        )
        mMediaCodec.start()
    }

    private fun initSocket() {
        val screenLive = SocketAcceptLive(this)
        screenLive.start()
    }

    override fun initData() {
    }

    override fun callBack(data: ByteArray?) {
        //回调
        LogUtils.e("接收到数据的长度:${data?.size}")
        //客户端主要将获取到的数据进行解码，首先需要通过dsp进行解码
        val index = mMediaCodec.dequeueInputBuffer(10000)
        if (index >= 0) {
            val inputBuffer = mMediaCodec.getInputBuffer(index)
            inputBuffer.clear()
            inputBuffer.put(data, 0, data!!.size)
            //通知dsp芯片帮忙解码
            mMediaCodec.queueInputBuffer(index, 0, data.size, System.currentTimeMillis(), 0)
        }
        //取出数据
        val bufferInfo = MediaCodec.BufferInfo()
        var outIndex: Int = mMediaCodec.dequeueOutputBuffer(bufferInfo, 10000)
        while (outIndex > 0) {
            mMediaCodec.releaseOutputBuffer(outIndex, true)
            outIndex = mMediaCodec.dequeueOutputBuffer(bufferInfo, 10000)
        }
    }
}