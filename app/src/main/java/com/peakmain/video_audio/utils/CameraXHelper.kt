package com.peakmain.video_audio.utils

import android.os.Handler
import android.os.HandlerThread
import android.util.Size
import android.view.TextureView
import android.view.ViewGroup
import androidx.camera.core.*
import androidx.camera.core.CameraX.LensFacing
import androidx.camera.core.Preview.OnPreviewOutputUpdateListener
import androidx.camera.core.Preview.PreviewOutput
import androidx.lifecycle.LifecycleOwner
import com.peakmain.ui.utils.SizeUtils
import java.util.concurrent.locks.ReentrantLock

/**
 * author ：Peakmain
 * createTime：2021/8/17
 * mail:2726449200@qq.com
 * describe：
 */
class CameraXHelper(
    private var mLifecycleOwner: LifecycleOwner? = null,
    private var mTextureView: TextureView
) :
    OnPreviewOutputUpdateListener, ImageAnalysis.Analyzer {
    private val mHandlerThread: HandlerThread = HandlerThread("CameraXHelper")
    var width = SizeUtils.screenWidth
    var height = SizeUtils.screenHeight

    //设置后摄像头
    private val currentFacing = LensFacing.BACK
    fun startCamera() {
        if (mLifecycleOwner == null) {
            return
        }
        CameraX.bindToLifecycle(mLifecycleOwner, preView, analysis)
    }

    //预览
    //setTargetResolution设置预览尺寸
    private val preView: Preview
        get() {
            //预览
            //setTargetResolution设置预览尺寸
            val previewConfig =
                PreviewConfig.Builder().setTargetResolution(Size(width, height))
                    .setLensFacing(currentFacing).build()
            val preview = Preview(previewConfig)
            preview.onPreviewOutputUpdateListener = this
            return preview
        }

    private val analysis: ImageAnalysis
        get() {
            val imageAnalysisConfig = ImageAnalysisConfig.Builder()
                .setCallbackHandler(Handler(mHandlerThread.looper))
                .setLensFacing(currentFacing)
                .setImageReaderMode(ImageAnalysis.ImageReaderMode.ACQUIRE_LATEST_IMAGE)
                .setTargetResolution(Size(width, height))
                .build()
            val imageAnalysis = ImageAnalysis(imageAnalysisConfig)
            imageAnalysis.analyzer = this
            return imageAnalysis
        }

    override fun onUpdated(output: PreviewOutput) {
        val surfaceTexture = output.surfaceTexture
        //防止切换镜头报错
        if (mTextureView.surfaceTexture !== surfaceTexture) {
            if (mTextureView.isAvailable) {
                // 当切换摄像头时，会报错
                val parent = mTextureView.parent as ViewGroup
                parent.removeView(mTextureView)
                parent.addView(mTextureView, 0)
                parent.requestLayout()
            }
            mTextureView.surfaceTexture = surfaceTexture
        }
    }

    private val lock =
        ReentrantLock()
    private var y: ByteArray?=null
    private var u: ByteArray?=null
    private var v: ByteArray?=null
    override fun analyze(image: ImageProxy, rotationDegrees: Int) {
        lock.lock()
        try {
            val planes = image.planes
            //初始化y v  u
            if (y == null) {
                y = ByteArray(
                    planes[0].buffer.limit() - planes[0].buffer.position()
                )
                u = ByteArray(
                    planes[1].buffer.limit() - planes[1].buffer.position()
                )
                v = ByteArray(
                    planes[2].buffer.limit() - planes[2].buffer.position()
                )
            }
            if (image.planes[0].buffer.remaining() == y!!.size) {
                planes[0].buffer[y]
                planes[1].buffer[u]
                planes[2].buffer[v]
                val stride = planes[0].rowStride
                val size = Size(image.width, image.height)
                if (cameraXListener != null) {
                    cameraXListener!!.invoke(y, u, v, size, stride)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            lock.unlock()
        }
    }

    private var cameraXListener: ((ByteArray?, ByteArray?, ByteArray?, Size, Int) -> Unit)? = null

    fun setCameraXListener(cameraXListener: ((ByteArray?, ByteArray?, ByteArray?, Size, Int) -> Unit)) {
        this.cameraXListener = cameraXListener
    }

    init {
        //子线程中回调
        mHandlerThread.start()
        mLifecycleOwner = mLifecycleOwner
    }
}