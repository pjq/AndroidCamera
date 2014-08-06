package me.pjq.camera;

import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import at.markushi.ui.CircleButton;

public class CameraActivity extends FragmentActivity implements View.OnClickListener {
    Camera camera;
    CameraPreview cameraPreview;
    private CircleButton capture;
    private CircleButton switchButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_activity_camera);

        LocalPathResolver.init(getApplicationContext());

        if (!checkCameraHardware(getApplicationContext())) {
            finish();
            return;
        }

        init();
    }

    private void init() {
        camera = getCameraInstance();
        cameraPreview = new CameraPreview(this, camera);

        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        preview.addView(cameraPreview);

        capture = (CircleButton) findViewById(R.id.button_capture);
        switchButton = (CircleButton) findViewById(R.id.button_switch);
        capture.setOnClickListener(this);
        switchButton.setOnClickListener(this);

    }

    private boolean checkCameraHardware(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            return true;
        } else {
            return false;
        }
    }

    private Camera getCameraInstance() {
        Camera c = null;

        try {
            int id = findFrontFacingCamera(Camera.CameraInfo.CAMERA_FACING_BACK);
            if (id < 0) {
                c = Camera.open();
            } else {
                c = Camera.open(id);
            }

            c.startFaceDetection();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return c;
    }

    private boolean safeCameraOpen(int id) {
        boolean open = false;

        try {
            releaseCameraAndPreview();
            camera = Camera.open(id);
            open = null != camera;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return open;
    }

    boolean isFront = false;

    private void cameraSwitch() {
        int facing = Camera.CameraInfo.CAMERA_FACING_BACK;
        if (isFront) {
            facing = Camera.CameraInfo.CAMERA_FACING_BACK;
        } else {
            facing = Camera.CameraInfo.CAMERA_FACING_FRONT;
        }

        int cameraId = findFrontFacingCamera(facing);
        boolean result = safeCameraOpen(cameraId);
        if (result) {
            isFront = !isFront;
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

    private int findFrontFacingCamera(int facing) {
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

    @Override
    protected void onDestroy() {
        super.onDestroy();

        camera.release();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        switch (id) {
            case R.id.button_capture: {
                if (isProcessing) {
                    Toast.makeText(getApplicationContext(), "Please waiting...", Toast.LENGTH_SHORT).show();
                } else {
                    try {
                        camera.takePicture(null, null, pictureCallback);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            break;

            case R.id.button_switch:
                cameraSwitch();

                break;

            default:
                break;
        }
    }

    public boolean isProcessing = false;
    private Camera.PictureCallback pictureCallback = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            isProcessing = true;

            File file = new File(LocalPathResolver.getBaseDir() + "/image_" + System.currentTimeMillis() + ".png");
            if (file.exists()) {

            } else {
                try {
                    file.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            try {
                FileOutputStream fileOutputStream = new FileOutputStream(file);
                fileOutputStream.write(data);
                fileOutputStream.close();
                Toast.makeText(getApplicationContext(), "Save to " + file.getAbsolutePath(), Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                e.printStackTrace();
            }

            camera.startPreview();
            isProcessing = false;
        }
    };
}
