package com.peakmain.video_audio.widget;

import android.content.Context;
import android.hardware.Camera;
import android.os.Build;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.peakmain.video_audio.utils.socket.SocketAcceptLive;
import com.peakmain.video_audio.video.EncodecVideoCallAcceptLiveH265;

import java.io.IOException;

/**
 * author ：Peakmain
 * createTime：2021/9/2
 * mail:2726449200@qq.com
 * describe：
 */
public class VideoCallAcceptSurfaceView extends SurfaceView implements SurfaceHolder.Callback, Camera.PreviewCallback {
    private Camera.Size size;
    private Camera mCamera;

    EncodecVideoCallAcceptLiveH265 encodecPushLiveH265;

    public VideoCallAcceptSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        getHolder().addCallback(this);
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {
        startPreview();
    }

    byte[] buffer;

    private void startPreview() {
        mCamera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
//        流程
        Camera.Parameters parameters = mCamera.getParameters();
//尺寸
        size = parameters.getPreviewSize();

        try {
            mCamera.setPreviewDisplay(getHolder());
//            横着
            mCamera.setDisplayOrientation(90);
            //y:u:v=4:1:1
            buffer = new byte[size.width * size.height * 3 / 2];
            mCamera.addCallbackBuffer(buffer);
            mCamera.setPreviewCallbackWithBuffer(this);
//            输出数据怎么办
            mCamera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {

    }

    public void startCaptrue(SocketAcceptLive.SocketCallback socketCallback) {
        encodecPushLiveH265 = new EncodecVideoCallAcceptLiveH265(socketCallback, size.width, size.height);
        encodecPushLiveH265.startLive();

    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onPreviewFrame(byte[] bytes, Camera camera) {
        if (encodecPushLiveH265 != null) {
            encodecPushLiveH265.encodeFrame(bytes);
        }

        mCamera.addCallbackBuffer(bytes);
    }
}
