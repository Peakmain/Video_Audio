package com.peakmain.video_audio.utils.socket;

import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.projection.MediaProjection;
import android.os.Handler;
import android.os.HandlerThread;
import android.view.Surface;

import com.peakmain.ui.utils.SizeUtils;

import java.io.IOException;
import java.nio.ByteBuffer;

import static android.media.MediaFormat.KEY_BIT_RATE;
import static android.media.MediaFormat.KEY_FRAME_RATE;

/**
 * author ：Peakmain
 * createTime：2021/8/31
 * mail:2726449200@qq.com
 * describe：编码H265(服务端开始直播)
 */
public class CodecLiveH265 implements Runnable {
    private MediaProjection mMediaProjection;
    private WebSocketSendLive mWebSocketSendLive;
    private MediaCodec mMediaCodec;
    private final int mWidth = SizeUtils.getScreenWidth();
    private final int mHeight = SizeUtils.getScreenHeight();
    VirtualDisplay mVirtualDisplay;
    private final Handler mHandler;

    public CodecLiveH265(WebSocketSendLive webSocketSendLive, MediaProjection mediaProjection) {
        this.mMediaProjection = mediaProjection;
        this.mWebSocketSendLive = webSocketSendLive;
        HandlerThread mHandlerThread = new HandlerThread("CodecLiveH265_HandlerThread");
        mHandlerThread.start();
        mHandler = new Handler(mHandlerThread.getLooper());
    }

    public void startLive() {
        try {
            //服务器端编码H264通过socket发送给客户端
            MediaFormat format = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_HEVC, mWidth, mHeight);
            format.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
            format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1);
            format.setInteger(KEY_BIT_RATE, mWidth * mHeight);
            format.setInteger(KEY_FRAME_RATE, 20);
            mMediaCodec = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_HEVC);
            mMediaCodec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
            //创建场地
            Surface surface = mMediaCodec.createInputSurface();
            mVirtualDisplay = mMediaProjection.createVirtualDisplay("CodecLiveH265",
                    mWidth, mHeight, 1, DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC, surface, null, null);
            mHandler.post(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        mMediaCodec.start();
        MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
        while (true) {
            //取出数据发送给客户端
            int outIndex = mMediaCodec.dequeueOutputBuffer(bufferInfo, 1000);
            if (outIndex >= 0) {
                ByteBuffer buffer = mMediaCodec.getOutputBuffer(outIndex);
                dealFrame(buffer, bufferInfo);
                mMediaCodec.releaseOutputBuffer(outIndex, false);
            }
        }
    }

    public static final int NAL_I = 19;
    public static final int NAL_VPS = 32;
    //vps+sps+pps是一帧，所以只需要获取vps
    private byte[] vps_sps_pps_buffer;

    private void dealFrame(ByteBuffer buffer, MediaCodec.BufferInfo bufferInfo) {
        //过滤掉第一个0x00 00 00 01 或者0x 00 00 01
        int offset = 4;
        if (buffer.get(2) == 0x01) {
            offset = 3;
        }
        //获取帧类型
        int type = (buffer.get(offset) & 0x7E) >> 1;
        if (type == NAL_VPS) {
            vps_sps_pps_buffer = new byte[bufferInfo.size];
            buffer.get(vps_sps_pps_buffer);
        } else if (type == NAL_I) {
            //I帧
            final byte[] bytes = new byte[bufferInfo.size];
            buffer.get(bytes);
            //vps_pps_sps+I帧的数据
            byte[] newBuffer = new byte[vps_sps_pps_buffer.length + bytes.length];
            System.arraycopy(vps_sps_pps_buffer, 0, newBuffer, 0, vps_sps_pps_buffer.length);
            System.arraycopy(bytes, 0, newBuffer, vps_sps_pps_buffer.length, bytes.length);
            mWebSocketSendLive.sendData(newBuffer);
        }else{
            //P帧 B帧直接发送就可以了
            final byte[] bytes = new byte[bufferInfo.size];
            buffer.get(bytes);
            mWebSocketSendLive.sendData(bytes);
        }
    }
}
