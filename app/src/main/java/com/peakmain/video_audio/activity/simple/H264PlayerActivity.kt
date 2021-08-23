package com.peakmain.video_audio.activity.simple

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import com.peakmain.video_audio.R
import com.peakmain.video_audio.basic.BaseActivity
import com.peakmain.video_audio.simple.BasicSurfaceHolderCallback
import com.peakmain.video_audio.simple.H264Player
import java.io.File

/**
 * author ：Peakmain
 * createTime：2021/8/23
 * mail:2726449200@qq.com
 * describe：
 */
class H264PlayerActivity : BaseActivity() {
    var h264Player: H264Player? = null
    override fun getLayoutId(): Int {
        return R.layout.activity_h264_player
    }

    override fun initView() {
        checkPermission()
        mNavigationBuilder!!.setTitleText("H264视频播放").create()
        initSurface()
    }

    private fun initSurface() {
        val surface = findViewById<SurfaceView>(R.id.surfaceView)
        val surfaceHolder = surface.holder
        surfaceHolder.addCallback(object : BasicSurfaceHolderCallback() {
            override fun surfaceCreated(holder: SurfaceHolder?) {
                h264Player = H264Player(
                    this@H264PlayerActivity,
                    File(
                        Environment.getExternalStorageDirectory(),
                        "out.h264"
                    ).absolutePath,
                    surfaceHolder.surface
                )
                h264Player?.startPlay()
            }

        })
    }

    private fun checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ), 1
            )
        }
    }

    override fun initData() {
    }

    fun mediaCodecH264PlayClick(view: View) {

    }

}