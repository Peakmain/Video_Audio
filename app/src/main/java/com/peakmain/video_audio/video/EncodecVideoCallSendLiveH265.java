package com.peakmain.video_audio.video;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;

import com.peakmain.video_audio.utils.FileUtils;
import com.peakmain.video_audio.utils.socket.WebSocketSendLive;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * author ：Peakmain
 * createTime：2021/9/2
 * mail:2726449200@qq.com
 * describe：
 */
public class EncodecVideoCallSendLiveH265 {
    private WebSocketSendLive socketLive;
    public static final int NAL_I = 19;
    public static final int NAL_VPS = 32;
    int width;
    int height;
    //nv21转换成nv12的数据
    byte[] nv12;
    //旋转之后的yuv数据
    byte[] yuv;
    private MediaCodec mediaCodec;
    int frameIndex;
    public EncodecVideoCallSendLiveH265(WebSocketSendLive.SocketCallback socketCallback, int width, int height) {
        this.socketLive = new WebSocketSendLive(socketCallback);
        socketLive.start();
        this.width = width;
        this.height = height;

    }

    public void startLive() {
        //编码器
        try {
            mediaCodec = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_HEVC);
            MediaFormat mediaFormat = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_HEVC, height, width);
            mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, width * height);
            mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, 15);
            mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible);
            mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 5);
            mediaCodec.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
            mediaCodec.start();
            int bufferLength = width * height * 3 / 2;
            nv12 = new byte[bufferLength];
            yuv = new byte[bufferLength];
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void encodeFrame(byte[] bytes) {
        nv12 = FileUtils.nv21toNV12(bytes);
        FileUtils.portraitData2Raw(nv12, yuv, width, height);
        //解析相机的数据
        int inputBufferIndex = mediaCodec.dequeueInputBuffer(100000);
        if (inputBufferIndex >= 0) {
            ByteBuffer inputBuffer = mediaCodec.getInputBuffer(inputBufferIndex);
            inputBuffer.clear();
            inputBuffer.put(yuv);
            long presentationTimeUs = computePresentationTime(frameIndex);
            mediaCodec.queueInputBuffer(inputBufferIndex, 0, yuv.length, presentationTimeUs, 0);
            frameIndex++;
        }
        //编码
        MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
        int outputBufferIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, 100000);
        while (outputBufferIndex >= 0) {
            ByteBuffer outputBuffer = mediaCodec.getOutputBuffer(outputBufferIndex);
            dealFrame(outputBuffer, bufferInfo);
            mediaCodec.releaseOutputBuffer(outputBufferIndex, false);
            outputBufferIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, 0);

        }
    }

    private long computePresentationTime(long frameIndex) {
        //帧率是15(自己设置的)  132是偏移量 frameIndex单位是微秒(us)
        return 132 + frameIndex * 1_000_000 / 15;
    }
    private byte[] vps_sps_pps_buf;
    private void dealFrame(ByteBuffer bb, MediaCodec.BufferInfo bufferInfo) {
        int offset = 4;
        if (bb.get(2) == 0x01) {
            offset = 3;
        }
        int type = (bb.get(offset) & 0x7E) >> 1;
        if (type == NAL_VPS) {
            vps_sps_pps_buf = new byte[bufferInfo.size];
            bb.get(vps_sps_pps_buf);
        } else if (type == NAL_I) {
            final byte[] bytes = new byte[bufferInfo.size];
            bb.get(bytes);
            byte[] newBuf = new byte[vps_sps_pps_buf.length + bytes.length];
            System.arraycopy(vps_sps_pps_buf, 0, newBuf, 0, vps_sps_pps_buf.length);
            System.arraycopy(bytes, 0, newBuf, vps_sps_pps_buf.length, bytes.length);
            this.socketLive.sendData(newBuf);
        } else {
            final byte[] bytes = new byte[bufferInfo.size];
            bb.get(bytes);
            this.socketLive.sendData(bytes);
        }
    }
}
