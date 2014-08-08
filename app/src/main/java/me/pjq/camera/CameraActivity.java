package me.pjq.camera;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.*;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import at.markushi.ui.CircleButton;
import me.pjq.camera.util.EFLogger;
import me.pjq.camera.util.LocalPathResolver;
import me.pjq.camera.util.NotificationUtil;
import me.pjq.camera.util.PreferenceUtil;

public class CameraActivity extends FragmentActivity implements View.OnClickListener {
    private static final String TAG = CameraActivity.class.getSimpleName();
    public static Camera camera;
    public static CameraPreview cameraPreview;
    private View root;
    private CircleButton captureButton;
    private CircleButton recordButton;
    private CircleButton switchButton;
    private DefaultPictureCallBack pictureCallback;
    private int cameraId = 0;
    private PreferenceUtil preferenceUtil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_activity_camera);
        root = findViewById(R.id.root);

        LocalPathResolver.init(getApplicationContext());
        preferenceUtil = PreferenceUtil.getInstance();

        if (!checkCameraHardware(getApplicationContext())) {
            finish();
            return;
        }

        init();

        registerMyReceiver();
    }

    private void init() {
        camera = getCameraInstance();
        cameraPreview = new CameraPreview(this, camera);

        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        preview.addView(cameraPreview);

        captureButton = (CircleButton) findViewById(R.id.button_capture);
        recordButton = (CircleButton) findViewById(R.id.button_record);
        switchButton = (CircleButton) findViewById(R.id.button_switch);
        captureButton.setOnClickListener(this);
        recordButton.setOnClickListener(this);
        switchButton.setOnClickListener(this);

        pictureCallback = new DefaultPictureCallBack(getApplicationContext());

        switchButtonMoveListener = new MoveTouchListener(root);
        captureButtonMoveListener = new MoveTouchListener(root);
        switchButton.setOnTouchListener(switchButtonMoveListener);
        captureButton.setOnTouchListener(captureButtonMoveListener);

        restoreLayout();
    }

    private void restoreLayout() {
        if (0 == preferenceUtil.getCaptureX() || 0 == preferenceUtil.getCaptureY()) {

        } else {
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) captureButton.getLayoutParams();
            params = new RelativeLayout.LayoutParams(params.width, params.height);
            params.leftMargin = preferenceUtil.getCaptureX();
            params.topMargin = preferenceUtil.getCaptureY();
            captureButton.setLayoutParams(params);
        }
        if (0 == preferenceUtil.getSwitchX() || 0 == preferenceUtil.getSwitchY()) {

        } else {
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) switchButton.getLayoutParams();
            params = new RelativeLayout.LayoutParams(params.width, params.height);
            params.leftMargin = preferenceUtil.getSwitchX();
            params.topMargin = preferenceUtil.getSwitchY();
            switchButton.setLayoutParams(params);
        }
    }

    private MoveTouchListener switchButtonMoveListener;
    private MoveTouchListener captureButtonMoveListener;

    public static class MoveTouchListener implements View.OnTouchListener {
        float x;
        float y;
        float dx = 0;
        float dy = 0;
        View root;

        public float lastX;
        public float lastY;

        public MoveTouchListener(View view) {
            root = view;
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN: {
                    x = event.getX();
                    y = event.getY();
                    dx = x - v.getX();
                    dy = y - v.getY();
//                    EFLogger.i(TAG, "ACTION_DOWN, x = " + x + ", y = " + y);
                }
                break;

                case MotionEvent.ACTION_MOVE: {
                    lastX = event.getX() - dx;
                    lastY = event.getY() - dy;
                    v.setX(event.getX() - dx);
                    v.setY(event.getY() - dy);
                    x = event.getX();
                    y = event.getY();
//                    EFLogger.i(TAG, "ACTION_MOVE, x = " + lastX + ", y = " + lastY);
                    root.requestLayout();
                }
                break;

                case MotionEvent.ACTION_UP: {
                    root.invalidate();
                    EFLogger.i(TAG, "ACTION_MOVE, x = " + lastX + ", y = " + lastY);
                }
                break;

                default:
                    break;

            }
            return false;
        }

    }

    ;

    private boolean checkCameraHardware(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            return true;
        } else {
            return false;
        }
    }

    public static Camera getCameraInstance() {
        Camera c = null;

        try {
            int id = findFrontFacingCamera(Camera.CameraInfo.CAMERA_FACING_BACK);
            if (id < 0) {
                c = Camera.open();
            } else {
                c = Camera.open(id);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return c;
    }

    private boolean safeCameraOpen(int id) {
        boolean open = false;

        try {
            releaseCameraAndPreview();
            if (id < 0) {
                camera = Camera.open();
            } else {
                camera = Camera.open(id);
            }
            open = null != camera;
            cameraId = id;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return open;
    }

    boolean isFront = false;

    private void cameraSwitch() {
        int facing;
        if (isFront) {
            facing = Camera.CameraInfo.CAMERA_FACING_BACK;
        } else {
            facing = Camera.CameraInfo.CAMERA_FACING_FRONT;
        }

        int cameraId = findFrontFacingCamera(facing);
        openCamera(cameraId);
        isFront = !isFront;
    }

    private void openCamera(int id) {
        boolean result = safeCameraOpen(id);
        if (result) {
            cameraPreview.setCamera(camera);
        }
    }

    private void releaseCameraAndPreview() {
        cameraPreview.setCamera(null);

        if (null != camera) {
            camera.release();
            camera = null;
        }
    }

    public static int findFrontFacingCamera(int facing) {
        int cameraId = -1;
        // Search for the front facing camera
        int numberOfCameras = Camera.getNumberOfCameras();
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == facing) {
                cameraId = i;
                break;
            }
        }
        return cameraId;
    }

    private void takePicture() {
        if (pictureCallback.isProcessing()) {
            Toast.makeText(getApplicationContext(), "Please waiting...", Toast.LENGTH_SHORT).show();
        } else {
            try {
                camera.takePicture(null, null, pictureCallback);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private boolean isRecording = false;

    private void recordVideo() {
        if (isRecording) {
            handleAction(Constants.ACTION_STOP_TAKE_VIDEO);
        } else {
            handleAction(Constants.ACTION_TAKE_VIDEO);
        }

        isRecording = !isRecording;
        updateRecordViewStatus(isRecording);
    }

    private void updateRecordViewStatus(boolean isRecording) {
        if (isRecording) {
            recordButton.setImageResource(R.drawable.ic_record);
        } else {
            recordButton.setImageResource(R.drawable.ic_action_tick);
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        switch (id) {
            case R.id.button_capture: {
                takePicture();
            }
            break;

            case R.id.button_switch:
                cameraSwitch();
                break;

            case R.id.button_record:
                recordVideo();
                break;

            default:
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        openCamera(cameraId);
        NotificationUtil.dismissNotification(getApplicationContext());
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    protected void onPause() {
        super.onPause();

        releaseCameraAndPreview();
        showNotification();
    }

    private void showNotification() {
        NotificationUtil.showNotification(getApplicationContext(), CameraActivity.class, "Camera On", "Click to show details");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != camera) {
            camera.release();
        }
        NotificationUtil.dismissNotification(getApplicationContext());
        unregisterReceiver(broadcastReceiver);

        Intent intent = new Intent();
        intent.setClass(this, RecorderService.class);
        intent.putExtra(Constants.COMMAND, Constants.COMMAND_STOP_SERVICE);
        startService(intent);

        if (captureButtonMoveListener.lastX == 0 || captureButtonMoveListener.lastY == 0) {

        } else {
            preferenceUtil.setCaptureX((int) captureButtonMoveListener.lastX);
            preferenceUtil.setCaptureY((int) captureButtonMoveListener.lastY);
        }
        if (switchButtonMoveListener.lastX == 0 || switchButtonMoveListener.lastY == 0) {

        } else {
            preferenceUtil.setSwitchX((int) switchButtonMoveListener.lastX);
            preferenceUtil.setSwitchY((int) switchButtonMoveListener.lastY);
        }

//        android.os.Process.killProcess(Process.myPid());
    }

    private void registerMyReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addCategory(Intent.CATEGORY_DEFAULT);
        intentFilter.addAction(Constants.ACTION_TAKE_PICTURE);
        intentFilter.addAction(Constants.ACTION_TAKE_VIDEO);

        registerReceiver(broadcastReceiver, intentFilter);
    }

    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            handleAction(action);
        }
    };

    private void handleAction(String action) {
        if (!TextUtils.isEmpty(action)) {
            if (action.equalsIgnoreCase(Constants.ACTION_TAKE_PICTURE)) {
                Intent intent = new Intent();
                intent.setClass(this, RecorderService.class);
                intent.putExtra(Constants.COMMAND, Constants.COMMAND_TAKE_PICTURE);
                startService(intent);
            } else if (action.equalsIgnoreCase(Constants.ACTION_TAKE_VIDEO)) {
                Intent intent = new Intent();
                intent.setClass(this, RecorderService.class);
                intent.putExtra(Constants.COMMAND, Constants.COMMAND_TAKE_VIDEO);
                startService(intent);
            } else if (action.equalsIgnoreCase(Constants.ACTION_STOP_TAKE_VIDEO)) {
                Intent intent = new Intent();
                intent.setClass(this, RecorderService.class);
                intent.putExtra(Constants.COMMAND, Constants.COMMAND_STOP_TAKE_VIDEO);
                startService(intent);
            } else if (action.equalsIgnoreCase(Constants.ACTION_STOP_TAKE_PICTURE)) {
                Intent intent = new Intent();
                intent.setClass(this, RecorderService.class);
                intent.putExtra(Constants.COMMAND, Constants.COMMAND_STOP_TAKE_PICTURE);
                startService(intent);
            }

            showNotification();
        }
    }
}
