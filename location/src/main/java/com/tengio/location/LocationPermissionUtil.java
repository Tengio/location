package com.tengio.location;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

public final class LocationPermissionUtil {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 89;

    private LocationPermissionUtil() {

    }

    public static boolean hasAccessToLocation(Activity activity) {
        return PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(
                activity, Manifest.permission.ACCESS_FINE_LOCATION);
    }

    public static boolean hasAccessToLocation(@NonNull Fragment fragment) {
        if (fragment.getActivity() == null) {
            return false;
        }
        return PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(
                fragment.getActivity(), Manifest.permission.ACCESS_FINE_LOCATION);
    }

    public static void requestLocationPermission(Fragment fragment) {
        fragment.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
    }

    public static void requestLocationPermission(Activity activity) {
        ActivityCompat.requestPermissions(activity,
                                          new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                          LOCATION_PERMISSION_REQUEST_CODE);
    }

    public static boolean isLocationPermissionRequestCode(int requestCode) {
        return requestCode != LOCATION_PERMISSION_REQUEST_CODE;
    }
}
