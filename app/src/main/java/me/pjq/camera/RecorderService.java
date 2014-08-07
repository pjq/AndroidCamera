package me.pjq.camera;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.hardware.Camera;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.view.SurfaceHolder;

import java.io.File;
import java.io.IOException;

/**
 * Created by pengjianqing on 8/7/14.
 */

public class RecorderService extends Service {
    private SurfaceHolder mSurfaceHolder;
    private static Camera camera;
    private static CameraPreview cameraPreview;
    public static boolean recordingStatus;
    private MediaRecorder mediaRecorder;
    private int cameraType = 0;

    @Override
    public void onCreate() {
        recordingStatus = false;
        camera = CameraActivity.camera;
        mSurfaceHolder = CameraActivity.surfaceView.getHolder();
        pictureCallBack = new DefaultPictureCallBack(getApplicationContext());

        super.onCreate();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        int command = intent.getIntExtra(Constants.COMMAND, -1);

        switch (command) {
            case Constants.COMMAND_TAKE_PICTURE:
                handler.sendEmptyMessageDelayed(0, 3000);
                break;
            case Constants.COMMAND_TAKE_VIDEO:
                if (recordingStatus == false) {
                    startRecording();
                }
                break;

            case Constants.COMMAND_STOP_TAKE_VIDEO:
                stopRecording();
                break;
            case Constants.COMMAND_STOP_TAKE_PICTURE:
                break;
            case Constants.COMMAND_STOP_SERVICE:
                stopSelf();
                break;
        }

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        stopRecording();
        if (null != camera) {
            camera.release();
        }
        camera = null;
        recordingStatus = false;

        super.onDestroy();
    }

    private DefaultPictureCallBack pictureCallBack;

    private void takePicture() {
        try {
            if (null == camera) {
                camera = CameraActivity.getCameraInstance();
            }
//            SurfaceView surfaceView = new SurfaceView(this);
            camera.setPreviewDisplay(mSurfaceHolder);
            camera.startPreview();

            try {
                camera.startPreview();
                camera.takePicture(null, null, pictureCallBack);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    Handler handler = new Handler() {
        @Override
        public void dispatchMessage(Message msg) {
            takePicture();
        }
    };


    @SuppressLint("NewApi")
    public boolean startRecording() {
        try {
            recordingStatus = true;

//            camera = Camera.open(cameraType);
            if (null == camera) {
                camera = CameraActivity.getCameraInstance();
            }
            camera.setPreviewDisplay(mSurfaceHolder);
            camera.setDisplayOrientation(90);
            Camera.Parameters p = camera.getParameters();
            camera.setParameters(p);
            camera.unlock();

            mediaRecorder = new MediaRecorder();
            mediaRecorder.setCamera(camera);
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
            mediaRecorder.setVideoSource(MediaRecorder.VideoSource.DEFAULT);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);

            String uniqueOutFile = LocalPathResolver.getVideoDir();
            File outFile = new File(uniqueOutFile);
            if (outFile.exists()) {
                outFile.delete();
            }

            mediaRecorder.setOutputFile(uniqueOutFile);

            mediaRecorder.setVideoSize(350, 250);
            mediaRecorder.setPreviewDisplay(mSurfaceHolder.getSurface());
            mediaRecorder.setOrientationHint(90);

            mediaRecorder.prepare();
            mediaRecorder.start();

            return true;
        } catch (IllegalStateException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Stop service
    public void stopRecording() {
        try {
            if (null != mediaRecorder) {
                mediaRecorder.stop();
                mediaRecorder.release();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

