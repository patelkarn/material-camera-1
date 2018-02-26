package com.afollestad.materialcamera.util;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.Surface;
import android.view.WindowManager;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author Aidan Follestad (afollestad)
 */
public class Degrees {

    public static final int DEGREES_0 = 0;
    public static final int DEGREES_90 = 90;
    public static final int DEGREES_180 = 180;
    public static final int DEGREES_270 = 270;
    static final int DEGREES_360 = 360;

    private Degrees() {
    }

    @DegreeUnits
    public static int mirror(@DegreeUnits int orientation) {
        switch (orientation) {
            case DEGREES_0:
            case DEGREES_360:
                return DEGREES_180;
            case DEGREES_90:
                return DEGREES_270;
            case DEGREES_180:
                return DEGREES_0;
            case DEGREES_270:
                return DEGREES_90;
        }
        return DEGREES_0;
    }

    @SuppressWarnings("ResourceType")
    @DegreeUnits
    private static int naturalize(@DegreeUnits int orientation) {
        if (orientation == 360) orientation = 0;
        else if (orientation > 360) {
            do {
                orientation = orientation - 360;
            } while (orientation > 360);
        } else if (orientation < 0) {
            do {
                orientation = 360 + orientation;
            } while (orientation < 0);
        }
        return orientation;
    }

    @DegreeUnits
    public static int getDisplayRotation(Context context) {
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        int rotation = windowManager.getDefaultDisplay().getRotation();
        switch (rotation) {
            case Surface.ROTATION_0:
                return DEGREES_0;
            case Surface.ROTATION_90:
                return DEGREES_90;
            case Surface.ROTATION_180:
                return DEGREES_180;
            case Surface.ROTATION_270:
                return DEGREES_270;
        }
        return DEGREES_0;
    }

    public static int getDeviceNaturalOrientation(Context context) {
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Configuration config = context.getResources().getConfiguration();
        int rotation = windowManager.getDefaultDisplay().getRotation();
        if (((rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_180) &&
                config.orientation == Configuration.ORIENTATION_LANDSCAPE)
                || ((rotation == Surface.ROTATION_90 || rotation == Surface.ROTATION_270) &&
                config.orientation == Configuration.ORIENTATION_PORTRAIT)) {
            return Configuration.ORIENTATION_LANDSCAPE;
        } else {
            return Configuration.ORIENTATION_PORTRAIT;
        }
    }

    @SuppressWarnings("ResourceType")
    @DegreeUnits
    public static int getDisplayOrientation(
            @Degrees.DegreeUnits int sensorOrientation,
            @Degrees.DegreeUnits int displayOrientation,
            int deviceNaturalOrientation,
            boolean front) {
        final boolean isLandscape = isLandscape(displayOrientation, deviceNaturalOrientation);
        if (displayOrientation == DEGREES_0) displayOrientation = DEGREES_360;
        int result = sensorOrientation - displayOrientation;
        result = Degrees.naturalize(result);
        if (isLandscape && front) result = mirror(result);
        return result;
    }

    @ActivityOrientation
    public static int getActivityOrientation(@NonNull Activity context) {
        @DegreeUnits final int rotation = getDisplayRotation(context);
        //Some devices have natural orientation in landscape
        int naturalOrientation = getDeviceNaturalOrientation(context);
        switch (rotation) {
            case DEGREES_0:
            case DEGREES_360:
                return naturalOrientation == Configuration.ORIENTATION_LANDSCAPE ? ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE : ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
            case DEGREES_90:
                return naturalOrientation == Configuration.ORIENTATION_LANDSCAPE ? ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT : ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
            case DEGREES_180:
                return naturalOrientation == Configuration.ORIENTATION_LANDSCAPE ? ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE : ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
            case DEGREES_270:
                return naturalOrientation == Configuration.ORIENTATION_LANDSCAPE ? ActivityInfo.SCREEN_ORIENTATION_PORTRAIT : ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
            default:
                Log.e("Degrees", "Unknown screen orientation. Defaulting to portrait.");
                return naturalOrientation == Configuration.ORIENTATION_LANDSCAPE ? ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE : ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
        }
    }

    public static boolean isPortrait(Context activity) {
        return isPortrait(getDisplayRotation(activity), getDeviceNaturalOrientation(activity));
    }

    public static boolean isLandscape(Context activity) {
        return isLandscape(getDisplayRotation(activity), getDeviceNaturalOrientation(activity));
    }

    public static boolean isPortrait(@DegreeUnits int degrees, int deviceNaturalOrientation) {
        if (deviceNaturalOrientation == Configuration.ORIENTATION_LANDSCAPE)
            return degrees == DEGREES_90 || degrees == DEGREES_270;
        return degrees == DEGREES_0 || degrees == DEGREES_180 || degrees == DEGREES_360;
    }

    private static boolean isLandscape(@DegreeUnits int degrees, int deviceNaturalOrientation) {
        if (deviceNaturalOrientation == Configuration.ORIENTATION_LANDSCAPE)
            return degrees == DEGREES_0 || degrees == DEGREES_180 || degrees == DEGREES_360;
        return degrees == DEGREES_90 || degrees == DEGREES_270;
    }

    @IntDef({
            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT,
            ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE,
            ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT,
            ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE
    })
    @Retention(RetentionPolicy.SOURCE)
    @interface ActivityOrientation {
    }

    @IntDef({DEGREES_0, DEGREES_90, DEGREES_180, DEGREES_270, DEGREES_360})
    @Retention(RetentionPolicy.SOURCE)
    public @interface DegreeUnits {}
}
