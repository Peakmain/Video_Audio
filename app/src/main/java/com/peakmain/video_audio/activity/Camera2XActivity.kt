package com.peakmain.video_audio.activity

import android.graphics.SurfaceTexture
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.util.Size
import android.view.TextureView
import android.view.View
import com.peakmain.ui.constants.PermissionConstants
import com.peakmain.ui.utils.PermissionUtils
import com.peakmain.video_audio.R
import com.peakmain.video_audio.basic.BaseActivity
import com.peakmain.video_audio.utils.Camera2Helper
import com.peakmain.video_audio.utils.CameraXHelper
import com.peakmain.video_audio.utils.FileUtils
import kotlinx.android.synthetic.main.activity_camera2x.*

/**
 * author ：Peakmain
 * createTime：2021/8/27
 * mail:2726449200@qq.com
 * describe：
 */
class Camera2XActivity : BaseActivity(), TextureView.SurfaceTextureListener {
    lateinit var camera2Helper: Camera2Helper
    lateinit var cameraXHelper: CameraXHelper
    override fun getLayoutId(): Int {
        return R.layout.activity_camera2x
    }

    private var mediaCodec: MediaCodec? = null
    override fun initView() {
        mNavigationBuilder?.setTitleText("Camera使用")?.create()
        if (!PermissionUtils.hasPermission(PermissionConstants.CAMERA)) {
            PermissionUtils.request(this,
                100,
                PermissionConstants.getPermissions(PermissionConstants.CAMERA),
                object : PermissionUtils.OnPermissionListener {
                    override fun onPermissionDenied(deniedPermissions: List<String>) {

                    }

                    override fun onPermissionGranted() {

                    }

                }
            )
        }
        texture_preview.surfaceTextureListener = this
    }

    override fun initData() {
    }


    override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture?, width: Int, height: Int) {

    }

    override fun onSurfaceTextureUpdated(surface: SurfaceTexture?) {
    }

    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture?): Boolean = false

    override fun onSurfaceTextureAvailable(surface: SurfaceTexture?, width: Int, height: Int) {
        // 打开摄像头
        initCamera()
    }

    private fun initCamera() {
/*        camera2Helper = Camera2Helper(this)
        camera2Helper.start(texture_preview, 0)*/
        cameraXHelper = CameraXHelper(this, texture_preview)
        cameraXHelper.setCameraXListener { y, u, v, size, stride ->
            onPreview(y, u, v, size, stride)
        }
    }

    private var nv21
            : ByteArray? = null
    private var nv21_rotated: ByteArray? = null
    private var nv12: ByteArray? = null
    private fun onPreview(
        y: ByteArray?,
        u: ByteArray?,
        v: ByteArray?,
        previewSize: Size,
        stride: Int
    ) {
        if (nv21 == null) {
            nv21 = ByteArray(stride * previewSize.height * 3 / 2)
            nv21_rotated = ByteArray(stride * previewSize.height * 3 / 2)
        }
        if (mediaCodec == null) {
            initCodec(previewSize)
        }
        //生成nv21
        FileUtils.yuvToNv21(y, u, v, nv21, stride, previewSize.height)
        //生成的数据旋转90度
        FileUtils.nv21RotateTo90(nv21, nv21_rotated, stride, previewSize.height)
        //nv21->nv12
        val temp: ByteArray = FileUtils.nv21toNV12(nv21_rotated)
        //输出成H264码流
        val bufferInfo = MediaCodec.BufferInfo()
        val inIndex = mediaCodec!!.dequeueInputBuffer(1000)
        //通知dsp去解析
        if (inIndex >= 0) {
            val byteBuffer = mediaCodec!!.getInputBuffer(inIndex)
            byteBuffer.clear()
            byteBuffer.put(temp, 0, temp.size)
            mediaCodec?.queueInputBuffer(inIndex, 0, temp.size, 0, 0)
        }
        //取出
        val outIndex = mediaCodec!!.dequeueOutputBuffer(bufferInfo, 1000)
        if (outIndex >= 0) {
            val outputBuffer = mediaCodec!!.getOutputBuffer(outIndex)
            val ba = ByteArray(outputBuffer.remaining())
            outputBuffer[ba]
            FileUtils.writeContent(ba, "Camera.txt")
            FileUtils.writeBytes(ba, "Camera.h264")
            mediaCodec?.releaseOutputBuffer(outIndex, false)
        }
    }

    //初始化编码器
    private fun initCodec(previewSize: Size) {
        try {
            mediaCodec = MediaCodec.createEncoderByType("video/avc")
            val format = MediaFormat.createVideoFormat(
                "video/avc",
                previewSize.height, previewSize.width
            )
            //设置帧率
            format.setInteger(
                MediaFormat.KEY_COLOR_FORMAT,
                MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible
            )
            format.setInteger(MediaFormat.KEY_FRAME_RATE, 15)
            format.setInteger(MediaFormat.KEY_BIT_RATE, 400_000)//越高字节越长，视频越清晰
            format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 2) //2s一个I帧
            mediaCodec?.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
            mediaCodec?.start()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    //———————————————————————————————————————————————————————————————————————————————————————————点击事件———————————————————————————————————————————————————————————————————————————— ———————————————
    fun camera2Click(view: View) {
        camera2Helper.setCamera2Listener { y, u, v, size, stride ->
            onPreview(y, u, v, size, stride)
        }
    }

    fun cameraClick(view: View) {
        surface.startCaptrue()
    }

    fun cameraXClick(view: View) {
       texture_preview.post{
           cameraXHelper.startCamera()
       }
    }

}