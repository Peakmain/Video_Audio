package com.peakmain.video_audio.utils.socket;

import android.media.projection.MediaProjection;
import android.util.Log;

import com.peakmain.ui.BuildConfig;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

/**
 * author ：Peakmain
 * createTime：2021/8/31
 * mail:2726449200@qq.com
 * describe：服务端
 */
public class WebSocketSendLive {
    int mPort;
    private WebSocket mWebSocket;
    CodecLiveH265 mCodecLiveH265;

    public void start(MediaProjection mediaProjection) {
        //启动服务器
        mWebSocketServer.start();
        mCodecLiveH265 = new CodecLiveH265(this, mediaProjection);
        mCodecLiveH265.startLive();
    }

    public void close() {
        try {
            mWebSocket.close();
            mWebSocketServer.stop();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    public WebSocketSendLive(int port) {
        this.mPort = port;
    }

    private final WebSocketServer mWebSocketServer = new WebSocketServer(new InetSocketAddress(mPort)) {
        @Override
        public void onOpen(WebSocket conn, ClientHandshake handshake) {
            WebSocketSendLive.this.mWebSocket = conn;
        }

        @Override
        public void onClose(WebSocket conn, int code, String reason, boolean remote) {
            Log.e(BuildConfig.TAG, "onClose: 关闭 socket ");
        }

        @Override
        public void onMessage(WebSocket conn, String message) {

        }

        @Override
        public void onMessage(WebSocket conn, ByteBuffer message) {
        }

        @Override
        public void onError(WebSocket conn, Exception e) {
            Log.e(BuildConfig.TAG, "onError:  " + e.toString());
        }

        @Override
        public void onStart() {
            Log.e(BuildConfig.TAG, "onStart: 开启 socket ");
        }
    };

    /**
     * 发送数据
     *
     * @param bytes 类型byte[]
     */
    public void sendData(byte[] bytes) {
        if (mWebSocket != null && mWebSocket.isOpen()) {
            mWebSocket.send(bytes);
        }
    }
}
