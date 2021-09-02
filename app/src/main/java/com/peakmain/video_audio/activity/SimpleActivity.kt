package com.peakmain.video_audio.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.hardware.display.DisplayManager
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Handler
import android.os.HandlerThread
import com.peakmain.ui.recyclerview.listener.OnItemClickListener
import com.peakmain.ui.utils.SizeUtils
import com.peakmain.video_audio.R
import com.peakmain.video_audio.activity.simple.H264PlayerActivity
import com.peakmain.video_audio.basic.BaseActivity
import com.peakmain.video_audio.basic.BaseRecyclerStringAdapter
import com.peakmain.video_audio.utils.FileUtils
import kotlinx.android.synthetic.main.activity_main.*

/**
 * author ：Peakmain
 * createTime：2021/8/22
 * mail:2726449200@qq.com
 * describe：
 */
class SimpleActivity : BaseActivity() {
    private var mData: ArrayList<String> = ArrayList()

    //屏幕录制的请求码
    private val screenRequestCode = 1010
    private lateinit var mMediaProjection: MediaProjection
    private lateinit var mMediaProjectionManager: MediaProjectionManager
    private lateinit var mMediaCodec: MediaCodec
    private lateinit var mHandlerThread: HandlerThread
    private lateinit var mHandler: Handler
    override fun getLayoutId(): Int {
        return R.layout.activity_main
    }

    override fun initView() {
        mNavigationBuilder!!.setTitleText("音视频简单使用").create()

        mMediaProjectionManager =
            getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        mHandlerThread = HandlerThread("Screen_Record")
        mHandlerThread.start()
    }

    override fun initData() {
        mData.add("MediaCodec实现H264音视频播放")
        mData.add("MediaCodec实现H264音视频转成图片(单张)")
        mData.add("MMKV的读写")
        mData.add("MediaCodec实现屏幕录制")
        mData.add("Camera实现预览编码")
        mData.add("CameraX和Camera2实现预览编码")
        val adapter = BaseRecyclerStringAdapter(this, data = mData)
        recycler_view.adapter = adapter
        adapter.setOnItemClickListener(object : OnItemClickListener {
            override fun onItemClick(position: Int) {
                when (position) {
                    0 -> {
                        val intent = Intent(this@SimpleActivity, H264PlayerActivity::class.java)
                        intent.putExtra("isPrintImage", false)
                        startActivity(intent)
                    }
                    1 -> {
                        val intent = Intent(this@SimpleActivity, H264PlayerActivity::class.java)
                        intent.putExtra("isPrintImage", true)
                        startActivity(intent)
                    }
                    2 -> {
                        val intent = Intent(this@SimpleActivity, MMKVActivity::class.java)
                        startActivity(intent)
                    }
                    3 -> {
                        val intent: Intent = mMediaProjectionManager.createScreenCaptureIntent()
                        startActivityForResult(intent, screenRequestCode)
                    }
                    4->{
                        val intent = Intent(this@SimpleActivity, CameraActivity::class.java)
                        startActivity(intent)
                    }
                    5->{
                        val intent = Intent(this@SimpleActivity, Camera2XActivity::class.java)
                        startActivity(intent)
                    }
                }
            }

        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == screenRequestCode && resultCode == Activity.RESULT_OK) {
            mMediaProjection = mMediaProjectionManager.getMediaProjection(resultCode, data)
            initMediaCodec()
        }
    }

    //初始化编码器
    private fun initMediaCodec() {
        mMediaCodec = MediaCodec.createEncoderByType("video/avc")
        //配置信息
        val format = MediaFormat.createVideoFormat(
            "video/avc",
            SizeUtils.screenWidth,
            SizeUtils.screenHeight
        )
        format.setInteger(
            MediaFormat.KEY_COLOR_FORMAT,
            MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface
        )

        format.setInteger(MediaFormat.KEY_FRAME_RATE, 15)
        //码率，码率越高，字节越长，视频就越清晰
        format.setInteger(MediaFormat.KEY_BIT_RATE, 400_000)
        //2s一个I帧
        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 2)
        //CONFIGURE_FLAG_ENCODE：表示编码
        mMediaCodec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
        //创建一个场地
        val surface = mMediaCodec.createInputSurface()
        mHandler = Handler(mHandlerThread.looper)
        mHandler.post {
            mMediaCodec.start()
            mMediaProjection.createVirtualDisplay(
                "screen_codec",
                SizeUtils.screenWidth,
                SizeUtils.screenHeight,
                1,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC,
                surface,
                null, null
            )
            //此时有了源数据，就可以取出数据进行编码
            val bufferInfo = MediaCodec.BufferInfo()
            while (true) {
                val outIndex = mMediaCodec.dequeueOutputBuffer(bufferInfo, 10000)
                if (outIndex >= 0) {
                    //这是dsp的数据，无法直接使用
                    val byteBuffer = mMediaCodec.getOutputBuffer(outIndex)
                    val outData = ByteArray(bufferInfo.size)
                    byteBuffer.get(outData)
                    FileUtils.writeBytes(outData, "codec.h264")
                    FileUtils.writeContent(outData, "codec.txt")
                    mMediaCodec.releaseOutputBuffer(outIndex, false)
                }
            }
        }

    }

}