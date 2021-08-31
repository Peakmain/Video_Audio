package com.peakmain.video_audio.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.MediaCodec
import android.media.MediaFormat
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.view.Surface
import android.view.SurfaceHolder
import android.view.View
import com.peakmain.ui.utils.LogUtils
import com.peakmain.ui.utils.SizeUtils
import com.peakmain.video_audio.R
import com.peakmain.video_audio.basic.BaseActivity
import com.peakmain.video_audio.simple.BasicSurfaceHolderCallback
import com.peakmain.video_audio.utils.socket.SocketAcceptLive
import com.peakmain.video_audio.utils.socket.WebSocketSendLive
import kotlinx.android.synthetic.main.activity_project_screen.*

/**
 * author ：Peakmain
 * createTime：2021/8/31
 * mail:2726449200@qq.com
 * describe：
 */
class ProjectScreenActivity : BaseActivity(), SocketAcceptLive.SocketCallback {
    var mSurface: Surface? = null
    lateinit var mMediaCodec: MediaCodec
    private val mWidth: Int = SizeUtils.screenWidth
    private val mHeight: Int = SizeUtils.screenHeight
    lateinit var mMediaProjectionManager: MediaProjectionManager
    private lateinit var mWebSocketLive: WebSocketSendLive
    private val mPort = 14004
    override fun getLayoutId(): Int {
        return R.layout.activity_project_screen
    }

    override fun initView() {
        surface_view.holder.addCallback(object : BasicSurfaceHolderCallback() {
            override fun surfaceCreated(holder: SurfaceHolder?) {
                mSurface = holder?.surface
                initClientSocket()
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
    }

    private fun initClientSocket() {
        val screenLive = SocketAcceptLive(this)
        screenLive.start()
    }

    override fun initData() {
    }



    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == 100) {
            val mediaProjection: MediaProjection =
                mMediaProjectionManager.getMediaProjection(resultCode, data)
                    ?: return
            mWebSocketLive = WebSocketSendLive(mPort)
            mWebSocketLive.start(mediaProjection)
        }
    }

    fun h265ProjectScreenAccept(view: View) {
        try {
            mMediaCodec.start()
        } catch (e: Exception) {
            LogUtils.e(e.toString())
        }
    }
    fun h265ProjectScreenSend(view: View) {
        mMediaProjectionManager =
            getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        val captureIntent: Intent = mMediaProjectionManager.createScreenCaptureIntent()
        startActivityForResult(captureIntent, 100)
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