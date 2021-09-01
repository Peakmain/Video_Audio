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
    private WebSocket mWebSocket;
    CodecLiveH265 mCodecLiveH265;
    private SocketCallback socketCallback;

    public WebSocketSendLive(SocketCallback socketCallback) {
        this.socketCallback = socketCallback;
    }

    public void start(MediaProjection mediaProjection) {
        //启动服务器
        mWebSocketServer.start();
        mCodecLiveH265 = new CodecLiveH265(this, mediaProjection);
        mCodecLiveH265.startLive();
    }

    public void start() {
        mWebSocketServer.start();
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

    public WebSocketSendLive() {
    }

    private final WebSocketServer mWebSocketServer = new WebSocketServer(new InetSocketAddress(14430)) {
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
        public void onMessage(WebSocket conn, ByteBuffer bytes) {
            byte[] buf = new byte[bytes.remaining()];
            bytes.get(buf);
            socketCallback.callBack(buf);
        }

        @Override
        public void onError(WebSocket conn, Exception e) {
            Log.e(BuildConfig.TAG, "onError:  " + e.toString());
        }

        @Override
        public void onStart() {
            Log.e(BuildConfig.TAG, "onStart: 开启 socket: ");
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

    public interface SocketCallback {
        void callBack(byte[] data);
    }
}
