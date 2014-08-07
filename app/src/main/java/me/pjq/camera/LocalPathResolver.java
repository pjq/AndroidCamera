package me.pjq.camera;

import android.content.Context;
import android.os.Build;
import android.os.Environment;

import java.io.File;

public class LocalPathResolver {

    private static final String TAG = LocalPathResolver.class.getSimpleName();

    private static final String BASE_DIR = "/qcamera";
    private static String base;

    public static void init(Context context) {
        String baseDir = getDir(context);
        LocalPathResolver.base = baseDir;

        File file = new File(getBaseDir());
        if (!file.isDirectory()) {
            boolean result = file.mkdirs();
            if (result) {

            }
        }
    }

    // Gets the root file storage directory.
    private static String getDir(Context context) {
        String base;
        boolean usingSdcard = true;
        if (usingSdcard) {
            base = Environment.getExternalStorageDirectory().getPath();
        } else {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                base = context.getExternalCacheDir().getPath();
            } else {
                File filesDir = context.getApplicationContext().getFilesDir();
                base = filesDir.getPath();
            }
        }

        return base;
    }

    public static String getBaseDir() {
        return base + BASE_DIR;
    }

    public static String getVideoDir() {
        return base + BASE_DIR + "/" + "video_" + System.currentTimeMillis() + ".mp4";
    }

    public static String getImageDir() {
        return base + BASE_DIR + "/" + "image_" + System.currentTimeMillis() + ".png";
    }

    public static String getLogDir() {
        return getBaseDir()+"/log";
    }
}
