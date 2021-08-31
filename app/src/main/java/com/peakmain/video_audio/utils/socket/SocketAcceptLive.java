package com.peakmain.video_audio.utils.socket;

import android.util.Log;

import com.peakmain.video_audio.BuildConfig;

import org.java_websocket.WebSocket;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;

/**
 * author ：Peakmain
 * createTime：2021/8/31
 * mail:2726449200@qq.com
 * describe：
 */
public class SocketAcceptLive {

    private SocketCallback mSocketCallback;
    private CustomWebSocketClient mWebSocketClient;

    public SocketAcceptLive(SocketCallback mSocketCallback) {
        this.mSocketCallback = mSocketCallback;
    }

    public void start() {
        try {
            URI url = new URI("ws://10.0.30.57:14004");
            mWebSocketClient = new CustomWebSocketClient(url);
            mWebSocketClient.connect();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    private class CustomWebSocketClient extends WebSocketClient {

        public CustomWebSocketClient(URI uri) {
            super(uri);
        }
        @Override
        public void onOpen(ServerHandshake handshakedata) {
            Log.e(BuildConfig.TAG, "SocketAcceptLive打开 socket  onOpen: ");
        }

        @Override
        public void onMessage(String message) {

        }

        @Override
        public void onMessage(ByteBuffer bytes) {
            //接收到消息，进行解码
            byte[] buffer = new byte[bytes.remaining()];
            bytes.get(buffer);
            mSocketCallback.callBack(buffer);
        }

        @Override
        public void onClose(int code, String reason, boolean remote) {
            Log.e(BuildConfig.TAG, "SocketAcceptLive  onClose ");
        }

        @Override
        public void onError(Exception e) {
            Log.e(BuildConfig.TAG, "onError: "+e);
        }
    }

    public interface SocketCallback {
        void callBack(byte[] data);
    }
}
