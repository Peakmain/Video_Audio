package com.peakmain.video_audio.simple;

import android.content.Context;
import android.media.MediaCodec;
import android.media.MediaFormat;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.Surface;

import androidx.annotation.RequiresApi;

import com.peakmain.ui.utils.LogUtils;
import com.peakmain.video_audio.utils.VideoAudioUtils;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * author ：Peakmain
 * createTime：2021/8/22
 * mail:2726449200@qq.com
 * describe：
 */
public class H264Player {
    private Context mContext;
    private String mPath;
    private Surface mSurface;
    private Handler mHandler;
    private MediaCodec mediaCodec;
    private static int TIMEOUT = 10000;
    private static final String TAG = H264Player.class.getSimpleName();

    public H264Player(Context context, String path, Surface surface) {
        this.mContext = context;
        this.mPath = path;
        this.mSurface = surface;
        HandlerThread handlerThread = new HandlerThread("H264Player");
        handlerThread.start();
        mHandler = new Handler(handlerThread.getLooper());
        try {
            mediaCodec = MediaCodec.createDecoderByType("video/avc");
            MediaFormat format = MediaFormat.createVideoFormat("video/avc", 720, 1280);
            //设置帧数
            format.setInteger(MediaFormat.KEY_FRAME_RATE, 15);
            mediaCodec.configure(format, surface, null, 0);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void startPlay() {
        mHandler.post(() -> {
            mediaCodec.start();
            decode();
        });
    }

    private void decode() {
        byte[] bytes = null;
        try {
            bytes = VideoAudioUtils.getBytes(mPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        int startIndex = 0;
        //总字节数
        int totalSize = bytes.length;
        while (true) {
            if (totalSize == 0 || startIndex >= totalSize) {
                break;
            }
            //+2的目的跳过pps和sps
            int nextFrameStart = findByFrame(bytes, startIndex + 2, totalSize);
            MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
            int decodeInputIndex = mediaCodec.dequeueInputBuffer(TIMEOUT);
            if (decodeInputIndex >= 0) {
                ByteBuffer byteBuffer = mediaCodec.getInputBuffer(decodeInputIndex);
                byteBuffer.clear();
                byteBuffer.put(bytes, startIndex, nextFrameStart - startIndex);
                //通知dsp进行解绑
                mediaCodec.queueInputBuffer(decodeInputIndex, 0, nextFrameStart - startIndex, 0, 0);
                startIndex = nextFrameStart;
            } else {
                continue;
            }
            //得到数据
            int decodeOutIndex = mediaCodec.dequeueOutputBuffer(info, TIMEOUT);
            if (decodeOutIndex >= 0) {
                try {
                    Thread.sleep(25);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                //如果绑定了surface则设置为true
                mediaCodec.releaseOutputBuffer(decodeOutIndex, true);
            } else {
                Log.e(TAG, "解绑失败");
            }
        }
    }

    /**
     * 找到下一帧的i帧
     */
    private int findByFrame(byte[] bytes, int start, int totalSize) {
        for (int i = start; i < totalSize - 4; i++) {
            //0x 00 00 00 01
            if (bytes[i] == 0x00 && bytes[i + 1] == 0x00 && bytes[i + 2] == 0x00 && bytes[i + 3] == 0x01) {
                return i;
            }
        }
        return -1;
    }
}
