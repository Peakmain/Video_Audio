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
class ProjectScreenSendActivity : BaseActivity() {
    lateinit var mMediaProjectionManager: MediaProjectionManager
    private lateinit var mWebSocketLive: WebSocketSendLive
    override fun getLayoutId(): Int {
        return R.layout.activity_project_screen
    }

    override fun initView() {
        mMediaProjectionManager =
            getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        val captureIntent: Intent = mMediaProjectionManager.createScreenCaptureIntent()
        startActivityForResult(captureIntent, 100)
    }


    override fun initData() {
        mNavigationBuilder?.setTitleText("H265实现手机投屏(发送端)")?.create()
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == 100) {
            val mediaProjection: MediaProjection =
                mMediaProjectionManager.getMediaProjection(resultCode, data)
                    ?: return
            mWebSocketLive = WebSocketSendLive()
            mWebSocketLive.start(mediaProjection)
        }
    }


}