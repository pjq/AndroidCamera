package me.pjq.camera;

import android.content.Context;
import android.hardware.Camera;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.List;

/**
 * Created by pengjianqing on 8/6/14.
 */
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
    private SurfaceHolder holder;
    private Camera camera;
    Camera.Size supportedSizes;


    public CameraPreview(Context context, Camera camera) {
        super(context);

        this.camera = camera;
        List<Camera.Size> sizes = camera.getParameters().getSupportedPreviewSizes();
        supportedSizes = sizes.get(0);

        holder = getHolder();
        holder.addCallback(this);
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            camera.setPreviewDisplay(holder);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if (null == holder.getSurface()) {
            return;
        }

        try {
            camera.stopPreview();
        } catch (Exception e) {
            e.printStackTrace();
        }

        setPreviewSize();

        try {
            camera.setPreviewDisplay(holder);
            camera.startPreview();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setPreviewSize() {
        Camera.Parameters parameters = camera.getParameters();
        parameters.setPreviewSize(supportedSizes.width, supportedSizes.height);
        requestLayout();
        camera.setParameters(parameters);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (camera != null) {
            // Call stopPreview() to stop updating the preview surface.
            camera.stopPreview();
        }
    }

    public void setCamera(Camera o) {
        if (camera == o) {
            return;
        }

        stopPreviewAndFreeCamera();

        camera = o;

        if (null != camera) {
            List<Camera.Size> sizes = camera.getParameters().getSupportedPreviewSizes();
            supportedSizes = sizes.get(0);

            requestLayout();

            try {
                camera.setPreviewDisplay(holder);
            } catch (Exception e) {
                e.printStackTrace();
            }

            camera.startPreview();
        }
    }

    private void stopPreviewAndFreeCamera() {
        if (null != camera) {
            camera.stopPreview();
            camera.release();
            camera = null;
        }
    }
}
