package com.peakmain.video_audio.simple

import android.view.SurfaceHolder

/**
 * author ：Peakmain
 * createTime：2021/8/23
 * mail:2726449200@qq.com
 * describe：接口适配器设计模式
 */
abstract class BasicSurfaceHolderCallback:SurfaceHolder.Callback{
    override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {

    }

    override fun surfaceDestroyed(holder: SurfaceHolder?) {
    }

    override fun surfaceCreated(holder: SurfaceHolder?) {
    }

}