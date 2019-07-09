package com.app.amber.chat;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.Vibrator;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.app.amber.chat.service.socket_events_listener;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.Socket;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.AudioSource;
import org.webrtc.AudioTrack;
import org.webrtc.Camera1Enumerator;
import org.webrtc.Camera2Enumerator;
import org.webrtc.CameraEnumerator;
import org.webrtc.CameraVideoCapturer;
import org.webrtc.DataChannel;
import org.webrtc.EglBase;
import org.webrtc.IceCandidate;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.RtpReceiver;
import org.webrtc.SdpObserver;
import org.webrtc.SessionDescription;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoRenderer;
import org.webrtc.VideoSource;
import org.webrtc.VideoTrack;

import java.util.ArrayList;


import static org.webrtc.SessionDescription.Type.ANSWER;
import static org.webrtc.SessionDescription.Type.OFFER;

public class video_call extends AppCompatActivity {
    private EglBase rootEglBase;
    private PeerConnectionFactory factory;
    private VideoTrack videoTrackFromCamera;
    AudioTrack localAudioTrack;
    private static final String TAG = "SamplePeerConnectionAct";
    public static final int VIDEO_RESOLUTION_WIDTH = 1280;
    public static final int VIDEO_RESOLUTION_HEIGHT = 720;
    public static final int FPS = 30;
    private static final String DTLS_SRTP_KEY_AGREEMENT_CONSTRAINT = "DtlsSrtpKeyAgreement";
    private PeerConnection localPeerConnection, remotePeerConnection;

    private SurfaceViewRenderer localView, remoteView;
    private Socket mSocket;
    VideoCapturer videoCapturer;
    ImageButton button_call_disconnect, cancel_request, speaker;
    String ownId, otherId;
    boolean isCaller;
    AudioManager mAudioMgr;
    TextView calling, before_calling, callType;
    RelativeLayout after_accept, before_accept;
    Button accept, reject;
    Handler handler;
    Runnable runnable;
    boolean isAudio = false, is_speaker_on = false;
    private Vibrator vib;
    FrameLayout own_video_container;
    MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_call);
        localView = (SurfaceViewRenderer) findViewById(R.id.gl_surface_local);
        remoteView = (SurfaceViewRenderer) findViewById(R.id.gl_surface_remote);
        calling = (TextView) findViewById(R.id.calling);
        callType = (TextView) findViewById(R.id.type);
        before_calling = (TextView) findViewById(R.id.before_calling);
        after_accept = (RelativeLayout) findViewById(R.id.after_accept);
        before_accept = (RelativeLayout) findViewById(R.id.before_accept);
        accept = (Button) findViewById(R.id.accept);
        reject = (Button) findViewById(R.id.reject);
        cancel_request = (ImageButton) findViewById(R.id.cancel_request);
        speaker = (ImageButton) findViewById(R.id.speaker);
        handler = new Handler();
        mAudioMgr = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        own_video_container = (FrameLayout) findViewById(R.id.own_video_container);

        own_video_container.setVisibility(View.VISIBLE);

        socket_events_listener.isBusy = true;
        speaker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                is_speaker_on = !is_speaker_on;
                if (is_speaker_on) {

                    speaker.setImageDrawable(getResources().getDrawable(R.drawable.ic_speaker_on_24dp));

                    handler.removeCallbacks(runnable);
                    runnable = new Runnable() {
                        @Override
                        public void run() {
                            mAudioMgr.setMode(AudioManager.MODE_IN_CALL);
                            if (!mAudioMgr.isSpeakerphoneOn())
                                mAudioMgr.setSpeakerphoneOn(true);
                        }
                    };
                    handler.postDelayed(runnable, 100);
                    Toast.makeText(getApplicationContext(), "Speaker is on", Toast.LENGTH_SHORT).show();

                } else {
                    speaker.setImageDrawable(getResources().getDrawable(R.drawable.ic_speaker_off_24dp));
                    handler.removeCallbacks(runnable);
                    runnable = new Runnable() {
                        @Override
                        public void run() {
                            mAudioMgr.setMode(AudioManager.MODE_IN_CALL);
                            mAudioMgr.setSpeakerphoneOn(false);
                        }
                    };
                    handler.postDelayed(runnable, 100);

                    Toast.makeText(getApplicationContext(), "Speaker is off", Toast.LENGTH_SHORT).show();
                }
            }
        });
        cancel_request.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("id", otherId);
                    jsonObject.put("ownId", ownId);
                    jsonObject.put("is_audio", isAudio);

                    if (!mSocket.connected()) {
                        // mSocket.connect();
                    }

                    mSocket.emit("end_call", jsonObject);



                    if(localPeerConnection!=null)
                        localPeerConnection.close();

                    if(remotePeerConnection!=null){
                        remotePeerConnection.close();
                    }


                    if(localView!=null){
                        localView.release();
                    }



                    if(remoteView!=null){
                        remoteView.release();

                    }
                    if (!isAudio && videoCapturer!=null)
                    {
                        videoCapturer.stopCapture();
                        videoCapturer.dispose();
                    }


                    if(rootEglBase!=null)
                        rootEglBase.release();


                    socket_events_listener.isBusy = false;
                    finish();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });


        accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               /* initializeSurfaceViews();

                initializePeerConnectionFactory();

                createVideoTrackFromCameraAndShowIt();

                initializePeerConnections();

                startStreamingVideo();*/
                socket_events_listener.cancelNotification(getApplicationContext(), socket_events_listener.endCallNotificationId);

                final JSONObject jsonObject = new JSONObject();

                final Handler handler = new Handler();
                final Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        try {
                            jsonObject.put("id", ownId);
                            jsonObject.put("ownId", otherId);
                            jsonObject.put("is_audio", isAudio);

                            if (!mSocket.connected()) {
                                //mSocket.connect();
                            }
                            mSocket.emit("ready_for_initiate", jsonObject);

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    calling.setVisibility(View.GONE);
                                    cancel_request.setVisibility(View.GONE);
                                    /*if(mediaPlayer.isPlaying()){
                                        mediaPlayer.stop();
                                    }
                                    vib.cancel();
                                    */
                                    socket_events_listener.StopRinging();

                                    before_accept.setVisibility(View.GONE);
                                    after_accept.setVisibility(View.VISIBLE);
                                    localView.setVisibility(View.VISIBLE);
                                }
                            });
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                };
                handler.postDelayed(runnable, 1000);
            }
        });

        reject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("id", otherId);
                    jsonObject.put("ownId", ownId);
                    jsonObject.put("is_audio", isAudio);

                    if (!mSocket.connected()) {
                        //mSocket.connect();
                    }

                    if (!mSocket.connected()) {
                        //mSocket.connect();
                    }
                    mSocket.emit("end_call", jsonObject);

                    if(localPeerConnection!=null)
                    localPeerConnection.close();

                    if(localView!=null){
                        localView.release();
                    }


                    if(remotePeerConnection!=null){
                        remotePeerConnection.close();
                    }
                    if (!isAudio && videoCapturer!=null)
                    {
                        videoCapturer.stopCapture();
                        videoCapturer.dispose();
                    }

                    if(rootEglBase!=null)
                    rootEglBase.release();
                    socket_events_listener.isBusy = false;


                    /*if(mediaPlayer.isPlaying()){
                        mediaPlayer.stop();
                    }
                    vib.cancel();
                    */
                    socket_events_listener.StopRinging();


                    socket_events_listener.cancelNotification(getApplicationContext(), socket_events_listener.endCallNotificationId);
                    finish();

                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        button_call_disconnect = (ImageButton) findViewById(R.id.button_call_disconnect);

        application app = (application) getApplicationContext();
        mSocket = app.getmSocket();


        Bundle bundle = getIntent().getExtras();
        ownId = bundle.getString("ownId");
        otherId = bundle.getString("otherId");

        isCaller = bundle.getBoolean("isCaller");
        isAudio = bundle.getBoolean("is_audio");
        if (isCaller) {
            calling.setText("Calling " + otherId + "  ...");
            if (isAudio) {
                callType.setText("Audio Call");
            } else {
                callType.setText("Video Call");
            }

        } else {

            /*mediaPlayer=MediaPlayer.create(this,R.raw.callring);
            vib = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            vib.vibrate(2000);
            AudioManager audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 5, 0);
            mediaPlayer.start();
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    mediaPlayer.seekTo(0);
                    mediaPlayer.start();
                }
            });
            */

            calling.setVisibility(View.GONE);
            cancel_request.setVisibility(View.GONE);
            before_accept.setVisibility(View.VISIBLE);

            after_accept.setVisibility(View.GONE);
            before_calling.setText(otherId + " is calling");
            if (isAudio) {
                callType.setText("Audio Call");
            } else {
                callType.setText("Video Call");
            }
        }
        /*initializeSurfaceViews();

        initializePeerConnectionFactory();

        createVideoTrackFromCameraAndShowIt();

        initializePeerConnections();

        startStreamingVideo();
*/
        final int PERMISSION_ALL = 1;
        final String[] PERMISSIONS = {
                Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO

        };

        if (!chat.hasPermissions(this, PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
        } else {
            initializeSurfaceViews();
            initializePeerConnectionFactory();
            createVideoTrackFromCameraAndShowIt();
            initializePeerConnections();
            startStreamingVideo();


            final JSONObject jsonObject = new JSONObject();

            try {
                jsonObject.put("id", otherId);
                jsonObject.put("ownId", ownId);
                jsonObject.put("is_audio", isAudio);
                jsonObject.put("notify_id", socket_events_listener.createID());
                if (isCaller) {

                    if (!mSocket.connected()) {
                        //mSocket.connect();
                    }
                    mSocket.emit("ready_for_call", jsonObject);
                } /*else {
                    final Handler handler = new Handler();
                    final Runnable runnable = new Runnable() {
                        @Override
                        public void run() {
                            try {
                                jsonObject.put("id", ownId);
                                jsonObject.put("ownId", otherId);

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            mSocket.emit("ready_for_initiate", jsonObject);
                        }
                    };
                    handler.postDelayed(runnable, 1000);
                }*/
            } catch (JSONException e) {
                e.printStackTrace();
            }

            if (mSocket != null) {
                if (!mSocket.connected()) {
                    //mSocket.disconnect();
                    //mSocket.connect();
                }


                mSocket.on("offer" + ownId, handleOfferReceived);
                mSocket.on("answer" + ownId, handleAnswerReceived);
                mSocket.on("ice_candidate" + ownId, handleCandidatesReceived);
                mSocket.on("ready_for_call" + ownId, handleReadyForCall);
                mSocket.on("ready_for_initiate" + ownId, handleReadyForInitiate);
                mSocket.on("end_call" + ownId, endCall);
            }


            button_call_disconnect.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    /*localPeerConnection.close();

                    if(!isAudio) {
                        videoCapturer.dispose();
                    }
                    if(rootEglBase.hasSurface())
                    rootEglBase.release();
                    */

                    if (mSocket != null) {
                        mSocket.off("offer" + ownId, handleOfferReceived);
                        mSocket.off("answer" + ownId, handleAnswerReceived);
                        mSocket.off("ice_candidate" + ownId, handleCandidatesReceived);
                        mSocket.off("ready_for_call" + ownId, handleReadyForCall);
                        mSocket.off("ready_for_initiate" + ownId, handleReadyForInitiate);
                        mSocket.off("end_call" + ownId, endCall);
                    }



                    if(localPeerConnection!=null)
                        localPeerConnection.close();

                    if(localView!=null){
                        localView.release();
                    }


                    if(remotePeerConnection!=null){
                        remotePeerConnection.close();
                    }
                    if (!isAudio && videoCapturer!=null)
                    {
                        try {
                            videoCapturer.stopCapture();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        videoCapturer.dispose();
                    }

                    if(rootEglBase!=null)
                        rootEglBase.release();


                    socket_events_listener.StopRinging();
                    socket_events_listener.isBusy = false;
                    // remotePeerConnection.dispose();
                    if (mSocket != null) {
                        mSocket.off("offer" + ownId, handleOfferReceived);
                        mSocket.off("answer" + ownId, handleAnswerReceived);
                        mSocket.off("ice_candidate" + ownId, handleCandidatesReceived);
                        mSocket.off("ready_for_call" + ownId, handleReadyForCall);
                        mSocket.off("ready_for_initiate" + ownId, handleReadyForInitiate);
                        mSocket.off("end_call" + ownId, endCall);
                    }

                    JSONObject jsonObject = new JSONObject();
                    try {
                        jsonObject.put("id", otherId);
                        jsonObject.put("ownId", ownId);

                        if (!mSocket.connected()) {
                            // mSocket.connect();
                        }
                        mSocket.emit("end_call", jsonObject);
                    } catch (Exception e) {
                        System.out.println("exception in sending endcall = " + e.toString());
                    }
                    application app = (application) getApplicationContext();
                    //startActivity(new Intent(video_call.this, users.class).putExtra("id", app.getUser_id()).putExtra("username", app.getUsername()));
                    finish();
                }
            });
        }

    }


    private void initializeSurfaceViews() {
        rootEglBase = EglBase.create();
        localView.init(rootEglBase.getEglBaseContext(), null);
        localView.setEnableHardwareScaler(true);
        localView.setMirror(true);

        remoteView.init(rootEglBase.getEglBaseContext(), null);
        remoteView.setEnableHardwareScaler(true);
        remoteView.setMirror(false);
    }

    private void initializePeerConnectionFactory() {

        if (isAudio) {
            PeerConnectionFactory.initializeAndroidGlobals(this, true, false, false);
        } else {
            PeerConnectionFactory.initializeAndroidGlobals(this, true, true, true);
        }
        factory = new PeerConnectionFactory(null);
        factory.setVideoHwAccelerationOptions(rootEglBase.getEglBaseContext(), rootEglBase.getEglBaseContext());
    }

    private void createVideoTrackFromCameraAndShowIt() {

        if (!isAudio) {
            videoCapturer = createVideoCapturer();
            VideoSource videoSource = factory.createVideoSource(videoCapturer);
            videoCapturer.startCapture(VIDEO_RESOLUTION_WIDTH, VIDEO_RESOLUTION_HEIGHT, FPS);

            videoTrackFromCamera = factory.createVideoTrack("ARDAMSv0", videoSource);
            videoTrackFromCamera.setEnabled(true);
            videoTrackFromCamera.addRenderer(new VideoRenderer(localView));

        }
        AudioSource audioSource = factory.createAudioSource(new MediaConstraints());
        localAudioTrack = factory.createAudioTrack("101", audioSource);
    }

    private void initializePeerConnections() {
        localPeerConnection = createPeerConnection(factory);
    }

    private void startStreamingVideo() {
        MediaStream mediaStream = factory.createLocalMediaStream("ARDAMS");
        if (!isAudio)
            mediaStream.addTrack(videoTrackFromCamera);
        mediaStream.addTrack(localAudioTrack);
        localPeerConnection.addStream(mediaStream);

    }


    private void createOffer() {
        final MediaConstraints sdpMediaConstraints = new MediaConstraints();

        localPeerConnection.createOffer(new SimpleSdpObserver() {
            @Override
            public void onCreateSuccess(SessionDescription sessionDescription) {
                Log.d("Offer create success", "onCreateSuccess: ");
                localPeerConnection.setLocalDescription(new SimpleSdpObserver(), sessionDescription);

                JSONObject jsonObject = new JSONObject();
                try {

                    ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
                    String stringifyCandidate = ow.writeValueAsString(sessionDescription);
                    jsonObject.put("id", otherId);
                    jsonObject.put("offer", stringifyCandidate);
                    jsonObject.put("ownId", ownId);
                    jsonObject.put("isFrom", "android");
                    jsonObject.put("description", (sessionDescription.description).toString());

                    if (!mSocket.connected()) {
                        //mSocket.connect();
                    }
                    mSocket.emit("offer", jsonObject);
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }

            }
        }, sdpMediaConstraints);
    }


    private void createAnswer() {
        localPeerConnection.createAnswer(new SimpleSdpObserver() {
            @Override
            public void onCreateSuccess(SessionDescription answerSessionDescription) {
                localPeerConnection.setLocalDescription(new SimpleSdpObserver(), answerSessionDescription);
                JSONObject jsonObject = new JSONObject();
                try {

                    System.out.println("description = " + answerSessionDescription.description);
                    ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
                    String stringifyCandidate = ow.writeValueAsString(answerSessionDescription);
                    jsonObject.put("id", otherId);
                    jsonObject.put("answer", stringifyCandidate);
                    jsonObject.put("ownId", ownId);
                    jsonObject.put("isFrom", "android");
                    jsonObject.put("description", (answerSessionDescription.description).toString());


                    if (!mSocket.connected()) {
                        //mSocket.connect();
                    }
                    mSocket.emit("answer", jsonObject);
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
            }
        }, new MediaConstraints());
    }


    private PeerConnection createPeerConnection(PeerConnectionFactory factory) {
        ArrayList<PeerConnection.IceServer> iceServers = new ArrayList<>();
        //iceServers.add(new PeerConnection.IceServer("stun:stun.awa-shima.com:3478"));
        iceServers.add(new PeerConnection.IceServer("turn:3.210.76.112:3478", "kurento", "kurentopw"));
        PeerConnection.RTCConfiguration rtcConfig = new PeerConnection.RTCConfiguration(iceServers);
        MediaConstraints pcConstraints = new MediaConstraints();

        PeerConnection.Observer pcObserver = new PeerConnection.Observer() {
            @Override
            public void onSignalingChange(PeerConnection.SignalingState signalingState) {
                Log.d(TAG, "onSignalingChange: ");
            }

            @Override
            public void onIceConnectionChange(PeerConnection.IceConnectionState iceConnectionState) {
                Log.d(TAG, "onIceConnectionChange: ");
            }

            @Override
            public void onIceConnectionReceivingChange(boolean b) {
                Log.d(TAG, "onIceConnectionReceivingChange: ");
            }

            @Override
            public void onIceGatheringChange(PeerConnection.IceGatheringState iceGatheringState) {
                Log.d(TAG, "onIceGatheringChange: ");
            }

            @Override
            public void onIceCandidate(IceCandidate iceCandidate) {
                Log.d(TAG, "onIceCandidate: ");
                System.out.println("sending candidate to server" + iceCandidate);
                JSONObject jsonObject = new JSONObject();
                try {

                    ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
                    String stringifyCandidate = ow.writeValueAsString(iceCandidate);

                    jsonObject.put("id", otherId);
                    jsonObject.put("candidate", stringifyCandidate);
                    jsonObject.put("sdp", iceCandidate.sdp);
                    jsonObject.put("sdpMid", iceCandidate.sdpMid);
                    jsonObject.put("sdpMLineIndex", iceCandidate.sdpMLineIndex);
                    jsonObject.put("ownId", ownId);
                    jsonObject.put("isFrom", "android");

                    if (!mSocket.connected()) {
                        //mSocket.connect();
                    }
                    mSocket.emit("ice_candidate", jsonObject);
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onIceCandidatesRemoved(IceCandidate[] iceCandidates) {
                Log.d(TAG, "onIceCandidatesRemoved: ");
            }

            @Override
            public void onAddStream(MediaStream mediaStream) {
                Log.d(TAG, "onAddStream: " + mediaStream.videoTracks.size());

                if (!isAudio) {
                    VideoTrack remoteVideoTrack = mediaStream.videoTracks.get(0);
                    remoteVideoTrack.setEnabled(true);
                    remoteVideoTrack.addRenderer(new VideoRenderer(remoteView));
                } else {
                    AudioTrack remoteVideoTrack = mediaStream.audioTracks.get(0);

                }

            }

            @Override
            public void onRemoveStream(MediaStream mediaStream) {
                Log.d(TAG, "onRemoveStream: ");
            }

            @Override
            public void onDataChannel(DataChannel dataChannel) {
                Log.d(TAG, "onDataChannel: ");
            }

            @Override
            public void onRenegotiationNeeded() {
                Log.d(TAG, "onRenegotiationNeeded: ");
            }

            @Override
            public void onAddTrack(RtpReceiver rtpReceiver, MediaStream[] mediaStreams) {

            }
        };

        return factory.createPeerConnection(rtcConfig, pcConstraints, pcObserver);
    }

    private VideoCapturer createVideoCapturer() {
        VideoCapturer videoCapturer;
        if (useCamera2()) {
            videoCapturer = createCameraCapturer(new Camera2Enumerator(this));
        } else {
            videoCapturer = createCameraCapturer(new Camera1Enumerator(true));
        }
        return videoCapturer;
    }

    private VideoCapturer createCameraCapturer(CameraEnumerator enumerator) {
        final String[] deviceNames = enumerator.getDeviceNames();

        // First, try to find front facing camera
        for (String deviceName : deviceNames) {
            if (enumerator.isFrontFacing(deviceName)) {
                videoCapturer = enumerator.createCapturer(deviceName, null);

                if (videoCapturer != null) {
                    return videoCapturer;
                }
            }
        }


        // Front facing camera not found, try something else
        for (String deviceName : deviceNames) {
            if (!enumerator.isFrontFacing(deviceName)) {
                videoCapturer = enumerator.createCapturer(deviceName, null);

                if (videoCapturer != null) {
                    return videoCapturer;
                }
            }
        }


        return null;
    }

    /*
     * Read more about Camera2 here
     * https://developer.android.com/reference/android/hardware/camera2/package-summary.html
     * */
    private boolean useCamera2() {
        return Camera2Enumerator.isSupported(this);
    }


    private Emitter.Listener handleOfferReceived = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            final JSONObject data = (JSONObject) args[0];
            try {
                // final String message = data.getString("message");
                System.out.println("offer received = " + data);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        try {
                            if (data.has("offer")) {
                                System.out.println("offer received = " + data);


                                final MediaConstraints sdpConstraints = new MediaConstraints();
                                sdpConstraints.mandatory.add(new MediaConstraints.KeyValuePair("offerToReceiveAudio", "true"));
                                sdpConstraints.mandatory.add(new MediaConstraints.KeyValuePair("offerToReceiveVideo", "true"));


                                SessionDescription sdpOffer = new SessionDescription(OFFER, data.getString("sdp"));
                                localPeerConnection.setRemoteDescription(new SimpleSdpObserver(), sdpOffer);
                                createAnswer();

                            }
                        } catch (Exception e) {
                            System.out.println("json exception = " + e.toString());
                        }
                    }
                });
            } catch (Exception e) {
                System.out.println("json exception = " + e.toString());
            }
        }
    };


    private Emitter.Listener handleCandidatesReceived = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            final JSONObject data = (JSONObject) args[0];
            try {
                // final String message = data.getString("message");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        try {
                            if (data.has("candidate")) {
                                System.out.println("candidates received = " + data);
                                Gson gson = new Gson();
                                IceCandidate iceCandidate = new IceCandidate(data.getString("sdpMid"),
                                        data.getInt("sdpMLineIndex"), data.getString("sdp"));

                                localPeerConnection.addIceCandidate(iceCandidate);
                                // nbmWebRTCPeer.processAnswer(sdpAnswer,data.getString("id"));
                            } else {
                                Toast.makeText(getApplicationContext(), "user name already exist", Toast.LENGTH_LONG).show();

                            }
                        } catch (JSONException e1) {
                            e1.printStackTrace();
                        }
                    }
                });

            } catch (Exception e) {
                System.out.println("json exception = " + e.toString());
            }
        }
    };


    private Emitter.Listener handleAnswerReceived = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            final JSONObject data = (JSONObject) args[0];
            try {
                // final String message = data.getString("message");
                System.out.println("answer received = " + data);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        try {
                            if (data.has("answer")) {
                                System.out.println("answer received = " + data);
                                Gson gson = new Gson();
                                SessionDescription sdpAnswer = new SessionDescription(ANSWER, data.getString("sdp"));
                                localPeerConnection.setRemoteDescription(new SdpObserver() {
                                    @Override
                                    public void onCreateSuccess(SessionDescription sessionDescription) {
                                        System.out.println("answer added successfully");

                                    }

                                    @Override
                                    public void onSetSuccess() {
                                        System.out.println("answer added successfully");
                                    }

                                    @Override
                                    public void onCreateFailure(String s) {
                                        System.out.println("answer added successfully failure");

                                    }

                                    @Override
                                    public void onSetFailure(String s) {
                                        System.out.println("answer added successfully failure");

                                    }
                                }, sdpAnswer);

                                // nbmWebRTCPeer.processAnswer(sdpAnswer,data.getString("id"));
                            } else {
                                Toast.makeText(getApplicationContext(), "user name already exist", Toast.LENGTH_LONG).show();

                            }
                        } catch (JSONException e1) {
                            e1.printStackTrace();
                        }
                    }
                });

            } catch (Exception e) {
                System.out.println("json exception = " + e.toString());
            }
        }
    };


    private Emitter.Listener handleReadyForCall = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            final JSONObject data = (JSONObject) args[0];
            try {
                // final String message = data.getString("message");
                System.out.println("readyforcall = " + data);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        try {
                            calling.setVisibility(View.GONE);
                            cancel_request.setVisibility(View.GONE);
                            before_accept.setVisibility(View.VISIBLE);
                            after_accept.setVisibility(View.GONE);
                            before_calling.setText(otherId);
                            if (isAudio) {
                                callType.setText("Audio Call");
                            } else {
                                callType.setText("Video Call");
                            }

                            /*initializeSurfaceViews();

                            initializePeerConnectionFactory();

                            createVideoTrackFromCameraAndShowIt();

                            initializePeerConnections();

                            startStreamingVideo();

                            final Handler handler=new Handler();
                            final Runnable runnable=new Runnable() {
                                @Override
                                public void run() {
                                    mSocket.emit("ready_for_initiate",data);
                                }
                            };
                            handler.postDelayed(runnable,1000);*/
                        } catch (Exception e1) {
                            e1.printStackTrace();
                        }
                    }
                });

            } catch (Exception e) {
                System.out.println("json exception = " + e.toString());
            }
        }
    };


    private Emitter.Listener handleReadyForInitiate = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            final JSONObject data = (JSONObject) args[0];
            try {
                // final String message = data.getString("message");
                System.out.println("handleReadyForInitiate  = " + data);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        try {

                            calling.setVisibility(View.GONE);
                            cancel_request.setVisibility(View.GONE);

                            after_accept.setVisibility(View.VISIBLE);
                            localView.setVisibility(View.VISIBLE);
                            before_accept.setVisibility(View.GONE);

                            createOffer();
                        } catch (Exception e1) {
                            e1.printStackTrace();
                        }
                    }
                });

            } catch (Exception e) {
                System.out.println("json exception = " + e.toString());
            }
        }
    };


    private Emitter.Listener endCall = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            final JSONObject data = (JSONObject) args[0];
            try {
                // final String message = data.getString("message");
                System.out.println("end call  = " + data);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        try {
                            //createOffer();

                            if (mSocket != null) {
                                mSocket.off("offer" + ownId, handleOfferReceived);
                                mSocket.off("answer" + ownId, handleAnswerReceived);
                                mSocket.off("ice_candidate" + ownId, handleCandidatesReceived);
                                mSocket.off("ready_for_call" + ownId, handleReadyForCall);
                                mSocket.off("ready_for_initiate" + ownId, handleReadyForInitiate);
                                mSocket.off("end_call" + ownId, endCall);
                            }



                            if(localPeerConnection!=null)
                                localPeerConnection.close();

                            if(localView!=null){
                                localView.release();
                            }


                            if(remotePeerConnection!=null){
                                remotePeerConnection.close();
                            }
                            if (!isAudio && videoCapturer!=null)
                            {
                                videoCapturer.stopCapture();
                                videoCapturer.dispose();
                            }


                            if(rootEglBase!=null)
                                rootEglBase.release();


                            //remotePeerConnection.dispose();
                            application app = (application) getApplicationContext();

                            socket_events_listener.isBusy = false;
                            socket_events_listener.StopRinging();
                            //startActivity(new Intent(video_call.this,users.class).putExtra("id",app.getUser_id()).putExtra("username",app.getUsername()));

                            finish();
                            startActivity(new Intent(video_call.this, users.class)
                                    .putExtra("id", app.getUser_id()).putExtra("username", app.getUsername())
                                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK));
                            //cancelNotification(getApplicationContext(),999);

                        } catch (Exception e1) {
                            e1.printStackTrace();
                        }
                    }
                });

            } catch (Exception e) {
                System.out.println("json exception = " + e.toString());
            }
        }
    };


    @Override
    protected void onResume() {
        super.onResume();


    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        socket_events_listener.isBusy = false;


        /*if(mediaPlayer.isPlaying()){
            mediaPlayer.stop();
        }
        vib.cancel();
        */

        if (mSocket != null) {
            mSocket.off("offer" + ownId, handleOfferReceived);
            mSocket.off("answer" + ownId, handleAnswerReceived);
            mSocket.off("ice_candidate" + ownId, handleCandidatesReceived);
            mSocket.off("ready_for_call" + ownId, handleReadyForCall);
            mSocket.off("ready_for_initiate" + ownId, handleReadyForInitiate);
            mSocket.off("end_call" + ownId, endCall);
        }


        if(localPeerConnection!=null)
            localPeerConnection.close();

        if(localView!=null){
            localView.release();
        }


        if(remotePeerConnection!=null){
            remotePeerConnection.close();
        }
        if (!isAudio && videoCapturer!=null)
        {
            try {
                videoCapturer.stopCapture();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            videoCapturer.dispose();
        }


        if(rootEglBase!=null)
            rootEglBase.release();



        socket_events_listener.isBusy = false;


        socket_events_listener.StopRinging();


        if (mSocket != null) {
            mSocket.off("offer" + ownId, handleOfferReceived);
            mSocket.off("answer" + ownId, handleAnswerReceived);
            mSocket.off("ice_candidate" + ownId, handleCandidatesReceived);
            mSocket.off("ready_for_call" + ownId, handleReadyForCall);
            mSocket.off("ready_for_initiate" + ownId, handleReadyForInitiate);
            mSocket.off("end_call" + ownId, endCall);
        }


        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("id", otherId);
            jsonObject.put("ownId", ownId);

            if (!mSocket.connected()) {
                //mSocket.connect();
            }
            mSocket.emit("end_call", jsonObject);
        } catch (Exception e) {
            System.out.println("exception in sending endcall = " + e.toString());
        }


        application app = (application) getApplicationContext();
        //startActivity(new Intent(video_call.this,users.class).putExtra("id",app.getUser_id()).putExtra("username",app.getUsername()));
        finish();

    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    System.out.println("");
                    //readContacts();read permission granted
                    //sendUserDataToSrver();
                } else {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                            Manifest.permission.CAMERA) && ActivityCompat.shouldShowRequestPermissionRationale(this,
                            Manifest.permission.RECORD_AUDIO)) {
                        new AlertDialog.Builder(this).
                                setTitle("Video calling Permission").
                                setMessage("You need to camera  permission to use this feature. Retry and grant it !").show();
                    } else {
                        new AlertDialog.Builder(this).
                                setTitle("Video calling Permission").
                                setMessage("You denied  permission." +
                                        " So, the feature will be disabled. To enable it" +
                                        ", go on settings and " +
                                        "grant read phone state permission for the application")
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        openSETTING();
                                    }
                                })
                                .setNegativeButton("Canel", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        Toast.makeText(getApplicationContext(), "Application will not work correctly without this permission.", Toast.LENGTH_LONG).show();
                                    }
                                }).show();
                    }
                }
                break;
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        /*if(mediaPlayer!=null && mediaPlayer.isPlaying()){
            mediaPlayer.stop();
        }
        if(vib!=null)
        vib.cancel();
        */


        handler.removeCallbacks(runnable);
        runnable = new Runnable() {
            @Override
            public void run() {
                mAudioMgr.setMode(AudioManager.MODE_IN_CALL);
                mAudioMgr.setSpeakerphoneOn(false);
            }
        };
        handler.postDelayed(runnable, 100);
    }

    @Override
    protected void onStop() {
        super.onStop();
        /*if(mediaPlayer!=null && mediaPlayer.isPlaying()){
            mediaPlayer.stop();
        }
        if(vib!=null)
        vib.cancel();
        */


        //socket_events_listener.StopRinging();

        handler.removeCallbacks(runnable);
        runnable = new Runnable() {
            @Override
            public void run() {
                mAudioMgr.setMode(AudioManager.MODE_IN_CALL);
                mAudioMgr.setSpeakerphoneOn(false);
            }
        };
        handler.postDelayed(runnable, 100);
    }


    public void openSETTING() {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        startActivity(intent);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.video_call, menu);


        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.switch_camera:
                switchCamera();
                break;


        }

        return true;
    }


    private void switchCamera() {
        if (videoCapturer != null) {
            if (videoCapturer instanceof CameraVideoCapturer) {
                CameraVideoCapturer cameraVideoCapturer =
                        (CameraVideoCapturer) videoCapturer;
                cameraVideoCapturer.switchCamera(null);
            } else {
            }
        }
    }
}
