package br.com.bradseg.bsmo.opentok;

import android.content.res.Resources;
import android.graphics.Point;
import android.opengl.GLSurfaceView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.getcapacitor.JSObject;
import com.getcapacitor.Logger;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;
import com.opentok.android.BaseVideoRenderer;
import com.opentok.android.OpentokError;
import com.opentok.android.Publisher;
import com.opentok.android.PublisherKit;
import com.opentok.android.Session;
import com.opentok.android.Stream;
import com.opentok.android.Subscriber;
import com.opentok.android.SubscriberKit;


@CapacitorPlugin(name = "CapacitorOpenTok")
public class CapacitorOpenTokPlugin extends Plugin {

    private static final String TAG = CapacitorOpenTokPlugin.class.getSimpleName();

    private PluginCall call;
    private Session session;
    private Publisher publisher;
    private Subscriber subscriber;

    @PluginMethod
    public void connect(PluginCall call) {
        Log.d(TAG, "PluginCall connect");
        Logger.debug("PluginCall connect");
        this.call = call;

        String apiKey = call.getString("api_key");
        String sessionId = call.getString("session_id");
        String token = call.getString("token");

        Logger.debug("connect apiKey "+apiKey);
        Logger.debug("connect sessionId "+sessionId);
        Logger.debug("connect token "+token);

        if (apiKey.isEmpty() || sessionId.isEmpty() || token.isEmpty()) {
            call.reject("É necessário enviar todos os parametros");
            return;
        }

        if (this.session == null) {
            this.publisher = null;
            initializeSession(apiKey, sessionId, token);
        } else {
            call.reject("Session is not disconnected. Connect only when session is disconnected.");
        }
    }

    @PluginMethod
    public void disconnect(PluginCall call) {
        Log.d(TAG, "PluginCall disconnect");
        Logger.debug("PluginCall disconnect");
        this.call = call;
        if (this.session.getConnection().getConnectionId().isEmpty()) {
            Logger.debug("Opentok: Erro ao desconectar - Não há conexão ativa");
            call.reject("Opentok: Não há conexão ativa");
        } else {
            Logger.debug("Opentok: Disconnecting from session");
            this.session.disconnect();
            call.resolve();
        }
    }

    @PluginMethod
    public void mute(PluginCall call) {
        Log.d(TAG, "PluginCall mute");
        Logger.debug("PluginCall mute");
        this.call = call;

        boolean audioStatus = call.getBoolean("enabled", true);
        this.publisher.setPublishAudio(audioStatus);

        JSObject ret = new JSObject();
        ret.put("enabled", audioStatus);
        call.resolve(ret);
    }

    private void initializeSession(String apiKey, String sessionId, String token) {
        Log.d(TAG, "apiKey: " + apiKey);
        Log.d(TAG, "sessionId: " + sessionId);
        Log.d(TAG, "token: " + token);

        Logger.debug("apiKey: " + apiKey);
        Logger.debug("sessionId: " + sessionId);
        Logger.debug("token: " + token);

        session = new Session.Builder(getActivity(), apiKey, sessionId).build();
        session.setSessionListener(sessionListener);
        session.connect(token);
    }

    private Session.SessionListener sessionListener = new Session.SessionListener() {

        @Override
        public void onConnected(Session session) {
            Log.d(TAG, "onConnected: Connected to session: " + session.getSessionId());

            publisher = new Publisher.Builder(getActivity()).build();
            publisher.setPublisherListener(publisherListener);
            publisher.getRenderer().setStyle(BaseVideoRenderer.STYLE_VIDEO_SCALE, BaseVideoRenderer.STYLE_VIDEO_FILL);

            int heightPixels = (int) (getHeightScreen() * 0.1) + 32;
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(150, 210);
            params.gravity = Gravity.BOTTOM | Gravity.END;
            params.setMargins(0, 0, 32, heightPixels);

            if (publisher.getView() instanceof GLSurfaceView) {
                ((GLSurfaceView) publisher.getView()).setZOrderOnTop(true);
            }

            getActivity().runOnUiThread(() -> {
                bridge.getActivity().addContentView(publisher.getView(), params);
            });

            session.publish(publisher);

            Logger.debug("Session onConnected");
        }

        @Override
        public void onDisconnected(Session session) {
            Log.d(TAG, "onDisconnected: Disconnected from session: " + session.getSessionId());
            removeViews(true);
            Logger.debug("Session onDisconnected");
        }

        @Override
        public void onStreamReceived(Session session, Stream stream) {
            Log.d(TAG, "onStreamReceived: New Stream Received " + stream.getStreamId() + " in session: " + session.getSessionId());

            if (subscriber == null) {
                subscriber = new Subscriber.Builder(getActivity(), stream).build();
                subscriber.getRenderer().setStyle(BaseVideoRenderer.STYLE_VIDEO_SCALE, BaseVideoRenderer.STYLE_VIDEO_FILL);
                subscriber.setSubscriberListener(subscriberListener);
                session.subscribe(subscriber);

                int heightPixels = (int) (getHeightScreen() * 0.9) - 36;
                FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, heightPixels);

                getActivity().runOnUiThread(() -> {
                    bridge.getActivity().addContentView(subscriber.getView(), params);
                });
            }

            Logger.debug("Session onStreamReceived");
        }

        @Override
        public void onStreamDropped(Session session, Stream stream) {
            Log.d(TAG, "onStreamDropped: Stream Dropped: " + stream.getStreamId() + " in session: " + session.getSessionId());
            Logger.debug("Session onStreamDropped");
            removeViews(false);
            call.resolve();
        }

        @Override
        public void onError(Session session, OpentokError opentokError) {
            Log.e(TAG, "Session error: " + opentokError.getMessage());
            Logger.debug("Session onError");
            call.reject("Erro ao conectar sessão", opentokError.getException());
        }
    };

    private PublisherKit.PublisherListener publisherListener = new PublisherKit.PublisherListener() {
        @Override
        public void onStreamCreated(PublisherKit publisherKit, Stream stream) {
            Log.d(TAG, "onStreamCreated: Publisher Stream Created. Own stream " + stream.getStreamId());
            Logger.debug("Publisher onStreamCreated");
        }

        @Override
        public void onStreamDestroyed(PublisherKit publisherKit, Stream stream) {
            Log.d(TAG, "onStreamDestroyed: Publisher Stream Destroyed. Own stream " + stream.getStreamId());
            Logger.debug("Publisher onStreamDestroyed");

        }

        @Override
        public void onError(PublisherKit publisherKit, OpentokError opentokError) {
            Log.e(TAG, "PublisherKit onError: " + opentokError.getMessage());
            Logger.debug("Publisher onError");
        }
    };

    private SubscriberKit.SubscriberListener subscriberListener = new SubscriberKit.SubscriberListener() {
        @Override
        public void onConnected(SubscriberKit subscriberKit) {
            Log.d(TAG, "onConnected: Subscriber connected. Stream: " + subscriberKit.getStream().getStreamId());
            Logger.debug("Subscriber onConnected");
        }

        @Override
        public void onDisconnected(SubscriberKit subscriberKit) {
            Log.d(TAG, "onDisconnected: Subscriber disconnected. Stream: " + subscriberKit.getStream().getStreamId());
            Logger.debug("Subscriber onDisconnected");
        }

        @Override
        public void onError(SubscriberKit subscriberKit, OpentokError opentokError) {
            Log.e(TAG, "SubscriberKit onError: " + opentokError.getMessage());
            Logger.debug("Subscriber onError");
        }
    };

    @PluginMethod
    public void echo(PluginCall call) {
        String value = call.getString("value");

        JSObject ret = new JSObject();
        ret.put("value", value);
        call.resolve(ret);
    }

    private void removeViews(boolean clear) {
        getActivity().runOnUiThread(() -> {
            try {
                ((ViewGroup) publisher.getView().getParent()).removeView(publisher.getView());
                ((ViewGroup) subscriber.getView().getParent()).removeView(subscriber.getView());
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
                e.printStackTrace();
            }
            if (clear) publisher = null;
        });
    }

    private int getHeightScreen() {
        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size.y;
    }
}
