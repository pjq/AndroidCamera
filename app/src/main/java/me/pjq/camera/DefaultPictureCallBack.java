package me.pjq.camera;

import android.content.Context;
import android.hardware.Camera;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by pengjianqing on 8/7/14.
 */
public class DefaultPictureCallBack implements Camera.PictureCallback {
    private boolean isProcessing = false;
    private Context context;

    public DefaultPictureCallBack(Context context) {
        this.context = context;
    }

    @Override
    public void onPictureTaken(byte[] data, Camera camera) {
        isProcessing = true;

        File file = new File(LocalPathResolver.getImageDir());
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
            Toast.makeText(context, "Saved to " + file.getAbsolutePath(), Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
        }

        camera.startPreview();
        isProcessing = false;
    }

    public boolean isProcessing() {
        return isProcessing;
    }
}
