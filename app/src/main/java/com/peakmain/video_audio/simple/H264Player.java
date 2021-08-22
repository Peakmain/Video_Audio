package com.peakmain.video_audio.simple;

import android.content.Context;
import android.media.MediaCodec;
import android.media.MediaFormat;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.view.Surface;

import androidx.annotation.RequiresApi;

import com.peakmain.video_audio.R;
import com.peakmain.video_audio.utils.Utils;

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
    private static int TIMEOUT = 1000;

    public H264Player(Context mContext, String mPath, Surface mSurface) {
        this.mContext = mContext;
        this.mPath = mPath;
        this.mSurface = mSurface;
        HandlerThread handlerThread = new HandlerThread("H264Player");
        handlerThread.start();
        mHandler = new Handler(handlerThread.getLooper());
        try {
            //解码器
            mediaCodec = MediaCodec.createDecoderByType("video/avc");
            MediaFormat format = new MediaFormat();
            //设置码率
            format.setInteger(MediaFormat.KEY_FRAME_RATE, 15);
            mediaCodec.configure(format, mSurface, null, 0);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void startPlay() {
        mHandler.post(() -> {
            mediaCodec.start();
            decode();
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void decode() {
        byte[] bytes = new byte[0];
        try {
            bytes = Utils.getBytes(mPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        int startIndex = 0;
        //总字节数
        int totalSize = bytes.length;
        while (true) {
            if (totalSize == 0 || startIndex >= totalSize) {
                return;
            }
            //+2的目的跳过pps和sps
            int nextFrameStart = findByFrame(bytes, startIndex + 2, totalSize);
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
            int decodeOutIndex = mediaCodec.dequeueOutputBuffer(new MediaCodec.BufferInfo(), TIMEOUT);
            if (decodeOutIndex >= 0) {
                //如果绑定了surface则设置为true
                mediaCodec.releaseOutputBuffer(decodeOutIndex, true);
            }
        }
    }

    //找到下一帧的i帧
    private int findByFrame(byte[] bytes, int start, int totalSize) {
        for (int i = 0; i < totalSize - 4; i++) {
            //0x 00 00 00 01
            if (bytes[i] == 0x00 && bytes[i + 1] == 0x00 && bytes[i + 2] == 0x00 && bytes[i + 3] == 0x01) {
                return i;
            }
        }
        return -1;
    }
}
