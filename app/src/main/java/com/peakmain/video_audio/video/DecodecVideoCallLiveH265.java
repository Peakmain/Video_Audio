package com.peakmain.video_audio.video;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.os.Build;
import android.util.Log;
import android.view.Surface;

import androidx.annotation.RequiresApi;

import com.peakmain.video_audio.BuildConfig;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * author ：Peakmain
 * createTime：2021/8/12
 * mail:2726449200@qq.com
 * describe：
 */
public class DecodecVideoCallLiveH265 {
    private MediaCodec mediaCodec;

    public void initDecoder(Surface surface) {
        try {
            mediaCodec = MediaCodec.createDecoderByType("video/hevc");
            final MediaFormat format = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_HEVC, 1080, 1920);
            format.setInteger(MediaFormat.KEY_BIT_RATE, 1080 * 1920);
            format.setInteger(MediaFormat.KEY_FRAME_RATE, 15);
            format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 5);
            mediaCodec.configure(format,
                    surface,
                    null, 0);
            mediaCodec.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void callBack(byte[] data) {
        Log.i(BuildConfig.TAG, "接收到消息: " + data.length);
        int index = mediaCodec.dequeueInputBuffer(100000);
        if (index >= 0) {
            ByteBuffer inputBuffer = mediaCodec.getInputBuffer(index);
            inputBuffer.clear();
            inputBuffer.put(data, 0, data.length);
            //dsp芯片解码    解码 的 传进去的   只需要保证编码顺序就好了  1000
            mediaCodec.queueInputBuffer(index,
                    0, data.length, System.currentTimeMillis(), 0);
        }
        // 获取到解码后的数据  编码 ipbn
        MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
        int outputBufferIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, 100000);
        while (outputBufferIndex >= 0) {
            mediaCodec.releaseOutputBuffer(outputBufferIndex, true);
            outputBufferIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, 0);
        }
    }
}
