package com.peakmain.video_audio.widget;

import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.os.Environment;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.peakmain.ui.utils.ToastUtils;
import com.peakmain.video_audio.utils.FileUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * author ：Peakmain
 * createTime：2021/8/27
 * mail:2726449200@qq.com
 * describe：Camera相机
 */
public class CameraSurface extends SurfaceView implements SurfaceHolder.Callback, Camera.PreviewCallback {
    private Camera mCamera;
    private Camera.Size size;
    byte[] mBuffer;

    public CameraSurface(Context context) {
        this(context, null);
    }

    public CameraSurface(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CameraSurface(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        getHolder().addCallback(this);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        startPrview();
    }

    private void startPrview() {
        //打开相机，后摄像头
        mCamera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
        //获取相机相关的参数
        Camera.Parameters parameters = mCamera.getParameters();
        size = parameters.getPreviewSize();
        try {
            mCamera.setPreviewDisplay(getHolder());
            mCamera.setDisplayOrientation(90);
            mBuffer = new byte[size.width * size.height * 3 / 2];
            mCamera.addCallbackBuffer(mBuffer);
            mCamera.setPreviewCallbackWithBuffer(this);
            mCamera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private volatile boolean isCaptrue;

    public void startCaptrue() {
        isCaptrue = true;
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        if (isCaptrue) {
            byte[] nv12 = FileUtils.nv21toNV12(data);
            FileUtils.portraitData2Raw(nv12, mBuffer, size.width, size.height);
            isCaptrue = false;
            captrue(mBuffer);
            ToastUtils.showLong("保存成功");
        }
        mCamera.addCallbackBuffer(data);
    }

    int index = 0;

    private void captrue(byte[] bytes) {
        String fileName = "Camera_" + index++ + ".jpg";
        File sdRoot = Environment.getExternalStorageDirectory();
        File pictureFile = new File(sdRoot, fileName);
        if (!pictureFile.exists()) {
            try {
                pictureFile.createNewFile();
                FileOutputStream fileOutputStream = new FileOutputStream(pictureFile);
                YuvImage image = new YuvImage(bytes, ImageFormat.NV21,size.height, size.width,null);   //将NV21 data保存成YuvImage
                image.compressToJpeg(
                        new Rect(0, 0, image.getWidth(), image.getHeight()),
                        100, fileOutputStream);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }
}
