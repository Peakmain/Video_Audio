package com.peakmain.video_audio.activity

import android.view.Surface
import android.view.SurfaceHolder
import android.view.View
import com.peakmain.video_audio.R
import com.peakmain.video_audio.basic.BaseActivity
import com.peakmain.video_audio.simple.BasicSurfaceHolderCallback
import com.peakmain.video_audio.utils.socket.WebSocketSendLive
import com.peakmain.video_audio.video.DecodecVideoCallLiveH265
import kotlinx.android.synthetic.main.activity_send_video_call.*


/**
 * author ：Peakmain
 * createTime：2021/9/2
 * mail:2726449200@qq.com
 * describe：
 */
class VideoSendVideoCallActivity : BaseActivity(), WebSocketSendLive.SocketCallback {
    var surface: Surface? = null
    var decodecPlayerLiveH265: DecodecVideoCallLiveH265? = null
    override fun getLayoutId(): Int {
        return R.layout.activity_send_video_call
    }

    override fun initView() {
        surface_view.holder.addCallback(object: BasicSurfaceHolderCallback(){
            override fun surfaceCreated(holder: SurfaceHolder?) {
                surface = holder?.surface
                decodecPlayerLiveH265 =
                    DecodecVideoCallLiveH265()
                decodecPlayerLiveH265!!.initDecoder(surface)
            }
        })
    }

    override fun initData() {
    }

    fun connect(view: View) {
        video_call_send_surface_view.startCaptrue (this)
    }

    override fun callBack(data: ByteArray?) {
        if (decodecPlayerLiveH265 != null) {
            decodecPlayerLiveH265?.callBack(data)
        }
    }
}