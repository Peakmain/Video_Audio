package com.peakmain.video_audio.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.ImageFormat
import android.graphics.Point
import android.graphics.SurfaceTexture
import android.hardware.camera2.*
import android.hardware.camera2.CameraCaptureSession.CaptureCallback
import android.media.ImageReader
import android.media.ImageReader.OnImageAvailableListener
import android.os.Handler
import android.os.HandlerThread
import android.util.Size
import android.view.Surface
import android.view.TextureView
import androidx.core.app.ActivityCompat
import java.util.*
import kotlin.math.abs

/**
 * author ：Peakmain
 * createTime：2021/8/30
 * mail:2726449200@qq.com
 * describe：
 */
class Camera2Helper(private val mContext: Context) {
    private lateinit var mPreviewSize: Size
    private var mTextureView: TextureView? = null
    private val mPreviewViewSize: Point? = null
    private lateinit var mImageReader: ImageReader
    private var mBackgroundHandler: Handler? = null
    private var mBackgroundThread: HandlerThread? = null
    private var mCameraDevice: CameraDevice? = null
    lateinit var mPreviewRequestBuilder: CaptureRequest.Builder
    private var mCaptureSession: CameraCaptureSession? = null
    private var mCamera2Listener: ((ByteArray?, ByteArray?, ByteArray?, Size, Int) -> Unit)? = null

    /**
     * @param textureView textureView
     * @param cameraId    0代表后置摄像头 1代表前置摄像头，不合理的默认是1
     */
    fun start(textureView: TextureView?, cameraId: Int) {
        mTextureView = textureView
        //摄像头的管理类
        val cameraManager =
            mContext.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        val characteristics: CameraCharacteristics
        try {
            characteristics = if (cameraId == 0) {
                cameraManager.getCameraCharacteristics("" + CameraCharacteristics.LENS_FACING_FRONT)
            } else {
                cameraManager.getCameraCharacteristics("" + CameraCharacteristics.LENS_FACING_BACK)
            }
            //管理摄像头支持的所有输出格式和尺寸
            val map =
                characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
            //最合适的尺寸
            mPreviewSize = getBestSupportedSize(
                ArrayList(
                    listOf(
                        *map.getOutputSizes(
                            SurfaceTexture::class.java
                        )
                    )
                )
            )
            mImageReader = ImageReader.newInstance(
                mPreviewSize.width, mPreviewSize.height
                , ImageFormat.YUV_420_888, 2
            )
            mBackgroundThread = HandlerThread("Camera2Helper")
            mBackgroundThread?.start()
            mBackgroundHandler = Handler(mBackgroundThread!!.looper)
            //拍照获得图片的回调
            mImageReader.setOnImageAvailableListener(
                OnImageAvailableListenerImpl(),
                mBackgroundHandler
            )
            if (ActivityCompat.checkSelfPermission(
                    mContext,
                    Manifest.permission.CAMERA
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            if (cameraId == 0) {
                cameraManager.openCamera(
                    "" + CameraCharacteristics.LENS_FACING_FRONT,
                    mDeviceStateCallback,
                    mBackgroundHandler
                )
            } else {
                cameraManager.openCamera(
                    "" + CameraCharacteristics.LENS_FACING_BACK,
                    mDeviceStateCallback,
                    mBackgroundHandler
                )
            }
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

    private val mDeviceStateCallback: CameraDevice.StateCallback =
        object : CameraDevice.StateCallback() {
            override fun onOpened(camera: CameraDevice) {
                mCameraDevice = camera
                createCameraPreviewSession()
            }

            override fun onDisconnected(camera: CameraDevice) {
                camera.close()
                mCameraDevice = null
            }

            override fun onError(camera: CameraDevice, error: Int) {
                camera.close()
                mCameraDevice = null
            }
        }

    /**
     * 创建相机预览
     */
    private fun createCameraPreviewSession() {
        try {
            val texture = mTextureView!!.surfaceTexture
            //设置预览的宽高
            texture.setDefaultBufferSize(mPreviewSize.width, mPreviewSize.height)
            val surface = Surface(texture)
            // 创建预览需要的CaptureRequest.Builder
            mPreviewRequestBuilder =
                mCameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
            //设置自动对焦
            mPreviewRequestBuilder.set(
                CaptureRequest.CONTROL_AE_ANTIBANDING_MODE,
                CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE
            )
            mPreviewRequestBuilder.addTarget(surface)
            mPreviewRequestBuilder.addTarget(mImageReader.surface)
            //该对象负责管理处理预览请求和拍照请求
            mCameraDevice!!.createCaptureSession(
                listOf(
                    surface,
                    mImageReader.surface
                ), mCaptureStateCallback, mBackgroundHandler
            )
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

    private val mCaptureStateCallback: CameraCaptureSession.StateCallback =
        object : CameraCaptureSession.StateCallback() {
            override fun onConfigured(session: CameraCaptureSession) {
                if (null == mCameraDevice) {
                    return
                }
                mCaptureSession = session
                try {
                    mCaptureSession!!.setRepeatingRequest(
                        mPreviewRequestBuilder.build(),
                        object : CaptureCallback() {},
                        mBackgroundHandler
                    )
                } catch (e: CameraAccessException) {
                    e.printStackTrace()
                }
            }

            override fun onConfigureFailed(session: CameraCaptureSession) {}
        }

    private fun getBestSupportedSize(sizes: ArrayList<Size>): Size {
        var sizes = sizes
        val maxPreviewSize = Point(1920, 1080)
        val minPreviewSize = Point(1280, 720)
        val defaultSize = sizes[0]
        val tempSizes = sizes.toTypedArray()
        Arrays.sort(
            tempSizes
        ) { o1: Size, o2: Size ->
            when {
                o1.width > o2.width -> {
                    return@sort -1
                }
                o1.width == o2.width -> {
                    return@sort if (o1.height > o2.height) -1 else 1
                }
                else -> {
                    return@sort 1
                }
            }
        }
        sizes =
            ArrayList(listOf(*tempSizes))
        for (i in sizes.indices.reversed()) {
            if (sizes[i].width > maxPreviewSize.x || sizes[i]
                    .height > maxPreviewSize.y
            ) {
                sizes.removeAt(i)
                continue
            }
            if (sizes[i].width < minPreviewSize.x || sizes[i]
                    .height < minPreviewSize.y
            ) {
                sizes.removeAt(i)
            }
        }
        if (sizes.size == 0) {
            return defaultSize
        }
        var bestSize = sizes[0]
        var previewViewRatio: Float
        previewViewRatio = if (mPreviewViewSize != null) {
            mPreviewViewSize.x.toFloat() / mPreviewViewSize.y.toFloat()
        } else {
            bestSize.width.toFloat() / bestSize.height.toFloat()
        }
        if (previewViewRatio > 1) {
            previewViewRatio = 1 / previewViewRatio
        }
        for (s in sizes) {
            if (abs(s.height / s.width.toFloat() - previewViewRatio) < Math.abs(
                    bestSize.height / bestSize.width.toFloat() - previewViewRatio
                )
            ) {
                bestSize = s
            }
        }
        return bestSize
    }


    private inner class OnImageAvailableListenerImpl : OnImageAvailableListener {
        private var y: ByteArray? = null
        private var u: ByteArray? = null
        private var v: ByteArray? = null
        override fun onImageAvailable(reader: ImageReader) {
            val image = reader.acquireNextImage()
            val planes = image.planes
            //初始化y u v
            if (y == null) {
                y = ByteArray((planes[0].buffer.limit() - planes[0].buffer.position()))
                u = ByteArray(
                    planes[1].buffer.limit() - planes[1].buffer.position()
                )
                v = ByteArray(
                    planes[2].buffer.limit() - planes[2].buffer.position()
                )
            }
            if (image.planes[0].buffer.remaining() == y?.size) {
                //分别填到 yuv
                planes[0].buffer[y]
                planes[1].buffer[u]
                planes[2].buffer[v]
            }
            mCamera2Listener?.invoke(y, u, v, mPreviewSize, planes[0].rowStride)
            image.close()
        }
    }

    fun setCamera2Listener(camera2Listener: ((ByteArray?, ByteArray?, ByteArray?, Size, Int) -> Unit)) {
        this.mCamera2Listener = camera2Listener
    }
}


