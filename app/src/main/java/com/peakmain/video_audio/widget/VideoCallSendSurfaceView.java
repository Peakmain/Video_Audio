package com.peakmain.video_audio.widget;

import android.content.Context;
import android.hardware.Camera;
import android.os.Build;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.peakmain.video_audio.utils.socket.WebSocketSendLive;
import com.peakmain.video_audio.video.EncodecVideoCallSendLiveH265;

import java.io.IOException;

/**
 * author:Peakmain
 * createTime:2021/8/12
 * mail:2726449200@qq.com
 * describe：
 */
public class VideoCallSendSurfaceView extends SurfaceView implements SurfaceHolder.Callback, Camera.PreviewCallback {
    private Camera.Size size;
    private Camera mCamera;
    EncodecVideoCallSendLiveH265 encodecSendLiveH265;
    public VideoCallSendSurfaceView(Context context, AttributeSet attrs) {
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
        Camera.Parameters parameters = mCamera.getParameters();
        size = parameters.getPreviewSize();
        try {
            mCamera.setPreviewDisplay(getHolder());
            mCamera.setDisplayOrientation(90);
            buffer = new byte[size.width * size.height * 3 / 2];
            mCamera.addCallbackBuffer(buffer);
            mCamera.setPreviewCallbackWithBuffer(this);
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
    public void startCaptrue(WebSocketSendLive.SocketCallback socketCallback){
        encodecSendLiveH265 = new EncodecVideoCallSendLiveH265(socketCallback,size.width, size.height);
        encodecSendLiveH265.startLive();

    }
    @Override
    public void onPreviewFrame(byte[] bytes, Camera camera) {
        if (encodecSendLiveH265 != null) {
            encodecSendLiveH265.encodeFrame(bytes);
        }
        mCamera.addCallbackBuffer(bytes);
    }
}
